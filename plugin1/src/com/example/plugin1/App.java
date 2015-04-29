package com.example.plugin1;

import cn.salesuite.saf.app.SAFApp;
import cn.salesuite.saf.orm.DBManager;

//@ReportsCrashes(
//		httpMethod = Method.PUT, 
//		reportType = Type.JSON, 
//		formUri = "http://10.8.208.46:5984/acra-plugin/_design/acra-storage/_update/report", 
//		formUriBasicAuthLogin = "plugin_user", 
//		formUriBasicAuthPassword = "plugin_user"
//)
public class App extends SAFApp {

	@Override
	public void onCreate() {
		super.onCreate();
//		ACRA.init(this);
//		setFileDir(Constant.CACHE_DIR);    //设置app默认文件路径,用于存放图片
		super.onCreate();
		mInstance = this;
		DBManager.initialize(this);

		initConfig();
		initRouterConfig();
//		initXgPush();
	}

	private static App mInstance = null;
	
	public String urlhost;
	public String securityHost;
	public String clientOs;
	public String clientVersion;
	public String channel;

	public static App getInstance() {
		return mInstance;
	}

	/*public void onCreate() {
		
		setFileDir(Constant.CACHE_DIR);    //设置app默认文件路径,用于存放图片
		super.onCreate();
		mInstance = this;
		DBManager.initialize(this);

		initConfig();
		initRouterConfig();
		initXgPush();
		
	}*/


	

	/**
	 * 初始化配置urlHost
	 */
	private void initConfig() {
		
		this.clientOs = "ANDROID";
/*        this.clientVersion = version;
        this.channel = getString(R.string.channel_id);
        if (getString(R.bool.isTest).equals("true")) {
        	urlhost = getString(R.string.local_service_platform);
        	securityHost = urlhost;
        	L.logLevel = LogLevel.DEBUG;
        } else {
        	urlhost = getString(R.string.service_platform);
        	securityHost = urlhost.replaceAll("http", "https") + "/security/";
        	L.logLevel = LogLevel.INFO;
        }*/
	}
	
	/**
	 * 初始化Router,配置好各个Activity跳转的映射
	 */
	private void initRouterConfig() {
//		Router.getInstance().setContext(getApplicationContext());
//		RouterMapping.setRouterMapping(Router.getInstance());
	}	
}
