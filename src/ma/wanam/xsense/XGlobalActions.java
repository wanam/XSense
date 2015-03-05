/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.wanam.xsense;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ma.wanam.xsense.adapters.BasicIconListItem;
import ma.wanam.xsense.adapters.IIconListAdapterItem;
import ma.wanam.xsense.adapters.IconListAdapter;
import ma.wanam.xsense.handlers.DataHandler;
import ma.wanam.xsense.handlers.ScreenRecordHandler;
import ma.wanam.xsense.handlers.ScreenshotHandler;
import ma.wanam.xsense.utils.Packages;
import ma.wanam.xsense.utils.Utils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.htc.widget.HtcAlertDialog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XGlobalActions {
	public static final String CLASS_GLOBAL_ACTIONS = "com.android.internal.policy.impl.GlobalActions";
	public static final String CLASS_ACTION = "com.android.internal.policy.impl.GlobalActions.Action";

	private static Context mContext;
	private static String mRebootStr;
	private static String mRebootSoftStr;
	private static String mRecoveryStr;
	private static String mBootloaderStr;
	private static String mToggleDataAction;
	private static String mToggleDataOnStr;
	private static String mToggleDataOffStr;
	private static Drawable mRebootIcon;
	private static Drawable mRebootSoftIcon;
	private static Drawable mRecoveryIcon;
	private static Drawable mBootloaderIcon;
	private static Drawable mScreenshotIcon;
	private static Drawable mScreenrecordIcon;
	private static Drawable mToggleDataIcon;
	private static List<IIconListAdapterItem> mRebootItemList;
	private static String mRebootConfirmStr;
	private static String mRebootConfirmRecoveryStr;
	private static String mRebootConfirmBootloaderStr;
	private static String mScreenshotStr;
	private static String mcScreenshotStr;
	private static String mScreenrecordStr;
	private static String mcScreenrecordStr;
	private static Unhook mRebootActionHook;
	private static Object mRebootActionItem;
	private static boolean mRebootActionItemStockExists;
	private static Object mScreenshotAction;
	private static Object mScreenrecordAction;
	private static Object mDataAction;
	private static boolean mRebootConfirmRequired;
	private static boolean mScreenshot;
	private static boolean mScreenrecord;
	private static boolean enable4WayReboot;
	private static boolean dataToggle;

	public static void init(final XSharedPreferences prefs, final ClassLoader classLoader) {

		try {

			mRebootConfirmRequired = prefs.getBoolean("mRebootConfirmRequired", false);
			mScreenshot = prefs.getBoolean("mScreenshot", false);
			mScreenrecord = prefs.getBoolean("mScreenrecord", false);
			enable4WayReboot = prefs.getBoolean("enable4WayReboot", false);
			dataToggle = prefs.getBoolean("dataToggle", true);

			final Class<?> globalActionsClass = XposedHelpers.findClass(CLASS_GLOBAL_ACTIONS, classLoader);
			final Class<?> actionClass = XposedHelpers.findClass(CLASS_ACTION, classLoader);

			XposedBridge.hookAllConstructors(globalActionsClass, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					mContext = (Context) param.args[0];
					Context gbContext = mContext.createPackageContext(Packages.XSense, Context.CONTEXT_IGNORE_SECURITY);
					Resources gbRes = gbContext.getResources();

					int rebootSoftStrId = R.string.reboot_hotboot;
					int recoveryStrId = R.string.reboot_recovery;
					int bootloaderStrId = R.string.reboot_download;
					mRebootStr = gbRes.getString(R.string.reboot_options);
					mRebootSoftStr = gbRes.getString(rebootSoftStrId);
					mRecoveryStr = gbRes.getString(recoveryStrId);
					mBootloaderStr = gbRes.getString(bootloaderStrId);
					mScreenshotStr = gbRes.getString(R.string.screenshot);
					mcScreenshotStr = gbRes.getString(R.string.take_a_screenshot);
					mScreenrecordStr = gbRes.getString(R.string.action_screenrecord);
					mcScreenrecordStr = gbRes.getString(R.string.record_screen);
					mToggleDataOnStr = gbRes.getString(R.string.mobile_data_on);
					mToggleDataOffStr = gbRes.getString(R.string.mobile_data_off);
					mToggleDataAction = gbRes.getString(R.string.mobile_data_action);

					mRebootIcon = gbRes.getDrawable(R.drawable.global_actions_reboot);
					mRebootSoftIcon = gbRes.getDrawable(R.drawable.global_actions_reboot_hot);
					mRecoveryIcon = gbRes.getDrawable(R.drawable.global_actions_reboot_recovery);
					mBootloaderIcon = gbRes.getDrawable(R.drawable.global_actions_reboot_bootloader);
					mScreenshotIcon = gbRes.getDrawable(R.drawable.ic_screenshot);
					mScreenrecordIcon = gbRes.getDrawable(R.drawable.ic_lock_screen_record);
					mToggleDataIcon = gbRes.getDrawable(R.drawable.ic_lock_data);

					mRebootItemList = new ArrayList<IIconListAdapterItem>();
					mRebootItemList.add(new BasicIconListItem(gbRes.getString(R.string.reboot), null, mRebootIcon, null));
					mRebootItemList.add(new BasicIconListItem(mRebootSoftStr, null, mRebootSoftIcon, null));
					mRebootItemList.add(new BasicIconListItem(mRecoveryStr, null, mRecoveryIcon, null));
					mRebootItemList.add(new BasicIconListItem(mBootloaderStr, null, mBootloaderIcon, null));

					mRebootConfirmStr = gbRes.getString(R.string.reboot_confirm);
					mRebootConfirmRecoveryStr = gbRes.getString(R.string.reboot_confirm_recovery);
					mRebootConfirmBootloaderStr = gbRes.getString(R.string.reboot_confirm_bootloader);

				}
			});

			XposedHelpers.findAndHookMethod(globalActionsClass, "createDialog", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (mRebootActionHook != null) {
						mRebootActionHook.unhook();
						mRebootActionHook = null;
					}
				}

				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					if (mContext == null)
						return;

					@SuppressWarnings("unchecked")
					List<Object> mItems = (List<Object>) XposedHelpers.getObjectField(param.thisObject, "mItems");
					BaseAdapter mAdapter = (BaseAdapter) XposedHelpers.getObjectField(param.thisObject, "mAdapter");
					int index = (mItems.size() > 0 ? mItems.size() - 1 : 0);

					// try to find out if reboot action item already exists
					// in
					// the list of GlobalActions items
					// strategy:
					// 1) check if Action has mIconResId field or
					// mMessageResId
					// field
					// 2) check if the name of the corresponding resource
					// contains "reboot" or "restart" substring
					if (mRebootActionItem == null) {
						Resources res = mContext.getResources();
						for (Object o : mItems) {
							// search for drawable
							try {
								Field f = XposedHelpers.findField(o.getClass(), "mIconResId");
								String resName = res.getResourceEntryName((Integer) f.get(o)).toLowerCase(Locale.US);
								if (resName.contains("reboot") || resName.contains("restart")) {
									mRebootActionItem = o;
									break;
								}
							} catch (NoSuchFieldError nfe) {
								// continue
							} catch (Resources.NotFoundException resnfe) {
								// continue
							} catch (IllegalArgumentException iae) {
								// continue
							}

							if (mRebootActionItem == null) {
								// search for text
								try {
									Field f = XposedHelpers.findField(o.getClass(), "mMessageResId");
									String resName = res.getResourceEntryName((Integer) f.get(o))
											.toLowerCase(Locale.US);
									if (resName.contains("reboot") || resName.contains("restart")) {
										mRebootActionItem = o;
										break;
									}
								} catch (NoSuchFieldError nfe) {
									// continue
								} catch (Resources.NotFoundException resnfe) {
									// continue
								} catch (IllegalArgumentException iae) {
									// continue
								}
							}
						}

						if (mRebootActionItem == null) {
							mRebootActionItemStockExists = false;
							mRebootActionItem = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
									new RebootAction());
						} else {
							mRebootActionItemStockExists = true;
						}
					}

					if (enable4WayReboot == true) {
						// Add/hook reboot action if enabled
						if (mRebootActionItemStockExists) {
							mRebootActionHook = XposedHelpers.findAndHookMethod(mRebootActionItem.getClass(),
									"onPress", new XC_MethodReplacement() {
										@Override
										protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
											RebootAction.showRebootDialog(mContext);
											return null;
										}
									});
						} else {
							// add to the second position
							mItems.add(index, mRebootActionItem);
						}
						index++;
					}

					// Add datatoggle action if enabled
					if (dataToggle) {
						if (mDataAction == null) {
							mDataAction = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
									new DataHandler(mContext, mToggleDataAction, mToggleDataOnStr, mToggleDataOffStr,
											mToggleDataIcon));
						}
						mItems.add(index++, mDataAction);
					}

					// Add screenshot action if enabled
					if (mScreenshot == true) {
						if (mScreenshotAction == null) {
							mScreenshotAction = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
									new ScreenshotHandler(mContext, mScreenshotStr, mcScreenshotStr, mScreenshotIcon));
						}
						mItems.add(index++, mScreenshotAction);
					}

					if (mScreenrecord == true) {
						// Add screenrecord action if enabled
						if (mScreenrecordAction == null) {
							mScreenrecordAction = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
									new ScreenRecordHandler(mContext, mScreenrecordStr, mcScreenrecordStr,
											mScreenrecordIcon));
						}
						mItems.add(index++, mScreenrecordAction);
					}

					mAdapter.notifyDataSetChanged();
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static class RebootAction implements InvocationHandler {
		private Context mContext;

		public RebootAction() {
		}

		public static void showRebootDialog(final Context context) {
			if (context == null) {
				return;
			}

			try {

				HtcAlertDialog.Builder builder = new HtcAlertDialog.Builder(context)
						.setTitle(mRebootStr)
						.setAdapter(new IconListAdapter(context, mRebootItemList),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										handleReboot(context, mRebootStr, which);
									}
								}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				HtcAlertDialog dialog = builder.create();
				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
				dialog.show();
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		private static void doReboot(Context context, int mode) {
			if (mode == 0) {
				Utils.rebootSystem(context, null);
			} else if (mode == 1) {
				Utils.performSoftReboot();
			} else if (mode == 2) {
				Utils.rebootSystem(context, "recovery");
			} else if (mode == 3) {
				Utils.rebootSystem(context, "download");
			}
		}

		private static void handleReboot(final Context context, String caption, final int mode) {
			try {
				if (!mRebootConfirmRequired) {
					doReboot(context, mode);
				} else {
					String message = mRebootConfirmStr;
					if (mode == 2) {
						message = mRebootConfirmRecoveryStr;
					} else if (mode == 3) {
						message = mRebootConfirmBootloaderStr;
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(caption)
							.setMessage(message)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									doReboot(context, mode);
								}
							}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
					dialog.show();
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();

			if (methodName.equals("create")) {
				mContext = (Context) args[0];
				Resources res = mContext.getResources();
				LayoutInflater li = (LayoutInflater) args[3];
				int layoutId = res.getIdentifier("global_actions_item", "layout", "android");
				View v = li.inflate(layoutId, (ViewGroup) args[2], false);

				ImageView icon = (ImageView) v.findViewById(res.getIdentifier("icon", "id", "android"));
				icon.setImageDrawable(mRebootIcon);

				TextView messageView = (TextView) v.findViewById(res.getIdentifier("message", "id", "android"));
				messageView.setText(mRebootStr);

				TextView statusView = (TextView) v.findViewById(res.getIdentifier("status", "id", "android"));
				statusView.setVisibility(View.GONE);

				return v;
			} else if (methodName.equals("onPress")) {
				showRebootDialog(mContext);
				return null;
			} else if (methodName.equals("onLongPress")) {
				handleReboot(mContext, mRebootStr, 0);
				return true;
			} else if (methodName.equals("showDuringKeyguard")) {
				return true;
			} else if (methodName.equals("showBeforeProvisioning")) {
				return true;
			} else if (methodName.equals("isEnabled")) {
				return true;
			} else if (methodName.equals("showConditional")) {
				return true;
			} else {
				return null;
			}
		}
	}

}