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

import java.util.HashSet;

import ma.wanam.xsense.utils.Packages;
import ma.wanam.xsense.utils.Utils;

import org.xmlpull.v1.XmlPullParser;

import com.android.internal.util.ArrayUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;

public class PermissionGranter {
	public static final boolean DEBUG = false;

	private static final String CLASS_PACKAGE_MANAGER_SERVICE = "com.android.server.pm.PackageManagerService";
	private static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";

	private static final String REBOOT = "android.permission.REBOOT";
	private static final String INTERACT_ACROSS_USERS_FULL = "android.permission.INTERACT_ACROSS_USERS_FULL";
	private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
	private static final String PERM_ACCESS_SURFACE_FLINGER = "android.permission.ACCESS_SURFACE_FLINGER";

	public static void initZygote(final XSharedPreferences prefs) {
		try {
			final Class<?> pmServiceClass = XposedHelpers.findClass(CLASS_PACKAGE_MANAGER_SERVICE, null);

			XposedHelpers.findAndHookMethod(pmServiceClass, "grantPermissionsLPw", CLASS_PACKAGE_PARSER_PACKAGE,
					boolean.class, new XC_MethodHook() {
						@SuppressWarnings("unchecked")
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							final String pkgName = (String) XposedHelpers.getObjectField(param.args[0], "packageName");

							if (Packages.XSense.equals(pkgName)) {
								final Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
								final HashSet<String> grantedPerms = (HashSet<String>) XposedHelpers.getObjectField(
										extras, "grantedPermissions");
								final Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
								final Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");

								try {
									// Add android.permission.REBOOT
									// needed
									// by reboot APM toggle
									if (!grantedPerms.contains(REBOOT)) {
										final Object pWriteSettings = XposedHelpers.callMethod(permissions, "get",
												REBOOT);
										grantedPerms.add(REBOOT);
										int[] gpGids = (int[]) XposedHelpers.getObjectField(extras, "gids");
										int[] bpGids = (int[]) XposedHelpers.getObjectField(pWriteSettings, "gids");
										gpGids = (int[]) XposedHelpers.callStaticMethod(param.thisObject.getClass(),
												"appendInts", gpGids, bpGids);

									}
								} catch (ClassNotFoundError t) {
									XposedBridge.log(t);
								} catch (Throwable e) {
									XposedBridge.log(e);
								}

								try {
									// Add
									// android.permission.INTERACT_ACROSS_USERS_FULL
									// needed
									// by torchlight
									if (!grantedPerms.contains(INTERACT_ACROSS_USERS_FULL)) {
										final Object pWriteSettings = XposedHelpers.callMethod(permissions, "get",
												INTERACT_ACROSS_USERS_FULL);
										grantedPerms.add(INTERACT_ACROSS_USERS_FULL);
										int[] gpGids = (int[]) XposedHelpers.getObjectField(extras, "gids");
										int[] bpGids = (int[]) XposedHelpers.getObjectField(pWriteSettings, "gids");
										gpGids = (int[]) XposedHelpers.callStaticMethod(param.thisObject.getClass(),
												"appendInts", gpGids, bpGids);
									}
								} catch (ClassNotFoundError t) {
									XposedBridge.log(t);
								} catch (Throwable e) {
									XposedBridge.log(e);
								}

								try {
									// Add
									// android.permission.WRITE_SETTINGS
									// needed
									// by screenrecord
									if (!grantedPerms.contains(WRITE_SETTINGS)) {
										final Object pWriteSettings = XposedHelpers.callMethod(permissions, "get",
												WRITE_SETTINGS);
										grantedPerms.add(WRITE_SETTINGS);
										int[] gpGids = (int[]) XposedHelpers.getObjectField(extras, "gids");
										int[] bpGids = (int[]) XposedHelpers.getObjectField(pWriteSettings, "gids");
										gpGids = (int[]) XposedHelpers.callStaticMethod(param.thisObject.getClass(),
												"appendInts", gpGids, bpGids);
									}
								} catch (ClassNotFoundError t) {
									XposedBridge.log(t);
								} catch (Throwable e) {

									XposedBridge.log(e);
								}

								try {
									// Add
									// android.permission.ACCESS_SURFACE_FLINGER
									// needed by screen recorder
									if (!grantedPerms.contains(PERM_ACCESS_SURFACE_FLINGER)) {
										final Object pAccessSurfaceFlinger = XposedHelpers.callMethod(permissions,
												"get", PERM_ACCESS_SURFACE_FLINGER);
										grantedPerms.add(PERM_ACCESS_SURFACE_FLINGER);
										int[] gpGids = (int[]) XposedHelpers.getObjectField(extras, "gids");
										int[] bpGids = (int[]) XposedHelpers.getObjectField(pAccessSurfaceFlinger,
												"gids");
										gpGids = (int[]) XposedHelpers.callStaticMethod(param.thisObject.getClass(),
												"appendInts", gpGids, bpGids);
									}
								} catch (ClassNotFoundError t) {
									XposedBridge.log(t);
								} catch (Throwable e) {

									XposedBridge.log(e);
								}

							}

						}
					});
		} catch (ClassNotFoundError t) {
			XposedBridge.log(t);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		if (prefs.getBoolean("addRWPermission", false)) {
			try {
				XposedHelpers.findAndHookMethod("com.android.server.pm.PackageManagerService", null, "readPermission",
						XmlPullParser.class, String.class, new XC_MethodHook() {
							protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								String permission = (String) param.args[1];

								if (permission.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
									Class<?> process = XposedHelpers.findClass("android.os.Process", null);
									int gid = (Integer) XposedHelpers.callStaticMethod(process, "getGidForName",
											"media_rw");
									Object mSettings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
									Object mPermissions = XposedHelpers.getObjectField(mSettings, "mPermissions");
									Object bp = XposedHelpers.callMethod(mPermissions, "get", permission);
									int[] bp_gids = (int[]) XposedHelpers.getObjectField(bp, "gids");
									if (!Utils.contains(bp_gids, gid)) {
										XposedHelpers.setObjectField(bp, "gids", ArrayUtils.appendInt(bp_gids, gid));
									}
								}
							}
						});
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

}