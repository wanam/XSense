package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import android.content.Context;
import android.content.Intent;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XLauncherPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		try {
			isGoogleApplicationsSupport(prefs, classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		if (prefs.getBoolean("enable4x5grid", false)) {
			try {
				disableShortCutInstall(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void isGoogleApplicationsSupport(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_LAUNCHER + ".util.SettingUtil", classLoader,
					"isGoogleApplicationsSupport", Context.class, new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							return prefs.getBoolean("isGoogleApplicationsSupport", true);
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void disableShortCutInstall(ClassLoader classLoader) {
		try {
			XposedHelpers.findAndHookMethod(Packages.HTC_LAUNCHER + ".InstallShortcutReceiver", classLoader,
					"onReceive", Context.class, Intent.class, XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

}
