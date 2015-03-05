package ma.wanam.xsense;

import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XHtcEasyAccessPackage {

	public static final int TP_DOUBLE_TOUCH = 15;

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {
		if (prefs.getBoolean("disableGestures", false)) {
			try {
				disableSwipeActions(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void disableSwipeActions(final ClassLoader classLoader) {

		try {

			Class<?> mSensorHubService = XposedHelpers.findClass(Packages.HTC_EASY_ACCESS + ".SensorHubService",
					classLoader);

			XposedHelpers.findAndHookMethod(mSensorHubService, "onHtcGestureMotion", int.class, int.class, int.class,
					new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							int gesture = (Integer) param.args[1];

							if (gesture == TP_DOUBLE_TOUCH) {
								XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
							}
							return null;
						}

					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

}
