package rip.snake.games.utils.colors;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;

public final class Legacy {

    public static final LegacyComponentSerializer LEGACY_AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final String AMPERSAND = "&";

    public static String deColor(String text) {
        if (text == null) return null;

        Matcher matcher = ChatColor.STRIP_COLOR_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder(text);

        while (matcher.find()) {
            String color = matcher.group(0);
            builder.replace(matcher.start(), matcher.end(), AMPERSAND + color.charAt(1));
        }

        return builder.toString();
    }

}