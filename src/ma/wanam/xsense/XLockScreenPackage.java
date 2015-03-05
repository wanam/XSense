package ma.wanam.xsense;

import java.lang.reflect.Method;

import ma.wanam.xsense.utils.Packages;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XLockScreenPackage {

	public static void doHook(final XSharedPreferences prefs, ClassLoader classLoader) {
		if (prefs.getBoolean("disableWeatherWidget", false)) {
			try {
				hideWeatherWidget(prefs, classLoader);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		try {
			setOperatorName(prefs, classLoader);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (prefs.getBoolean("hideEmergencyCallButton", false)) {
			try {
				setEmergencyButton(prefs, classLoader);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if (prefs.getBoolean("quickPinUnlockEnabled", false)) {
			try {
				enableQuickUnlock(prefs, classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);
			}
		}

	}

	private static void hideWeatherWidget(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_LOCKSCREEN + ".keyguard.KeyguardHostView", classLoader,
					"getDefaultWidgetId", XC_MethodReplacement.returnConstant(Integer.valueOf(0)));

			XposedHelpers.findAndHookMethod(Packages.HTC_LOCKSCREEN + ".keyguard.KeyguardHostView", classLoader,
					"allocateIdForDefaultAppWidget", XC_MethodReplacement.returnConstant(Integer.valueOf(0)));

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void setOperatorName(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.HTC_LOCKSCREEN + ".ui.OperatorView", classLoader,
					"updateOperatorName", new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							String opName = prefs.getString("customCarrierLockscreen", "");
							if (prefs.getBoolean("hideCarrierLockscreen", false)) {
								((TextView) param.thisObject).setText("");
							} else if (!opName.isEmpty()) {
								((TextView) param.thisObject).setText(opName);
							}

						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void setEmergencyButton(final XSharedPreferences prefs, ClassLoader classLoader) {
		try {

			final Class<?> state = XposedHelpers.findClass("com.htc.lockscreen.wrapper.IccCardConstants$State",
					classLoader);

			XposedHelpers.findAndHookMethod(Packages.HTC_LOCKSCREEN + ".keyguard.EmergencyButton", classLoader,
					"updateEmergencyCallButton", state, int.class, new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							((View) param.thisObject).setVisibility(View.GONE);
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private static void enableQuickUnlock(final XSharedPreferences prefs, final ClassLoader classLoader) {

		XposedHelpers.findAndHookMethod("com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView", classLoader,
				"initView", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(final MethodHookParam paramMethodHookParam) throws Throwable {
						final Object mLockPatternUtils = XposedHelpers.getObjectField(paramMethodHookParam.thisObject,
								"mLockPatternUtils");
						final AutoCompleteTextView localAutoCompleteTextView = (AutoCompleteTextView) XposedHelpers
								.getObjectField(paramMethodHookParam.thisObject, "mPasswordEntry");
						if ((mLockPatternUtils != null) && (localAutoCompleteTextView != null)) {
							localAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
								public void afterTextChanged(Editable paramAnonymousEditable) {
									if (localAutoCompleteTextView != null) {
										if (localAutoCompleteTextView.getText().length() > 3) {
											try {

												if ((Boolean) XposedHelpers
														.callMethod(mLockPatternUtils, "checkPassword",
																localAutoCompleteTextView.getText().toString())) {
													Method localMethod = XposedHelpers.findMethodExact(
															"com.htc.lockscreen.unlockscreen.HtcKeyInputUnlockView",
															classLoader, "verifyPasswordAndUnlock");
													localMethod.invoke(paramMethodHookParam.thisObject);

												}

											} catch (Throwable localThrowable) {
												XposedBridge.log(localThrowable);
											}
										}
									}

								}

								public void beforeTextChanged(CharSequence paramAnonymousCharSequence,
										int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {
								}

								public void onTextChanged(CharSequence paramAnonymousCharSequence,
										int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {
								}
							});
						}
					}
				});

	}

}
