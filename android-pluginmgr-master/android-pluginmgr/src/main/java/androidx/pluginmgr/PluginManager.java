/*
 * Copyright (C) 2015 HouKx <hkx.aidream@gmail.com>
 *
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
package androidx.pluginmgr;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

/**
 * The Plug-in Manager
 * 
 * @author HouKangxi
 */
public class PluginManager implements FileFilter {
	private static final String TAG = "plugmgr";
	
	private static final PluginManager instance = new PluginManager();

	private final Map<String, PlugInfo> pluginIdToInfoMap = new ConcurrentHashMap<String, PlugInfo>();
	private final Map<String, PlugInfo> pluginPkgToInfoMap = new ConcurrentHashMap<String, PlugInfo>();
	private Context context;
	private String dexOutputPath;
	private volatile boolean hasInit = false;
	private File dexInternalStoragePath;
	private FrameworkClassLoader frameworkClassLoader;
	private PluginActivityLifeCycleCallback pluginActivityLifeCycleCallback;
	private volatile Activity actFrom;
	
	private PluginManager() {
	}

	
	public static PluginManager getInstance(Context context) {
		if (instance.hasInit || context == null) {
			if (instance.actFrom == null && context instanceof Activity) {
				instance.actFrom = (Activity) context;
			}
			return instance;
		}
		Context ctx = context;
		if (context instanceof Activity) {
			instance.actFrom = (Activity) context;
			ctx = ((Activity) context).getApplication();
		} else if (context instanceof Service) {
			ctx = ((Service) context).getApplication();
		} else if (context instanceof Application) {
			ctx = (Application) context;
		} else {
			ctx = context.getApplicationContext();
		}
		synchronized (PluginManager.class) {
			instance.init(ctx);
		}
		return instance;
	}

	static PluginManager getInstance() {
		return instance;
	}

