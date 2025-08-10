package com.daniramos.ravenstream.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.daniramos.ravenstream.RavenStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoCommand implements SimpleCommand {

    private final RavenStream plugin;
    private final Map<String, Object> config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    // ... (el resto del código es el mismo, incluyendo el constructor y el método execute)
    // El método execute llama a este centerText, asi que solo con este cambio basta.

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
        
        if (invocation.arguments().length == 0) {
            String message = (String) ((Map<String, Object>) config.get("messages")).get("usage");
            player.sendMessage(serializer.deserialize(message));
            return;
        }

        String link = invocation.arguments()[0];
        String platform = getPlatform(link);

        if (platform == null) {
            String message = (String) ((Map<String, Object>) config.get("messages")).get("invalid_link");
            player.sendMessage(serializer.deserialize(message));
            return;
        }

        Map<String, Object> platforms = (Map<String, Object>) config.get("platforms");
        if (platforms == null) {
            player.sendMessage(Component.text("Error en la configuración: La sección 'platforms' no existe."));
            return;
        }

        Map<String, Object> platformConfig = (Map<String, Object>) platforms.get(platform.toLowerCase());
        if (platformConfig == null) {
            player.sendMessage(Component.text("Error en la configuración: La plataforma '" + platform + "' no está configurada."));
            return;
        }

        List<String> messageLines = (List<String>) platformConfig.get("message");
        if (messageLines == null || messageLines.isEmpty()) {
            player.sendMessage(Component.text("Error en la configuración: El mensaje para la plataforma '" + platform + "' no está definido."));
            return;
        }

        for (String line : messageLines) {
            String formattedLine = line
                .replace("%player%", player.getUsername())
                .replace("%link%", link);
            plugin.getServer().getAllPlayers().forEach(p -> p.sendMessage(serializer.deserialize(centerText(formattedLine))));
        }
        
        cooldowns.put(playerUuid, currentTime);
    }
    
    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission("ravenstream.use");
    }

    private String getPlatform(String link) {
        if (link.contains("twitch.tv")) return "Twitch";
        if (link.contains("youtube.com") || link.contains("youtu.be")) return "YouTube";
        if (link.contains("kick.com")) return "Kick";
        if (link.contains("tiktok.com")) return "TikTok";
        return null;
    }
    
    /**
     * Calcula los espacios necesarios para centrar un texto en el chat.
     * Ignora los códigos de color y formato.
     * @param text El texto a centrar.
     * @return El texto con los espacios para ser centrado.
     */
    private String centerText(String text) {
        int chatWidth = 320;
        int textWidth = 0;
        boolean isBold = false;
        
        // Expresión regular para encontrar y eliminar todos los códigos de color
        // y formato (incluyendo HEX) de la cadena de texto.
        String cleanText = text.replaceAll("(?i)&[0-9a-fklmnor]|&#[0-9a-f]{6}", "");

        // Anchos de los caracteres, aproximados para el centrado.
        for (char c : cleanText.toCharArray()) {
            if (c == ' ') {
                textWidth += isBold ? 4 : 3;
            } else if ("i,.:;|!".indexOf(c) != -1) {
                textWidth += isBold ? 2 : 1;
            } else if ("l".indexOf(c) != -1) {
                textWidth += isBold ? 4 : 3;
            } else if ("k".indexOf(c) != -1) {
                textWidth += isBold ? 6 : 5;
            } else {
                textWidth += isBold ? 5 : 4;
            }
        }
        
        // Si el texto es mas ancho que el chat, no lo centramos
        if (textWidth >= chatWidth) {
            return text;
        }

        int spaceWidth = 4;
        int spaces = (int) Math.floor((double) (chatWidth - textWidth) / spaceWidth / 2);
        
        StringBuilder centeredText = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            centeredText.append(" ");
        }
        centeredText.append(text);
        
        return centeredText.toString();
    }
}
