package ma.wanam.xsense;

import static de.robv.android.xposed.XposedHelpers.findClass;
import ma.wanam.xsense.utils.Packages;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XAndroidPackage {

	public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

		if (prefs.getBoolean("enable4WayReboot", false) || prefs.getBoolean("mScreenshot", false)
				|| prefs.getBoolean("mScreenrecord", false)) {
			try {
				XGlobalActions.init(prefs, classLoader);

			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("disableScrollingCache", false)) {
			try {
				disableScrollingCache(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("disableSyncNotification", false)) {
			try {
				disableUSBNotification(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("disableLoudVolumeWarning", false)) {
			try {
				disableLoudVolumeWarning(prefs, classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (prefs.getBoolean("disableVolumeChangeSound", false)) {
			try {
				disableVolumeChangeSound(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void disableScrollingCache(ClassLoader classLoader) {
		try {
			Class<?> absListView = findClass(Packages.ANDROID + ".widget.AbsListView", classLoader);
			XposedHelpers.findAndHookMethod(absListView, "setScrollingCacheEnabled", boolean.class,
					new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.args[0] = false;
						}

					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void disableUSBNotification(ClassLoader classLoader) {
		try {
			Class<?> usbHandler = findClass("com.android.server.usb.UsbDeviceManager.UsbHandler", classLoader);

			XposedHelpers.findAndHookMethod(usbHandler, "updateUsbNotification", XC_MethodReplacement.DO_NOTHING);

			XposedHelpers.findAndHookMethod(usbHandler, "updateAdbNotification", XC_MethodReplacement.DO_NOTHING);

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void disableLoudVolumeWarning(XSharedPreferences prefs, ClassLoader classLoader) {
		try {
			Class<?> usbHandler = findClass(Packages.ANDROID + ".media.AudioService", classLoader);

			XposedHelpers.findAndHookMethod(usbHandler, "checkSafeMediaVolume", int.class, int.class, int.class,
					XC_MethodReplacement.returnConstant(Boolean.TRUE));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void disableVolumeChangeSound(ClassLoader classLoader) {
		try {
			Class<?> usbHandler = findClass("com.htc.view.VolumePanel", classLoader);

			XposedHelpers.findAndHookMethod(usbHandler, "internalPlaySound", int.class, int.class, boolean.class,
					XC_MethodReplacement.DO_NOTHING);

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

}
