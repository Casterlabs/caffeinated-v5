package co.casterlabs.caffeinated.core.ui;

import dev.webview.webview_java.Webview;
import dev.webview.webview_java.bridge.WebviewBridge;

public class AppInterface {
    private static Webview webview;
    private static WebviewBridge bridge;

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

            // TODO bridge tings

            webview.setDarkAppearance(useDarkAppearance);
            webview.setTitle("Casterlabs-Caffeinated");
            // TODO sizes.
            webview.loadURL("https://google.com");

            MainThread.getInstance().execute(webview); // Take over.
        });
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
