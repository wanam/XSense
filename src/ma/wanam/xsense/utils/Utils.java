package ma.wanam.xsense.utils;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.widget.TextView;
import de.robv.android.xposed.XSharedPreferences;

public class Utils {
	private static Process process;

	public static void closeStatusBar(Context context) throws Throwable {
		Object sbservice = context.getSystemService("statusbar");
		Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
		Method showsb = statusbarManager.getMethod("collapsePanels");
		showsb.invoke(sbservice);
	}

	/**
	 * Check connection
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isDeviceOnline(Context context) {
		NetworkInfo networkinfo = ((ConnectivityManager) context.getSystemService("connectivity"))
				.getActiveNetworkInfo();
		boolean isOnline = false;
		if (networkinfo != null) {
			isOnline = networkinfo.isConnected();
		}

		return isOnline;
	}

	public static boolean contains(final int[] array, final int v) {
		for (final int e : array)
			if (e == v)
				return true;

		return false;
	}

	public static void performSoftReboot() {
		try {
			SystemProp.set("ctl.restart", "surfaceflinger");
			SystemProp.set("ctl.restart", "zygote");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void rebootSystem(Context context, String rebootType) {
		final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		pm.reboot(rebootType);
	}

	public static TextView setTypeface(XSharedPreferences prefs, TextView tv) {

		int typeStyle = Typeface.NORMAL;
		if (!prefs.getString("statusbarTextStyle", "Normal").equalsIgnoreCase("Normal")) {
			if (prefs.getString("statusbarTextStyle", "Normal").equalsIgnoreCase("Italic")) {
				typeStyle = Typeface.ITALIC;
			} else if (prefs.getString("statusbarTextStyle", "Normal").equalsIgnoreCase("Bold")) {
				typeStyle = Typeface.BOLD;
			}
		}
		String typeFace = "sans-serif";
		if (!prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Regluar")) {
			if (prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Light")) {
				typeFace = "sans-serif-light";
			}
			if (prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Condensed")) {
				typeFace = "sans-serif-condensed";
			}
			if (prefs.getString("statusbarTextFace", "Regular").equalsIgnoreCase("Thin")) {
				typeFace = "sans-serif-thin";
			}

		}
		tv.setTypeface(Typeface.create(typeFace, typeStyle));

		return tv;

	}

	public static boolean isSenseRom() {
		if (new File("/system/framework/framework-res-htc.apk").exists()) {
			return true;
		}

		return false;
	}

	public static boolean isPackageExisted(Context context, String targetPackage) {
		List<ApplicationInfo> packages;
		PackageManager pm;
		pm = context.getPackageManager();
		packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			if (packageInfo.packageName.equals(targetPackage))
				return true;
		}
		return false;
	}

	public static void setEnglishLocale(Context context) {

		Locale.setDefault(Locale.ENGLISH);
		Configuration config = new Configuration();
		config.locale = Locale.ENGLISH;
		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
	}

	public static class SystemProp {

		private SystemProp() {

		}

		// Get the value for the given key
		// @param key: key to lookup
		// @return null if the key isn't found
		public static String get(String key) {
			String ret;

			try {
				Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
				ret = (String) callStaticMethod(classSystemProperties, "get", key);
			} catch (Throwable t) {
				t.printStackTrace();
				ret = null;
			}
			return ret;
		}

		// Get the value for the given key
		// @param key: key to lookup
		// @param def: default value to return
		// @return if the key isn't found, return def if it isn't null, or an
		// empty string otherwise
		public static String get(String key, String def) {
			String ret = def;

			try {
				Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
				ret = (String) callStaticMethod(classSystemProperties, "get", key, def);
			} catch (Throwable t) {
				t.printStackTrace();
				ret = def;
			}
			return ret;
		}

		// Get the value for the given key, and return as an integer
		// @param key: key to lookup
		// @param def: default value to return
		// @return the key parsed as an integer, or def if the key isn't found
		// or cannot be parsed
		public static Integer getInt(String key, Integer def) {
			Integer ret = def;

			try {
				Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
				ret = (Integer) callStaticMethod(classSystemProperties, "getInt", key, def);
			} catch (Throwable t) {
				t.printStackTrace();
				ret = def;
			}
			return ret;
		}

		// Get the value for the given key, and return as a long
		// @param key: key to lookup
		// @param def: default value to return
		// @return the key parsed as a long, or def if the key isn't found or
		// cannot be parsed
		public static Long getLong(String key, Long def) {
			Long ret = def;

			try {
				Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
				ret = (Long) callStaticMethod(classSystemProperties, "getLong", key, def);
			} catch (Throwable t) {
				t.printStackTrace();
				ret = def;
			}
			return ret;
		}

		// Get the value (case insensitive) for the given key, returned as a
		// boolean
		// Values 'n', 'no', '0', 'false' or 'off' are considered false
		// Values 'y', 'yes', '1', 'true' or 'on' are considered true
		// If the key does not exist, or has any other value, then the default
		// result is returned
		// @param key: key to lookup
		// @param def: default value to return
		// @return the key parsed as a boolean, or def if the key isn't found or
		// cannot be parsed
		public static Boolean getBoolean(String key, boolean def) {
			Boolean ret = def;

			try {
				Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
				ret = (Boolean) callStaticMethod(classSystemProperties, "getBoolean", key, def);
			} catch (Throwable t) {
				t.printStackTrace();
				ret = def;
			}
			return ret;
		}

		// Set the value for the given key
		public static void set(String key, String val) {
			try {
				Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
				callStaticMethod(classSystemProperties, "set", key, val);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void runSuCommand(String sucommand) {
		try {
			if (process == null) {
				process = Runtime.getRuntime().exec(Constants.SU_COMMAND);
			}
			OutputStream os = process.getOutputStream();
			writeLine(os, sucommand);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeLine(OutputStream os, String value) throws IOException {
		String line = value + Constants.NEW_LINE;
		os.write(line.getBytes());
	}

}
