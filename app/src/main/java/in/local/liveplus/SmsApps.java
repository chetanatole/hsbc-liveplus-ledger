package in.local.liveplus;

import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.provider.Telephony;
import java.util.*;

/** Only apps implementing SMS delivery and composition are offered as notification sources. */
public final class SmsApps {
 public static Map<String,String> installed(Context context){
  PackageManager pm=context.getPackageManager();Set<String> receivers=new HashSet<>();
  for(ResolveInfo app:pm.queryBroadcastReceivers(new Intent(Telephony.Sms.Intents.SMS_DELIVER_ACTION),0))
   if(app.activityInfo!=null&&"android.permission.BROADCAST_SMS".equals(app.activityInfo.permission))receivers.add(app.activityInfo.packageName);
  Map<String,String> result=new TreeMap<>();
  for(ResolveInfo app:pm.queryIntentActivities(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:")),0)){
   if(app.activityInfo!=null&&receivers.contains(app.activityInfo.packageName))result.put(app.activityInfo.packageName,app.loadLabel(pm).toString());
  }
  String current=Telephony.Sms.getDefaultSmsPackage(context);
  if(current!=null)try{result.put(current,pm.getApplicationLabel(pm.getApplicationInfo(current,0)).toString());}catch(PackageManager.NameNotFoundException ignored){}
  return result;
 }
 public static String selected(Context context){
  android.content.SharedPreferences prefs=Store.prefs(context);Map<String,String> apps=installed(context);Set<String> saved=prefs.getStringSet("packages",Collections.emptySet());
  String current=Telephony.Sms.getDefaultSmsPackage(context),choice="";
  if(saved.size()==1&&apps.containsKey(saved.iterator().next()))choice=saved.iterator().next();
  else if(current!=null&&apps.containsKey(current))choice=current;
  else for(String app:apps.keySet())if(saved.contains(app)){choice=app;break;}
  Set<String> single=choice.isEmpty()?Collections.emptySet():Collections.singleton(choice);
  if(!saved.equals(single))prefs.edit().putStringSet("packages",new HashSet<>(single)).apply();
  return choice;
 }
}