package co.casterlabs.caffeinated.core;

import java.io.IOException;

import dev.webview.webview_java.bridge.JavascriptObject;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class App extends JavascriptObject {
    public static final FastLogger LOGGER = new FastLogger();
    public static final App INSTANCE;
    public static final Throwable appFailReason;

    static {
        App i = null;
        Throwable f = null;
        try {
            i = new App();
        } catch (Throwable t) {
            App.LOGGER.fatal("Unable to load app:\n%s", t);
            f = t;
        }
        INSTANCE = i;
        appFailReason = f;
    }

    public final Preferences preferences;

    private App() throws IOException {
        this.preferences = Preferences.load();
    }

}
