package androidx.pluginmgr;



/**
 * Created by lody  on 2015/4/3.
 */
public class LPluginBug {
    public Throwable error;
    public long errorTime;
    public Thread errorThread;
    public PlugInfo errorPlugin;
    public int processId;
    
    public LPluginBug(Thread t, Throwable e, PlugInfo plugInfo) {
    	this.error = e;
    	this.errorThread = t;
    	this.processId = android.os.Process.myPid();
    	this.errorTime = System.currentTimeMillis();
    	this.errorPlugin = plugInfo; 
	}
}
