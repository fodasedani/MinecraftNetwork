package at.albin.VELUTILITYGUARD.listener;

import at.albin.VELUTILITYGUARD.helper.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;

public class UnknownCommandListener implements Listener {

    @EventHandler
    public void onUnknown(UnknownCommandEvent event) {
        CommandSender sender = event.getSender();
        if (!(sender instanceof Player p)) return;

        event.message(Messages.unknownCMD());

    }
}

