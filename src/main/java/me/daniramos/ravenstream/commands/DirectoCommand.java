package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.inject.Inject;

public class DirectoCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final Map<String, Object> config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    @Inject
    public DirectoCommand(RavenStream plugin, Map<String, Object> config) {
        this.plugin = plugin;
        this.config = config;
    }
    // ... (el resto del código es el mismo)
}
