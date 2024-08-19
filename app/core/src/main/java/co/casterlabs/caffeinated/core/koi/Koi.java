package co.casterlabs.caffeinated.core.koi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import co.casterlabs.caffeinated.core.App;
import co.casterlabs.caffeinated.core.launcher.AppInterface;
import co.casterlabs.caffeinated.core.util.ValueIdentityHashMap;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.koi.api.KoiConnection;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.CatchupEvent;
import co.casterlabs.koi.api.types.events.ConnectionStateEvent;
import co.casterlabs.koi.api.types.events.ConnectionStateEvent.ConnectionState;
import co.casterlabs.koi.api.types.events.MessageMetaEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptValue;
import lombok.EqualsAndHashCode;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@JavascriptObject
public class Koi {
    private static final FastLogger LOGGER = new FastLogger();
    private static final long RECONNECT_DELAY = TimeUnit.SECONDS.toMillis(10);

    public static final String CLIENT_ID = "LmHG2ux992BxqQ7w9RJrfhkW";

    public final List<Function<KoiEvent, Boolean>> eventPreprocessors = new ArrayList<>();
    public final List<Consumer<KoiEvent>> eventListeners = new ArrayList<>();

    @JavascriptValue(watchForMutate = true)
    public final Map<String, KoiAccount> accounts = new ValueIdentityHashMap<>();

    public final KoiHistory history = new KoiHistory();

    public void init() throws SQLException {
        this.history.init();
    }

    public synchronized void handleEvent(KoiEvent event, boolean triggerPreprocessors) {
        switch (event.type()) {
            case CONNECTION_STATE:
                return; // Already handled.

            case CATCHUP: {
                CatchupEvent catchup = (CatchupEvent) event;
                for (JsonElement oldEventJson : catchup.events) {
                    KoiEvent old = KoiEventType.get((JsonObject) oldEventJson);
                    if (old == null) continue;
                    this.history.storeEvent(old, oldEventJson);
                }
                return;
            }

            case META: {
                // Fire listeners.
                for (Consumer<KoiEvent> listener : this.eventListeners) {
                    try {
                        listener.accept(event);
                    } catch (Throwable t) {
                        LOGGER.severe("An error occurred whilst firing listener, ignoring.\n%s", t);
                    }
                }

                // Update the database.
                this.history.handleMetaEvent((MessageMetaEvent) event);
                return;
            }

            default: {
                if (triggerPreprocessors) {
                    for (Function<KoiEvent, Boolean> preprocessor : this.eventPreprocessors) {
                        try {
                            boolean shouldCancel = preprocessor.apply(event);
                            if (shouldCancel) return;
                        } catch (Throwable t) {
                            LOGGER.severe("An error occurred whilst firing listener, ignoring.\n%s", event);
                        }
                    }
                }

                JsonElement eventJson = Rson.DEFAULT.toJson(event);
                AppInterface.emit("koi-event", Rson.DEFAULT.toJson(event));
                this.history.storeEvent(event, eventJson);

                for (Consumer<KoiEvent> listener : this.eventListeners) {
                    try {
                        listener.accept(event);
                    } catch (Throwable t) {
                        LOGGER.severe("An error occurred whilst firing listener, ignoring.\n%s", t);
                    }
                }
                return;
            }
        }
    }

    void remove(String tokenId) {
        KoiAccount account = this.accounts.remove(tokenId);
        if (account == null) return;
        account.tokenId = null; // Tell the reconnect logic to bail-out.
        account.connection.close();
    }

    @SuppressWarnings("resource")
    public void connect(String tokenId) {
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

            @KoiEventHandler
            public void onConnectionStates(ConnectionStateEvent e) {
                account.connectionStates = e.states;
            }

            @Override
            public void onSupportedFeatures(List<KoiIntegrationFeatures> features) {
                account.supportedFeatures = features;
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
                switch (errorCode) {
                    case "USER_AUTH_INVALID": {
//                        CaffeinatedApp.getInstance().notify(
//                            "co.casterlabs.caffeinated.app.auth.logged_out",
//                            Map.of("platform", this.tokenId),
//                            NotificationType.WARNING
//                        );
                        remove(account.tokenId);
                        return;
                    }
                }
            }

            @Override
            public void onServerMessage(String message) {
                account.connection.getLogger().info("Server message: %s", message);
            }

            @Override
            public void onClose(boolean remote) {
                account.isAlive = false;
                try {
                    Thread.sleep(RECONNECT_DELAY + ThreadLocalRandom.current().nextLong(RECONNECT_DELAY)); // 5-10s.
                } catch (InterruptedException ignored) {}
                account.login();
            }
        }, CLIENT_ID);

        account.tokenId = tokenId;
        account.connection = connection;

        account.login();
        this.accounts.put(tokenId, account);
    }

    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class KoiAccount {
        public boolean isAlive = false;
        public String tokenId = null;

        public List<KoiIntegrationFeatures> supportedFeatures = Collections.emptyList();
        public Map<String, ConnectionState> connectionStates = Collections.emptyMap();
        public User profile = null;

        public @JsonExclude KoiConnection connection;

        private void login() {
            if (this.isAlive) return;
            if (this.tokenId == null) return;

            AsyncTask.create(() -> {
                try {
                    this.connection.login(App.INSTANCE.preferences.auth.getToken("koi", this.tokenId));
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
