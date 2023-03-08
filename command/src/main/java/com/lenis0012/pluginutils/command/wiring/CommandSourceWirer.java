package com.lenis0012.pluginutils.command.wiring;

import com.lenis0012.pluginutils.command.CommandSource;
import com.lenis0012.pluginutils.command.api.Command;
import com.lenis0012.pluginutils.command.api.Completion;
import com.lenis0012.pluginutils.command.api.Resolver;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandSourceWirer {
    private final Map<Class<?>, List<WiredResolver>> resolvers = new HashMap<>();
    private final Map<Class<?>, List<WiredCompletion>> completions = new HashMap<>();

    public void registerWires(CommandSource<?> source) {
        for (Method method : source.getType().getMethods()) {
            if (method.isAnnotationPresent(Resolver.class)) {
                Resolver resolver = method.getAnnotation(Resolver.class);
                WiredResolver wiredResolver = WiredResolver.create(source, resolver, method);
                resolvers.computeIfAbsent(resolver.value(), k -> new LinkedList<>()).add(0, wiredResolver);
            }
            if (method.isAnnotationPresent(Completion.class)) {
                Completion completion = method.getAnnotation(Completion.class);
                WiredCompletion wiredCompletion = WiredCompletion.create(source, completion, method);
                completions.computeIfAbsent(completion.value(), k -> new LinkedList<>()).add(0, wiredCompletion);
            }
        }
    }

    public List<WiredCommand> wire(CommandSource<?> source) {
        CommandPath path = source.getType().isAnnotationPresent(Command.class) ?
            new CommandPath(source.getType().getAnnotation(Command.class).value()) :
            new CommandPath("");

        List<WiredCommand> wiredCommands = new ArrayList<>();
        for (Method method : source.getType().getMethods()) {
            Command[] commands = method.getAnnotationsByType(Command.class);
            for (Command command : commands) {
                WiredResolver[] parameterResolvers = Arrays.stream(method.getParameters())
                    .map(parameter -> findResolver(path, command, parameter))
                    .toArray(WiredResolver[]::new);
                Optional<WiredCompletion> completion = findCompletion(method, command);
                wiredCommands.add(WiredCommand.create(source, method, path, command, parameterResolvers, completion.orElse(null)));
            }
        }

        return wiredCommands;
    }

    private WiredResolver findResolver(CommandPath path, Command command, Parameter parameter) {
        Class<?> type = parameter.getType();
        CommandPath fullPath = path.concat(command.value());

        // Find resolver by name
        int index = Arrays.asList(fullPath.toString().split(" ")).indexOf("<" + parameter.getName() + ">");
        if (index >= 0) {
            for (WiredResolver resolver : resolvers.getOrDefault(type, new LinkedList<>())) {
                if (resolver.isContextual()) continue;
                if (resolver.matches(fullPath, parameter.getType())) return resolver.withArgument(index);
            }
        }

        // Find contextual resolver
        for (WiredResolver resolver : resolvers.getOrDefault(type, new LinkedList<>())) {
            if (!resolver.isContextual()) continue;
            if (resolver.matches(fullPath, parameter.getType())) return resolver;
        }

        return null;
//        throw new CommandWiringException("No resolver found suitable to " + type.getSimpleName() + " for " + parameter.getName());
    }

    private Optional<WiredCompletion> findCompletion(Method method, Command command) {
        String[] path = command.value().split(" ");
        String last = path[path.length - 1];

        if (!last.startsWith("<") || !last.endsWith(">")) {
            return Optional.empty();
        }

        String name = last.substring(1, last.length() - 1);
        return Arrays.stream(method.getParameters())
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst()
            .flatMap(parameter -> findCompletion(command, parameter));
    }

    private Optional<WiredCompletion> findCompletion(Command command, Parameter parameter) {
        Class<?> type = parameter.getType();

        for (WiredCompletion completion : completions.getOrDefault(type, new LinkedList<>())) {
            if (completion.matches(command.value(), parameter.getType())) return Optional.of(completion);
        }

        return Optional.empty();
    }
}