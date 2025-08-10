package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DReloadCommand implements SimpleCommand {

    private final RavenStream plugin;

    public DReloadCommand(RavenStream plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("ravenstream.reload")) {
            invocation.source().sendMessage(Component.text("No tienes permiso para usar este comando.", NamedTextColor.RED));
            return;
        }

        plugin.reloadConfig();
        invocation.source().sendMessage(Component.text("Configuración de RavenStream recargada.", NamedTextColor.GREEN));
    }
}
