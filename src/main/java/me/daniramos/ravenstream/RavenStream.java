package me.daniramos.ravenstream;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(
        id = "ravenstream",
        name = "RavenStream",
        version = "1.0.0",
        authors = {"Daniel Ramos"}
)
public class RavenStream {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private Map<String, Object> config;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @Inject
    public RavenStream(ProxyServer server, Logger logger, Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        loadConfig();
        server.getCommandManager().register("directo", new DirectoCommand());
        logger.info("RavenStream ha sido cargado.");
    }

    private void loadConfig() {
        try {
            File configFile = dataDirectory.resolve("config.yml").toFile();
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(getDefaultConfig());
                }
            }
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                config = yaml.load(fis);
            }
        } catch (Exception e) {
            logger.error("Error cargando config.yml", e);
        }
    }

    private String getDefaultConfig() {
        return "permission: \"ravenstream.use\"\n" +
                "cooldown-seconds: 60\n" +
                "cooldown-message:\n" +
                "  - \"&cDebes esperar {time} segundos antes de usar este comando de nuevo.\"\n" +
                "no-permission-message:\n" +
                "  - \"&cNo tienes permiso para usar este comando.\"\n" +
                "platforms:\n" +
                "  twitch:\n" +
                "    - \"&5[Directo] &f{player} está en vivo en &dTwitch&f:\"\n" +
                "    - \"&e{link}\"\n" +
                "  youtube:\n" +
                "    - \"&c[Directo] &f{player} está en vivo en &cYouTube&f:\"\n" +
                "    - \"&e{link}\"\n" +
                "  kick:\n" +
                "    - \"&a[Directo] &f{player} está en vivo en &aKick&f:\"\n" +
                "    - \"&e{link}\"\n" +
                "  tiktok:\n" +
                "    - \"&d[Directo] &f{player} está en vivo en &dTikTok&f:\"\n" +
                "    - \"&e{link}\"\n";
    }

    private class DirectoCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();

            if (!(source instanceof Player player)) {
                source.sendMessage(Component.text("Solo los jugadores pueden usar este comando."));
                return;
            }

            String permission = (String) config.getOrDefault("permission", "ravenstream.use");
            if (!player.hasPermission(permission)) {
                sendConfigMessage(source, "no-permission-message", Map.of());
                return;
            }

            String[] args = invocation.arguments();
            if (args.length != 1) {
                source.sendMessage(Component.text("Uso: /directo <link>"));
                return;
            }

            String link = args[0].toLowerCase();
            String platform = getPlatformFromLink(link);
            if (platform == null) {
                source.sendMessage(Component.text("Plataforma no reconocida."));
                return;
            }

            long now = System.currentTimeMillis();
            long cooldownMs = ((Number) config.getOrDefault("cooldown-seconds", 60)).longValue() * 1000;
            if (cooldowns.containsKey(player.getUniqueId())) {
                long lastUse = cooldowns.get(player.getUniqueId());
                if (now - lastUse < cooldownMs) {
                    long remaining = (cooldownMs - (now - lastUse)) / 1000;
                    sendConfigMessage(source, "cooldown-message", Map.of("time", String.valueOf(remaining)));
                    return;
                }
            }
            cooldowns.put(player.getUniqueId(), now);

            Map<String, Object> platforms = (Map<String, Object>) config.get("platforms");
            List<String> lines = (List<String>) platforms.get(platform);
            if (lines != null) {
                for (String line : lines) {
                    String formatted = line.replace("{player}", player.getUsername()).replace("{link}", link);
                    Component message = MiniMessage.miniMessage().deserialize(formatted.replace("&", "§"));
                    server.getAllPlayers().forEach(p -> p.sendMessage(message));
                }
            }
        }

        private String getPlatformFromLink(String link) {
            if (link.contains("twitch.tv")) return "twitch";
            if (link.contains("youtube.com") || link.contains("youtu.be")) return "youtube";
            if (link.contains("kick.com")) return "kick";
            if (link.contains("tiktok.com")) return "tiktok";
            return null;
        }

        private void sendConfigMessage(CommandSource source, String path, Map<String, String> placeholders) {
            List<String> messages = (List<String>) config.get(path);
            if (messages != null) {
                for (String msg : messages) {
                    String formatted = msg;
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        formatted = formatted.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                    source.sendMessage(MiniMessage.miniMessage().deserialize(formatted.replace("&", "§")));
                }
            }
        }
    }
}
