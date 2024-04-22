package co.casterlabs.caffeinated.core.ui;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.rhs.session.HttpSession;
import co.casterlabs.rhs.util.MimeTypes;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.LogUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppSchemeHandler implements Function<HttpSession, HttpResponse> {
    public static final Function<HttpSession, HttpResponse> INSTANCE = new AppSchemeHandler();
    private static final FastLogger LOGGER = new FastLogger();

    @SneakyThrows
    @Override
    public @Nullable HttpResponse apply(@Nullable HttpSession request) {
        String uri = request.getUri();

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
            try (InputStream in = AppSchemeHandler.class.getClassLoader().getResourceAsStream("co/casterlabs/caffeinated/core/ui/html" + uri)) {
                if (in == null) {
                    throw new FileNotFoundException("Could not find UI file: co/casterlabs/caffeinated/core/ui/html" + uri);
                }
                contents = in.readAllBytes();
            }
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            LOGGER.debug("200 %s -> app%s (%s)", request.getUri(), uri, mimeType);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, contents)
                .setMimeType(mimeType);
        } catch (Throwable t) {
            LOGGER.severe("404 %s -> app%s\n%s", request.getUri(), uri, t);

            return HttpResponse.newFixedLengthResponse(
                StandardHttpStatus.INTERNAL_ERROR,
                "<!DOCTYPE html>"
                    + "<html style='background-color: #111113; color: #EEEEF0; font-family: system-ui;'>"
                    + "<h1 style='font-size: 1.25rem;'>500 Internal Error</h1>"
                    + "<pre>" + LogUtil.getExceptionStack(t) + "</pre>"
                    + "</html>"
            )
                .setMimeType("text/html");
        }
    }

}
