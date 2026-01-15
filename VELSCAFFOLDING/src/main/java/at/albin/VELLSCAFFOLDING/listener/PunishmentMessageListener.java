package at.albin.VELLSCAFFOLDING.listener;

import at.albin.VELLSCAFFOLDING.database.DatabaseManager;
import at.albin.VELLSCAFFOLDING.helper.Channels;
import at.albin.VELLSCAFFOLDING.helper.Messages;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PunishmentMessageListener {

    private final ProxyServer proxy;
    private final Logger logger;
    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(2);

    private static final DateTimeFormatter UNTIL_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public PunishmentMessageListener(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(Channels.PUNISHMENT)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        byte[] data = event.getData();
        dbExecutor.execute(() -> handleMessage(data));
    }

    private void handleMessage(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {

            String action = in.readUTF();
            UUID actor = UUID.fromString(in.readUTF());
            UUID target = UUID.fromString(in.readUTF());

            String targetName = in.readUTF();
            String reason = in.readUTF();

            byte mode = in.readByte();   // 0=PERMA, 1=RELATIVE, 2=ABSOLUTE
            long amount = in.readLong();
            char unit = in.readChar();
            long absoluteMs = in.readLong();

            String shownName = targetName;

            switch (action) {
                case "BAN" -> {
                    ApplyResult res = applyPunishmentWithRules(target, "BAN", actor, reason, mode, amount, unit, absoluteMs);

                    proxy.getPlayer(actor).ifPresent(p -> {
                        if (!res.ok()) {
                            p.sendMessage(Messages.maxPunishment("BAN", shownName));
                        } else if (res.updated()) {
                            p.sendMessage(Messages.punishmentUpdated("BAN", shownName, formatUntil(res.newExpiry())));
                        } else {
                            p.sendMessage(Messages.executed("BAN", shownName));
                        }
                    });

                    if (res.ok()) {
                        proxy.getPlayer(target).ifPresent(p ->
                                p.disconnect(Messages.bannedWithId(reason, res.newId()))
                        );
                    }


                }

                case "MUTE" -> {
                    ApplyResult res = applyPunishmentWithRules(target, "MUTE", actor, reason, mode, amount, unit, absoluteMs);

                    // Feedback an Actor
                    proxy.getPlayer(actor).ifPresent(p -> {
                        if (!res.ok()) {
                            p.sendMessage(Messages.maxPunishment("MUTE", shownName));
                        } else if (res.updated()) {
                            p.sendMessage(Messages.punishmentUpdated("MUTE", shownName, formatUntil(res.newExpiry())));
                        } else {
                            p.sendMessage(Messages.executed("MUTE", shownName));
                        }
                    });

                    // Info an Target
                    if (res.ok()) {
                        proxy.getPlayer(target).ifPresent(p -> {
                            if (res.updated()) {
                                // gewünschte Message bei Anpassung
                                p.sendMessage(Messages.muteAdjusted());
                            } else {
                                // neuer Mute => "Du wurdest gemutet: <Grund> (ID)"
                                p.sendMessage(Messages.mutedWithId(reason, res.newId()));
                            }
                        });
                    }
                }

                case "UNBAN" -> {
                    boolean ended = endPunishment(target, "BAN");

                    proxy.getPlayer(actor).ifPresent(p -> {
                        if (ended) p.sendMessage(Messages.executed("UNBAN", shownName));
                        else p.sendMessage(Messages.noActivePunishment("BAN", shownName));
                    });
                }

                case "UNMUTE" -> {
                    boolean ended = endPunishment(target, "MUTE");

                    proxy.getPlayer(actor).ifPresent(p -> {
                        if (ended) p.sendMessage(Messages.executed("UNMUTE", shownName));
                        else p.sendMessage(Messages.noActivePunishment("MUTE", shownName));
                    });

                    if (ended) {
                        proxy.getPlayer(target).ifPresent(p -> p.sendMessage(Messages.muteLifted()));
                    }
                }

                case "KICK" -> {
                    insertPunishment(target, "KICK", actor, reason, null);

                    proxy.getPlayer(target).ifPresent(p ->
                            p.disconnect(Messages.kickScreen(reason))
                    );

                    proxy.getPlayer(actor).ifPresent(p ->
                            p.sendMessage(Messages.executed("KICK", shownName))
                    );
                }

                default -> logger.warn("Unbekannte Aktion via PluginMessage: {}", action);
            }

        } catch (Exception e) {
            logger.error("Fehler beim Verarbeiten der Punishment PluginMessage", e);
        }
    }

    private ApplyResult applyPunishmentWithRules(UUID target, String type, UUID actor, String reason,
                                                 byte mode, long amount, char unit, long absoluteMs) throws Exception {

        ActivePunishment active = getActive(target, type);

        // bereits permanent aktiv => maximale Strafe
        if (active != null && active.expiresAt == null) {
            return new ApplyResult(false, false, null, -1);
        }

        boolean updated = (active != null); // es gab schon eine aktive temp Strafe

        Timestamp currentExpiry = (active == null) ? null : active.expiresAt;
        Timestamp newExpiry = computeNewExpiry(mode, amount, unit, absoluteMs, currentExpiry);

        if (active != null) {
            endActiveNow(active.id);
        }

        long newId = insertPunishment(target, type, actor, reason, newExpiry);

        return new ApplyResult(true, updated, newExpiry, newId);
    }

    private Timestamp computeNewExpiry(byte mode, long amount, char unit, long absoluteMs, Timestamp currentExpiry) {
        if (mode == 0) return null; // PERMA

        if (mode == 2) { // ABSOLUTE
            return new Timestamp(absoluteMs);
        }

        ZonedDateTime base = (currentExpiry != null)
                ? currentExpiry.toInstant().atZone(ZoneId.systemDefault())
                : ZonedDateTime.now(ZoneId.systemDefault());

        ZonedDateTime out = switch (Character.toLowerCase(unit)) {
            case 'd' -> base.plusDays(amount);
            case 'w' -> base.plusWeeks(amount);
            case 'm' -> base.plusMonths(amount);
            case 'y' -> base.plusYears(amount);
            default -> base;
        };

        return Timestamp.from(out.toInstant());
    }

    private ActivePunishment getActive(UUID target, String type) throws Exception {
        String sql =
                "SELECT id, expires_at FROM punishment " +
                        "WHERE UUID = ? AND TYPE = ? AND (expires_at IS NULL OR expires_at > NOW()) " +
                        "ORDER BY created_at DESC LIMIT 1";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, target.toString());
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                long id = rs.getLong("id");
                Timestamp exp = rs.getTimestamp("expires_at"); // null => perma
                return new ActivePunishment(id, exp);
            }
        }
    }

    private void endActiveNow(long id) throws Exception {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE punishment SET expires_at = NOW() WHERE id = ?"
             )) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private boolean endPunishment(UUID target, String type) throws Exception {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE punishment SET expires_at = NOW() " +
                             "WHERE UUID = ? AND TYPE = ? AND (expires_at IS NULL OR expires_at > NOW())"
             )) {
            ps.setString(1, target.toString());
            ps.setString(2, type);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    private long insertPunishment(UUID target, String type, UUID actor, String reason, Timestamp expires) throws Exception {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO punishment (UUID, TYPE, Reason, ACTOR, created_at, expires_at) " +
                             "VALUES (?, ?, ?, ?, NOW(), ?)",
                     Statement.RETURN_GENERATED_KEYS
             )) {

            ps.setString(1, target.toString());
            ps.setString(2, type);
            ps.setString(3, reason);
            ps.setString(4, actor.toString());

            if (expires == null) ps.setNull(5, Types.TIMESTAMP);
            else ps.setTimestamp(5, expires);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        return -1;
    }

    private String formatUntil(Timestamp ts) {
        if (ts == null) return "PERMANENT";
        return UNTIL_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public void shutdown() {
        dbExecutor.shutdownNow();
    }

    private record ActivePunishment(long id, Timestamp expiresAt) {}
    private record ApplyResult(boolean ok, boolean updated, Timestamp newExpiry, long newId) {}
}
