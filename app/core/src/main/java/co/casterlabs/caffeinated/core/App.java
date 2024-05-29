package co.casterlabs.caffeinated.core;

import java.io.IOException;

import co.casterlabs.caffeinated.core.ui.AppInterface;
import co.casterlabs.caffeinated.core.ui.Themes;
import co.casterlabs.rakurai.json.Rson;
import dev.webview.webview_java.bridge.JavascriptObject;
import dev.webview.webview_java.bridge.JavascriptValue;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class App extends JavascriptObject {
    public static final FastLogger LOGGER = new FastLogger();
    public static final App INSTANCE;
    public static final Throwable appFailReason;

    static {
        App.class.getClassLoader().setPackageAssertionStatus("co.casterlabs.caffeinated.core", true);

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

    public final Koi koi;

    @JavascriptValue(watchForMutate = true, allowSet = false)
    public final Themes themes;

    private App() throws IOException {
        this.preferences = Preferences.load();
        this.themes = new Themes();
        this.koi = new Koi();

        // Gross and almost circular. I don't care.
        this.koi.eventListeners.add((e) -> {
            AppInterface.getBridge().emit("koi-event", Rson.DEFAULT.toJson(e));
        });
    }

}
