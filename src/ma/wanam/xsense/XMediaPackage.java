package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XMediaPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("disableMTPNotification", false)) {
			try {
				disableMTPNotification(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void disableMTPNotification(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.MEDIA + ".MtpService", classLoader, "updateMTPNotification",
					XC_MethodReplacement.DO_NOTHING);

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}
}
