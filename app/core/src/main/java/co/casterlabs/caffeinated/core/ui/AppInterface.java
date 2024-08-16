package co.casterlabs.caffeinated.core.ui;

import org.unbescape.uri.UriEscape;

import co.casterlabs.caffeinated.core.App;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.utils.SaucerIcon;
import co.casterlabs.saucer.utils.SaucerSize;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.AllArgsConstructor;
import xyz.e3ndr.fastloggingframework.LogUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppInterface {
    private static final FastLogger LOGGER = new FastLogger();

    private static Saucer saucer;

//    private static boolean useDarkAppearance = true;

    public static boolean isWindowOpen() {
        return saucer != null;
    }

    public static void openWindow() {
        if (isWindowOpen()) return;

        // We need to create the resources on the main thread.
        MainThread.getInstance().execute(() -> {
            saucer = Saucer.create();

//            saucer.bridge().defineObject(null, LOGGER)

            if (App.INSTANCE == null) {
                // We are so borked at this point that we won't even attempt to setup the
                // bridge.
                saucer.webview().setUrl(AppInterface.generateErrorUrl("App failed to initialize:", App.appFailReason));
            } else {
                try {
                    // Check for the development UI server.
                    if (System.getProperty("caffeinated.ui_override") == null) {
                        saucer.webview().serveScheme("/");
                    } else {
                        saucer.webview().setDevtoolsVisible(true);
                        saucer.webview().setUrl(System.getProperty("caffeinated.ui_override"));
                    }

//                    webview.bind("internalSetDarkAppearance", (args) -> {
//                        useDarkAppearance = args.contains("true");
//                        webview.setDarkAppearance(useDarkAppearance);
//                        return null;
//                    });

                    saucer.window().setSize(new SaucerSize(App.INSTANCE.preferences.ui.width, App.INSTANCE.preferences.ui.height));

                    saucer.bridge().defineObject("App", App.INSTANCE);
                    saucer.bridge().apply();
                } catch (Throwable t) {
                    LOGGER.fatal("Unable to start UI server: %s", t);
                    saucer.webview().setUrl(AppInterface.generateErrorUrl("Unable to start the UI server! Please report this to the Casterlabs developers:", t));
                }
            }

            saucer.webview().setSchemeHandler(AppSchemeHandler.INSTANCE);

//            webview.setDarkAppearance(useDarkAppearance);
            setIcon(App.INSTANCE.preferences.ui.icon);
            setTitle("Casterlabs-Caffeinated");
            saucer.window().show();

            LOGGER.info("Starting saucer...");
            MainThread.getInstance().execute(new DummySaucerRunnable(saucer)); // Take over.
        });
    }

    static void onUIClose() {
        saucer.close();
        System.exit(0); // TODO Temporary.
    }

    public static void setTitle(String title) {
        if (saucer != null) {
            saucer.window().setTitle(title);
        }
    }

    public static void setIcon(String name) {
        if (saucer != null) {
            try {
                byte[] bytes = StreamUtil.toBytes(App.class.getResourceAsStream("/co/casterlabs/caffeinated/core/ui/assets/logo/" + name + ".png"));
                saucer.window().setIcon(SaucerIcon.of(SaucerStash.of(bytes)));
            } catch (Throwable t) {
                LOGGER.severe("Unable to load icon '%s':\n%s", name, t);
            }
        }
    }

    public static String generateErrorUrl(String title, Throwable reason) {
        return "data:text/html," + UriEscape.escapeUriPath(
            "<!DOCTYPE html>"
                + "<html style='background-color: #111113; color: #EEEEF0; font-family: system-ui;'>"
                + "<h1 style='font-size: 1.25rem;'>" + title + "</h1>"
                + "<pre>" + LogUtil.getExceptionStack(reason) + "</pre>"
                + "</html>"
        );
    }

    public static void emit(String type, JsonElement data) {
        if (saucer != null) {
            saucer.messages().emit(
                new JsonObject()
                    .put("type", type)
                    .put("data", data)
            );
        }
    }

    @AllArgsConstructor
    public static class DummySaucerRunnable implements Runnable {
        public final Saucer saucer;

        @Override
        public void run() {}
    }

}
