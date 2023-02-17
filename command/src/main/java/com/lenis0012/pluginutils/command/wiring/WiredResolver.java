package com.lenis0012.pluginutils.command.wiring;

import com.lenis0012.pluginutils.command.CommandSource;
import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.CommandException;
import com.lenis0012.pluginutils.command.api.Context;
import com.lenis0012.pluginutils.command.api.Resolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public interface WiredResolver {

    boolean matches(CommandPath input, Class<?> type);

    boolean isContextual();

    Object resolve(CommandContext context);

    WiredResolver withArgument(int index);

    static WiredResolver create(CommandSource<?> source, Resolver resolver, Method method) {
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
        return new DirectWiredResolver(source, method, method.isAnnotationPresent(Context.class), resolver.value(), resolver.path(), mappings);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class DirectWiredResolver implements WiredResolver {
        private final CommandSource<?> source;
        private final Method method;
        private final boolean contextual;
        private final Class<?> type;
        private final String path;
        private final List<BiFunction<CommandContext, String, Object>> paramaterMappings;

        @Override
        public boolean matches(CommandPath input, Class<?> type) {
            return this.type.equals(type) && CommandPath.matches(this.path, input.toString());
        }

        @Override
        public boolean isContextual() {
            return contextual;
        }

        @Override
        public Object resolve(CommandContext context) {
            return resolve(context, null);
        }

        public Object resolve(CommandContext context, String input) {
            try {
                return method.invoke(source.getInstance(), paramaterMappings.stream().map(mapping -> mapping.apply(context, input)).toArray());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof CommandException) {
                    throw (CommandException) cause;
                } else {
                    String commandline = "/" + context.getLabel() + " " + String.join(" ", context.getArgs());
                    throw new RuntimeException("Failed to resolve argument '" + input + "' for command " + commandline, cause);
                }
            }
        }

        @Override
        public WiredResolver withArgument(int index) {
            return new IndexedWiredResolver(this, index);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class IndexedWiredResolver implements WiredResolver {
        private final DirectWiredResolver resolver;
        private final int index;

        @Override
        public boolean matches(CommandPath input, Class<?> type) {
            return resolver.matches(input, type);
        }

        @Override
        public boolean isContextual() {
            return resolver.isContextual();
        }

        @Override
        public Object resolve(CommandContext context) {
            return resolver.resolve(context, context.getArgs()[index - 1]);
        }

        @Override
        public WiredResolver withArgument(int index) {
            throw new UnsupportedOperationException("Cannot chain indexed resolvers");
        }
    }
}
