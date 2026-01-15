package at.albin.VELUTILITYGUARD.helper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Messages {

    private Messages() {}

    public static Component prefix() {
        return Component.text("VEL-UTILITY", NamedTextColor.GOLD)
                .append(Component.space())
                .append(Component.text("»", NamedTextColor.GRAY))
                .append(Component.space());
    }

    public static Component unknownCMD() {
        return prefix()
                .append(Component.text("Unbekannter oder unvollständiger Befehl.", NamedTextColor.YELLOW))
                .append(Component.space());
    }
}
