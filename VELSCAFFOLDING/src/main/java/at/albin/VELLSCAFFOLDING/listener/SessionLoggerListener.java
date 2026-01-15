package at.albin.VELLSCAFFOLDING.listener;

import at.albin.VELLSCAFFOLDING.database.DatabaseManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SessionLoggerListener {

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isPresent()) {
            return; // Serverwechsel -> keine neue Session-Zeile
        }

        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();

        try (Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);

            // Falls noch eine ONLINE-Session existiert -> NULL (Crash/Proxy-Kill)
            try (PreparedStatement crashMark = con.prepareStatement(
                    "UPDATE UserSessions SET LEAVE_DATETIME = NULL " +
                            "WHERE UUID = ? AND LEAVE_DATETIME = 'ONLINE'"
            )) {
                crashMark.setString(1, uuid);
                crashMark.executeUpdate();
            }

            // Neue Session starten
            try (PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO UserSessions (UUID, IP_ADRESS, JOIN_DATETIME, LEAVE_DATETIME) " +
                            "VALUES (?, ?, NOW(), 'ONLINE')"
            )) {
                insert.setString(1, uuid);
                insert.setString(2, ip);
                insert.executeUpdate();
            }

            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement update = con.prepareStatement(
                     "UPDATE UserSessions " +
                             "SET LEAVE_DATETIME = DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') " +
                             "WHERE UUID = ? AND LEAVE_DATETIME = 'ONLINE' " +
                             "ORDER BY JOIN_DATETIME DESC " +
                             "LIMIT 1"
             )) {
            update.setString(1, uuid);
            update.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
