package ru.minecourt;

import java.util.Objects;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineCourtPlugin extends JavaPlugin {
    private CourtService courtService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        courtService = new CourtService(this);

        PluginCommand command = Objects.requireNonNull(getCommand("court"), "Команда court не зарегистрирована");
        CourtCommand courtCommand = new CourtCommand(courtService);
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
