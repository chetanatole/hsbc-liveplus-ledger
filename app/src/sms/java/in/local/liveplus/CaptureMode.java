package in.local.liveplus;
import android.content.Context;
import android.content.pm.PackageManager;
public final class CaptureMode {
 public static final boolean SMS=true;
 public static final String ID="sms";
 public static boolean access(Context context){return context.checkSelfPermission("android.permission.RECEIVE_SMS")==PackageManager.PERMISSION_GRANTED;}
 public static void enabled(){}
}