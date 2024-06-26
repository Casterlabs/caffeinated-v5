import Currencies from "./currencies.mjs";
import Conn from "./conn.mjs";
import App from "./app.mjs";
import { setLocaleProvider } from "./app.mjs";
import EventHandler from "./eventHandler.mjs";

const queryParams = (() => {
    let vars = {};

    location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, (m, key, value) => {
        vars[key] = value;
    });

    return vars;
})();

const { pluginId, widgetId, authorization } = queryParams;
const port = queryParams.port || "8092";
const address = queryParams.validAddress || "localhost";
const widgetMode = (queryParams.mode || "WIDGET").toUpperCase();
const customCSS = (queryParams.customCSS || "");

export function deepFreeze(object) {
    const propNames = Object.getOwnPropertyNames(object);

    for (const name of propNames) {
        const value = object[name];

        if (value && typeof value === "object") {
            deepFreeze(value);
        }
    }

    return Object.freeze(object);
}

export function escapeHtml(unsafe) {
    return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

export function init({ initHandler, disconnectHandler }) {
    if (window.parent) {
        window.parent.postMessage({ call: "init", value: widgetId }, "*");
    }

    const conn = new Conn(`ws://${address}:${port}/api/plugin/${pluginId}/widget/${widgetId}/realtime?authorization=${authorization}&mode=${widgetMode}`);

    const openLink = (link) => {
        conn.send("OPEN_LINK", { link });
    };

    const widgetInstanceEventHandler = new EventHandler();

    // The `Widget` global.
    const widgetInstance = {
        ...widgetInstanceEventHandler,

        on(type, handler) {
            if (conn.connectionId && ["init", "update"].includes(type.toLowerCase())) {
                setTimeout(handler, 2); // Execute the handler after a couple of browser ticks.
            }
            widgetInstanceEventHandler.on(type, handler);
        },

        get connectionId() {
            return conn.connectionId;
        },

        get widgetData() {
            return conn.widgetData;
        },

        get mode() {
            return widgetMode;
        },

        getSetting(key) {
            return conn.widgetData.settings[key];
        },

        emit(type, data = {}) {
            if (type.startsWith("__internal:")) {
                throw "__internal is a reserved prefix.";
            }

            conn.emit(type, data);
        },

        getResource(resourceId) {
            return new Promise((resolve) => {
                this.once(`__internal:resource_poll:${resourceId}`, resolve);
                conn.emit("__internal:resource_poll", resourceId);
            });
        }
    };

    // The `Koi` global.
    let koi_statics = {};

    const koiInstance = {
        ...new EventHandler(),

        get eventHistory() {
            return koi_statics.history;
        },

        get viewers() {
            return koi_statics.viewers;
        },

        get viewerCounts() {
            return koi_statics.viewerCounts;
        },

        get userStates() {
            return koi_statics.userStates;
        },

        get streamStates() {
            return koi_statics.streamStates;
        },

        get roomStates() {
            return koi_statics.roomStates;
        },

        get features() {
            return koi_statics.features;
        },

        upvoteChat(platform, messageId) {
            conn.send("KOI", {
                type: "UPVOTE",
                platform: platform,
                messageId: messageId
            });
        },

        deleteChat(platform, messageId, isUserGesture = true) {
            conn.send("KOI", {
                type: "DELETE",
                platform: platform,
                messageId: messageId,
                isUserGesture: isUserGesture
            });
        },

        sendChat(platform, message, chatter = "CLIENT", replyTarget = null, isUserGesture = true) {
            conn.send("KOI", {
                type: "MESSAGE",
                platform: platform,
                message: message,
                chatter: chatter,
                replyTarget: replyTarget,
                isUserGesture: isUserGesture
            });
        },

        getMaxLength(platform) {
            switch (platform) {
                case "CAFFEINE":
                    return 80;

                case "TWITCH":
                    return 500;

                case "TROVO":
                    return 300;

                case "GLIMESH":
                    return 255;

                case "BRIME":
                    return 300;

                default:
                    console.debug(platform);
                    return 100; // ?
            }
        }

        // test(event) {
        //     if (this.isAlive()) {
        //         this.ws.send(JSON.stringify({
        //             type: "TEST",
        //             eventType: event.toUpperCase()
        //         }));
        //     }
        // }
    };

    // The `Music` global.
    let music_data = {};

    const musicInstance = {
        ...new EventHandler(),

        get providers() {
            return music_data.providers;
        },

        get activePlayback() {
            return music_data.activePlayback;
        }
    };

    // The `App` global.
    let appLocalizationTasks = {};
    setLocaleProvider((key = '', knownPlaceholders = {}, knownComponents = []) => {
        return new Promise((resolve) => {
            const nonce = Math.random(28).toString();

            appLocalizationTasks[nonce] = resolve;

            conn.send("LOCALIZE", {
                nonce: nonce,
                key: key || '',
                knownPlaceholders: knownPlaceholders || {},
                knownComponents: knownComponents || []
            });
        });
    });

    deepFreeze(widgetInstance);
    deepFreeze(koiInstance);
    deepFreeze(musicInstance);

    // Listen for events on the conn, fire them off, yeah you get the idea.
    conn.on("init", ({ basePath }) => {
        // initHandler returns false if it should be handled by the implementer manually.
        if (!initHandler || initHandler({ conn, koiInstance, widgetInstance, musicInstance, Currencies, koi_statics, address, port, pluginId, widgetId, authorization, widgetMode, App, basePath, openLink, customCSS })) {
            App.init();
            widgetInstance.broadcast("init");
            widgetInstance.broadcast("update");
            koiInstance.broadcast("koi_statics", koi_statics);
            conn.send("READY", {});
        }
    });

    conn.on("update", () => {
        widgetInstance.broadcast("update");
    });

    conn.on("emission", ({ type, data }) => {
        widgetInstance.broadcast(type, data, false);
    });

    conn.on("koi_statics", (statics) => {
        deepFreeze(statics);
        koi_statics = statics;
        koiInstance.broadcast("koi_statics", koi_statics);
    });

    conn.on("koi", (event) => {
        koiInstance.broadcast(event.event_type, event);
    });

    conn.on("music", (music) => {
        deepFreeze(music);
        music_data = music;
        musicInstance.broadcast("music", music_data);
    });

    conn.on("app", ({ language, emojiProvider, theme, appearance, zoom }) => {
        App.mutate("language", language);
        App.mutate("emojiProvider", emojiProvider);
        App.mutate("theme", theme);
        App.mutate("appearance", appearance);
        App.mutate("zoom", zoom);
    });

    conn.on("localize", ({ nonce, value }) => {
        appLocalizationTasks[nonce](value);
    });

    // We completely reset the widget everytime it loses connection.
    conn.on("close", () => {
        if (!disconnectHandler || disconnectHandler()) {
            setTimeout(() => {
                location.reload();
            }, 2500);
        }
    });

    // Connect.
    conn.connect();

    // Debug
    Object.defineProperty(window, "Koi", {
        value: koiInstance,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "Widget", {
        value: widgetInstance,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "Music", {
        value: musicInstance,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "escapeHtml", {
        value: escapeHtml,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "openLink", {
        value: openLink,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "Currencies", {
        value: Currencies,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "App", {
        value: App,
        writable: false,
        configurable: true
    });
}