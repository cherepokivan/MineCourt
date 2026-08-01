package ru.minecourt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class CourtCommand implements CommandExecutor, TabCompleter {
    private final CourtService courtService;
    private final MessageService messages;

    public CourtCommand(CourtService courtService, MessageService messages) {
        this.courtService = courtService;
        this.messages = messages;
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
            sender.sendMessage(messages.get("player-only"));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.get("usage-create"));
            return true;
        }
        OfflinePlayer defendant = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (defendant == null) {
            defendant = Bukkit.getPlayerExact(args[1]);
        }
        if (defendant == null || !defendant.hasPlayedBefore() && !defendant.isOnline()) {
            sender.sendMessage(messages.get("player-not-found", Map.of("player", args[1])));
            return true;
        }
        if (defendant.getUniqueId().equals(plaintiff.getUniqueId())) {
            sender.sendMessage(messages.get("self-case"));
            return true;
        }
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (reason.isEmpty()) {
            sender.sendMessage(messages.get("reason-required"));
            return true;
        }
        courtService.createCase(plaintiff, defendant, reason);
        sender.sendMessage(messages.get("case-created"));
        return true;
    }

    private boolean view(CommandSender sender) {
        List<CourtCase> cases = courtService.getCases();
        if (cases.isEmpty()) {
            sender.sendMessage(messages.get("view-empty"));
            return true;
        }
        sender.sendMessage(messages.get("view-header", Map.of("count", String.valueOf(cases.size()))));
        for (int index = 0; index < cases.size(); index++) {
            CourtCase courtCase = cases.get(index);
            sender.sendMessage(messages.get("view-entry", Map.of(
                    "number", String.valueOf(index + 1),
                    "plaintiff", courtCase.plaintiffName(),
                    "defendant", courtCase.defendantName(),
                    "reason", courtCase.reason(),
                    "date", messages.formatDate(courtCase.createdAt())
            )));
        }
        return true;
    }

    private boolean setJudge(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecourt.setjudge")) {
            sender.sendMessage(messages.get("no-permission-judge"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(messages.get("usage-setjudge"));
            return true;
        }
        OfflinePlayer judge = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (judge == null) {
            judge = Bukkit.getPlayerExact(args[1]);
        }
        if (judge == null || !judge.hasPlayedBefore() && !judge.isOnline()) {
            sender.sendMessage(messages.get("player-not-found", Map.of("player", args[1])));
            return true;
        }
        courtService.setJudge(judge);
        sender.sendMessage(messages.get("judge-set", Map.of("judge", courtService.getJudgeName())));
        return true;
    }

    private boolean close(CommandSender sender, String[] args) {
        if (!sender.hasPermission("minecourt.setjudge")) {
            sender.sendMessage(messages.get("no-permission-close"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(messages.get("usage-close"));
            return true;
        }
        int caseNumber;
        try {
            caseNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(messages.get("case-number-invalid"));
            return true;
        }
        CourtCase closedCase = courtService.closeCase(caseNumber);
        if (closedCase == null) {
            sender.sendMessage(messages.get("case-not-found", Map.of("number", String.valueOf(caseNumber))));
            return true;
        }
        String message = messages.get("case-closed", Map.of(
                "number", String.valueOf(caseNumber),
                "plaintiff", closedCase.plaintiffName(),
                "defendant", closedCase.defendantName()
        ));
        Bukkit.broadcastMessage(message);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(messages.get("usage-create"));
        sender.sendMessage(messages.get("usage-view"));
        sender.sendMessage(messages.get("usage-close"));
        sender.sendMessage(messages.get("usage-setjudge"));
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
