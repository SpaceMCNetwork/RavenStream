package me.danielortega.ravenstream;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Plugin(
        id = "ravenstream",
        name = "RavenStream",
        version = "1.0.0",
        authors = {"Daniel Ortega"}
)
public class RavenStream {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Map<String, Object> config;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Inject
    public RavenStream(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @com.velocitypowered.api.plugin.annotation.Subscribe
    public void onProxyInitialization(com.velocitypowered.api.event.proxy.ProxyInitializeEvent event) {
        loadConfig();

        server.getCommandManager().register("directo", new DirectoCommand());
        logger.info("RavenStream cargado correctamente.");
    }

    private void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            Path configPath = dataDirectory.resolve("config.yml");
            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    Files.copy(in, configPath);
                }
            }
            Yaml yaml = new Yaml();
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                config = yaml.load(inputStream);
            }
        } catch (IOException e) {
            logger.error("No se pudo cargar el config.yml", e);
        }
    }

    private class DirectoCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            if (!(source instanceof com.velocitypowered.api.proxy.Player player)) {
                source.sendMessage(MiniMessage.miniMessage().deserialize("<red>Este comando solo puede ejecutarse como jugador."));
                return;
            }

            if (!player.hasPermission("ravenstream.use")) {
                String msg = (String) config.getOrDefault("no-permission", "&cNo tienes permiso.");
                player.sendMessage(MiniMessage.miniMessage().deserialize(translateColors(msg)));
                return;
            }

            String[] args = invocation.arguments();
            if (args.length != 1) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Uso: /directo <link>"));
                return;
            }

            String link = args[0];
            String platform = detectPlatform(link);

            if (platform == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Plataforma no soportada."));
                return;
            }

            long now = System.currentTimeMillis();
            long cooldownSeconds = ((Number) config.getOrDefault("cooldown", 60)).longValue();
            if (cooldowns.containsKey(player.getUniqueId()) && (now - cooldowns.get(player.getUniqueId())) < cooldownSeconds * 1000) {
                long timeLeft = cooldownSeconds - ((now - cooldowns.get(player.getUniqueId())) / 1000);
                String cdMsg = (String) config.getOrDefault("cooldown-message", "&eDebes esperar {time} segundos.");
                player.sendMessage(MiniMessage.miniMessage().deserialize(translateColors(cdMsg.replace("{time}", String.valueOf(timeLeft)))));
                return;
            }

            cooldowns.put(player.getUniqueId(), now);

            String format = getPlatformFormat(platform);
            if (format == null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>No hay formato configurado para esta plataforma."));
                return;
            }

            String finalMsg = format.replace("{player}", player.getUsername()).replace("{link}", link);
            for (String line : finalMsg.split("\n")) {
                server.sendMessage(MiniMessage.miniMessage().deserialize(translateColors(line)));
            }
        }

        private String detectPlatform(String link) {
            if (link.contains("youtube.com") || link.contains("youtu.be")) return "youtube";
            if (link.contains("twitch.tv")) return "twitch";
            if (link.contains("tiktok.com")) return "tiktok";
            if (link.contains("kick.com")) return "kick";
            return null;
        }

        private String getPlatformFormat(String platform) {
            Object formats = config.get("message-format");
            if (formats instanceof Map<?, ?> map) {
                Object f = map.get(platform);
                if (f != null) return f.toString();
            }
            return null;
        }

        private String translateColors(String text) {
            return text.replace("&", "§");
        }
    }
}
