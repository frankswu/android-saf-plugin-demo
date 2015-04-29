package com.example.plugin1;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;


public class MainActivity extends Activity {

    private static final String TAG = "Client-MainActivity";
    private ServiceConnection mConnecton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        this.setContentView(generateContentView(this));
    }

    private View generateContentView(final Context context) {
    	
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.parseColor("#F79AB5"));
        Button btnStartActivity = new Button(context);
        btnStartActivity.setText("Start TestActivity");
        layout.addView(btnStartActivity, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        btnStartActivity.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
                Intent intent = new Intent(MainActivity.this, TestFragmentActivity.class);
                intent.putExtra("dl_extra", "from DL framework");
                startActivityForResult(intent, 0);
            }
        });
        
        Button btnStartServer = new Button(context);
        btnStartServer.setText("Start Service");
        layout.addView(btnStartServer, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        btnStartServer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestService.class);
                startService(intent);
            }
        });
        
       
        Button button3 = new Button(context);
        button3.setText("bind Service");
        layout.addView(button3, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        button3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnecton == null) {
                    mConnecton = new ServiceConnection() {
                        public void onServiceDisconnected(ComponentName name) {
                        }
                        public void onServiceConnected(ComponentName name, IBinder binder) {
//                            int sum = ((ITestServiceInterface)binder).sum(5, 5);
//                            Log.e("MainActivity", "onServiceConnected sum(5 + 5) = " + sum);
                        }
                    };
                }
                Intent intent = new Intent(MainActivity.this, TestService.class);
                bindService(intent, mConnecton, Context.BIND_AUTO_CREATE);
            }
        });
        
        Button btnUnbindService = new Button(context);
        btnUnbindService.setText("unbind Service");
        layout.addView(btnUnbindService, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        btnUnbindService.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mConnecton != null) {
//                    Intent intent = new Intent(MainActivity.this, TestService.class);
                    unbindService(mConnecton);
//                    unbindService(intent, mConnecton);
                    mConnecton = null;
                }
            }
        });
        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult resultCode=" + resultCode);
        if (resultCode == RESULT_FIRST_USER) {
            this.finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static class Person implements Parcelable {

        private String mName;
        private int mAge;

        public Person(String name, int age) {
            mName = name;
            mAge = age;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mName);
            dest.writeInt(mAge);
        }

        public Person(Parcel in) {
            mName = in.readString();
            mAge = in.readInt();
        }

        public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
            public Person createFromParcel(Parcel in) {
                return new Person(in);
            }

            public Person[] newArray(int size) {
                return new Person[size];
            }
        };

        @Override
        public String toString() {
            return "name = " + mName + " age = " + mAge;
        }

    }
}
