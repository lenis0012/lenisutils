package com.lenis0012.pluginutils.command;

import com.lenis0012.pluginutils.command.api.Command;
import com.lenis0012.pluginutils.command.api.Description;
import com.lenis0012.pluginutils.command.api.MessageProcessor;
import com.lenis0012.pluginutils.command.api.Permission;
import com.lenis0012.pluginutils.command.wiring.CommandNode;
import com.lenis0012.pluginutils.command.wiring.CommandPath;
import com.lenis0012.pluginutils.command.wiring.CommandSourceWirer;
import com.lenis0012.pluginutils.command.wiring.WiredCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    private final Plugin plugin;
    private final List<CommandSource<?>> sources = new ArrayList<>();
    private final CommandSourceWirer wirer = new CommandSourceWirer();
    private final Map<String, CommandNode> rootNodes = new HashMap<>();
    private MessageProcessor messageProcessor = MessageProcessor.DEFAULT;

    public CommandRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public CommandRegistry setMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
        return this;
    }

    public CommandRegistry register(Object object) {
        CommandSource source = new CommandSource(object.getClass(), () -> object);
        sources.add(source);
        wirer.registerWires(source);
        return this;
    }

    public CommandRegistry finishAndApply() {
        rootNodes.clear();
        sources.forEach(this::populateSubCommands);
        sources.stream()
            .flatMap(source -> wirer.wire(source).stream())
            .forEach(this::apply);
        Map<String, CommandNode> tree = new HashMap<>(rootNodes);
        BukkitCommandExecutor executor = new BukkitCommandExecutor(tree, messageProcessor);
        rootNodes.keySet().forEach(rootKey -> {
            String commandName = rootKey.substring(1);
            PluginCommand pluginCommand = plugin.getServer().getPluginCommand(commandName);
            if(pluginCommand == null) {
                throw new IllegalStateException("Command " + commandName + " not found in plugin yml!");
            }
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);
        });
        return this;
    }

    private void populateSubCommands(CommandSource<?> commandSource) {
        if(!commandSource.getType().isAnnotationPresent(Command.class)) {
            return;
        }
        Command command = commandSource.getType().getAnnotation(Command.class);
        CommandNode node = makeNodeAt(new CommandPath(command.value()));
        if(commandSource.getType().isAnnotationPresent(Description.class)) {
            Description description = commandSource.getType().getAnnotation(Description.class);
            node.setDescription(description.value());
        }
        if(commandSource.getType().isAnnotationPresent(Permission.class)) {
            Permission permission = commandSource.getType().getAnnotation(Permission.class);
            node.setPermission(permission.value());
        }
    }

    private void apply(WiredCommand command) {
        CommandNode node = makeNodeAt(command.getPath());
        node.setWiredCommand(command);
        if(!command.getDescription().isEmpty()) {
            node.setDescription(command.getDescription());
        }
        if(command.getPermission() != null) {
            node.setPermission(command.getPermission().isEmpty() ? null : command.getPermission());
        }
    }

    private CommandNode makeNodeAt(CommandPath path) {
        String[] parts = path.toString().split(" ");
        CommandNode node, rootNode;
        node = rootNode = rootNodes.get(parts[0]);
        if(node == null) {
            node = rootNode = new CommandNode(null, new CommandPath(parts[0]), parts[0]);
            rootNodes.put(parts[0], node);
        }

        for(int i = 1; i < parts.length; i++) {
            CommandNode child = node.getChild(parts[i]);
            if(child == null) {
                child = new CommandNode(rootNode, node.getPath().concat(parts[i]), parts[i]);
                node.addChild(child);
            }

            node = child;
        }

        return node;
    }
}
