package at.albin.VELLSCAFFOLDING.helper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Messages {

    private Messages() {}

    public static Component prefix() {
        return Component.text("VEL-BKM", NamedTextColor.DARK_RED)
                .append(Component.space())
                .append(Component.text("»", NamedTextColor.GRAY))
                .append(Component.space());
    }

    public static Component executed(String type, String playerName) {
        return prefix()
                .append(Component.text("Der Auftrag wurde durchgeführt. ", NamedTextColor.RED))
                .append(Component.text("("+type + ", " + playerName.toUpperCase()+")", NamedTextColor.GRAY));
    }

    public static Component mutedWithId(String reason, long muteId) {
        return prefix()
                .append(Component.text("Du wurdest gemutet: ", NamedTextColor.RED))
                .append(Component.text(reason, NamedTextColor.GRAY))
                .append(Component.space());
    }

    public static Component bannedWithId(String reason, long banId) {
        return Component.text("VEL-BKM", NamedTextColor.DARK_RED)
                .append(Component.newline())
                .append(Component.newline()) // eine Zeile Abstand
                .append(Component.text("Du wurdest gebannt.", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Grund: ", NamedTextColor.DARK_RED))
                .append(Component.text(reason, NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Ban-ID: ", NamedTextColor.DARK_RED))
                .append(Component.text(String.valueOf(banId), NamedTextColor.RED));
    }

    public static Component banScreen(String reason, long banId, String until) {
        return Component.text("VEL-BKM", NamedTextColor.DARK_RED)
                .append(Component.newline())
                .append(Component.newline())

                .append(Component.text("Du wurdest gebannt.", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.newline())

                .append(Component.text("Grund: ", NamedTextColor.DARK_RED))
                .append(Component.text(reason, NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.newline())

                .append(Component.text("Bis: ", NamedTextColor.DARK_RED))
                .append(Component.text(until, NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.newline())

                .append(Component.text("Ban-ID: ", NamedTextColor.DARK_RED))
                .append(Component.text(String.valueOf(banId), NamedTextColor.RED));
    }



    public static Component noActivePunishment(String type, String playerName) {
        return prefix()
                .append(Component.text("Der Spieler hat keine aktive Strafe.", NamedTextColor.RED));
    }

    public static Component maxPunishment(String type, String playerName) {
        return prefix()
                .append(Component.text("Der Spieler hat bereits die maximale Strafe. ", NamedTextColor.RED));
    }

    public static Component punishmentUpdated(String type, String playerName, String newUntil) {
        return prefix()
                .append(Component.text(playerName.toUpperCase(), NamedTextColor.DARK_RED))
                .append(Component.text(" hatte bereits eine aktive Strafe. Aktualisiert: ", NamedTextColor.RED))
                .append(Component.text(type, NamedTextColor.DARK_RED))
                .append(Component.text(" bis ", NamedTextColor.RED))
                .append(Component.text(newUntil, NamedTextColor.DARK_RED));
    }

    public static Component kickScreen(String reason) {
        return Component.text("VEL-BKM", NamedTextColor.DARK_RED)
                .append(Component.newline())
                .append(Component.newline())

                .append(Component.text("Du wurdest gekickt.", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.newline())

                .append(Component.text("Grund: ", NamedTextColor.DARK_RED))
                .append(Component.text(reason, NamedTextColor.RED));
    }

    public static Component muteAdjusted() {
        return prefix()
                .append(Component.text("Dein Mute wurde angepasst.", NamedTextColor.RED));
    }

    public static Component muteLifted() {
        return prefix()
                .append(Component.text("Dein Mute wurde aufgehoben.", NamedTextColor.GREEN));
    }

}
