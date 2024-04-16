package co.casterlabs.caffeinated.core.ui;

public class Launcher {

    public static void main(String[] args) throws InterruptedException {
        new MainThread(Launcher::startupLogic); // Capture the main thread.
    }

    static void startupLogic() {
        AppInterface.openWindow();
    }

}
