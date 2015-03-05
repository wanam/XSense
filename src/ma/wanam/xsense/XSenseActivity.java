package ma.wanam.xsense;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import ma.wanam.xsense.dialogs.CreditsDialog;
import ma.wanam.xsense.dialogs.RestoreDialog;
import ma.wanam.xsense.dialogs.RestoreDialog.RestoreDialogListener;
import ma.wanam.xsense.dialogs.SaveDialog;
import ma.wanam.xsense.notifications.RebootNotification;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

public class XSenseActivity extends Activity implements RestoreDialogListener {

	private ProgressDialog mDialog;

	private final SettingsFragment settingsFragment = new SettingsFragment();

	private static final String[] defaultSettings = new String[] { "disableWeatherWidget", "enableCallRecording",
			"enable4x5grid", "enable4WayReboot", "isWanamXposedFirstLaunch", "mScreenshot", "mScreenrecord",
			"expandNotifications", "hideImeSwitcher", "disableSyncNotification", "isGoogleApplicationsSupport",
			"disableMTPNotification", "selectedBatteryIcon", "disableKBFullScreen", "screenONSMS", "enableBigCallerId",
			"enableAllRotation", "disableVolumeChangeSound", "disableKBAutoCorrection", "headsUPNotifications",
			"unplugScreenOn", "isDisclosed", "disableAllCaps", "addRWPermission" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initScreen();
		setContentView(R.layout.wanam_main);

		MainApplication.setWindowsSize(new Point());
		getWindowManager().getDefaultDisplay().getSize(MainApplication.getWindowsSize());

		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putInt("notificationSize", MainApplication.getWindowsSize().x).commit();
		new Handler().post(new Runnable() {
			public void run() {
				getFragmentManager().beginTransaction().replace(R.id.prefs, settingsFragment).commitAllowingStateLoss();
			}
		});

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		initScreen();
		super.onConfigurationChanged(newConfig);
	}

	private void initScreen() {

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Throwable t) {
			// Ignore
		}
	}

	@Override
	protected void onPause() {
		try {
			if (mDialog != null) {
				mDialog.dismiss();
			}
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_credits:
			new CreditsDialog().show(getFragmentManager(), "credits");
			break;
		case R.id.recommended_settings:
			ShowRecommendedSettingsDiag();
			break;
		case R.id.action_save:
			new SaveDialog().show(getFragmentManager(), "save");
			break;
		case R.id.action_restore:
			new RestoreDialog().show(getFragmentManager(), "restore");
			break;

		default:
			break;
		}
		return true;

	}

	public boolean ShowRecommendedSettingsDiag() {
		AlertDialog.Builder builder = new AlertDialog.Builder(XSenseActivity.this);
		builder.setCancelable(true).setTitle(R.string.app_name).setMessage(R.string.set_recommended_settings)
				.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).setPositiveButton(R.string.apply, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						restoreRecommendedSettings();

					}
				}).create().show();

		return true;
	}

	public void restoreRecommendedSettings() {

		Editor editor = PreferenceManager.getDefaultSharedPreferences(XSenseActivity.this).edit();
		editor.clear().commit();
		PreferenceManager.setDefaultValues(this, R.xml.wanam_settings, false);

		for (String defaultSetting : defaultSettings) {
			editor.putBoolean(defaultSetting, true).commit();
		}

		editor.putInt("notificationSize", MainApplication.getWindowsSize().x).commit();

		editor.putString("screenOffEffect", "1").commit();

		Toast.makeText(this, R.string.recommended_restored, Toast.LENGTH_SHORT).show();

		RebootNotification.notify(this, 999, false);

		recreate();

	}

	@Override
	public void onRestoreDefaults() {

		PreferenceManager.getDefaultSharedPreferences(XSenseActivity.this).edit().clear().commit();
		PreferenceManager.setDefaultValues(this, R.xml.wanam_settings, false);

		Toast.makeText(this, R.string.defaults_restored, Toast.LENGTH_SHORT).show();

		PreferenceManager.getDefaultSharedPreferences(XSenseActivity.this).edit()
				.putInt("notificationSize", MainApplication.getWindowsSize().x).commit();

		recreate();

		RebootNotification.notify(this, 999, false);
	}

	@Override
	public void onRestoreBackup(final File backup) {
		new RestoreBackupTask(backup).execute();
	}

	class RestoreBackupTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;
		private File backup;

		public RestoreBackupTask(File backup) {
			this.backup = backup;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(XSenseActivity.this);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage(getString(R.string.restoring_backup));
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(backup));
				Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(XSenseActivity.this).edit();
				prefEdit.clear();
				@SuppressWarnings("unchecked")
				Map<String, ?> entries = (Map<String, ?>) input.readObject();
				for (Entry<String, ?> entry : entries.entrySet()) {
					Object v = entry.getValue();
					String key = entry.getKey();

					if (v instanceof Boolean)
						prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
					else if (v instanceof Float)
						prefEdit.putFloat(key, ((Float) v).floatValue());
					else if (v instanceof Integer)
						prefEdit.putInt(key, ((Integer) v).intValue());
					else if (v instanceof Long)
						prefEdit.putLong(key, ((Long) v).longValue());
					else if (v instanceof String)
						prefEdit.putString(key, ((String) v));
				}
				prefEdit.commit();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			SystemClock.sleep(1500);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			Toast.makeText(XSenseActivity.this, R.string.backup_restored, Toast.LENGTH_SHORT).show();
			RebootNotification.notify(XSenseActivity.this, 999, false);
		}

	}

}
