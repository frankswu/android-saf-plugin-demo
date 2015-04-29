package com.example.plugin1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class TestService extends Service {

    private static final String TAG = "TestService";
    private MyBinder mBinder = new MyBinder();
    
    private class MyBinder extends Binder { 
//    implements ITestServiceInterface{
        
/*        public int sum(int a, int b) {
            // TODO Auto-generated method stub
            return a + b;
        }
*/    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return mBinder;
    } 
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand " + this.toString());
        return super.onStartCommand(intent, flags, startId);
    }

    
    
}
