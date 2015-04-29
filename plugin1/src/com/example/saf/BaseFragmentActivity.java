/**
 * 
 */
package com.example.saf;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import cn.salesuite.saf.app.SAFFragmentActivity;
import cn.salesuite.saf.eventbus.EventBus;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.log.L;
import cn.salesuite.saf.route.Router;
import cn.salesuite.saf.utils.AsyncTaskExecutor;
import cn.salesuite.saf.utils.SAFUtils;

import com.example.plugin1.App;
import com.example.plugin1.R;


/**
 * @author Tony Shen
 *
 */
public class BaseFragmentActivity extends SAFFragmentActivity {

	protected App app;
	protected EventBus eventBus;
    protected Dialog mDialog;
    protected Router router;
    protected Context mContext;

    protected Handler mHandler = new MyHandler(this);
    protected boolean firstResume = true;
    protected LocationManager locationManager;
    protected Bundle mBundle;
	private BroadcastReceiver mNetworkStateReceiver;
	
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mNetworkStateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!SAFUtils.checkNetworkStatus(context)) {       // 网络断了的情况
//					toast(R.string.network_error);
					return;
				}
			}
			
		};
		IntentFilter filter = new IntentFilter();   
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);  
		registerReceiver(mNetworkStateReceiver, filter);
		
		mContext = this;
		app = (App) App.getInstance();
//        locationManager = LocationManager.getInstance();
        mBundle = getIntent().getExtras();
		router = Router.getInstance();
		
		eventBus = EventBusManager.getInstance();
		eventBus.register(this);
		L.init(this);
//		dataPoint4OpenActivityOrFragment(mContext, mBundle);
		
//    	if (!getString(R.bool.isTest).equals("true")) { // release版本 使用
//    		GlobalExceptionHandler.register(this);
//    	}
	}
	
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
//		Injector.injectInto(this);
	}
	
	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public boolean isAppOnForeground() {
		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}
	
    @Override  
    public void onStop() {  
        super.onStop();  
        if (!isAppOnForeground()) {  // 进入后台
        	L.i("++++++++进入后台++++++++++");

//			AsyncTaskExecutor.executeAsyncTask(new RecordActionTask(Constant.APP_INTO_THE_BACKGROUND));
        }
    }

    @Override
    protected void onDestroy() {
		unregisterReceiver(mNetworkStateReceiver);
        eventBus.unregister(this);
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onDestroy();
    }
    
	/**
	 * 显示loading
	 */
	protected Dialog showLoading() {		
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
//    	mDialog = new LPLoadingDailog(mContext);
//    	mDialog.show();

    	return mDialog;
	}
    
	/**
	 * 防止内部Handler类引起内存泄露
	 * @author Tony Shen
	 *
	 */
    public static class MyHandler extends Handler{
	    private final WeakReference<Activity> mActivity;
	    public MyHandler(Activity activity) {
	        mActivity = new WeakReference<Activity>(activity);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	        if(mActivity.get() == null) {
	            return;
	        }
	    }
	}
    

}
