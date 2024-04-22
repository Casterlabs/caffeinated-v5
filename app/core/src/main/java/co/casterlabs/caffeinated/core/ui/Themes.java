package co.casterlabs.caffeinated.core.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.caffeinated.core.App;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@JsonClass(exposeAll = true)
public class Themes {

    // Mutable, incase any plugins wanna do something ;)
    public final Map<String, Theme> map = new HashMap<>();

    public Themes() throws IOException {
        for (String id : Arrays.asList("nqp_dark", "nqp_light")) {
            String str = StreamUtil.toString(App.class.getResourceAsStream("/co/casterlabs/caffeinated/core/ui/themes/" + id + ".json"), StandardCharsets.UTF_8);
            Theme t = Rson.DEFAULT.fromJson(str, Theme.class);

            this.map.put("co.casterlabs." + id, t);
        }
    }

    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Theme {
        public boolean isDark;

        public String[] baseScale;
        public String[] accentScale;

        // Optional. Must be NULL if you don't want it.
        public String[] baseScaleP3;
        public String[] accentScaleP3;

        @JsonValidate
        private void $validate() {
            assert this.baseScale != null : "You must define baseScale.";
            assert this.accentScale != null : "You must define accentScale.";
            assert this.baseScale.length == 12 : "baseScale must have 12 entries.";
            assert this.accentScale.length == 12 : "accentScale must have 12 entries.";

            if (this.baseScaleP3 != null) {
                assert this.baseScaleP3.length == 12 : "baseScaleP3 must have 12 entries.";
            }
            if (this.accentScaleP3 != null) {
                assert this.accentScaleP3.length == 12 : "accentScaleP3 must have 12 entries.";
            }
        }

    }

}
