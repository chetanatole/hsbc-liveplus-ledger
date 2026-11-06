package in.local.liveplus;

import android.content.*;

/** Shared persistence only. notification and SMS delivery are separate build components. */
public final class CaptureStore {
 public static synchronized int save(Context context,Engine.Parsed parsed,String event,long received,boolean possibleDuplicate,String source){
  try(Store store=new Store(context)){
   SharedPreferences p=Store.prefs(context);
   Engine.Tx x=new Engine.Tx();x.amount=parsed.amount;x.merchant=parsed.merchant;x.date=parsed.date;x.refund=parsed.refund;x.received=received;x.source=source;x.event=event;
   store.categorize(x,p.getBoolean("myntra",false));x.duplicate=possibleDuplicate;if(x.duplicate)x.confirmed=false;
   for(Engine.Tx existing:store.all()){
    if(existing.event.equals(x.event))return 2;
    if(!existing.refund&&!x.refund&&existing.active&&existing.amount==x.amount&&Math.abs(existing.received-x.received)<=120000&&Engine.key(existing.merchant).equals(Engine.key(x.merchant))){x.duplicate=true;x.confirmed=false;}
   }
   if(store.save(x)>0){p.edit().putLong("lastCaptured",System.currentTimeMillis()).apply();context.sendBroadcast(new Intent("in.local.liveplus.CHANGED").setPackage(context.getPackageName()));return 1;}
  }catch(Exception ignored){}
  Store.prefs(context).edit().putBoolean("captureError",true).apply();return 0;
 }
 public static void prepareEdition(Context context){
  SharedPreferences p=Store.prefs(context);String previous=p.getString("captureEdition","notifications");
  SharedPreferences.Editor editor=p.edit().putString("captureEdition",CaptureMode.ID).remove("capture_diagnostics");
  if(!CaptureMode.ID.equals(previous)){
   editor.putBoolean("capture",false).putBoolean("captureError",false).remove("started").remove("lastCaptured").remove("capture_diagnostics");
   for(String key:p.getAll().keySet())if(key.startsWith("capture_seen_"))editor.remove(key);
  }
  editor.apply();
 }
}