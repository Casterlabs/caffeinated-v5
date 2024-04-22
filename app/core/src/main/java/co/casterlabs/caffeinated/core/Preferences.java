package co.casterlabs.caffeinated.core;

import java.io.IOException;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import dev.webview.webview_java.bridge.JavascriptFunction;
import dev.webview.webview_java.bridge.JavascriptObject;
import dev.webview.webview_java.bridge.JavascriptValue;

@JsonClass(exposeAll = true)
public class Preferences extends JavascriptObject {

    /* ------------------------ */
    public UIPreferences ui = new UIPreferences();

    @JsonClass(exposeAll = true)
    public static class UIPreferences extends JavascriptObject {
        @JavascriptValue(watchForMutate = true)
        public String theme = "co.casterlabs.nqp_dark";

        @JavascriptValue(watchForMutate = true)
        public float zoom = 1;

        @JavascriptValue
        public int width = 800;

        @JavascriptValue
        public int height = 600;

    }

    /* ------------------------ */

    @JavascriptFunction
    public void save() {
        // TODO
    }

    static Preferences load() throws IOException {
        return Rson.DEFAULT.fromJson("{}", Preferences.class);
    }

}
