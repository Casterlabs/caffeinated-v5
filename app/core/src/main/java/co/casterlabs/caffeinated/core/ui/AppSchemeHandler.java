package co.casterlabs.caffeinated.core.ui;

import java.io.FileNotFoundException;
import java.io.InputStream;

import co.casterlabs.caffeinated.core.App;
import co.casterlabs.saucer.scheme.MimeTypes;
import co.casterlabs.saucer.scheme.SaucerSchemeHandler;
import co.casterlabs.saucer.scheme.SaucerSchemeRequest;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse.SaucerRequestError;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppSchemeHandler implements SaucerSchemeHandler {
    public static final SaucerSchemeHandler INSTANCE = new AppSchemeHandler();
    private static final FastLogger LOGGER = new FastLogger();

    @SneakyThrows
    @Override
    public SaucerSchemeResponse handle(SaucerSchemeRequest request) throws Throwable {
        String uri = request.url();

        try {
            if (uri.startsWith("/$caffeinated-sdk-root$")) {
                uri = uri.substring("/$caffeinated-sdk-root$".length());
            }

            if (uri.isEmpty()) {
                uri = "/index.html";
            } else {
                // Append `index.html` to the end when required.
                if (!uri.contains(".")) {
                    if (uri.endsWith("/")) {
                        uri += "index.html";
                    } else {
                        uri += ".html";
                    }
                }
            }

            byte[] contents;
            try (InputStream in = App.class.getResourceAsStream("/co/casterlabs/caffeinated/core/ui/html" + uri)) {
                if (in == null) {
                    throw new FileNotFoundException("Could not find UI file: /co/casterlabs/caffeinated/core/ui/html" + uri);
                }
                contents = in.readAllBytes();
            }
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            LOGGER.debug("200 %s -> app%s (%s)", request.url(), uri, mimeType);

            return SaucerSchemeResponse.success(SaucerStash.of(contents), mimeType);
        } catch (Throwable t) {
            LOGGER.severe("404 %s -> app%s\n%s", request.url(), uri, t);
            return SaucerSchemeResponse.error(SaucerRequestError.SAUCER_REQUEST_ERROR_NOT_FOUND);
        }
    }

}
