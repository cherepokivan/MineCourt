package ru.minecourt;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageService {
    private final FileConfiguration messages;
    private final DateTimeFormatter dateFormatter;

    public MessageService(MineCourtPlugin plugin) {
        String language = plugin.getConfig().getString("Languale", "RU").toUpperCase(Locale.ROOT);
        if (!language.equals("RU") && !language.equals("EN")) {
            plugin.getLogger().warning("Unknown Languale value '" + language + "'. RU will be used.");
            language = "RU";
        }
        String fileName = language + ".yml";
        plugin.saveResource(fileName, false);
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), fileName));
        dateFormatter = DateTimeFormatter.ofPattern(messages.getString("date-format", "dd.MM.yyyy HH:mm"))
                .withLocale(language.equals("RU") ? new Locale("ru") : Locale.ENGLISH)
                .withZone(ZoneId.systemDefault());
    }

    public String get(String key) {
        return color(messages.getString(key, "Missing message: " + key));
    }

    public String get(String key, Map<String, String> replacements) {
        String message = get(key);
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            message = message.replace("{" + replacement.getKey() + "}", replacement.getValue());
        }
        return message;
    }

    public String formatDate(long timestamp) {
        return dateFormatter.format(Instant.ofEpochMilli(timestamp));
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
