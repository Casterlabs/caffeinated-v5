package co.casterlabs.caffeinated.core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;

import co.casterlabs.caffeinated.core.koi.Koi;
import co.casterlabs.caffeinated.core.preferences.Preferences;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import xyz.e3ndr.fastloggingframework.loggerimpl.FileLogHandler;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@JavascriptObject
public class App {
    public static final FastLogger LOGGER = new FastLogger();
    public static final File APP_DATA_DIR;

    public static App INSTANCE;
    public static Throwable appFailReason;

    static {
        App.class.getClassLoader().setPackageAssertionStatus("co.casterlabs.caffeinated.core", true);

        AppDirs appDirs = AppDirsFactory.getInstance();
        APP_DATA_DIR = new File(appDirs.getUserDataDir("casterlabs-caffeinated", null, null, true));

        new File(APP_DATA_DIR, "preferences").mkdirs();
        new File(APP_DATA_DIR, "preferences/old").mkdir();

        final File logsDir = new File(APP_DATA_DIR, "logs");
        final File logFile = new File(logsDir, "app.log");

        try {
            logsDir.mkdirs();
            logFile.delete();
            logFile.createNewFile();

            new FileLogHandler(logFile);

            FastLogger.logStatic("\n\n---------- %s ----------\n", Instant.now());
            FastLogger.logStatic("Log file: %s", logFile);
        } catch (IOException e) {
            FastLogger.logException(e);
        }

        try {
            INSTANCE = new App();
            INSTANCE.init();
        } catch (Throwable t) {
            App.LOGGER.fatal("Unable to load app:\n%s", t);
            appFailReason = t;
        }
    }

    public final Connection preferencesDatabase;
    public final Preferences preferences;

    public final UI ui;
    public final Auth auth;
    public final Koi koi;

    @JavascriptValue(watchForMutate = true, allowSet = false)
    public final Themes themes;

    private App() throws Throwable {
        this.preferencesDatabase = DriverManager.getConnection("jdbc:sqlite:" + new File(Preferences.PREFERENCE_DATA_DIR, "database.sqlite").getCanonicalPath());
        this.preferences = Preferences.load();

        this.themes = new Themes();
        this.ui = new UI();
        this.auth = new Auth();
        this.koi = new Koi();
    }

    public void init() throws Throwable {
        this.koi.init();
        this.auth.init();
    }

}
