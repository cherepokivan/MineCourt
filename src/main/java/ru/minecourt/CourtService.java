package ru.minecourt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class CourtService {
    private final MineCourtPlugin plugin;
    private final List<CourtCase> cases = new ArrayList<>();
    private UUID judgeId;
    private String judgeName;

    public CourtService(MineCourtPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void createCase(Player plaintiff, OfflinePlayer defendant, String reason) {
        String defendantName = playerName(defendant);
        cases.add(new CourtCase(plaintiff.getUniqueId(), plaintiff.getName(), defendant.getUniqueId(), defendantName,
                reason, System.currentTimeMillis()));
        save();

        String publicMessage = "§6[MineCourt] §fИгрок §e" + plaintiff.getName()
                + " §fподал в суд на игрока §e" + defendantName + "§f.";
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getUniqueId().equals(judgeId)) {
                onlinePlayer.sendMessage(publicMessage);
            }
        }

        Player judge = getOnlineJudge();
        if (judge != null) {
            judge.sendMessage(publicMessage + " §fПричина: §e" + reason);
        }

        Player defendantPlayer = defendant.getPlayer();
        if (defendantPlayer != null) {
            defendantPlayer.sendMessage("§c[MineCourt] На вас подали в суд! §fЯвитесь в течение 3–5 минут!");
        }
    }

    public List<CourtCase> getCases() {
        return List.copyOf(cases);
    }

    public void setJudge(OfflinePlayer judge) {
        judgeId = judge.getUniqueId();
        judgeName = playerName(judge);
        save();
    }

    public String getJudgeName() {
        return judgeName;
    }

    private Player getOnlineJudge() {
        return judgeId == null ? null : Bukkit.getPlayer(judgeId);
    }

    private void load() {
        FileConfiguration config = plugin.getConfig();
        String savedJudgeId = config.getString("judge.uuid");
        if (savedJudgeId != null) {
            try {
                judgeId = UUID.fromString(savedJudgeId);
                judgeName = config.getString("judge.name");
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("В config.yml указан некорректный UUID судьи.");
            }
        }

        ConfigurationSection casesSection = config.getConfigurationSection("cases");
        if (casesSection == null) {
            return;
        }
        for (String key : casesSection.getKeys(false)) {
            ConfigurationSection section = casesSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                cases.add(new CourtCase(
                        UUID.fromString(section.getString("plaintiff.uuid", "")),
                        section.getString("plaintiff.name", "Неизвестно"),
                        UUID.fromString(section.getString("defendant.uuid", "")),
                        section.getString("defendant.name", "Неизвестно"),
                        section.getString("reason", "Не указана"),
                        section.getLong("created-at")
                ));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Пропущено повреждённое судебное дело: " + key);
            }
        }
    }

    public void save() {
        FileConfiguration config = plugin.getConfig();
        config.set("judge", null);
        config.set("cases", null);
        if (judgeId != null) {
            config.set("judge.uuid", judgeId.toString());
            config.set("judge.name", judgeName);
        }
        for (int index = 0; index < cases.size(); index++) {
            CourtCase courtCase = cases.get(index);
            String path = "cases." + (index + 1) + ".";
            config.set(path + "plaintiff.uuid", courtCase.plaintiffId().toString());
            config.set(path + "plaintiff.name", courtCase.plaintiffName());
            config.set(path + "defendant.uuid", courtCase.defendantId().toString());
            config.set(path + "defendant.name", courtCase.defendantName());
            config.set(path + "reason", courtCase.reason());
            config.set(path + "created-at", courtCase.createdAt());
        }
        plugin.saveConfig();
    }

    private String playerName(OfflinePlayer player) {
        return player.getName() == null ? player.getUniqueId().toString() : player.getName();
    }
}
