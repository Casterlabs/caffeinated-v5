package co.casterlabs.caffeinated.core.preferences;

import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;

@JavascriptObject
@JsonClass(exposeAll = true)
public class UIPreferences {
    public @JavascriptValue(watchForMutate = true) String theme = "co.casterlabs.nqp_dark";
    public @JavascriptValue(watchForMutate = true) String icon = "casterlabs";
    public @JavascriptValue(watchForMutate = true) String emojiProvider = Platform.osDistribution == OSDistribution.MACOS ? "system" : "twemoji";
    public @JavascriptValue(watchForMutate = true) float zoom = 1;

    public @JavascriptValue int width = 800;
    public @JavascriptValue int height = 600;

    @JsonDeserializationMethod("theme")
    private void $deserialize_theme(JsonElement e) {
        String str = e.getAsString();
        switch (str) { // Compat. for v1.2
            case "co.casterlabs.dark":
                this.theme = "co.casterlabs.nqp_dark";
                break;
            case "co.casterlabs.light":
                this.theme = "co.casterlabs.nqp_light";
                break;
            default:
                this.theme = str;
                break;
        }
    }

}
