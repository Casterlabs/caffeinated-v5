package co.casterlabs.caffeinated.core.launcher;

import co.casterlabs.caffeinated.core.App;

public class Launcher {

    public static void main(String[] args) throws Exception {
        App.LOGGER.info("Loading the app...");
        App.class.toString(); // LOAD.

        App.LOGGER.info("Capturing main thread...");
        new MainThread(Launcher::startupLogic); // Capture the main thread.
    }

    static void startupLogic() {
        App.LOGGER.info("Opening window...");
        AppInterface.openWindow();
    }

}
