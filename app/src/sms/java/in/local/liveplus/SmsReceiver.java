package in.local.liveplus;

import android.content.*;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import java.util.*;
import java.util.concurrent.*;

/** OS-only incoming-SMS broadcast receiver. No SMS provider or notification listener. */
public final class SmsReceiver extends BroadcastReceiver {
 private static final ExecutorService WORK=new ThreadPoolExecutor(1,1,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(64));
 @Override public void onReceive(Context context,Intent intent){
  if(!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction()))return;
  SharedPreferences p=Store.prefs(context);
  if(!CaptureMode.ID.equals(p.getString("captureEdition",""))||!p.getBoolean("capture",false)||!CaptureMode.access(context))return;
  long received=System.currentTimeMillis(),started=p.getLong("started",Long.MAX_VALUE);
  String last4=p.getString("last4","");List<SmsCapture.Part> parts=new ArrayList<>();
  try{
   SmsMessage[] messages=Telephony.Sms.Intents.getMessagesFromIntent(intent);if(messages==null)return;
   for(SmsMessage message:messages){if(message==null)return;parts.add(new SmsCapture.Part(message.getOriginatingAddress(),message.getMessageBody(),message.getTimestampMillis()));}
  }catch(RuntimeException ignored){p.edit().putBoolean("captureError",true).apply();return;}
  int subscription=intent.getIntExtra("subscription",-1);Context app=context.getApplicationContext();PendingResult pending=goAsync();
  try{WORK.execute(()->{
   try{
    if(!p.getBoolean("capture",false)||p.getLong("started",Long.MAX_VALUE)!=started||!CaptureMode.access(app))return;
    SmsCapture.Candidate candidate=SmsCapture.collect(parts,last4,started,received,subscription);
    if(candidate!=null)CaptureStore.save(app,candidate.transaction,candidate.event,candidate.received,false,"SMS");
   }catch(RuntimeException ignored){p.edit().putBoolean("captureError",true).apply();}finally{pending.finish();}
  });}catch(RejectedExecutionException ignored){pending.finish();p.edit().putBoolean("captureError",true).apply();}
 }
}