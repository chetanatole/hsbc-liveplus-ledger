package in.local.liveplus;

import android.app.Notification;
import android.content.*;
import android.os.*;
import android.service.notification.*;
import java.util.*;

public class AlertListener extends NotificationListenerService {
 private static volatile AlertListener connected;
 @Override public void onListenerConnected(){super.onListenerConnected();connected=this;baselineActive();}
 @Override public void onListenerDisconnected(){if(connected==this)connected=null;super.onListenerDisconnected();}
 @Override public void onDestroy(){if(connected==this)connected=null;super.onDestroy();}
 public static void captureEnabled(){if(connected!=null)connected.baselineActive();}
 private NotificationCapture.Envelope envelope(StatusBarNotification sbn){
  Notification n=sbn.getNotification();Bundle extras=n.extras==null?new Bundle():n.extras;
  NotificationCapture.Envelope out=new NotificationCapture.Envelope();out.source=sbn.getPackageName();out.key=sbn.getKey();out.posted=sbn.getPostTime();out.summary=(n.flags&Notification.FLAG_GROUP_SUMMARY)!=0;
  out.title=String.valueOf(extras.getCharSequence(Notification.EXTRA_TITLE,""));
  Parcelable[] data=extras.getParcelableArray(Notification.EXTRA_MESSAGES);
  if(data!=null)for(Notification.MessagingStyle.Message message:Notification.MessagingStyle.Message.getMessagesFromBundleArray(data)){
   String sender=Build.VERSION.SDK_INT>=28&&message.getSenderPerson()!=null?String.valueOf(message.getSenderPerson().getName()):String.valueOf(message.getSender());
   out.messages.add(new NotificationCapture.Message(String.valueOf(message.getText()),sender,message.getTimestamp()));
  }
  CharSequence[] lines=extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
  if(lines!=null)for(CharSequence line:lines)if(line!=null)out.lines.add(line.toString());
  CharSequence big=extras.getCharSequence(Notification.EXTRA_BIG_TEXT),small=extras.getCharSequence(Notification.EXTRA_TEXT);
  out.bigText=big==null?"":big.toString();out.text=small==null?"":small.toString();
  out.redacted=(hiddenContent(out.text)||hiddenContent(out.bigText))&&(out.text.trim().isEmpty()||hiddenContent(out.text))&&(out.bigText.trim().isEmpty()||hiddenContent(out.bigText));
  for(NotificationCapture.Message message:out.messages)if(!message.text.trim().isEmpty()&&!hiddenContent(message.text))out.redacted=false;
  for(String line:out.lines)if(!line.trim().isEmpty()&&!hiddenContent(line))out.redacted=false;
  return out;
 }
 private void baselineActive(){
  SharedPreferences p=Store.prefs(this);if(!p.getBoolean("capture",false))return;
  try{
   StatusBarNotification[] active=getActiveNotifications();if(active==null)return;
   Set<String> sources=Collections.singleton(SmsApps.selected(this));long started=p.getLong("started",Long.MAX_VALUE);
   for(StatusBarNotification sbn:active){
    if(!sources.contains(sbn.getPackageName()))continue;
    NotificationCapture.Envelope input=envelope(sbn);String key=NotificationCapture.cacheKey(input);
    NotificationCapture.State old=NotificationCapture.State.decode(p.getString(key,null));if(old.started==started)continue;
    NotificationCapture.Batch batch=NotificationCapture.collect(input,p.getString("last4",""),started,null);batch.baseline();saveSnapshot(p,key,batch.state);
   }
  }catch(RuntimeException ignored){p.edit().putBoolean("captureError",true).apply();}
 }
 @Override public void onNotificationPosted(StatusBarNotification sbn){
  SharedPreferences p=Store.prefs(this);
  if(!p.getBoolean("capture",false)||!"notifications".equals(p.getString("captureEdition","notifications"))||!sbn.getPackageName().equals(SmsApps.selected(this)))return;
  long started=p.getLong("started",Long.MAX_VALUE);if(sbn.getPostTime()<started)return;
  try{
   NotificationCapture.Envelope input=envelope(sbn);String key=NotificationCapture.cacheKey(input);
   NotificationCapture.Batch batch=NotificationCapture.collect(input,p.getString("last4",""),started,NotificationCapture.State.decode(p.getString(key,null)));
   for(NotificationCapture.Candidate candidate:batch.candidates){
    int result=capture(candidate);if(result>0)candidate.processed();
   }
   saveSnapshot(p,key,batch.state);
  }catch(RuntimeException ignored){p.edit().putBoolean("captureError",true).apply();}
 }
 @Override public void onNotificationRemoved(StatusBarNotification sbn){
  String key="capture_seen_"+NotificationCapture.digest(sbn.getPackageName()+"|"+sbn.getKey());Store.prefs(this).edit().remove(key).apply();
 }
 private void saveSnapshot(SharedPreferences p,String key,NotificationCapture.State state){
  if(state.empty()){p.edit().remove(key).apply();return;}
  SharedPreferences.Editor edit=p.edit().putString(key,state.encode());
  List<Map.Entry<String,?>> caches=new ArrayList<>();
  for(Map.Entry<String,?> entry:p.getAll().entrySet())if(entry.getKey().startsWith("capture_seen_")&&!entry.getKey().equals(key))caches.add(entry);
  caches.sort((a,b)->Long.compare(NotificationCapture.State.decode(String.valueOf(a.getValue())).posted,NotificationCapture.State.decode(String.valueOf(b.getValue())).posted));
  for(int i=0;i<caches.size()-63;i++)edit.remove(caches.get(i).getKey());edit.apply();
 }
 private int capture(NotificationCapture.Candidate candidate){
  return CaptureStore.save(this,candidate.transaction,candidate.event,candidate.received,candidate.possibleDuplicate,"Alert");
 }
 private boolean hiddenContent(String text){
  if(text==null||text.trim().isEmpty())return false;
  String value=text.trim();
  // Best-effort localized placeholder detection. OEMs can use a different placeholder.
  try{
   android.content.res.Resources resources=android.content.res.Resources.getSystem();
   int id=resources.getIdentifier("redacted_notification_message","string","android");
   if(id!=0&&value.equals(resources.getString(id).trim()))return true;
  }catch(RuntimeException ignored){}
  return value.equalsIgnoreCase("Sensitive notification content hidden");
 }
}
