package co.casterlabs.caffeinated.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;

@JavascriptObject
public class Preferences {

    /* ------------------------ */
    public UIPreferences ui = new UIPreferences();

    @JavascriptObject
    @JsonClass(exposeAll = true)
    public static class UIPreferences {
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
        return new Preferences(); // TODO file loading.
    }

}
