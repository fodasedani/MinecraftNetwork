package at.albin.VELLSCAFFOLDING.listener;

import at.albin.VELLSCAFFOLDING.database.DatabaseManager;
import at.albin.VELLSCAFFOLDING.helper.Messages;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class PunishmentEnforcementListener {

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, Reason, expires_at FROM punishment " +
                             "WHERE UUID = ? AND TYPE = 'BAN' AND (expires_at IS NULL OR expires_at > NOW()) " +
                             "ORDER BY created_at DESC LIMIT 1"
             )) {

            ps.setString(1, uuid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String reason = rs.getString("Reason");
                    Timestamp expires = rs.getTimestamp("expires_at");

                    String until = (expires == null) ? "PERMANENT" : expires.toString();

                    event.setResult(ResultedEvent.ComponentResult.denied(
                            Messages.banScreen(reason, id, until)
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // im Fehlerfall lieber NICHT blocken
        }
    }

}
