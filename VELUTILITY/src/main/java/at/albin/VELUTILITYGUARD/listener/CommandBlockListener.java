package at.albin.VELUTILITYGUARD.listener;

import at.albin.VELUTILITYGUARD.helper.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class CommandBlockListener implements Listener {

    private static final String BYPASS = "vellscaffolding.utility.bypasscmd";

    private static final Set<String> BLOCKED = Set.of(
            "tell",
            "me",
            "pl",
            "plugins",
            "bukkit:plugins",
            "version",
            "bukkit:version",
            "bukkit:help",
            "help",
            "minecraft:help",
            "bukkit:about",
            "about",
            "?",
            "bukkit:?",
            "bukkit:ver",
            "bukkit:pl",
            "ver",
            "minecraft:me",
            "minecraft:tell",
            "minecraft:w",
            "w",
            "minecraft:version",
            "minecraft:kick",
            "minecraft:ban",
            "minecraft:teammsg",
            "minecraft:tm",
            "tm",
            "teammsg",
            "icanhasbukkit",
            "velocity:callback",
            "trigger",
            "minecraft:trigger",
            "luckperms:lp",
            "luckperms:permission",
            "luckperms:permissions",
            "luckperms:perms",
            "perms",
            "permissions",
            "permission",
            "lp",
            "luckpermsvelocity",
            "lpv",
            "luckperms:perm",
            "perm",
            "luckperms:luckperms",
            "luckperms",
            "minecraft:pardon"
    );

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().hasPermission(BYPASS)) return;

        String msg = e.getMessage();
        if (msg == null) return;

        String cmd = msg.trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        cmd = cmd.toLowerCase();

        String base = cmd.split("\\s+")[0];

        if (BLOCKED.contains(base)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Messages.unknownCMD());
        }
    }
}