package co.casterlabs.caffeinated.pluginsdk.koi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;

public class TestEvents {
    private static final HttpClient httpClient = HttpClient
        .newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();

    @SneakyThrows
    public static KoiEvent createTestEvent(@NonNull KoiEventType type, @Nullable UserPlatform platform) {
        String response;

        if (platform == null) {
            response = httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://api.casterlabs.co/v2/koi/test-event/%s", type.name())))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body();
        } else {
            response = httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://api.casterlabs.co/v2/koi/test-event/%s?platform=%s", type.name(), platform.name())))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body();
        }

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        return KoiEventType.get(json.getObject("data"));
    }

}
