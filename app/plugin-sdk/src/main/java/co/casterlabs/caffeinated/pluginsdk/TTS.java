package co.casterlabs.caffeinated.pluginsdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.emoji.generator.WebUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class TTS {
    private static final HttpClient httpClient = HttpClient
        .newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();

    private static final long UPDATE_INTERVAL = TimeUnit.HOURS.toMillis(6);

    private static @Getter List<String> voices = Collections.emptyList();

    // TODO create the v3 speech api and use that instead.
    static {
        Thread.ofVirtual().start(() -> {
            while (true) {
                try {
                    JsonArray response = Rson.DEFAULT.fromJson(
                        httpClient.send(
                            HttpRequest.newBuilder()
                                .uri(URI.create("https://api.casterlabs.co/v1/polly?request=voices"))
                                .GET()
                                .build(),
                            HttpResponse.BodyHandlers.ofString()
                        ).body(),
                        JsonArray.class
                    );

                    List<String> voicesList = new ArrayList<>(response.size());

                    for (JsonElement e : response) {
                        voicesList.add(e.getAsString());
                    }

                    voices = Collections.unmodifiableList(voicesList);
                    FastLogger.logStatic(LogLevel.DEBUG, "Successfully updated TTS info.");

                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst updating TTS info.");
                    FastLogger.logException(e);

                    // We want to retry VERY quickly.
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(15));
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    public static String[] getVoicesAsArray() {
        return voices.toArray(new String[0]);
    }

    public static byte[] getSpeech(@NonNull String defaultVoice, @NonNull String text) throws IOException, InterruptedException {
        return httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(getSpeechAsUrl(defaultVoice, text)))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofByteArray()
        ).body();
    }

    public static String getSpeechAsUrl(@NonNull String defaultVoice, @NonNull String text) throws IOException {
        assert doesVoiceExist(defaultVoice) : "Invalid voice";

        return String.format(
            "https://api.casterlabs.co/v1/polly?request=speech&voice=%s&text=%s",
            defaultVoice,
            WebUtil.encodeURIComponent(text)
        );
    }

    public static boolean doesVoiceExist(@Nullable String voice) {
        if (voice == null) {
            return false;
        } else {
            for (String v : voices) {
                if (v.equalsIgnoreCase(voice)) {
                    return true;
                }
            }

            return false;
        }
    }

}
