package co.casterlabs.caffeinated.core.ui;

public class Launcher {

    public static void main(String[] args) throws Exception {
        System.out.println("Capturing main thread...");
        new MainThread(Launcher::startupLogic); // Capture the main thread.
    }

    static void startupLogic() {
        System.out.println("Opening window...");
        AppInterface.openWindow();
    }

}
