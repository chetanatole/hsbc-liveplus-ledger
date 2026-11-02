package in.local.liveplus;

import android.app.Notification;
import android.content.*;
import android.os.*;
import android.service.notification.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;

public class AlertListener extends NotificationListenerService {
 @Override public void onNotificationPosted(StatusBarNotification sbn){
  SharedPreferences p=Store.prefs(this);
  if(!p.getBoolean("capture",false)||sbn.getPostTime()<p.getLong("started",Long.MAX_VALUE))return;
  Set<String> packages=p.getStringSet("packages",Collections.emptySet());
  if(!packages.contains(sbn.getPackageName()))return;
  Notification n=sbn.getNotification();if((n.flags&Notification.FLAG_GROUP_SUMMARY)!=0)return;
  String context=String.valueOf(n.extras.getCharSequence(Notification.EXTRA_TITLE,""));
  Parcelable[] data=n.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
  if(data!=null && data.length>0){
   for(Notification.MessagingStyle.Message m:Notification.MessagingStyle.Message.getMessagesFromBundleArray(data)){
    long time=m.getTimestamp()>0?m.getTimestamp():sbn.getPostTime();if(time<p.getLong("started",Long.MAX_VALUE))continue;
    String sender=Build.VERSION.SDK_INT>=28&&m.getSenderPerson()!=null?String.valueOf(m.getSenderPerson().getName()):String.valueOf(m.getSender());
    capture(sbn,context+" "+sender,String.valueOf(m.getText()),time);
   }
  }else {
   CharSequence big=n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
   CharSequence small=n.extras.getCharSequence(Notification.EXTRA_TEXT);
   capture(sbn,context,String.valueOf(big!=null?big:small),sbn.getPostTime());
  }
 }
 private void capture(StatusBarNotification sbn,String context,String body,long time){
  try(Store s=new Store(this)){
   SharedPreferences p=Store.prefs(this);Engine.Parsed parsed=Engine.parse(context,body,p.getString("last4",""),time);if(parsed==null)return;
   Engine.Tx x=new Engine.Tx();x.amount=parsed.amount;x.merchant=parsed.merchant;x.date=parsed.date;x.refund=parsed.refund;x.received=time;x.source="Alert";
   x.event=hash(sbn.getPackageName()+"|"+sbn.getKey()+"|"+time+"|"+body);
   int learned=s.rule(x.merchant);
   x.category=learned>=0?learned:Engine.suggest(x.merchant,LocalDate.parse(x.date),p.getBoolean("myntra",false));
   x.confirmed=learned>=0;
   for(Engine.Tx existing:s.all()){
    if(existing.event.equals(x.event))return;
    if(!existing.refund&&!x.refund&&existing.active&&existing.amount==x.amount&&Math.abs(existing.received-x.received)<=120000&&Engine.key(existing.merchant).equals(Engine.key(x.merchant))){x.duplicate=true;x.confirmed=false;break;}
   }
   if(x.refund){x.category=0;x.confirmed=false;}
   if(s.save(x)>0){p.edit().putLong("lastCaptured",System.currentTimeMillis()).apply();sendBroadcast(new Intent("in.local.liveplus.CHANGED").setPackage(getPackageName()));}
  }catch(Exception ignored){Store.prefs(this).edit().putBoolean("captureError",true).apply();}
 }
 private String hash(String s)throws Exception {byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder o=new StringBuilder();for(byte v:b)o.append(String.format(Locale.ROOT,"%02x",v&255));return o.toString();}
}
