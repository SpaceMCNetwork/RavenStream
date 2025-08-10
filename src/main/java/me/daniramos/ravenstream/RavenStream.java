package me.daniramos.ravenstream;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.google.inject.Inject;
import me.daniramos.ravenstream.commands.DirectoCommand;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Plugin(
        id = "ravenstream",
        name = "RavenStream",
        version = "1.0.0",
        authors = {"DaniRamos"}
)
public class RavenStream {

    private final ProxyServer server;
    private final Logger logger;
    private Map<String, Object> config;
    private final Path dataFolder;

    @Inject
    public RavenStream(ProxyServer server, Logger logger, Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        server.getCommandManager().register("directo", new DirectoCommand(this));
        logger.info("RavenStream cargado correctamente.");
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }
            Path configPath = dataFolder.resolve("config.yml");
            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    Files.copy(in, configPath);
                }
            }
            try (InputStream input = Files.newInputStream(configPath)) {
                Yaml yaml = new Yaml();
                config = yaml.load(input);
            }
        } catch (IOException e) {
            logger.error("Error cargando config.yml", e);
        }
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}
