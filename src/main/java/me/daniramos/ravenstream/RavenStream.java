package com.daniramos.ravenstream;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandManager;
import com.daniramos.ravenstream.commands.DirectoCommand;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

@Plugin(
    id = "ravenstream",
    name = "RavenStream",
    version = "1.0",
    description = "Publicita tu stream en el servidor.",
    authors = {"DaniRamos"}
)
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
        
        CommandManager commandManager = server.getCommandManager();
        // Se crea una nueva instancia del comando y se le pasan las dependencias.
        commandManager.register(commandManager.metaBuilder("directo").build(), new DirectoCommand(this.server, this.config));
        
        logger.info("El plugin RavenStream ha sido inicializado.");
    }
    
    private void loadConfig() {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("Error al crear la carpeta de datos del plugin: " + e.getMessage());
                return;
            }
        }
        
        File configFile = new File(dataDirectory.toFile(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                logger.error("No se pudo crear el archivo de configuración: " + e.getMessage());
            }
        }

        try {
            Yaml yaml = new Yaml();
            config = yaml.load(Files.newBufferedReader(configFile.toPath()));
        } catch (IOException e) {
            logger.error("No se pudo cargar el archivo de configuración: " + e.getMessage());
        }
    }
}