	public boolean startMainActivity(Context context, String pkgOrId) {
		Log.d(TAG, "startMainActivity by:" + pkgOrId);
		final PlugInfo plug = preparePlugForStartActivity(context, pkgOrId);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LPluginBugManager.callAllBugListener(new LPluginBug(t,e,plug));
			}
		});
		String detailMessage;
		if (frameworkClassLoader == null) {
			detailMessage = "startMainActivity: frameworkClassLoader == null!";
			Log.e(TAG, detailMessage);
			throw new PluginException(detailMessage);
//			return false;
		}
		if (plug.getMainActivity() == null) {
			detailMessage = "startMainActivity: plug.getMainActivity() == null!";
			Log.e(TAG, detailMessage);
			throw new PluginException(detailMessage);
//			return false;
		}
		if (plug.getMainActivity().activityInfo == null) {
			detailMessage = "startMainActivity: plug.getMainActivity().activityInfo == null!"; 
			Log.e(TAG, detailMessage);
			throw new PluginException(detailMessage);
//			return false;
		}
		
		String className = frameworkClassLoader.newActivityClassName(
				plug.getId(), plug.getMainActivity().activityInfo.name);
		context.startActivity(new Intent().setComponent(new ComponentName(
				context, className)));
		return true;
	}

	public void startActivity(Context context, Intent intent) {
		performStartActivity(context, intent);
		context.startActivity(intent);
	}

	public void startActivityForResult(Activity activity, Intent intent,
			int requestCode) {
		performStartActivity(context, intent);
		activity.startActivityForResult(intent, requestCode);
	}

	private PlugInfo preparePlugForStartActivity(Context context,
			String plugIdOrPkg) {
		PlugInfo plug = null;
		plug = getPluginByPackageName(plugIdOrPkg);
		if (plug == null) {
			plug = getPluginById(plugIdOrPkg);
		}
		if (plug == null) {
			throw new IllegalArgumentException("plug not found by:"
					+ plugIdOrPkg);
		}
		return plug;
	}

	private void performStartActivity(Context context, Intent intent) {
		checkInit();

		String plugIdOrPkg;
		String actName;
		ComponentName origComp = intent.getComponent();
		if (origComp != null) {
			plugIdOrPkg = origComp.getPackageName();
			actName = origComp.getClassName();
		} else {
			throw new IllegalArgumentException(
					"plug intent must set the ComponentName!");
		}
		PlugInfo plug = preparePlugForStartActivity(context, plugIdOrPkg);
		String className = frameworkClassLoader.newActivityClassName(
				plug.getId(), actName);
		Log.d(TAG, "performStartActivity: " + actName);
		ComponentName comp = new ComponentName(context, className);
		intent.setAction(null);
		intent.setComponent(comp);
	}


	private void init(Context ctx) {
		Log.d(TAG, "init()...");
		context = ctx;
		File optimizedDexPath = ctx.getDir("plugsout", Context.MODE_PRIVATE);
		if (!optimizedDexPath.exists()) {
			optimizedDexPath.mkdirs();
		}
		dexOutputPath = optimizedDexPath.getAbsolutePath();
		dexInternalStoragePath = context
				.getDir("plugins", Context.MODE_PRIVATE);
		dexInternalStoragePath.mkdirs();
		// change ClassLoader
		try {
			Object mPackageInfo = ReflectionUtils.getFieldValue(ctx,
					"mBase.mPackageInfo", true);
			frameworkClassLoader = new FrameworkClassLoader(ctx.getClassLoader());
			// set Application's classLoader to FrameworkClassLoader
			ReflectionUtils.setFieldValue(mPackageInfo, "mClassLoader",
					frameworkClassLoader, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PluginException("init pluginManager is error", e);
		}
		hasInit = true;
	}

	private void checkInit() {
		if (!hasInit) {
			throw new IllegalStateException("PluginManager has not init!");
		}
	}

	public PlugInfo getPluginById(String pluginId) {
		if (pluginId == null) {
			return null;
		}
		return pluginIdToInfoMap.get(pluginId);
	}

	public PlugInfo getPluginByPackageName(String packageName) {
		return pluginPkgToInfoMap.get(packageName);
	}

	public Collection<PlugInfo> getPlugins() {
		return pluginIdToInfoMap.values();
	}

	public void uninstallPluginById(String pluginId) {
		uninstallPlugin(pluginId, true);
	}

	public void uninstallPluginByPkg(String pkg) {
		uninstallPlugin(pkg, false);
	}

	private void uninstallPlugin(String k, boolean isId) {
		checkInit();
		PlugInfo pl = isId ? removePlugById(k) : removePlugByPkg(k);
		if (pl == null) {
			return;
		}
		if (context instanceof Application) {
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				try {
					Application.class
							.getMethod(
									"unregisterComponentCallbacks",
									Class.forName("android.content.ComponentCallbacks"))
							.invoke(context, pl.getApplication());
				} catch (Exception e) {
					e.printStackTrace();
					throw new PluginException("uninstallPlugin by pluginManager is error", e);
				}
			}
		}
	}

	private PlugInfo removePlugById(String pluginId) {
		PlugInfo pl = null;
		synchronized (this) {
			pl = pluginIdToInfoMap.remove(pluginId);
			if (pl == null) {
				return null;
			}
			pluginPkgToInfoMap.remove(pl.getPackageName());
		}
		return pl;
	}

	private PlugInfo removePlugByPkg(String pkg) {
		PlugInfo pl = null;
		synchronized (this) {
			pl = pluginPkgToInfoMap.remove(pkg);
			if (pl == null) {
				return null;
			}
			pluginIdToInfoMap.remove(pl.getId());
		}
		return pl;
	}

	/**
	 * 加载指定插件或指定目录下的所有插件
	 * <p>
	 * 都使用文件名作为Id
	 * 
	 * @param pluginSrcDirFile
	 *            - apk或apk目录
	 * @return 插件集合
	 * @throws Exception
	 */
	public Collection<PlugInfo> loadPlugin(final File pluginSrcDirFile)
			throws Exception {
		checkInit();
		if (pluginSrcDirFile == null || !pluginSrcDirFile.exists()) {
			
			Log.e(TAG, "invalidate plugin file or Directory :" + pluginSrcDirFile);
			throw new PluginException("invalidate plugin file or Directory :" + pluginSrcDirFile);
//			return null;
		}
		if (pluginSrcDirFile.isFile()) {
			// 如果是文件则尝试加载单个插件，暂不检查文件类型，除apk外，以后可能支持加载其他类型文件,如jar
			final PlugInfo one = loadPluginWithId(pluginSrcDirFile, null, null);
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					LPluginBugManager.callAllBugListener(new LPluginBug(t,e,one));
				}
			});		
			return Collections.singletonList(one);
		}
		// clear all first
		synchronized (this) {
			pluginPkgToInfoMap.clear();
			pluginIdToInfoMap.clear();
		}
		File[] pluginApks = pluginSrcDirFile.listFiles(this);
		if (pluginApks == null || pluginApks.length < 1) {
			throw new FileNotFoundException("could not find plugins in:"
					+ pluginSrcDirFile);
		}
		for (File pluginApk : pluginApks) {
			final PlugInfo plugInfo = buildPlugInfo(pluginApk, null, null);
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					LPluginBugManager.callAllBugListener(new LPluginBug(t,e,plugInfo));
				}
			});		
			if (plugInfo != null) {
				savePluginToMap(plugInfo);
			}
		}
		return pluginIdToInfoMap.values();
	}

	private synchronized void savePluginToMap(PlugInfo plugInfo) {
		pluginPkgToInfoMap.put(plugInfo.getPackageName(), plugInfo);
		pluginIdToInfoMap.put(plugInfo.getId(), plugInfo);
	}

	// /**
	// * 单独加载一个apk <br/>
	// * 使用文件名作为插件id <br/>
	// * 目标文件也是与源文件同名
	// *
	// * @param pluginApk
	// * @return
	// * @throws Exception
	// */
	// public PlugInfo loadPlugin(File pluginApk) throws Exception {
	// return loadPluginWithId(pluginApk, null, null);
	// }

	/**
	 * 单独加载一个apk
	 * 
	 * @param pluginApk
	 * @param pluginId
	 *            - 如果参数为null,则使用文件名作为插件id
	 * @return
	 * @throws Exception
	 */
	public PlugInfo loadPluginWithId(File pluginApk, String pluginId)
			throws Exception {
		return loadPluginWithId(pluginApk, pluginId, null);
	}

	public PlugInfo loadPluginWithId(File pluginApk, String pluginId,
			String targetFileName) throws Exception {
		checkInit();
		PlugInfo plugInfo = buildPlugInfo(pluginApk, pluginId, targetFileName);
		if (plugInfo != null) {
			savePluginToMap(plugInfo);
		}
		return plugInfo;
	}

	private PlugInfo buildPlugInfo(File pluginApk, String pluginId,
			String targetFileName) throws Exception {
		PlugInfo info = new PlugInfo();
		info.setId(pluginId == null ? pluginApk.getName() : pluginId);

		File privateFile = new File(dexInternalStoragePath,
				targetFileName == null ? pluginApk.getName() : targetFileName);

		info.setFilePath(privateFile.getAbsolutePath());

		if (!pluginApk.getAbsolutePath().equals(privateFile.getAbsolutePath())) {
			copyApkToPrivatePath(pluginApk, privateFile);
		}
		String dexPath = privateFile.getAbsolutePath();
		PluginManifestUtil.setManifestInfo(context, dexPath, info);

		PluginClassLoader loader = new PluginClassLoader(dexPath,
				dexOutputPath, frameworkClassLoader, info);
		info.setClassLoader(loader);

		try {
			AssetManager am = (AssetManager) AssetManager.class.newInstance();
			am.getClass().getMethod("addAssetPath", String.class)
					.invoke(am, dexPath);
			info.setAssetManager(am);
			Resources ctxres = context.getResources();
			Resources res = new Resources(am, ctxres.getDisplayMetrics(),
					ctxres.getConfiguration());
			info.setResources(res);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PluginException("pulginManager.buildPlugInfo is error :", e);
		}
		if (actFrom != null) {
			initPluginApplication(info, actFrom, true);
		}
		// createPluginActivityProxyDexes(info);
		Log.d(TAG, "buildPlugInfo: " + info);
		return info;
	}

	// private void createPluginActivityProxyDexes(PlugInfo plugin) {
	// {
	// ActInfo act = plugin.getApplicationInfo().getMainActivity();
	// if (act != null) {
	// ActivityOverider.createProxyDex(plugin, act.name, false);
	// }
	// }
	// if (plugin.getApplicationInfo().getOtherActivities() != null) {
	// for (ActInfo act : plugin.getApplicationInfo().getOtherActivities())
	// {
	// ActivityOverider.createProxyDex(plugin, act.name, false);
	// }
	// }
	// }
	void initPluginApplication(final PlugInfo info, Activity actFrom)
			throws Exception {
		initPluginApplication(info, actFrom, false);
	}

	private void initPluginApplication(final PlugInfo plugin, Activity actFrom, boolean onLoad) throws Exception {
		if (!onLoad && plugin.getApplication() != null) {
			Log.e(TAG, "!onLoad && plugin.getApplication() != null");
			return;
		}
		final String className = plugin.getPackageInfo().applicationInfo.name;
		if (className == null) {
			if (onLoad) {
				Log.e(TAG, "plugin.getPackageInfo().applicationInfo.name==null && onLoad==true");
				return;
			}
			Application application = new Application();
			setApplicationBase(plugin, application);
			return;
		}
		Log.d(TAG, "id="+plugin.getId() + ", ApplicationClassName = " + className);
		
		Runnable setApplicationTask = new Runnable() {
			public void run() {
				ClassLoader loader = plugin.getClassLoader();
				try {
					// TODO 先查找 parent的class，找不到然后再看此classLoader的
					Class<?> applicationClass = null; 
//							findByParent(loader, className, false);
					if (applicationClass == null) {
						applicationClass = loader.loadClass(className);
					}
					Application application = (Application) applicationClass.newInstance();
					setApplicationBase(plugin, application);
					// invoke plugin application's onCreate()
					application.onCreate();
				} catch (Throwable e) {
					Log.e(TAG, Log.getStackTraceString(e));
//					throw new PluginException("PluginManager.initPluginApplication is error", e);
				}
			}
		};
		// create Application instance for plugin
		if (actFrom == null) {
			if(onLoad)
			return;
			setApplicationTask.run();
		}else{
			actFrom.runOnUiThread(setApplicationTask);
		}
	}

    private  Class<?>  findByParent(ClassLoader thisClassLoader, String name,boolean throwEx)throws ClassNotFoundException{
    	Class<?> c =null;
    	try {
			ClassLoader parent = thisClassLoader.getParent();
			if (parent != null) {
				if (parent.getClass() == FrameworkClassLoader.class) {
					parent = parent.getParent();
				}
				if (parent != null) {
					c = parent.loadClass(name);
				}
			}
		} catch (ClassNotFoundException e) {
			if(throwEx){
				throw new PluginException(TAG+".findByParent is error", e);
			}
		}
    	return c;
    }
	
	private void setApplicationBase(PlugInfo plugin, Application application)
			throws Exception {
		
		synchronized (plugin) {
			if (plugin.getApplication() != null) {
				// set plugin's Application only once
				Log.d(TAG, "plugin.getApplication() != null");
				return;
			}
			plugin.setApplication(application);
			//
			PluginContextWrapper ctxWrapper = new PluginContextWrapper(context,
					plugin);
			plugin.appWrapper = ctxWrapper;
			// attach
			Method attachMethod = android.app.Application.class.getDeclaredMethod(
					"attach", Context.class);
			attachMethod.setAccessible(true);
			attachMethod.invoke(application, ctxWrapper);
			if (context instanceof Application) {
				if (android.os.Build.VERSION.SDK_INT >= 14) {
					Application.class.getMethod("registerComponentCallbacks",
							Class.forName("android.content.ComponentCallbacks"))
							.invoke(context, application);
				}
			}
			
		}
	}

	private void copyApkToPrivatePath(File pluginApk, File f) {
		// if (f.exists() && pluginApk.length() == f.length()) {
		// // 这里只是简单的判断如果两个文件长度相同则不拷贝，严格的做应该比较签名如 md5\sha-1
		// return;
		// }
		FileUtil.copyFile(pluginApk, f);
	}

	File getDexInternalStoragePath() {
		return dexInternalStoragePath;
	}

	Context getContext() {
		return context;
	}

	public PluginActivityLifeCycleCallback getPluginActivityLifeCycleCallback() {
		return pluginActivityLifeCycleCallback;
	}

	public void setPluginActivityLifeCycleCallback(
			PluginActivityLifeCycleCallback pluginActivityLifeCycleCallback) {
		this.pluginActivityLifeCycleCallback = pluginActivityLifeCycleCallback;
	}

	FrameworkClassLoader getFrameworkClassLoader() {
		return frameworkClassLoader;
	}

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return false;
		}
		String fname = pathname.getName();
		return fname.endsWith(".apk");
	}

}
