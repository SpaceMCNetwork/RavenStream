package me.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.Map;

public class DReloadCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().character('&').build();

    public DReloadCommand(RavenStream plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        Map<String, Object> config = plugin.getConfig();
        String noPermissionMessage = (String) ((Map<String, Object>) config.get("messages")).get("no_reload_permission");
        String reloadSuccessMessage = (String) ((Map<String, Object>) config.get("messages")).get("reload_success");
        
        if (!invocation.source().hasPermission("ravenstream.reload")) {
            invocation.source().sendMessage(serializer.deserialize(noPermissionMessage));
            return;
        }

        plugin.reloadConfig();
        invocation.source().sendMessage(serializer.deserialize(reloadSuccessMessage));
    }
}
