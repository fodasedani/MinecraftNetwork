package at.albin.VELUTILITYGUARD.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.Map;

public class PunishmentAutocompleteFilter implements Listener {

    private final String pluginNamespace;

    // Command -> Permission
    private static final Map<String, String> PERMS = Map.of(
            "ban",   "vellscaffolding.punishment.ban",
            "unban", "vellscaffolding.punishment.unban",
            "mute",  "vellscaffolding.punishment.mute",
            "unmute","vellscaffolding.punishment.unmute",
            "kick",  "vellscaffolding.punishment.kick"
    );

    public PunishmentAutocompleteFilter(Plugin plugin) {
        // Namespace ist normalerweise plugin.yml "name" (klein geschrieben)
        this.pluginNamespace = "vel-bkm-guard";
    }

    @EventHandler
    public void onSend(PlayerCommandSendEvent event) {
        Player p = event.getPlayer();

        for (var entry : PERMS.entrySet()) {
            String cmd = entry.getKey();
            String perm = entry.getValue();

            boolean allowed = p.hasPermission(perm);

            String ns = pluginNamespace + ":" + cmd;

            if (!allowed) {
                event.getCommands().remove(cmd);
                event.getCommands().remove(ns);

            } else {
                event.getCommands().add(cmd);
                event.getCommands().add(ns);
            }
        }
    }
}