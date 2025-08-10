package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final Properties config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    public DirectoCommand(RavenStream plugin, Properties config) {
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
            player.sendMessage(serializer.deserialize(config.getProperty("messages.no_permission")));
            return;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownDuration = Long.parseLong(config.getProperty("cooldown", "300")) * 1000;
        
        if (cooldowns.containsKey(playerUuid)) {
            long lastUsed = cooldowns.get(playerUuid);
            if (currentTime - lastUsed < cooldownDuration) {
                long remaining = (cooldownDuration - (currentTime - lastUsed)) / 1000;
                player.sendMessage(serializer.deserialize(config.getProperty("messages.cooldown_message").replace("%cooldown%", String.valueOf(remaining))));
                return;
            }
        }
        
        if (invocation.arguments().length == 0) {
            player.sendMessage(serializer.deserialize(config.getProperty("messages.usage")));
            return;
        }

        String link = invocation.arguments()[0];
        String platform = getPlatform(link);

        if (platform == null) {
            player.sendMessage(serializer.deserialize(config.getProperty("messages.invalid_link")));
            return;
        }

        String platformKey = "platforms." + platform.toLowerCase() + ".message";
        String messageLines = config.getProperty(platformKey);

        if (messageLines == null || messageLines.isEmpty()) {
            player.sendMessage(Component.text("El mensaje para esta plataforma no está configurado correctamente."));
            return;
        }

        for (String line : messageLines.split("\n")) {
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
        
        // Simplemente simplificamos el cálculo del ancho de caracteres para que sea mas facil de entender.
        int spaceWidth = 4; // Ancho del espacio por defecto
        
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
