


基于SAF和第三方Plugin库的 插件
=================


###   1     依赖的plugin库

[android-pluginmgr](https://github.com/houkx/android-pluginmgr)

目前此插件项目支持SAF框架，但是不能使用SAF框架中的基类如SAFapp等，可能导致和宿主基类冲突（同名，但是不属于一个Classload）不能识别。建议可以在plugin项目中自己实现基类的所有功能。
由于此plugin实现的原理是通过ProxyActivity继承pluginApk中的Activity来实现的，所以plugin中的Activity如果使用注解的话，可能会失效。

支持特性如下：

普通的apk可以像插件一样加载，普通是指按照android通常用的实现方式
启动插件的Activity和从插件来startActivyt
支持插件自定义 theme
支持.so 和android2.x

How to use:

需要定义的权限和 PluginActivity 在 AndroidManifest.xml:

<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />

<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

regist an activity

<activity android:name="androidx.pluginmgr.PluginActivity" />

加载plugin apk

PluginManager pluginMgr = PluginManager.getInstance(MyActivity);
File myPlug = new File("/mnt/sdcard/Download/myplug.apk");
PlugInfo plug = pluginMgr.loadPlugin(myPlug).iterator().next();
start activity:  pluginMgr.startMainActivity(context, plug.getPackageName()); 



###   2     DONE AND TODO

由于直接使用SAF作为plugin的时候，注解失效，所以通过如下方式在Activity中实现注解

```java

    public class ViewHolder {
        @InjectView
        public Button start_plugin_b;
        @InjectView
        public Button show_fragment;
        @InjectView
        public Button button1;
        @InjectView
        public EditText editText1;
        
        @OnClick(id = R.id.button1)
        public void button1OnClick(View view) {
            ToastUtils.showLong(TestFragmentActivity.this, "quit");
            setResult(RESULT_FIRST_USER);
            finish();
        }     
        
        @OnClick(id = R.id.start_plugin_b)
        public void start_plugin_bOnClick(View view) {
            ToastUtils.showLong(TestFragmentActivity.this, "start_plugin_b");
            eventBus.post(new TestEvent());
        }
        
        public ViewHolder(View view) {
            eventBus.register(this);
            Injector.injectInto(this, view);
            }
        
        
        @Subscribe
        public void onTestEvent(TestEvent event) {
            ToastUtils.showLong(mContext, "test event");
        }
        
      
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(mContext).inflate(R.layout.test, null);
        setContentView(view);
        viewHolder = new ViewHolder(view);
    

```

DONE
实现@InjectView
实现@OnClick
实现EventBus的@Subscribe


TODO
@InjectExtre需要在SAF中实现ViewHolder形式，并且最好支持熟悉名称可以为key的方式
其他页面元素的注解
