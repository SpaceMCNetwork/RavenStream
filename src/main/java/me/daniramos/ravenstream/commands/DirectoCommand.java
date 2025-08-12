package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoCommand implements SimpleCommand {

    private final ProxyServer server;
    private final Map<String, Object> config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    public DirectoCommand(ProxyServer server, Map<String, Object> config) {
        this.server = server;
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

        if (!player.hasPermission("ravenstream.use")) {
            String message = (String) ((Map<String, Object>) config.get("messages")).get("no_permission");
            player.sendMessage(serializer.deserialize(message));
            return;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownDuration = ((Integer) config.get("cooldown")).longValue() * 1000;
        
        if (cooldowns.containsKey(playerUuid)) {
            long lastUsed = cooldowns.get(playerUuid);
            if (currentTime - lastUsed < cooldownDuration) {
                long remaining = (cooldownDuration - (currentTime - lastUsed)) / 1000;
                String message = (String) ((Map<String, Object>) config.get("messages")).get("cooldown_message");
                player.sendMessage(serializer.deserialize(message.replace("%cooldown%", String.valueOf(remaining))));
                return;
            }
        }
        
        if (invocation.arguments().length == 0
