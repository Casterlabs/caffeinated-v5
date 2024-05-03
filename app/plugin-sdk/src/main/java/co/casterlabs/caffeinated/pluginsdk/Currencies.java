package co.casterlabs.caffeinated.pluginsdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Currencies {
    private static final HttpClient httpClient = HttpClient
        .newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();

    private static final long UPDATE_INTERVAL = TimeUnit.HOURS.toMillis(2);

    private static List<CurrencyInfo> currencies = new ArrayList<>();
    private static List<String> psuedoCurrencies = new ArrayList<>();
    public static final String baseCurrency = "USD";

    static {
        Thread.ofVirtual().start(() -> {
            while (true) {
                try {
                    JsonObject response = Rson.DEFAULT.fromJson(
                        httpClient.send(
                            HttpRequest.newBuilder()
                                .uri(URI.create("https://api.casterlabs.co/v3/currencies"))
                                .GET()
                                .build(),
                            HttpResponse.BodyHandlers.ofString()
                        ).body(),
                        JsonObject.class
                    ).getObject("data");

                    psuedoCurrencies = Rson.DEFAULT.fromJson(response.get("psuedoCurrencies"), new TypeToken<List<String>>() {
                    });

                    currencies = Rson.DEFAULT.fromJson(response.get("currencies"), new TypeToken<List<CurrencyInfo>>() {
                    });

//                    baseCurrency = response.getString("baseCurrency");

                    FastLogger.logStatic(LogLevel.DEBUG, "Successfully updated currency info.");

                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst updating currency info.");
                    FastLogger.logException(e);

                    // We want to retry VERY quickly.
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(15));
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    public static List<CurrencyInfo> getCurrencies() {
        return new ArrayList<>(currencies);
    }

    public static List<String> getPsuedoCurrencies() {
        return new ArrayList<>(psuedoCurrencies);
    }

    /**
     * @return An HTML formatted string to be displayed to the user.
     */
    public static String formatCurrency(double amount, @NonNull String currency) throws ApiException, IOException, InterruptedException {
        String response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://api.casterlabs.co/v3/currencies/format?currency=%s&amount=%f", currency, amount)))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();

        if (response.startsWith("{")) {
            throw new ApiException(
                Rson.DEFAULT
                    .fromJson(response, JsonObject.class)
                    .getArray("errors")
                    .toString()
            );
        } else {
            return response;
        }
    }

    public static double convertCurrency(double amount, @NonNull String from, @NonNull String to) throws ApiException, IOException, InterruptedException {
        String response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://api.casterlabs.co/v3/currencies/convert?from=%s&to=%s&amount=%f&formatResult=false", from, to, amount)))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();

        if (response.startsWith("{")) {
            throw new ApiException(
                Rson.DEFAULT
                    .fromJson(response, JsonObject.class)
                    .getArray("errors")
                    .toString()
            );
        } else {
            return Double.parseDouble(response);
        }
    }

    /**
     * @return An HTML formatted string to be displayed to the user.
     */
    public static String convertAndFormatCurrency(double amount, @NonNull String from, @NonNull String to) throws ApiException, IOException, InterruptedException {
        String response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(String.format("https://api.casterlabs.co/v3/currencies/convert?from=%s&to=%s&amount=%f&formatResult=true", from, to, amount)))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();

        if (response.startsWith("{")) {
            throw new ApiException(
                Rson.DEFAULT
                    .fromJson(response, JsonObject.class)
                    .getArray("errors")
                    .toString()
            );
        } else {
            return response;
        }
    }

    @Getter
    @ToString
    @JsonClass(exposeAll = true)
    public static class CurrencyInfo {
        private String currencyName;
        private String currencyCode;
        private String locale;

        @Override
        public int hashCode() {
            return this.currencyCode.hashCode();
        }

    }

}
