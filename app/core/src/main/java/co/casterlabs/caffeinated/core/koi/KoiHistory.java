package co.casterlabs.caffeinated.core.koi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import co.casterlabs.caffeinated.core.App;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.ChannelPointsEvent;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.LikeEvent;
import co.casterlabs.koi.api.types.events.MessageMetaEvent;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.events.ViewerJoinEvent;
import co.casterlabs.koi.api.types.events.ViewerLeaveEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptObject;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@JavascriptObject
public class KoiHistory {
    private static final FastLogger LOGGER = new FastLogger();
    private static final int MAX_HISTORY_CHUNK = 250;

    public void init() throws SQLException {
        try {
            App.INSTANCE.preferencesDatabase.prepareStatement(
                "CREATE TABLE IF NOT EXISTS koi_historical (timestamp INTEGER NOT NULL, eventId TEXT NOT NULL PRIMARY KEY, data BLOB NOT NULL);"
            ).execute();
        } catch (SQLException x) {
            LOGGER.severe("Could not make historical data table:\n%s", x);
            throw x;
        }
    }

    @JavascriptFunction
    public List<KoiHistoryEntry> getHistoryAtOrBeforeTimestamp(long beforeOrAt) {
        List<Pair<String, byte[]>> history = new LinkedList<>();

        try (PreparedStatement ps = App.INSTANCE.preferencesDatabase.prepareStatement("SELECT data, eventId FROM koi_historical WHERE timestamp <= ?1 ORDER BY rowid ASC LIMIT " + MAX_HISTORY_CHUNK + ";")) {
            ps.setLong(1, beforeOrAt);
            ps.execute();

            ResultSet results = ps.getResultSet();
            while (results.next()) {
                history.add(new Pair<>(results.getString("eventId"), results.getBytes("data")));
            }
        } catch (Throwable t) {
            LOGGER.severe("Could not add onto historical data:\n%s", t);
        }

        return history.parallelStream()
            .map((entry) -> {
                try {
                    KoiEvent event = KoiEventType.get(Rson.DEFAULT.fromJson(decompress(entry.b()), JsonObject.class));
                    return new KoiHistoryEntry(entry.a(), event);
                } catch (JsonParseException e) {
                    e.printStackTrace(); // Probably never thrown...
                    return null;
                }
            })
            .filter((entry) -> entry != null)
            .sorted((entry1, entry2) -> Long.compare(entry1.event.timestamp.toEpochMilli(), entry2.event.timestamp.toEpochMilli()))
            .collect(Collectors.toList());
    }

    public void storeEvent(KoiEvent event, JsonElement eventJson) {
        String eventId = getEventId(event);
        if (eventId == null) return;

        try (PreparedStatement ps = App.INSTANCE.preferencesDatabase.prepareStatement("INSERT OR REPLACE INTO koi_historical (timestamp, eventId, data) VALUES (?1, ?2, ?3);")) {
            ps.setLong(1, event.timestamp.toEpochMilli());
            ps.setString(2, eventId);
            ps.setBytes(3, compress(eventJson.toString(false)));
            ps.execute();
        } catch (Throwable t) {
            LOGGER.severe("Could not add onto historical data:\n%s", t);
        }
    }

    public void handleMetaEvent(MessageMetaEvent metaEvent) {
        try (PreparedStatement ps = App.INSTANCE.preferencesDatabase.prepareStatement("SELECT data FROM koi_historical WHERE (eventId = ?1);")) {
            ps.setString(1, metaEvent.metaId);
            ps.execute();

            ResultSet results = ps.getResultSet();
            if (!results.first()) {
                return; // No data, don't try to modify.
            }

            String eventJson = decompress(results.getBytes("data"));
            JsonObject eventToModify = Rson.DEFAULT.fromJson(eventJson, JsonObject.class);

            eventToModify.put("upvotes", metaEvent.upvotes);
            eventToModify.put("is_visible", metaEvent.visible);

            try (PreparedStatement ps2 = App.INSTANCE.preferencesDatabase.prepareStatement("UPDATE koi_historical SET data = ?2 WHERE eventId = ?1;")) {
                ps2.setString(1, metaEvent.metaId);
                ps2.setBytes(2, compress(eventToModify.toString(false)));
                ps2.execute();
            }
        } catch (Throwable t) {
            LOGGER.severe("Could not modify historical data for event '%s':\n%s", metaEvent.metaId, t);
        }
    }

    private static String getEventId(KoiEvent event) {
        switch (event.type()) {
            case CHANNEL_POINTS:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((ChannelPointsEvent) event).sender.UPID);

            case FOLLOW:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((FollowEvent) event).follower.UPID);

            case LIKE:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((LikeEvent) event).liker.UPID);

            case RAID:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((RaidEvent) event).host.UPID);

            case RICH_MESSAGE:
                return ((RichMessageEvent) event).metaId;

            case SUBSCRIPTION: {
                String recipientIds = ((SubscriptionEvent) event).giftRecipients
                    .stream()
                    .map((u) -> u.UPID)
                    .collect(Collectors.joining("+"));
                return String.format("genid|%d|%s|%s>%d", event.timestamp.toEpochMilli(), event.type(), ((SubscriptionEvent) event).subscriber.UPID, recipientIds);
            }

            case VIEWER_JOIN:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((ViewerJoinEvent) event).viewer.UPID);

            case VIEWER_LEAVE:
                return String.format("genid|%d|%s|%s", event.timestamp.toEpochMilli(), event.type(), ((ViewerLeaveEvent) event).viewer.UPID);

            case CLEARCHAT:
            case ROOMSTATE:
            case STREAM_STATUS:
            case VIEWER_COUNT:
            case VIEWER_LIST:
                return String.format("genid|%d|%s", event.timestamp.toEpochMilli(), event.type());

            default:
                return null;
        }
    }

    private static byte[] compress(String input) {
        Deflater d = new Deflater();
        d.setLevel(5);
        d.setInput(input.getBytes(StandardCharsets.UTF_8));
        d.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!d.finished()) {
            int compressedSize = d.deflate(buffer);
            outputStream.write(buffer, 0, compressedSize);
        }

        return outputStream.toByteArray();
    }

    @SneakyThrows
    private static String decompress(byte[] input) {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int decompressedSize = inflater.inflate(buffer);
            outputStream.write(buffer, 0, decompressedSize);
        }

        byte[] bytes = outputStream.toByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @JsonClass(exposeAll = true)
    public static record KoiHistoryEntry(String uuid, KoiEvent event) {

    }

}
