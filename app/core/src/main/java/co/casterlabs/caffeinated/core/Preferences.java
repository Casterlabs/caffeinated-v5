package co.casterlabs.caffeinated.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import dev.webview.webview_java.bridge.JavascriptFunction;
import dev.webview.webview_java.bridge.JavascriptObject;
import dev.webview.webview_java.bridge.JavascriptValue;

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
    public TokenPreferences tokens = new TokenPreferences();

    @JsonClass(exposeAll = true)
    public static class TokenPreferences {
        public List<String> koi = new LinkedList<>();
        public Map<String, String> other = new HashMap<>();
    }

    /* ------------------------ */

    @JavascriptFunction
    public void save() {
        // TODO
    }

    static Preferences load() throws IOException {
        Preferences prefs = new Preferences();
        prefs.ui = Rson.DEFAULT.fromJson("{}", UIPreferences.class);
        prefs.tokens = Rson.DEFAULT.fromJson("{}", TokenPreferences.class);
        return prefs;
    }

}
