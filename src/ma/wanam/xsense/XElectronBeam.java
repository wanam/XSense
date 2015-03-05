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

import android.animation.ObjectAnimator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;

public class XElectronBeam {
	private static final String CLASS_DISPLAY_POWER_STATE = "com.android.server.power.DisplayPowerState";
	private static final String CLASS_DISPLAY_POWER_CONTROLLER = "com.android.server.power.DisplayPowerController";

	private static int screenOffEffect = 0;
	private static boolean shouldAnimate = false;

	public static void initZygote(final XSharedPreferences prefs) {
		try {
			screenOffEffect = Integer.valueOf(prefs.getString("screenOffEffect", "0"));
			if (screenOffEffect != 0) {

				XposedHelpers.findAndHookMethod(CLASS_DISPLAY_POWER_CONTROLLER, (ClassLoader) null, "setScreenOn",
						boolean.class, new XC_MethodHook() {

							protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
								if (shouldAnimate) {
									methodHookParam.setResult((Object) null);
									shouldAnimate = false;
									ObjectAnimator objectAnimator = (ObjectAnimator) XposedHelpers.getObjectField(
											methodHookParam.thisObject, "mElectronBeamOffAnimator");
									Object mPowerState = XposedHelpers.getObjectField(methodHookParam.thisObject,
											"mPowerState");
									if (!objectAnimator.isStarted()) {
										if (((Float) XposedHelpers.callMethod(mPowerState, "getElectronBeamLevel",
												new Object[0])).floatValue() != 0.0F) {

											if (((Boolean) XposedHelpers.callMethod(mPowerState, "prepareElectronBeam",
													screenOffEffect)).booleanValue()
													&& ((Boolean) XposedHelpers.callMethod(mPowerState, "isScreenOn",
															new Object[0])).booleanValue()) {

												objectAnimator.start();
												return;
											}

											objectAnimator.end();
											return;
										}

										XposedHelpers.callMethod(methodHookParam.thisObject, "setScreenOn",
												Boolean.FALSE);
									}
								}

							}

						});

				XposedHelpers.findAndHookMethod(CLASS_DISPLAY_POWER_CONTROLLER, (ClassLoader) null,
						"animateScreenBrightness", int.class, int.class, int.class, int.class, new XC_MethodHook() {
							protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
								if (((Integer) methodHookParam.args[0]).intValue() == 0
										&& ((Integer) methodHookParam.args[1]).intValue() == 0
										&& ((Integer) methodHookParam.args[2]).intValue() == 0
										&& ((Integer) methodHookParam.args[3]).intValue() == -1) {
									shouldAnimate = true;
									methodHookParam.setResult((Object) null);
								}

							}
						});

				XposedHelpers.findAndHookMethod(CLASS_DISPLAY_POWER_STATE, (ClassLoader) null, "setElectronBeamLevel",
						float.class, new XC_MethodHook() {
							protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
								if (shouldAnimate) {
									{
										methodHookParam.setResult((Object) null);
									}
								}

							}
						});

			}

		} catch (ClassNotFoundError t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
}