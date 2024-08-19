package co.casterlabs.caffeinated.core.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import co.casterlabs.caffeinated.core.App;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptObject;

@JavascriptObject
public class Preferences {
    public static final File PREFERENCE_DATA_DIR = new File(App.APP_DATA_DIR, "preferences");

    public AppPreferences app = new AppPreferences();
    public AuthPreferences auth = new AuthPreferences();
    public UIPreferences ui = new UIPreferences();

    @JavascriptFunction
    public void save() {
        // TODO
    }

    public static Preferences load() throws IOException {
        Preferences p = new Preferences(); // TODO file loading.
        p.auth = loadFile(new File(PREFERENCE_DATA_DIR, "auth.json"), AuthPreferences.class);
        return p;
    }

    private static <T> T loadFile(File file, Class<T> clazz) throws FileNotFoundException, IOException {
        try (FileInputStream fin = new FileInputStream(file)) {
            String contents = StreamUtil.toString(fin, StandardCharsets.UTF_8);
            return Rson.DEFAULT.fromJson(contents, clazz);
        }
    }

}
