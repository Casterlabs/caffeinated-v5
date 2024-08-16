package co.casterlabs.caffeinated.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import co.casterlabs.caffeinated.core.ui.AppInterface;
import co.casterlabs.caffeinated.core.util.ValueIdentityHashMap;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.koi.api.KoiConnection;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;
import lombok.EqualsAndHashCode;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@JavascriptObject
public class Koi {
    private static final FastLogger LOGGER = new FastLogger();
    private static final long RECONNECT_DELAY = TimeUnit.SECONDS.toMillis(15);

    public static final String CLIENT_ID = "LmHG2ux992BxqQ7w9RJrfhkW";

    public final List<Function<KoiEvent, Boolean>> eventPreprocessors = new ArrayList<>();
    public final List<Consumer<KoiEvent>> eventListeners = new ArrayList<>();

    @JavascriptValue(watchForMutate = true)
    public final Map<String, KoiAccount> accounts = new ValueIdentityHashMap<>();

    @JavascriptValue
    public final List<KoiEvent> history = new LinkedList<>();

    Koi() {
        // TODO
//        if (App.INSTANCE.preferences.tokens.koi.isEmpty()) {
//            LOGGER.info("No tokens!");
//        } else {
//            LOGGER.info("Found some tokens, connecting them...");
//            App.INSTANCE.preferences.tokens.koi.forEach(this::connect);
//        }
        this.connect(System.getProperty("caffeinated.test.token"));

        this.eventListeners.add((e) -> {
            AppInterface.emit("koi-event", Rson.DEFAULT.toJson(e));
        });
    }

    public void handleEvent(KoiEvent e, boolean triggerPreprocessors) {
        // TODO
        LOGGER.info(e.type());

        if (triggerPreprocessors) {
            for (Function<KoiEvent, Boolean> preprocessor : this.eventPreprocessors) {
                try {
                    boolean shouldCancel = preprocessor.apply(e);
                    if (shouldCancel) return;
                } catch (Throwable t) {
                    LOGGER.severe("An error occurred whilst firing listener, ignoring.\n%s", e);
                }
            }
        }

        this.history.add(e);

        for (Consumer<KoiEvent> listener : this.eventListeners) {
            try {
                listener.accept(e);
            } catch (Throwable t) {
                LOGGER.severe("An error occurred whilst firing listener, ignoring.\n%s", e);
            }
        }
    }

    public void remove(String token) {
        KoiAccount account = this.accounts.remove(token);
        if (account == null) return;
        account.token = null; // Tell the reconnect logic to bail-out.
        account.connection.close();
    }

    @SuppressWarnings("resource")
    private void connect(String token) {
        KoiAccount account = new KoiAccount();
        KoiConnection connection = new KoiConnection(KoiConnection.KOI_URL, new KoiLifeCycleHandler() {
            @KoiEventHandler
            public void onEvent(KoiEvent e) {
                LOGGER.trace(e);
                handleEvent(e, true);
            }

            @KoiEventHandler
            public void onUserUpdate(UserUpdateEvent e) {
                account.profile = e.streamer;
                if (!account.isAlive) {
                    account.isAlive = true;
                    account.connection.getLogger().info("Logged in!");
                }
            }

            @Override
            public void onSupportedFeatures(List<KoiIntegrationFeatures> features) {
                account.connection.getLogger().debug("Platform features: %s", features);
            }

            @Override
            public void onException(Exception e) {
                account.connection.getLogger().severe(e);
            }

            @Override
            public void onClientScopes(List<String> scopes) {
                account.connection.getLogger().debug("Client scopes: %s", scopes);
            }

            @Override
            public void onError(String errorCode) {
                account.connection.getLogger().severe("Server reported error: %s", errorCode);
            }

            @Override
            public void onServerMessage(String message) {
                account.connection.getLogger().info("Server message: %s", message);
            }

            @Override
            public void onClose(boolean remote) {
                account.isAlive = false;
                try {
                    Thread.sleep(RECONNECT_DELAY);
                } catch (InterruptedException ignored) {}
                account.login();
            }
        }, CLIENT_ID);

        account.token = token;
        account.connection = connection;

        account.login();
        this.accounts.put(token, account);
    }

    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class KoiAccount {
        public boolean isAlive = false;
        public String token;
        public User profile;

        public @JsonExclude KoiConnection connection;

        private void login() {
            if (this.isAlive) return;
            if (this.token == null) return;

            AsyncTask.create(() -> {
                try {
                    this.connection.login(this.token);
                } catch (IllegalStateException ignored) {
                    // NOOP
                } catch (InterruptedException | IOException e) {
                    this.connection.getLogger().severe("An exception occurred whilst trying to login... Retrying in a few seconds...");
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ignored) {}
                    this.login();
                }
            });
        }

    }

}
