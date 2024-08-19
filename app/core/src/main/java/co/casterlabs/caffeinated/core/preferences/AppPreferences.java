package co.casterlabs.caffeinated.core.preferences;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.core.launcher.AppInterface;
import co.casterlabs.caffeinated.core.util.Crypto;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;

@JavascriptObject
@JsonClass(exposeAll = true)
public class AppPreferences {
    private static final int RANDOM_OFFSET = ThreadLocalRandom.current().nextInt(10) + 1;

    public @JavascriptValue int conductorPort = 8092; // Caffeinated <1.2 was 8091.

    public @JavascriptValue String conductorKey = new String(Crypto.generateSecureRandomKey());
    public @JavascriptValue String developerApiKey = new String(Crypto.generateSecureRandomKey());

    public @JavascriptValue Set<String> oneTimeEvents = new HashSet<>();

    public @JavascriptValue String koiUrl = "wss://api.casterlabs.co/v2/koi";

    @JavascriptGetter("conductorPort")
    public int getConductorPort() {
        if (AppInterface.isDev()) {
            // Assign a "random" port when in dev mode. We do this in a method so that when
            // save() is called we don't accidentally write this port to disk.
            return this.conductorPort + RANDOM_OFFSET;
        }

        return this.conductorPort;
    }

}
