package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final Properties config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
    private long lastUsed = 0;

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
        
        // Verificar permisos
        if (!player.hasPermission("ravenstream.use")) {
            player.sendMessage(serializer.deserialize(config.getProperty("messages.no_permission", "&cNo tienes permiso para usar este comando.")));
            return;
        }

        // Verificar el cooldown
        long currentTime = System.currentTimeMillis();
        long cooldown = Long.parseLong(config.getProperty("cooldown", "300")) * 1000;
        if (currentTime - lastUsed < cooldown) {
            long remaining = (cooldown - (currentTime - lastUsed)) / 1000;
            player.sendMessage(serializer.deserialize(config.getProperty("messages.cooldown_message", "&cDebes esperar %cooldown% segundos para volver a usar el comando.").replace("%cooldown%", String.valueOf(remaining))));
            return;
        }

        // Obtener el argumento del link
        if (invocation.arguments().length == 0) {
            player.sendMessage(serializer.deserialize(config.getProperty("messages.usage", "&cUso incorrecto del comando: /directo <link>")));
            return;
        }

        String link = invocation.arguments()[0];
        String platform = getPlatform(link);

        if (platform == null) {
            player.sendMessage(Component.text("El enlace no es de una plataforma de streaming válida (YouTube, Twitch, Kick, TikTok)."));
            return;
        }

        // Reemplazar placeholders y enviar el mensaje
        String message = String.join("\n", Arrays.asList(config.getProperty("messages.stream_message", "").split("\n")))
                .replace("%player%", player.getUsername())
                .replace("%platform%", platform)
                .replace("%link%", link);
                
        plugin.getServer().getAllPlayers().forEach(p -> p.sendMessage(serializer.deserialize(message)));

        // Actualizar el cooldown
        lastUsed = currentTime;
    }

    private String getPlatform(String link) {
        if (link.contains("twitch.tv")) return "Twitch";
        if (link.contains("youtube.com") || link.contains("youtu.be")) return "YouTube";
        if (link.contains("kick.com")) return "Kick";
        if (link.contains("tiktok.com")) return "TikTok";
        return null;
    }
}
