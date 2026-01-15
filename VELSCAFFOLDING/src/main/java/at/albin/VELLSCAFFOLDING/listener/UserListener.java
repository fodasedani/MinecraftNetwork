package at.albin.VELLSCAFFOLDING.listener;

import at.albin.VELLSCAFFOLDING.database.DatabaseManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserListener {
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();

        try (Connection con = DatabaseManager.getConnection()) {
            try (PreparedStatement check = con.prepareStatement("SELECT UUID FROM Users WHERE UUID = ?")) {
                check.setString(1, uuid);

                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        // Neuer User -> NICHT ONLINE setzen, weil er evtl. noch nicht auf Lobby ist
                        try (PreparedStatement insert = con.prepareStatement(
                                "INSERT INTO Users (UUID, IP_ADRESS, ONLINE, SERVER, RANK, PLAYTIME, FIRST_JOIN) " +
                                        "VALUES (?, ?, 'OFFLINE', '0', 'default', 0.00, ?)"
                        )) {
                            insert.setString(1, uuid);
                            insert.setString(2, ip);
                            insert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                            insert.executeUpdate();
                        }
                    } else {
                        // Existiert -> nur IP updaten, ONLINE/SERVER macht ServerConnectedEvent
                        try (PreparedStatement update = con.prepareStatement(
                                "UPDATE Users SET IP_ADRESS = ? WHERE UUID = ?"
                        )) {
                            update.setString(1, ip);
                            update.setString(2, uuid);
                            update.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String serverName = event.getServer().getServerInfo().getName();

        // Beim ersten echten Server-Connect (also wirklich online)
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement update = con.prepareStatement(
                     "UPDATE Users SET ONLINE = 'ONLINE', SERVER = ? WHERE UUID = ?"
             )) {
            update.setString(1, serverName); // z.B. lobby, survival, ...
            update.setString(2, uuid);
            update.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String now = LocalDateTime.now().format(TS_FMT);

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement update = con.prepareStatement(
                     "UPDATE Users SET ONLINE = ?, SERVER = '0' WHERE UUID = ?"
             )) {
            update.setString(1, now);   // offline -> Timestamp
            update.setString(2, uuid);
            update.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
