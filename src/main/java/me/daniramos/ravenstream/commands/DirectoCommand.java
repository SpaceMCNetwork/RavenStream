package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final Map<String, Object> config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    public DirectoCommand(RavenStream plugin, Map<String, Object> config) {
        this.plugin = plugin;
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
            plugin.getServer().getAllPlayers().forEach(p -> p.sendMessage(serializer.deserialize(centerText(formattedLine))));
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
    
    private String centerText(String text) {
        int chatWidth = 320;
        int textWidth = 0;
        boolean isBold = false;
        
        int spaceWidth = 4;
        
        for (char c : text.toCharArray()) {
            if (c == '&') {
                isBold = text.charAt(text.indexOf(c) + 1) == 'l';
                continue;
            }
            if (c == ' ') {
                textWidth += isBold ? 4 : 3;
            } else if ("i,.:;|!".indexOf(c) != -1) {
                textWidth += isBold ? 2 : 1;
            } else if ("l".indexOf(c) != -1) {
                textWidth += isBold ? 4 : 3;
            } else if ("k".indexOf(c) != -1) {
                textWidth += isBold ? 6 : 5;
            } else {
                textWidth += isBold ? 5 : 4;
            }
        }
        
        if (textWidth >= chatWidth) {
            return text;
        }

        int spaces = (int) Math.floor((double) (chatWidth - textWidth) / spaceWidth / 2);
        
        StringBuilder centeredText = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            centeredText.append(" ");
        }
        centeredText.append(text);
        
        return centeredText.toString();
    }
}
