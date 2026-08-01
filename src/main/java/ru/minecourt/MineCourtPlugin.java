package ru.minecourt;

import java.util.Objects;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineCourtPlugin extends JavaPlugin {
    private CourtService courtService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getConfig().isSet("Languale")) {
            getConfig().set("Languale", "RU");
            saveConfig();
        }
        MessageService messages = new MessageService(this);
        courtService = new CourtService(this, messages);

        PluginCommand command = Objects.requireNonNull(getCommand("court"), "The court command is not registered");
        CourtCommand courtCommand = new CourtCommand(courtService, messages);
        command.setExecutor(courtCommand);
        command.setTabCompleter(courtCommand);
    }

    @Override
    public void onDisable() {
        if (courtService != null) {
            courtService.save();
        }
    }
}
