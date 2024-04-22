package co.casterlabs.caffeinated.core.ui;

import java.io.IOException;

import co.casterlabs.caffeinated.core.App;
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

            if (App.INSTANCE == null) {
                // We are so borked at this point that we won't even attempt to setup the
                // bridge.
                webview.setHTML(AppInterface.generateErrorHtml("App failed to initialize:", App.appFailReason));
            } else {
                try {
                    // Check for the development UI server.
                    if (System.getProperty("caffeinated.ui_override") == null) {
                        server.start();
                        webview.loadURL(server.getLocalAddress());
                    } else {
                        webview.loadURL(System.getProperty("caffeinated.ui_override"));
                    }

                    webview.bind("internalSetDarkAppearance", (args) -> {
                        useDarkAppearance = args.contains("true");
                        webview.setDarkAppearance(useDarkAppearance);
                        return null;
                    });

                    bridge.defineObject("App", App.INSTANCE);
                    webview.setSize(App.INSTANCE.preferences.ui.width, App.INSTANCE.preferences.ui.height);
                } catch (IOException e) {
                    LOGGER.fatal("Unable to start UI server: %s", e);
                    webview.setHTML(
                        AppInterface.generateErrorHtml("Unable to start the UI server! Please report this to the Casterlabs developers:", e)
                    );
                }
            }

            webview.setDarkAppearance(useDarkAppearance);
            webview.setTitle("Casterlabs-Caffeinated");

            MainThread.getInstance().execute(webview); // Take over.
        });
    }

    static void onUIClose() {
        try {
            server.close();
        } catch (IOException ignored) {}

        System.exit(0); // TODO Temporary.
    }

    public static void setTitle(String title) {
        if (webview != null) {
            webview.setTitle(title);
        }
    }

    public static String generateErrorHtml(String title, Throwable reason) {
        return "<!DOCTYPE html>"
            + "<html style='background-color: #111113; color: #EEEEF0; font-family: system-ui;'>"
            + "<h1 style='font-size: 1.25rem;'>" + title + "</h1>"
            + "<pre>" + LogUtil.getExceptionStack(reason) + "</pre>"
            + "</html>";
    }

}
