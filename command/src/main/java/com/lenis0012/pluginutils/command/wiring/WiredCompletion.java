package com.lenis0012.pluginutils.command.wiring;

import com.lenis0012.pluginutils.command.CommandSource;
import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.Completion;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WiredCompletion {
    private final CommandSource<?> source;
    private final String path;
    private final Class<?> type;
    private final Method method;
    private final List<BiFunction<CommandContext, String, Object>> paramaterMappings;

    public List<String> complete(CommandContext context) {
        String[] args = context.getArgs();
        String lastArg = args.length > 0 ? args[args.length - 1] : "";
        try {
            Object[] parameters = paramaterMappings.stream()
                .map(mapping -> mapping.apply(context, lastArg))
                .toArray();
            List<String> completions = (List<String>) method.invoke(source.getInstance(), parameters);
            if (completions == null || completions.isEmpty()) {
                return Collections.emptyList();
            }
            return completions.stream()
                .filter(completion -> completion.toLowerCase(Locale.ROOT).startsWith(lastArg.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("failed to invoke completion method", e);
        }
    }

    public boolean matches(String value, Class<?> type) {
        return this.type.equals(type) && CommandPath.matches(this.path, value);
    }

    public static WiredCompletion create(CommandSource<?> source, Completion completion, Method method) {
        List<BiFunction<CommandContext, String, Object>> mappings = Arrays.stream(method.getParameterTypes())
            .map(type -> {
                if (type.equals(CommandContext.class)) {
                    return (BiFunction<CommandContext, String, Object>) (context, input) -> context;
                } else if (type.equals(String.class)) {
                    return (BiFunction<CommandContext, String, Object>) (context, input) -> input;
                } else {
                    throw new CommandWiringException("unsupported parameter type for resolver " + type.getName());
                }
            })
            .collect(Collectors.toList());
        return new WiredCompletion(source, completion.path(), completion.value(), method, mappings);
    }
}
