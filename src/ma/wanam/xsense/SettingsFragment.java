package ma.wanam.xsense;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ma.wanam.xsense.dialogs.DisclaimerDialog;
import ma.wanam.xsense.notifications.RebootNotification;
import ma.wanam.xsense.utils.Constants;
import ma.wanam.xsense.utils.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private static Context mContext;
	private static Activity parentActivity;
	@SuppressLint("SdCardPath")
	private final String MODULES_WHITELIST_23 = "/data/data/de.robv.android.xposed.installer/conf/modules.list";

	// Fields
	private List<String> changesMade;
	private Resources res;
	private AlertDialog alertDialog;
	private ProgressDialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			changesMade = new ArrayList<String>();
			parentActivity = getActivity();
			mContext = getActivity();

			res = getResources();

			addPreferencesFromResource(R.xml.wanam_settings);

			if (!Utils.isSenseRom()) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parentActivity);
				alertDialogBuilder.setTitle(res.getString(R.string.sense_rom_warning));

				alertDialogBuilder.setMessage(res.getString(R.string.sense_rom_warning_msg)).setCancelable(false)
						.setPositiveButton(res.getString(R.string.ok_btn), null);

				alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}

			if (!Utils.isPackageExisted(mContext, Constants.XPOSED_PACKAGE_NAME)) {
				new DisclaimerDialog().show(getFragmentManager(), "xposedinstaller");
			} else {
				checkXposedModule();
			}

			findPreference("removedAds").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					boolean isRemoved = preference.getSharedPreferences().getBoolean("removedAds", false);
					if (isRemoved)
						showDonateAlert();
					return isRemoved;
				}
			});

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.cancel();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	protected void showDonateAlert() {

		try {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

			alertDialogBuilder.setTitle(R.string.support_app);

			alertDialogBuilder.setMessage(res.getString(R.string.note_please_consider_making_a_donation))
					.setCancelable(true).setPositiveButton(R.string.rate_app, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
							Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
							mContext.startActivity(goToMarket);
						}
					}).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});

			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void checkXposedModule() {
		Scanner scanner = null;

		try {

			File xposedWhiteList23 = new File(MODULES_WHITELIST_23);
			if (xposedWhiteList23.exists() && xposedWhiteList23.canRead()) {
				try {
					boolean foundModule = false;
					String pName = parentActivity.getPackageManager()
							.getPackageInfo(parentActivity.getPackageName(), 0).applicationInfo.sourceDir;
					scanner = new Scanner(xposedWhiteList23);
					while (scanner.hasNextLine()) {
						if (scanner.nextLine().contains(pName)) {
							foundModule = true;
							break;
						}
					}
					if (!foundModule) {
						Toast.makeText(mContext, getString(R.string.xsense_module_does_not_seems_to_be_enabled),
								Toast.LENGTH_LONG).show();
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (scanner != null) {
						scanner.close();
					}
				}
			}

			if (!PreferenceManager.getDefaultSharedPreferences(parentActivity).getBoolean("isXSenseFirstLaunch", false)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
				builder.setCancelable(true).setTitle(R.string.app_name).setMessage(R.string.xsense_disclaimer)
						.setPositiveButton(R.string.ok_btn, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).create().show();
				PreferenceManager.getDefaultSharedPreferences(parentActivity).edit()
						.putBoolean("isXSenseFirstLaunch", true).commit();
			}

		} catch (Throwable e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerPrefsReceiver();
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterPrefsReceiver();
	}

	private void registerPrefsReceiver() {
		PreferenceManager.getDefaultSharedPreferences(parentActivity).registerOnSharedPreferenceChangeListener(this);
	}

	private void unregisterPrefsReceiver() {
		PreferenceManager.getDefaultSharedPreferences(parentActivity).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		try {
			// No reboot notification required
			String[] litePrefs = new String[] { "appChooserShowAllActivities", "removedAds", "keepMyExtraCscFeatures",
					"drt", "drt_ts", "isXSenseFirstLaunch", "forceEnglish", "notificationSize" };
			for (String string : litePrefs) {
				if (key.equalsIgnoreCase(string)) {
					return;
				}
			}

			// Add preference key to changed keys list
			if (!changesMade.contains(key)) {
				changesMade.add(key);
			}

			RebootNotification.notify(parentActivity, changesMade.size(), true);
			Log.i(Constants.LOG_FLAG, "Xposed Sense - ChangesMade:" + changesMade);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
