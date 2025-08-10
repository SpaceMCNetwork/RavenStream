package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        
        // Verificar permisos
        if (!player.hasPermission("ravenstream.use")) {
            player.sendMessage(serializer.deserialize(config.getProperty("messages.no_permission")));
            return;
        }

        // Verificar el cooldown
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
        
        // Obtener el argumento del link
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

        // Construir el mensaje de la plataforma
        String platformKey = "platforms." + platform.toLowerCase() + ".message";
        String message = config.getProperty(platformKey);
        
        if (message == null || message.isEmpty()) {
            player.sendMessage(Component.text("El mensaje para esta plataforma no está configurado correctamente."));
            return;
        }

        // Reemplazar placeholders y enviar el mensaje
        String[] messageLines = message.split("\n");
        for (String line : messageLines) {
            String formattedLine = line
                .replace("%player%", player.getUsername())
                .replace("%link%", link);
            plugin.getServer().getAllPlayers().forEach(p -> p.sendMessage(serializer.deserialize(formattedLine)));
        }
        
        // Actualizar el cooldown del jugador
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
}
