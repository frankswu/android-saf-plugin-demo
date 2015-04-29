/**
 * 
 */
package com.example.saf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.inputmethod.InputMethodManager;
import cn.salesuite.saf.app.SAFFragment;
import cn.salesuite.saf.eventbus.EventBus;
import cn.salesuite.saf.log.L;
import cn.salesuite.saf.route.Router;

import com.example.plugin1.App;


/**
 * @author Tony Shen
 *
 */
public class BaseFragment extends SAFFragment {

    protected EventBus eventBus;
    protected App app;
    protected Router router;
    
    private Dialog mDialog;
    protected Handler mHandler = new Handler();
    protected boolean firstResume = true;
	
	public BaseFragment() {
    }
	
    //后退按键强制隐藏软键盘
    public void popBackStack() {

        FragmentManager fmgr = getFragmentManager();
        if (fmgr.getBackStackEntryCount()==0 )
        {
            mContext.finish();
            // 当前窗口消费了back，那么就不再传递
            return;
        }else{
        	if (mContext.getCurrentFocus()!=null) {
            	((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).
                hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        	}

            fmgr.popBackStack();
        }
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        app = App.getInstance();
        eventBus = EventBusManager.getInstance();
		eventBus.register(this);
		router = Router.getInstance();
		
//		dataPoint4OpenActivityOrFragment(mContext, getActivityBundle());
		L.init(this);
	}
    
	
	protected void dismissDialog() {		
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}

	}
	
    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        super.onDestroy();
    }
    

}
