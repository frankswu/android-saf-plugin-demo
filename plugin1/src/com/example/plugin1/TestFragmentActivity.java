package com.example.plugin1;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.InjectViews;
import cn.salesuite.saf.inject.annotation.OnClick;
import cn.salesuite.saf.log.L;
import cn.salesuite.saf.utils.SAFUtils;
import cn.salesuite.saf.utils.ToastUtils;

import com.example.saf.BaseFragmentActivity;

public class TestFragmentActivity extends BaseFragmentActivity implements
		OnClickListener {

	private static final String TAG = "TestFragmentActivity";

	public ViewHolder viewHolder;

	@InjectExtra
	public String dl_extra;
	
	public class ViewHolder {
		
		@InjectExtra
		public String dl_extra;
		
		@InjectViews(ids = {R.id.start_plugin_b,R.id.show_fragment,R.id.button1})
		public Button[] buttons;
		
		@InjectView
		public Button start_plugin_b;
		@InjectView
		public Button show_fragment;
		@InjectView
		public Button button1;
		@InjectView
		public EditText editText1;
		@InjectView
		public TextView txtTestInfo;
		

		@OnClick(id = R.id.button1)
		public void button1OnClick(View view) {
			ToastUtils.showLong(TestFragmentActivity.this, "quit");
			setResult(RESULT_FIRST_USER);
			finish();
		}

		@OnClick(id = R.id.start_plugin_b)
		public void start_plugin_bOnClick(View view) {
			ToastUtils.showLong(TestFragmentActivity.this, "start_plugin_b");
			// startActivity(new
			// Intent("com.ryg.dynamicload.sample.mainpluginb",
			// ".MainActivity"));
			// START_RESULT_SUCCESS = 0;
			// if (result != DLPluginManager.START_RESULT_SUCCESS) {
			// ToastUtils.showLong(TestFragmentActivity.this);
			// }

			eventBus.post(new TestEvent("test activity"));
		}

		@OnClick(id = R.id.show_fragment)
		public void show_fragmentOnClick(View view) {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.fragment_container, new TestFragment());
			transaction.addToBackStack("TestFragment#1");
			transaction.commit();
		}

		public ViewHolder(View view) {
			eventBus.register(this);
			Injector.injectInto(this, view);
		}

		@Subscribe
		public void onTestEvent(TestEvent event) {
			ToastUtils.showLong(mContext, event.msg);
		}

	}

	private ImageView mImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = LayoutInflater.from(mContext).inflate(R.layout.test, null);
		setContentView(view);
		
		Injector.injectInto(this);
		StringBuilder txtTestInfo = new StringBuilder();
		L.d(TAG + "this.getClass=", this.getClass().getName().toString());
		L.d(TAG + "super.mContext=", super.mContext.getClass().getName().toString());
		viewHolder = new ViewHolder(view);
		// FIXME 此注解失效？原始没有实现
		txtTestInfo.append("\n@InjectExtra=" + viewHolder.dl_extra);
		for (Button button : viewHolder.buttons) {
			txtTestInfo.append("\nbutton[]="+button.getText().toString());
		}
		txtTestInfo.append("\ngetIntent().getExtras()="+SAFUtils.printObject(getIntent().getExtras()));
		txtTestInfo.append("\nApp.getInstance().getClass().getName()="+SAFUtils.printObject(App.getInstance().getClass().getName()));
//		// 输出Parcelable对象信息
//		String person = "";
//		if (getIntent().getExtras().containsKey("person")) {
//			person = getIntent().getExtras().getParcelable("person");
//		}
//		Toast.makeText(this, person, Toast.LENGTH_SHORT).show();

//		Log.d(TAG, "### person info : " + person);
		viewHolder.button1.setText("test");
		viewHolder.editText1.setText(R.string.hello_world);
		viewHolder.txtTestInfo.setText(txtTestInfo.toString());
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageView.setImageResource(R.drawable.ppmm);
		Log.d(TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onClick(View v) {
		if (v == viewHolder.show_fragment) {
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			// transaction.add(R.id.fragment_container,
			// new TestFragment().setPluginPackageName(getPackageName()));
			transaction.addToBackStack("TestFragment#1");
			transaction.commit();
		} else if (v == viewHolder.start_plugin_b) {
			// TODO plugin b
			// startActivity(new
			// Intent("com.ryg.dynamicload.sample.mainpluginb",
			// ".MainActivity"));
			// if (result != DLPluginManager.START_RESULT_SUCCESS) {
			// Toast.makeText(this, "start Activity failed",
			// Toast.LENGTH_SHORT).show();
			// }
		}

	}

}
