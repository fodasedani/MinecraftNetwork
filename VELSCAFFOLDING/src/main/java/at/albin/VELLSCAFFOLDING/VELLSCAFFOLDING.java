package at.albin.VELLSCAFFOLDING;

import at.albin.VELLSCAFFOLDING.listener.*;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

@Plugin(
        id = "vell-scaffolding",
        name = "VELL-SCAFFOLDING",
        version = at.albin.VELLSCAFFOLDING.BuildConstants.VERSION
)
public class VELLSCAFFOLDING {

    private final ProxyServer proxy;
    private final Logger logger;

    @Inject
    public VELLSCAFFOLDING(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        proxy.getChannelRegistrar().register(
                MinecraftChannelIdentifier.create("vellscaffolding", "punishment")
        );

        proxy.getEventManager().register(this, new UserListener());
        proxy.getEventManager().register(this, new SessionLoggerListener());
        proxy.getEventManager().register(this, new PunishmentEnforcementListener());
        proxy.getEventManager().register(this, new PunishmentMessageListener(proxy, logger));
        logger.info("VELL-SCAFFOLDING Plugin wurde erfolgreich geladen!");
    }
}
