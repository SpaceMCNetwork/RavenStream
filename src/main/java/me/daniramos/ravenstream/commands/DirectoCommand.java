package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import me.daniramos.ravenstream.legacy.LegacyProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoCommand implements SimpleCommand {
    private final ProxyServer server;
    private final Map<String, Object> config;
    public static final MiniMessage miniMessage = MiniMessage.builder().postProcessor(new LegacyProcessor()).build();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    public DirectoCommand(ProxyServer server, Map<String, Object> config) {
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("Solo los jugadores pueden usar este comando."));
            return;
        }

        if (!player.hasPermission("ravenstream.use")) {
            sendMessage(player, "no_permission");
            return;
        }

        String playerUuid = player.getUniqueId().toString();
        long currentTime = System.currentTimeMillis();
        long cooldownDuration = ((Integer) config.get("cooldown")).longValue() * 1000;

        if (isOnCooldown(playerUuid, currentTime, cooldownDuration)) {
            long remaining = (cooldownDuration - (currentTime - cooldowns.get(playerUuid))) / 1000;
            String message = getMessage("cooldown_message").replace("%cooldown%", String.valueOf(remaining));
            player.sendMessage(miniMessage.deserialize(message));
            return;
        }

        if (invocation.arguments().length == 0) {
            sendMessage(player, "usage");
            return;
        }

        String link = normalizeLink(invocation.arguments()[0]);
        String platform = getPlatform(link);

        if (platform == null) {
            sendMessage(player, "invalid_link");
            return;
        }

        broadcastStream(player, link, platform);
        cooldowns.put(playerUuid, currentTime);
    }

    private boolean isOnCooldown(String playerUuid, long currentTime, long cooldownDuration) {
        return cooldowns.containsKey(playerUuid) && 
               (currentTime - cooldowns.get(playerUuid)) < cooldownDuration;
    }

    private String normalizeLink(String link) {
        if (!link.startsWith("https://")) {
            link = "https://" + link;
        }
        if (!link.startsWith("https://www.") && !link.contains("youtu.be")) {
            link = link.replace("https://", "https://www.");
        }
        return link;
    }

    private void broadcastStream(Player player, String link, String platform) {
        Map<String, Object> platformConfig = getPlatformConfig(platform);
        if (platformConfig == null) return;

        List<String> messageLines = (List<String>) platformConfig.get("message");
        if (messageLines == null || messageLines.isEmpty()) {
            player.sendMessage(Component.text("Error: Mensaje no configurado para " + platform));
            return;
        }

        messageLines.forEach(line -> {
            String formattedLine = line
                .replace("%player%", player.getUsername())
                .replace("%link%", link);
            server.getAllPlayers().forEach(p -> 
                p.sendMessage(miniMessage.deserialize(formattedLine)));
        });
    }

    private Map<String, Object> getPlatformConfig(String platform) {
        Map<String, Object> platforms = (Map<String, Object>) config.get("platforms");
        return platforms != null ? (Map<String, Object>) platforms.get(platform.toLowerCase()) : null;
    }

    private void sendMessage(Player player, String key) {
        player.sendMessage(miniMessage.deserialize(getMessage(key)));
    }

    private String getMessage(String key) {
        Map<String, Object> messages = (Map<String, Object>) config.get("messages");
        return (String) messages.get(key);
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
}