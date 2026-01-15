package at.albin.VELLSCAFFOLDING.helper;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

public final class Channels {
    private Channels() {}

    public static final ChannelIdentifier PUNISHMENT =
            MinecraftChannelIdentifier.create("vellscaffolding", "punishment");
}
