package androidx.pluginmgr;

/**
 * Created by lody  on 2015/3/27.
 */
public class PluginException extends RuntimeException{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8601727166019919044L;

	public PluginException() {
    }

    public PluginException(String detailMessage) {
        super(detailMessage);
    }

    public PluginException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PluginException(Throwable throwable) {
        super(throwable);
    }
}
