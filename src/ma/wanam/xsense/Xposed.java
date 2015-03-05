package ma.wanam.xsense;

import ma.wanam.xsense.utils.Constants;
import ma.wanam.xsense.utils.Packages;
import ma.wanam.xsense.utils.Utils;
import android.database.sqlite.SQLiteDatabase;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Xposed implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {

	private static String MODULE_PATH = null;
	private static XSharedPreferences prefs;

	@Override
	public void initZygote(StartupParam startupParam) {

		// Do not load if Not a Sense Rom
		if (!Utils.isSenseRom())
			return;

		MODULE_PATH = startupParam.modulePath;
		prefs = new XSharedPreferences(Packages.XSense);

		try {
			PermissionGranter.initZygote(prefs);
		} catch (Throwable e1) {
			XposedBridge.log(e1);
		}

		try {
			XElectronBeam.initZygote(prefs);
		} catch (Throwable e1) {
			XposedBridge.log(e1);
		}

		try {

			XSystemWide.doHook(MODULE_PATH, prefs);

		} catch (Throwable e) {
			XposedBridge.log(e);
		}

		try {
			SQLiteDatabase mydb = SQLiteDatabase.openDatabase(Constants.CUSTOMIZATION_SETTINGS, null,
					SQLiteDatabase.OPEN_READWRITE);
			if (prefs.getBoolean("tweakLocales", false)) {
				mydb.execSQL("UPDATE SettingTable set key='tweak_system_locale' WHERE key='system_locale'");
			} else {
				mydb.execSQL("UPDATE SettingTable set key='system_locale' WHERE key='tweak_system_locale'");
			}
			mydb.close();
		} catch (Throwable e) {
			XposedBridge.log(e);
		}

	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		// Do not load if Not a Sense Rom
		if (!Utils.isSenseRom())
			return;

		if (lpparam.packageName.equalsIgnoreCase(Packages.ANDROID)) {
			try {
				XAndroidPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.HTC_EASY_ACCESS)) {
			try {
				XHtcEasyAccessPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.SYSTEM_UI)) {
			try {
				XSysUIFeaturePackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.SETTINGS)) {
			try {
				XSettingsPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.MEDIA)) {
			try {
				XMediaPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.HTC_LAUNCHER)) {
			try {
				XLauncherPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.HTC_LOCKSCREEN)) {
			try {
				XLockScreenPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.PHONE)) {
			try {
				XPhonePackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.HTC_MESSAGE)) {
			try {
				XMessagingPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.HTC_SYNC)) {
			try {
				XHtcSyncPackage.doHook(prefs, lpparam.classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (lpparam.packageName.equalsIgnoreCase(Packages.HTC_KEYBOARD)
				|| lpparam.packageName.equalsIgnoreCase(Packages.SWIFT_KEYBOARD)
				|| lpparam.packageName.equalsIgnoreCase(Packages.SWIFT_KEYBOARD_TRIAL)
				|| lpparam.packageName.equalsIgnoreCase(Packages.GOOGLE_KEYBOARD)
				|| lpparam.packageName.equalsIgnoreCase(Packages.AOSP_KEYBOARD)) {
			try {
				XKeyboardPackage.doHook(prefs, lpparam.classLoader, lpparam.packageName);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {

		// Do not load if Not a Sense Rom
		if (!Utils.isSenseRom())
			return;

		final android.content.res.XModuleResources moduleResources = android.content.res.XModuleResources
				.createInstance(MODULE_PATH, resparam.res);

		if (resparam.packageName.equalsIgnoreCase(Packages.SYSTEM_UI)) {
			try {
				XSysUIResources.doHook(prefs, resparam, moduleResources);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (resparam.packageName.equalsIgnoreCase(Packages.PHONE)) {
			try {
				XPhoneResources.doHook(prefs, resparam, moduleResources);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

		if (resparam.packageName.equalsIgnoreCase(Packages.HTC_LAUNCHER)) {
			try {
				XLauncherResources.doHook(prefs, resparam, moduleResources);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

}
