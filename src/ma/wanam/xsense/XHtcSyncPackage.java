package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.Context;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XHtcSyncPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {
		if (prefs.getBoolean("disableSyncNotification", true)) {
			try {
				disableSyncNotification(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}
	}

	private static void disableSyncNotification(final ClassLoader classLoader) {

		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_SYNC + ".CDMountReceiver", classLoader, "showNotification",
					Context.class, boolean.class, XC_MethodReplacement.DO_NOTHING);

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}
