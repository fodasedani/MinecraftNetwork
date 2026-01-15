package at.albin.VELUTILITYGUARD.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Set;

public class CommandAutocompleteFilter implements Listener {

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
            "trigger",
            "minecraft:trigger",
            "minecraft:ban",
            "velocity:callback",
            "minecraft:teammsg",
            "minecraft:tm",
            "tm",
            "teammsg",
            "icanhasbukkit",
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
    public void onSend(PlayerCommandSendEvent event) {
        if (event.getPlayer().hasPermission(BYPASS)) return;

        event.getCommands().removeIf(cmd -> {
            String c = cmd.toLowerCase();
            return BLOCKED.contains(c) || BLOCKED.contains("minecraft:" + c) || BLOCKED.contains("bukkit:" + c);
        });
    }
}