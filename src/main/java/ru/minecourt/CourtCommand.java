package ru.minecourt;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class CourtCommand implements CommandExecutor, TabCompleter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withLocale(new Locale("ru"))
            .withZone(ZoneId.systemDefault());
    private final CourtService courtService;

    public CourtCommand(CourtService courtService) {
        this.courtService = courtService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> create(sender, args);
            case "view" -> view(sender);
            case "close" -> close(sender, args);
            case "setjudge" -> setJudge(sender, args);
            default -> {
                sendUsage(sender);
                yield true;
            }
        };
    }

    private boolean create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player plaintiff)) {
            sender.sendMessage("§cПодать в суд можно только от имени игрока.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /court create <ник> <причина>");
            return true;
        }
        OfflinePlayer defendant = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (defendant == null) {
            defendant = Bukkit.getPlayerExact(args[1]);
        }
        if (defendant == null || !defendant.hasPlayedBefore() && !defendant.isOnline()) {
            sender.sendMessage("§cИгрок §e" + args[1] + " §cне найден. Он должен хотя бы раз зайти на сервер.");
            return true;
        }
        if (defendant.getUniqueId().equals(plaintiff.getUniqueId())) {
            sender.sendMessage("§cНельзя подать в суд на самого себя.");
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (reason.isEmpty()) {
            sender.sendMessage("§cУкажите причину подачи в суд.");
            return true;
        }
        courtService.createCase(plaintiff, defendant, reason);
        sender.sendMessage("§aЗаявление в суд успешно подано.");
        return true;
    }

    private boolean view(CommandSender sender) {
        List<CourtCase> cases = courtService.getCases();
        if (cases.isEmpty()) {
            sender.sendMessage("§eСудебных дел пока нет.");
            return true;
        }
        sender.sendMessage("§6§lMineCourt §7— список судебных дел (" + cases.size() + "):");
        for (int index = 0; index < cases.size(); index++) {
            CourtCase courtCase = cases.get(index);
            sender.sendMessage("§e#" + (index + 1) + " §f" + courtCase.plaintiffName() + " §7→ §f"
                    + courtCase.defendantName() + " §7| Причина: §f" + courtCase.reason()
                    + " §8(" + DATE_FORMAT.format(Instant.ofEpochMilli(courtCase.createdAt())) + ")");
        }
        return true;
    }

    private boolean setJudge(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecourt.setjudge")) {
            sender.sendMessage("§cУ вас нет прав для назначения судьи.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /court setjudge <ник>");
            return true;
        }
        OfflinePlayer judge = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (judge == null) {
            judge = Bukkit.getPlayerExact(args[1]);
        }
        if (judge == null || !judge.hasPlayedBefore() && !judge.isOnline()) {
            sender.sendMessage("§cИгрок §e" + args[1] + " §cне найден. Он должен хотя бы раз зайти на сервер.");
            return true;
        }
        courtService.setJudge(judge);
        sender.sendMessage("§aСудья назначен: §e" + courtService.getJudgeName());
        return true;
    }

    private boolean close(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecourt.setjudge")) {
            sender.sendMessage("§cУ вас нет прав для закрытия судебного дела.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /court close <номер>");
            return true;
        }
        int caseNumber;
        try {
            caseNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("§cНомер дела должен быть целым числом.");
            return true;
        }
        CourtCase closedCase = courtService.closeCase(caseNumber);
        if (closedCase == null) {
            sender.sendMessage("§cДело с номером §e" + caseNumber + " §cне найдено.");
            return true;
        }
        String message = "§6[MineCourt] §fСудебное дело §e#" + caseNumber + " §fзакрыто: §e"
                + closedCase.plaintiffName() + " §fпротив §e" + closedCase.defendantName() + "§f.";
        Bukkit.broadcastMessage(message);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6MineCourt: §f/court create <ник> <причина>");
        sender.sendMessage("§6MineCourt: §f/court view");
        sender.sendMessage("§6MineCourt: §f/court close <номер>");
        sender.sendMessage("§6MineCourt: §f/court setjudge <ник>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("create", "view", "close", "setjudge"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("setjudge"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String lowerCaseInput = input.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerCaseInput))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
