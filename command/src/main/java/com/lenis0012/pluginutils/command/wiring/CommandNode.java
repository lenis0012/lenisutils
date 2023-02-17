package com.lenis0012.pluginutils.command.wiring;

import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.CommandException;
import com.lenis0012.pluginutils.command.api.HelpContext;
import com.lenis0012.pluginutils.command.api.HelpMessage;
import com.lenis0012.pluginutils.command.defaults.DefaultMessages;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CommandNode {
    private final CommandNode root;
    @Getter
    private final CommandPath path;
    private final String name;
    private final List<CommandNode> children = new ArrayList<>();
    private WiredCommand command;
    @Setter
    private String description = "";
    @Setter
    private String permission;

    public void addChild(CommandNode node) {
        children.add(node);
    }

    public void setWiredCommand(WiredCommand command) {
        if (this.command != null)
            throw new CommandWiringException(command + " conflicting with existing command " + this.command);
        this.command = command;
    }

    public boolean matches(String argument) {
        return this.name.equalsIgnoreCase(argument) || (this.name.startsWith("<") && this.name.endsWith(">"));
    }

    @Nullable
    public CommandNode getChild(String argument) {
        for (CommandNode child : children) {
            if (child.matches(argument)) return child;
        }

        return null;
    }

    @Nullable
    public CommandNode getChild(String argument, Permissible permissible) {
        for (CommandNode child : children) {
            if (child.matches(argument)) {
                if (child.permission != null && !permissible.hasPermission(child.permission)) {
                    throw new CommandException(DefaultMessages.NO_PERMISSION);
                }
                return child;
            }
        }

        return null;
    }

    public void execute(CommandContext context) {
        if(command == null) {
            throw new CommandException(HelpMessage.ofCurrent());
        }
        command.execute(context);
    }

    public List<String> complete(CommandContext context, String arg) {
        List<String> completions = new ArrayList<>();
        for (CommandNode child : children) {
            if(child.name.startsWith("<") && child.name.endsWith(">")) {
                if(child.command != null) {
                    completions.addAll(Objects.requireNonNull(child.command.complete(context), "Completions must return a list, not null"));
                }
                continue;
            }
            if (child.name.toLowerCase(Locale.ROOT).startsWith(arg.toLowerCase(Locale.ROOT))) {
                completions.add(child.name);
            }
        }
        return completions;
    }

    public CommandNodeHelp getHelpContext(Permissible permissible) {
        return new CommandNodeHelp(permissible);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class CommandNodeHelp implements HelpContext {
        private final Permissible permissible;

        @Override
        public HelpContext getRoot() {
            return root == null ? this : root.getHelpContext(permissible);
        }

        @Override
        public List<HelpEntry> getEntries() {
            if(children.isEmpty()) {
                if(command == null) {
                    throw new IllegalStateException("No commands found for path " + path);
                }
                return Collections.singletonList(new CommandNodeHelpEntry(path.toString(), "", description));
            }
            Stream<HelpEntry> selfStream = command == null ? Stream.empty() : Stream.of(new CommandNodeHelpEntry(path.toString(), "", description));
            Stream<HelpEntry> childStream = children.stream()
                .filter(child -> child.permission == null || permissible.hasPermission(child.permission))
                .map(child -> {
                    List<HelpEntry> entries = child.getHelpContext(permissible).getEntries();
                    if(entries.size() <= 1) {
                        return entries;
                    }
                    if(child.description == null) {
                        return Collections.<HelpEntry>emptyList();
                    }
                    return Collections.singletonList(new CommandNodeHelpEntry(path.toString(), child.name, child.description));
                })
                .flatMap(List::stream);

            return Stream.concat(selfStream, childStream)
                .peek(x -> {
                    Bukkit.getLogger().info("Path: " + path + " child: " + x.getBaseCommand());
                })
                .filter(entry -> entry.getBaseCommand().startsWith(path.toString()))
                .map(entry -> {
                    String trimmedBasePath = entry.getBaseCommand().substring(path.toString().length()).trim();
                    String newSubPath = trimmedBasePath.isEmpty() ? entry.getSubCommand() : trimmedBasePath + " " + entry.getSubCommand();
                    return new CommandNodeHelpEntry(path.toString(), newSubPath, entry.getDescription());
                })
                .collect(Collectors.toList());
        }

        @Override
        public BaseComponent[] serialize(ChatColor highlightColor, ChatColor accentColor) {
            ComponentBuilder builder = new ComponentBuilder("");
            List<HelpEntry> entries = getEntries();
            if(entries.size() > 1) {
                builder.append("--------")
                    .color(accentColor).strikethrough(true)
                    .append("< ").strikethrough(false)
                    .append(path.toString()).append(" commands").color(highlightColor).bold(true).reset()
                    .append(" >").color(accentColor)
                    .append("--------").strikethrough(true)
                    .append("\n").reset();
            }

            for(int i = 0; i < entries.size(); i++) {
                HelpEntry entry = entries.get(i);
                builder.append(entry.serialize(highlightColor));
                if(i < entries.size() - 1) {
                    builder.append("\n").reset();
                }
            }
            return builder.create();
        }
    }

    @Builder
    @Value
    public static class CommandNodeHelpEntry implements HelpContext.HelpEntry {
        String baseCommand;
        String subCommand;
        String description;

        @Override
        public BaseComponent[] serialize(ChatColor highlightColor) {
            return new ComponentBuilder(baseCommand)
                .color(highlightColor)
                .append(" " + subCommand)
                .append(" - ").color(ChatColor.WHITE)
                .append(description).color(ChatColor.GRAY)
                .create();
        }
    }
}
