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

package ma.wanam.xsense.battery;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.wanam.xsense.battery.BatteryInfoManager.BatteryStatusListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import de.robv.android.xposed.XposedBridge;

public class StatusBarIconManager implements BroadcastSubReceiver {
	public static final int DEFAULT_DATA_ACTIVITY_COLOR = Color.WHITE;

	public static final int SI_MODE_GB = 0;
	public static final int SI_MODE_STOCK = 1;
	public static final int SI_MODE_DISABLED = 2;

	public static final int JELLYBEAN = 0;
	public static final int KITKAT = 1;

	public static final int FLAG_COLORING_ENABLED_CHANGED = 1 << 0;
	public static final int FLAG_SIGNAL_ICON_MODE_CHANGED = 1 << 1;
	public static final int FLAG_ICON_COLOR_CHANGED = 1 << 2;
	public static final int FLAG_ICON_COLOR_SECONDARY_CHANGED = 1 << 3;
	public static final int FLAG_DATA_ACTIVITY_COLOR_CHANGED = 1 << 4;
	public static final int FLAG_LOW_PROFILE_CHANGED = 1 << 5;
	public static final int FLAG_ICON_STYLE_CHANGED = 1 << 6;
	public static final int FLAG_ICON_ALPHA_CHANGED = 1 << 7;
	private static final int FLAG_ALL = 0xFF;

	private Map<String, SoftReference<Drawable>> mIconCache;
	private ColorInfo mColorInfo;
	private List<IconManagerListener> mListeners;
	private BatteryInfoManager mBatteryInfo;

	public interface IconManagerListener {
		void onIconManagerStatusChanged(int flags, ColorInfo colorInfo);
	}

	static class ColorInfo {
		boolean coloringEnabled;
		int defaultIconColor;
		int[] iconColor;
		int defaultDataActivityColor;
		int[] dataActivityColor;
		int signalIconMode;
		boolean lowProfile;
		int iconStyle;
		float alphaSignalCluster;
		float alphaTextAndBattery;
	}

