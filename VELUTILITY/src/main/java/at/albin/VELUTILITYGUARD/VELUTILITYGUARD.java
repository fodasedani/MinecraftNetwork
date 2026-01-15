package at.albin.VELUTILITYGUARD;

import at.albin.VELUTILITYGUARD.listener.CommandAutocompleteFilter;
import at.albin.VELUTILITYGUARD.listener.CommandBlockListener;
import at.albin.VELUTILITYGUARD.listener.PunishmentAutocompleteFilter;
import at.albin.VELUTILITYGUARD.listener.UnknownCommandListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class VELUTILITYGUARD extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new CommandAutocompleteFilter(), this);

        getServer().getPluginManager().registerEvents(new CommandBlockListener(), this);

        getServer().getPluginManager().registerEvents(new PunishmentAutocompleteFilter(this), this);

        getServer().getPluginManager().registerEvents(new UnknownCommandListener(), this);
    }


}
