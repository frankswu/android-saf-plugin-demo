package com.example.plugin1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;

import com.example.saf.BaseFragment;


public class TestFragment extends BaseFragment {

//    private String mPluginPackageName;
//    private Button button1;

    @InjectView Button button1;
    
    @OnClick(id = R.id.button1)
    public void button1OnClick(View view ){
    	eventBus.post(new TestEvent("test fragment"));
    	startActivity(new Intent(mContext,MainActivity.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_fragment, container, false);
        Injector.injectInto(this, view);
        return view;
    }

    @Override
    public void onResume() {
//        button1 = (Button)(getView().findViewById(R.id.button1));
//        button1.setOnClickListener(this);
        super.onResume();
    }

//    public TestFragment setPluginPackageName(String pluginPackageName) {
//        mPluginPackageName = pluginPackageName;
//        return this;
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v == button1) {
//            Context context = getActivity();
////            DLIntent dlIntent = new DLIntent(mPluginPackageName, MainActivity.class);
////            DLPluginManager.getInstance(context).startPluginActivity(context, dlIntent);
//        }
//        
//    }

}
