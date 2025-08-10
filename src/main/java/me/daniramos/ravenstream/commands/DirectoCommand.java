package me.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.daniramos.ravenstream.RavenStream;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public DirectoCommand(RavenStream plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Solo jugadores pueden usar este comando."));
            return;
        }
        Player player = (Player) invocation.source();
        String permission = (String) plugin.getConfig().get("permission");
        if (!player.hasPermission(permission)) {
            sendMessageList(player, "messages.no-permission", Collections.emptyMap());
            return;
        }
        String[] args = invocation.arguments();
        if (args.length != 1) {
            player.sendMessage(Component.text("Uso: /directo <link>"));
            return;
        }
        long now = System.currentTimeMillis();
        long cdSeconds = ((Number) plugin.getConfig().get("cooldown-seconds")).longValue();
        if (cooldowns.containsKey(player.getUniqueId()) && now - cooldowns.get(player.getUniqueId()) < cdSeconds * 1000) {
            long timeLeft = cdSeconds - ((now - cooldowns.get(player.getUniqueId())) / 1000);
            sendMessageList(player, "messages.cooldown", Map.of("%time%", String.valueOf(timeLeft)));
            return;
        }
        cooldowns.put(player.getUniqueId(), now);

        String link = args[0];
        String platform = getPlatform(link);
        if (platform == null) {
            player.sendMessage(Component.text("Plataforma no reconocida."));
            return;
        }
        Map<String, String> placeholders = Map.of(
                "%player%", player.getUsername(),
                "%link%", link
        );
        sendMessageListToAll("messages.platforms." + platform, placeholders);
    }

    private String getPlatform(String link) {
        if (link.contains("youtube.com") || link.contains("youtu.be")) return "youtube";
        if (link.contains("twitch.tv")) return "twitch";
        if (link.contains("tiktok.com")) return "tiktok";
        if (link.contains("kick.com")) return "kick";
        return null;
    }

    private void sendMessageList(Player player, String path, Map<String, String> placeholders) {
        List<String> lines = (List<String>) getPath(path);
        if (lines != null) {
            for (String line : lines) {
                for (Map.Entry<String, String> ph : placeholders.entrySet()) {
                    line = line.replace(ph.getKey(), ph.getValue());
                }
                player.sendMessage(Component.text(colorize(line)));
            }
        }
    }

    private void sendMessageListToAll(String path, Map<String, String> placeholders) {
        List<String> lines = (List<String>) getPath(path);
        if (lines != null) {
            for (String line : lines) {
                for (Map.Entry<String, String> ph : placeholders.entrySet()) {
                    line = line.replace(ph.getKey(), ph.getValue());
                }
                plugin.getServer().sendMessage(Component.text(colorize(line)));
            }
        }
    }

    private Object getPath(String path) {
        String[] keys = path.split("\\.");
        Object value = plugin.getConfig();
        for (String key : keys) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(key);
            }
        }
        return value;
    }

    private String colorize(String input) {
        return input.replace("&", "§");
    }
}
