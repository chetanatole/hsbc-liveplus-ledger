package in.local.liveplus;
import android.content.Context;
import android.provider.Settings;
public final class CaptureMode {
 public static final boolean SMS=false;
 public static final String ID="notifications";
 public static boolean access(Context context){String enabled=Settings.Secure.getString(context.getContentResolver(),"enabled_notification_listeners");return enabled!=null&&enabled.contains(context.getPackageName()+"/");}
 public static void enabled(){AlertListener.captureEnabled();}
}