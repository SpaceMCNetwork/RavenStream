package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

public class DirectoCommand implements SimpleCommand {

    private final ProxyServer server;
    private final Map<String, Object> config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    public DirectoCommand(ProxyServer server, Map<String, Object> config) {
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Solo los jugadores pueden usar este comando."));
            return;
        }

        Player player = (Player) invocation.source();
        String playerUuid = player.getUniqueId().toString();

        if (!player.hasPermission("ravenstream.use")) {
            String message = (String) ((Map<String, Object>) config.get("messages")).get("no_permission");
            player.sendMessage(serializer.deserialize(message));
            return;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownDuration = ((Integer) config.get("cooldown")).longValue() * 1000;
        
        if (cooldowns.containsKey(playerUuid)) {
            long lastUsed = cooldowns.get(playerUuid);
            if (currentTime - lastUsed < cooldownDuration) {
                long remaining = (cooldownDuration - (currentTime - lastUsed)) / 1000;
                String message = (String) ((Map<String, Object>) config.get("messages")).get("cooldown_message");
                player.sendMessage(serializer.deserialize(message.replace("%cooldown%", String.valueOf(remaining))));
                return;
            }
        }
        
        if (invocation.arguments().length == 0) {
            String message = (String) ((Map<String, Object>) config.get("messages")).get("usage");
            player.sendMessage(serializer.deserialize(message));
            return;
        }

        String link = invocation.arguments()[0];
        
        if (!link.startsWith("https://")) {
            link = "https://" + link;
        }
        if (!link.startsWith("https://www.")) {
             link = link.replace("https://", "https://www.");
        }
        
        String platform = getPlatform(link);

        if (platform == null) {
            String message = (String) ((Map<String, Object>) config.get("messages")).get("invalid_link");
            player.sendMessage(serializer.deserialize(message));
            return;
        }

        Map<String, Object> platforms = (Map<String, Object>) config.get("platforms");
        if (platforms == null) {
            player.sendMessage(Component.text("Error en la configuración: La sección 'platforms' no existe."));
            return;
        }

        Map<String, Object> platformConfig = (Map<String, Object>) platforms.get(platform.toLowerCase());
        if (platformConfig == null) {
            player.sendMessage(Component.text("Error en la configuración: La plataforma '" + platform + "' no está configurada."));
            return;
        }

        List<String> messageLines = (List<String>) platformConfig.get("message");
        if (messageLines == null || messageLines.isEmpty()) {
            player.sendMessage(Component.text("Error en la configuración: El mensaje para la plataforma '" + platform + "' no está definido."));
            return;
        }

        for (String line : messageLines) {
            String formattedLine = line
                .replace("%player%", player.getUsername())
                .replace("%link%", link);
            server.getAllPlayers().forEach(p -> p.sendMessage(serializer.deserialize(centerText(formattedLine))));
        }
        
        cooldowns.put(playerUuid, currentTime);
    }

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission("ravenstream.use");
    }

    private String getPlatform(String link) {
        if (link.contains("twitch.tv")) return "Twitch";
        if (link.contains("youtube.com") || link.contains("youtu.be")) return "YouTube";
        if (link.contains("kick.com")) return "Kick";
        if (link.contains("tiktok.com")) return "TikTok";
        return null;
    }
    
    private static final int CHAT_WIDTH = 320;
    private static final int CHAT_CHARACTER_SPACING = 1;
    private static final int CHAT_PADDING = 2;
    private static final Map<Character, Integer> CHAR_WIDTH = new HashMap<>();
    
    static {
        CHAR_WIDTH.put(' ', 4);
        CHAR_WIDTH.put('!', 2);
        CHAR_WIDTH.put('"', 5);
        CHAR_WIDTH.put('#', 6);
        CHAR_WIDTH.put('$', 6);
        CHAR_WIDTH.put('%', 6);
        CHAR_WIDTH.put('&', 6);
        CHAR_WIDTH.put('\'', 3);
        CHAR_WIDTH.put('(', 5);
        CHAR_WIDTH.put(')', 5);
        CHAR_WIDTH.put('*', 5);
        CHAR_WIDTH.put('+', 6);
        CHAR_WIDTH.put(',', 2);
        CHAR_WIDTH.put('-', 6);
        CHAR_WIDTH.put('.', 2);
        CHAR_WIDTH.put('/', 6);
        CHAR_WIDTH.put('0', 6);
        CHAR_WIDTH.put('1', 6);
        CHAR_WIDTH.put('2', 6);
        CHAR_WIDTH.put('3', 6);
        CHAR_WIDTH.put('4', 6);
        CHAR_WIDTH.put('5', 6);
        CHAR_WIDTH.put('6', 6);
        CHAR_WIDTH.put('7', 6);
        CHAR_WIDTH.put('8', 6);
        CHAR_WIDTH.put('9', 6);
        CHAR_WIDTH.put(':', 2);
        CHAR_WIDTH.put(';', 2);
        CHAR_WIDTH.put('<', 5);
        CHAR_WIDTH.put('=', 6);
        CHAR_WIDTH.put('>', 5);
        CHAR_WIDTH.put('?', 6);
        CHAR_WIDTH.put('@', 7);
        CHAR_WIDTH.put('A', 6);
        CHAR_WIDTH.put('B', 6);
        CHAR_WIDTH.put('C', 6);
        CHAR_WIDTH.put('D', 6);
        CHAR_WIDTH.put('E', 6);
        CHAR_WIDTH.put('F', 6);
        CHAR_WIDTH.put('G', 6);
        CHAR_WIDTH.put('H', 6);
        CHAR_WIDTH.put('I', 4);
        CHAR_WIDTH.put('J', 6);
        CHAR_WIDTH.put('K', 6);
        CHAR_WIDTH.put('L', 6);
        CHAR_WIDTH.put('M', 6);
        CHAR_WIDTH.put('N', 6);
        CHAR_WIDTH.put('O', 6);
        CHAR_WIDTH.put('P', 6);
        CHAR_WIDTH.put('Q', 6);
        CHAR_WIDTH.put('R', 6);
        CHAR_WIDTH.put('S', 6);
        CHAR_WIDTH.put('T', 6);
        CHAR_WIDTH.put('U', 6);
        CHAR_WIDTH.put('V', 6);
        CHAR_WIDTH.put('W', 6);
        CHAR_WIDTH.put('X', 6);
        CHAR_WIDTH.put('Y', 6);
        CHAR_WIDTH.put('Z', 6);
        CHAR_WIDTH.put('[', 4);
        CHAR_WIDTH.put('\\', 6);
        CHAR_WIDTH.put(']', 4);
        CHAR_WIDTH.put('^', 6);
        CHAR_WIDTH.put('_', 6);
        CHAR_WIDTH.put('`', 3);
        CHAR_WIDTH.put('a', 6);
        CHAR_WIDTH.put('b', 6);
        CHAR_WIDTH.put('c', 6);
        CHAR_WIDTH.put('d', 6);
        CHAR_WIDTH.put('e', 6);
        CHAR_WIDTH.put('f', 5);
        CHAR_WIDTH.put('g', 6);
        CHAR_WIDTH.put('h', 6);
        CHAR_WIDTH.put('i', 2);
        CHAR_WIDTH.put('j', 6);
        CHAR_WIDTH.put('k', 5);
        CHAR_WIDTH.put('l', 3);
        CHAR_WIDTH.put('m', 6);
        CHAR_WIDTH.put('n', 6);
        CHAR_WIDTH.put('o', 6);
        CHAR_WIDTH.put('p', 6);
        CHAR_WIDTH.put('q', 6);
        CHAR_WIDTH.put('r', 6);
        CHAR_WIDTH.put('s', 6);
        CHAR_WIDTH.put('t', 4);
        CHAR_WIDTH.put('u', 6);
        CHAR_WIDTH.put('v', 6);
        CHAR_WIDTH.put('w', 6);
        CHAR_WIDTH.put('x', 6);
        CHAR_WIDTH.put('y', 6);
        CHAR_WIDTH.put('z', 6);
        CHAR_WIDTH.put('{', 5);
        CHAR_WIDTH.put('|', 2);
        CHAR_WIDTH.put('}', 5);
        CHAR_WIDTH.put('~', 7);
    }
    
    private String centerText(String text) {
        String cleanText = text.replaceAll("(?i)&[0-9a-fklmnor]|&#[0-9a-f]{6}", "");
        
        int textWidth = 0;
        boolean isBold = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (c == '&' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (code == 'l' || code == 'L') {
                    isBold = true;
                    i++;
                } else if ("0123456789abcdefklmnor".indexOf(code) != -1) {
                    isBold = false;
                    i++;
                } else if (code == '#') {
                    if (i + 7 < text.length()) {
                        isBold = false;
                        i += 7;
                    }
                }
            } else {
                int charWidth = CHAR_WIDTH.getOrDefault(c, 6);
                if (isBold) {
                    textWidth += charWidth + CHAT_CHARACTER_SPACING;
                } else {
                    textWidth += charWidth;
                }
            }
        }

        int spacesToCenter = (CHAT_WIDTH - textWidth) / (CHAR_WIDTH.getOrDefault(' ', 4) + CHAT_PADDING);
        
        if (spacesToCenter < 0) {
            return text;
        }
        
        StringBuilder centeredText = new StringBuilder();
        for (int i = 0; i < spacesToCenter; i++) {
            centeredText.append(" ");
        }
        centeredText.append(text);
        
        return centeredText.toString();
    }
}
