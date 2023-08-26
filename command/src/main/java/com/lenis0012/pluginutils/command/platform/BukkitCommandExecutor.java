package com.lenis0012.pluginutils.command.platform;

import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.CommandException;
import com.lenis0012.pluginutils.command.api.message.MessageProcessor;
import com.lenis0012.pluginutils.command.defaults.CommandErrorMessage;
import com.lenis0012.pluginutils.command.wiring.CommandNode;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@RequiredArgsConstructor
public class BukkitCommandExecutor implements CommandExecutor, TabCompleter {
    private final Map<String, CommandNode> rootNodes;
    private final MessageProcessor messageProcessor;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String rootKey = "/" + command.getName();
        CommandNode node = rootNodes.get(rootKey);
        if(node == null) {
            return false;
        }

        CommandContext.CommandContextBuilder context = CommandContext.builder()
            .author(new BukkitCommandAuthor(sender))
            .command(command.getName())
            .helpContext(node.getHelpContext(sender))
            .label(label)
            .args(args);

        try {
            for(String arg : args) {
                CommandNode child = node.getChild(arg, sender);
                if(child == null) {
                    messageProcessor.process(context.build(), CommandErrorMessage.INVALID_ARGUMENT, arg, "/" + label);
                    return true;
                }

                node = child;
            }

            CommandContext builtContext = context.helpContext(node.getHelpContext(sender)).build();
            node.execute(builtContext);
        } catch (CommandException e) {
            messageProcessor.process(context.build(), e.getUserMessage(), e.getArgs());
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while executing command: " + command.getName(), e);
            messageProcessor.process(context.build(), CommandErrorMessage.INTERNAL_ERROR);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String rootKey = "/" + command.getName();
        CommandNode node = rootNodes.get(rootKey);
        if(node == null) {
            return null;
        }

        try {
            for(int i = 0; i < args.length - 1; i++) {
                CommandNode child = node.getChild(args[i], sender);
                if(child == null) {
                    return Collections.emptyList();
                }

                node = child;
            }

            String arg = args.length > 0 ? args[args.length - 1] : "";
            CommandContext context = CommandContext.builder()
                .author(new BukkitCommandAuthor(sender))
                .command(command.getName())
                .label(label)
                .args(args)
                .build();
            return node.complete(context, arg);
        } catch (CommandException e) {
            return Collections.emptyList();
        }
    }
}
