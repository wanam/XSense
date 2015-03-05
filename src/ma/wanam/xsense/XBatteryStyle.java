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

import ma.wanam.xsense.battery.CmCircleBattery;
import ma.wanam.xsense.utils.Packages;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class XBatteryStyle {
	public static final String CLASS_BATTERY_CONTROLLER = "com.android.systemui.statusbar.policy.BatteryController";

	private static CmCircleBattery mCircleBattery;
	private static View mStockBattery;
	private static XSharedPreferences xPrefs;
	private static int circleColor = Color.WHITE;
	private static int circleTextColor = Color.WHITE;

	public static void initResources(final XSharedPreferences prefs, InitPackageResourcesParam resparam) {
		try {
			String layout = "super_status_bar";
			circleColor = prefs.getInt("circleBatteryColor", Color.WHITE);
			circleTextColor = prefs.getInt("circleTextColor", Color.WHITE);

			xPrefs = prefs;
			resparam.res.hookLayout(Packages.SYSTEM_UI, "layout", layout, new XC_LayoutInflated() {

				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {

					ViewGroup vg = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier(
							"signal_battery_cluster", "id", Packages.SYSTEM_UI));

					// inject circle battery view
					mCircleBattery = new CmCircleBattery(vg.getContext(), circleColor);
					mCircleBattery.setTag("circle_battery");
					LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lParams.gravity = Gravity.CENTER_VERTICAL;
					mCircleBattery.setLayoutParams(lParams);
					mCircleBattery.setPadding(6, 0, 0, 0);
					mCircleBattery.setCircleTextColor(circleTextColor);
					mCircleBattery.setVisibility(View.GONE);
					XStatusbar.registerIconManagerListener(mCircleBattery);
					vg.addView(mCircleBattery);

					// find battery
					mStockBattery = vg.findViewById(liparam.res.getIdentifier("battery", "id", Packages.SYSTEM_UI));
					if (mStockBattery != null) {
						mStockBattery.setTag("stock_battery");
					}
				}

			});
		} catch (ClassCastException t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void init(final XSharedPreferences prefs, ClassLoader classLoader) {

		try {

			XStatusbar.init(prefs, classLoader);

			Class<?> batteryControllerClass = XposedHelpers.findClass(CLASS_BATTERY_CONTROLLER, classLoader);

			XposedBridge.hookAllConstructors(batteryControllerClass, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					updateBatteryStyle();
				}
			});
		} catch (ClassNotFoundError t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static void updateBatteryStyle() {
		try {
			if (mStockBattery != null) {
				mStockBattery.setVisibility(View.GONE);
			}

			if (mCircleBattery != null) {
				if (xPrefs.getBoolean("hideBatteryIcon", false)) {
					mCircleBattery.setVisibility(View.GONE);
				} else {
					mCircleBattery.setVisibility(View.VISIBLE);
					mCircleBattery.setPercentage(true); // display percentage
				}
			}

		} catch (ClassCastException t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
}
