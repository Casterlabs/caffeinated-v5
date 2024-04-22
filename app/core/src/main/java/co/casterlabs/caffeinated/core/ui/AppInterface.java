package co.casterlabs.caffeinated.core.ui;

import java.io.IOException;

import dev.webview.webview_java.Webview;
import dev.webview.webview_java.bridge.WebviewBridge;
import dev.webview.webview_java.uiserver.UIServer;
import xyz.e3ndr.fastloggingframework.LogUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppInterface {
    private static final FastLogger LOGGER = new FastLogger();

    private static Webview webview;
    private static WebviewBridge bridge;
    private static UIServer server;

    private static boolean useDarkAppearance = true;

    public static boolean isWindowOpen() {
        return webview != null;
    }

    public static void openWindow() {
        if (isWindowOpen()) return;

        // We need to create the resources on the main thread.
        MainThread.getInstance().execute(() -> {
            webview = new Webview(true);
            bridge = new WebviewBridge(webview); // TODO register objects.
            server = new UIServer();
            server.setHandler(AppSchemeHandler.INSTANCE);

            // TODO bridge tings
            // TODO sizes.

            webview.setDarkAppearance(useDarkAppearance);
            webview.setTitle("Casterlabs-Caffeinated");

            try {
                server.start();
                webview.loadURL(server.getLocalAddress());
            } catch (IOException e) {
                LOGGER.fatal("Unable to start UI server: %s", e);
                webview.setHTML(
                    "<!DOCTYPE html>"
                        + "<html style='background-color: #111113; color: #EEEEF0; font-family: system-ui;'>"
                        + "<h1 style='font-size: 1.25rem;'>Unable to start the UI server! Please report this to the Casterlabs developers:</h1>"
                        + "<pre>" + LogUtil.getExceptionStack(e) + "</pre>"
                        + "</html>"
                );
            }

            MainThread.getInstance().execute(webview); // Take over.
        });
    }

    static void onUIClose() {
        try {
            server.close();
        } catch (IOException ignored) {}

        System.exit(0); // TODO Temporary.
    }

    public static void setDarkAppearance(boolean shouldBeDark) {
        useDarkAppearance = shouldBeDark;
        if (webview != null) {
            webview.setDarkAppearance(shouldBeDark);
        }
    }

    public static void setTitle(String title) {
        if (webview != null) {
            webview.setTitle(title);
        }
    }

}
