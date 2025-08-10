package me.daniramos.ravenstream;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import me.daniramos.ravenstream.commands.DirectoCommand;
import me.daniramos.ravenstream.commands.ReloadCommand;

import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

@Plugin(id = "ravenstream", name = "RavenStream", version = "1.0-SNAPSHOT",
        description = "Publica tus directos de forma simple en todo el servidor.",
        authors = {"DaniRamos"})
public class RavenStream {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Map<String, Object> config;

    @Inject
    public RavenStream(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        server.getCommandManager().register("directo", new DirectoCommand(server, config));
        server.getCommandManager().register("dreload", new ReloadCommand(this, logger));
        logger.info("El plugin RavenStream se ha activado correctamente.");
    }
    
    public void reloadConfig() {
        loadConfig();
    }

    private void loadConfig() {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("No se pudo crear el directorio de datos", e);
                return;
            }
        }

        File configFile = dataDirectory.resolve("config.yml").toFile();
        if (!configFile.exists()) {
            try {
                Files.copy(getClass().getResourceAsStream("/config.yml"), configFile.toPath());
            } catch (IOException e) {
                logger.error("No se pudo crear el archivo de configuración por defecto", e);
                return;
            }
        }

        try {
            Yaml yaml = new Yaml();
            config = yaml.load(Files.newInputStream(configFile.toPath()));
        } catch (IOException e) {
            logger.error("Error al leer el archivo de configuración", e);
        }
    }
}
