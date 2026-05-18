package com.github.kodalee.easyspeak.command;

import com.github.kodalee.easyspeak.Easyspeak;
import com.github.kodalee.easyspeak.database.entity.ChatMessageRecord;
import com.github.kodalee.easyspeak.database.repository.ChatRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatHistoryCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final int PAGE_SIZE = 10;
    private static final int CONTEXT_RADIUS = 5;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {

        if (!sender.hasPermission("easyspeak.chathistory")) {
            sender.sendMessage(MM.deserialize(Easyspeak.config.getString("messages.common.no_permission")));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        if (args[0].equalsIgnoreCase("-c")) {
            handleContext(sender, args);
            return true;
        }

        handleHistory(sender, args);
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(MM.deserialize("<red>Usage: /" + label + " <player> [page]"));
        sender.sendMessage(MM.deserialize("<red>       /" + label + " -c <messageId>"));
    }

    private void handleHistory(CommandSender sender, String[] args) {
        String playerName = args[0];
        Integer requestedPage = null;
        if (args.length >= 2) {
            try {
                requestedPage = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {}
        }
        final Integer explicitPage = requestedPage;

        Bukkit.getScheduler().runTaskAsynchronously(Easyspeak.plugin, () -> {
            ChatRepository repo = Easyspeak.chatRepository;

            int total = repo.countByName(playerName);
            if (total == 0) {
                sender.sendMessage(MM.deserialize(
                        "<red>No chat history found for <white>" + playerName + "</white>."));
                return;
            }

            int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            int safePage = explicitPage == null ? totalPages : Math.min(explicitPage, totalPages);
            int offset = (safePage - 1) * PAGE_SIZE;

            List<ChatMessageRecord> records = repo.getHistoryByName(playerName, PAGE_SIZE, offset);

            sender.sendMessage(MM.deserialize(
                    "<gold>=== <white>" + playerName + "</white>'s chats " +
                            "<gray>(page " + safePage + "/" + totalPages + ", " + total + " total)</gray> ==="));

            for (ChatMessageRecord r : records) {
                sender.sendMessage(formatLine(r, false));
            }

            sender.sendMessage(buildNav(playerName, safePage, totalPages));
        });
    }

    private void handleContext(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MM.deserialize("<red>Usage: /chathistory -c <messageId>"));
            sender.sendMessage(MM.deserialize("<red>Using the -c tick allows you to view context of a message."));
            return;
        }

        long id;
        try {
            id = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize("<red>Invalid message ID."));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Easyspeak.plugin, () -> {
            ChatRepository repo = Easyspeak.chatRepository;

            ChatMessageRecord target = repo.getById(id);
            if (target == null) {
                sender.sendMessage(MM.deserialize("<red>Message <white>#" + id + "</white> not found."));
                return;
            }

            List<ChatMessageRecord> before = repo.getBeforeContext(target, CONTEXT_RADIUS);
            List<ChatMessageRecord> after = repo.getAfterContext(target, CONTEXT_RADIUS);

            String header = target.isPrivate()
                    ? "<gold>=== <white>#" + id + "</white> / PM: <white>" +
                        target.playerName() + "</white> <-> <white>" +
                        target.recipientName() + "</white> ==="
                    : "<gold>=== <white>#" + id + "</white> / <white>" +
                        target.playerName() + "</white> @ <white>" + target.server() + "</white> ===";
            sender.sendMessage(MM.deserialize(header));

            for (ChatMessageRecord r : before) sender.sendMessage(formatLine(r, false));
            sender.sendMessage(formatLine(target, true));
            for (ChatMessageRecord r : after) sender.sendMessage(formatLine(r, false));
        });
    }

    private Component formatLine(ChatMessageRecord r, boolean highlighted) {
        String time = TS.format(Instant.ofEpochMilli(r.timestamp()));

        NamedTextColor nameColor = highlighted ? NamedTextColor.YELLOW : NamedTextColor.WHITE;
        NamedTextColor msgColor = highlighted ? NamedTextColor.WHITE : NamedTextColor.GRAY;

        Component tag;
        Component name;
        if (r.isPrivate()) {
            tag = Component.text("[PM] ").color(NamedTextColor.LIGHT_PURPLE);
            name = Component.text(r.playerName() + " -> " + r.recipientName() + ": ").color(nameColor);
        } else {
            tag = Component.text("[" + r.server() + "] ").color(NamedTextColor.DARK_AQUA);
            name = Component.text(r.playerName() + ": ").color(nameColor);
        }

        Component line = Component.empty()
                .append(Component.text("[" + time + "] ").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(" "))
                .append(tag)
                .append(name)
                .append(Component.text(r.message()).color(msgColor)
                        .clickEvent(ClickEvent.runCommand("/chathistory -c " + r.id()))
                        .hoverEvent(HoverEvent.showText(Component.text(
                                "View Context (" + CONTEXT_RADIUS + " messages before & after)"))
                        )
                );

        if (highlighted) {
            line = Component.empty()
                    .append(Component.text(">> ")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD))
                    .append(line);
        }

        return line;
    }

    private Component buildNav(String playerName, int page, int totalPages) {
        Component nav = Component.empty();

        if (page > 1) {
            nav = nav.append(Component.text("[<- prev]")
                    .color(NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.runCommand(
                            "/chathistory " + playerName + " " + (page - 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Previous page"))));
        }

        if (page > 1 && page < totalPages) {
            nav = nav.append(Component.text("  ").color(NamedTextColor.DARK_GRAY));
        }

        if (page < totalPages) {
            nav = nav.append(Component.text("[next ->]")
                    .color(NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.runCommand(
                            "/chathistory " + playerName + " " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Next page"))));
        }

        return nav;
    }
}