	public StatusBarIconManager(Context context, Context gbContext) {

		try {
			mIconCache = new HashMap<String, SoftReference<Drawable>>();

			initColorInfo();
			mBatteryInfo = new BatteryInfoManager(gbContext);

			mListeners = new ArrayList<IconManagerListener>();
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private void initColorInfo() {
		mColorInfo = new ColorInfo();
		mColorInfo.coloringEnabled = false;
		mColorInfo.defaultIconColor = getDefaultIconColor();
		mColorInfo.iconColor = new int[2];
		mColorInfo.defaultDataActivityColor = DEFAULT_DATA_ACTIVITY_COLOR;
		mColorInfo.dataActivityColor = new int[2];
		mColorInfo.signalIconMode = SI_MODE_STOCK;
		mColorInfo.lowProfile = false;
		mColorInfo.iconStyle = KITKAT;
		mColorInfo.alphaSignalCluster = 1;
		mColorInfo.alphaTextAndBattery = 1;
	}

	@Override
	public void onBroadcastReceived(Context context, Intent intent) {
		try {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				mBatteryInfo.updateBatteryInfo(intent);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public BatteryInfoManager getBatteryInfoManager() {
		return mBatteryInfo;
	}

	public void registerListener(IconManagerListener listener) {
		try {
			if (!mListeners.contains(listener)) {
				mListeners.add(listener);
			}
			if (listener instanceof BatteryStatusListener) {
				mBatteryInfo.registerListener((BatteryStatusListener) listener);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	private void notifyListeners(int flags) {
		try {
			for (IconManagerListener listener : mListeners) {
				listener.onIconManagerStatusChanged(flags, mColorInfo);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public void refreshState() {
		notifyListeners(FLAG_ALL);
	}

	public void setColoringEnabled(boolean enabled) {
		if (mColorInfo.coloringEnabled != enabled) {
			mColorInfo.coloringEnabled = enabled;
			clearCache();
			notifyListeners(FLAG_COLORING_ENABLED_CHANGED | FLAG_ICON_COLOR_CHANGED);
		}
	}

	public boolean isColoringEnabled() {
		return mColorInfo.coloringEnabled;
	}

	public void setLowProfile(boolean lowProfile) {
		if (mColorInfo.lowProfile != lowProfile) {
			mColorInfo.lowProfile = lowProfile;
			notifyListeners(FLAG_LOW_PROFILE_CHANGED);
		}
	}

	public int getDefaultIconColor() {
		return Color.WHITE;
	}

	public void setSignalIconMode(int mode) {
		try {
			if (mColorInfo.signalIconMode != mode) {
				mColorInfo.signalIconMode = mode;
				clearCache();
				notifyListeners(FLAG_SIGNAL_ICON_MODE_CHANGED);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public int getSignalIconMode() {
		return mColorInfo.signalIconMode;
	}

	public int getIconColor(int index) {
		return mColorInfo.iconColor[index];
	}

	public int getIconColor() {
		return getIconColor(0);
	}

	public int getDataActivityColor(int index) {
		return mColorInfo.dataActivityColor[index];
	}

	public void setIconColor(int index, int color) {
		try {
			if (mColorInfo.iconColor[index] != color) {
				mColorInfo.iconColor[index] = color;
				clearCache();
				notifyListeners(index == 0 ? FLAG_ICON_COLOR_CHANGED : FLAG_ICON_COLOR_SECONDARY_CHANGED);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public void setIconColor(int color) {
		setIconColor(0, color);
	}

	public void setDataActivityColor(int index, int color) {
		try {
			if (mColorInfo.dataActivityColor[index] != color) {
				mColorInfo.dataActivityColor[index] = color;
				notifyListeners(FLAG_DATA_ACTIVITY_COLOR_CHANGED);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public void setDataActivityColor(int color) {
		setDataActivityColor(0, color);
	}

	public void setIconStyle(int style) {
		try {
			if ((style == JELLYBEAN || style == KITKAT) && mColorInfo.iconStyle != style) {
				mColorInfo.iconStyle = style;
				clearCache();
				notifyListeners(FLAG_ICON_STYLE_CHANGED);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public void setIconAlpha(float alphaSignalCluster, float alphaTextAndBattery) {
		try {
			if (mColorInfo.alphaSignalCluster != alphaSignalCluster
					|| mColorInfo.alphaTextAndBattery != alphaTextAndBattery) {
				mColorInfo.alphaSignalCluster = alphaSignalCluster;
				mColorInfo.alphaTextAndBattery = alphaTextAndBattery;
				notifyListeners(FLAG_ICON_ALPHA_CHANGED);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
	}

	public Drawable applyColorFilter(int index, Drawable drawable, PorterDuff.Mode mode) {
		try {
			if (drawable != null) {
				drawable.setColorFilter(mColorInfo.iconColor[index], mode);
			}
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
		return drawable;
	}

	public Drawable applyColorFilter(int index, Drawable drawable) {
		return applyColorFilter(index, drawable, PorterDuff.Mode.SRC_IN);
	}

	public Drawable applyColorFilter(Drawable drawable) {
		return applyColorFilter(0, drawable, PorterDuff.Mode.SRC_IN);
	}

	public Drawable applyColorFilter(Drawable drawable, PorterDuff.Mode mode) {
		return applyColorFilter(0, drawable, mode);
	}

	public Drawable applyDataActivityColorFilter(int index, Drawable drawable) {
		try {
			drawable.setColorFilter(mColorInfo.dataActivityColor[index], PorterDuff.Mode.SRC_IN);
		} catch (Throwable e) {
			XposedBridge.log(e);
		}
		return drawable;
	}

	public Drawable applyDataActivityColorFilter(Drawable drawable) {
		return applyDataActivityColorFilter(0, drawable);
	}

	public void clearCache() {
		mIconCache.clear();
	}

}
