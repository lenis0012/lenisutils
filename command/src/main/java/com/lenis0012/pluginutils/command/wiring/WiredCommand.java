package com.lenis0012.pluginutils.command.wiring;

import com.lenis0012.pluginutils.command.CommandSource;
import com.lenis0012.pluginutils.command.api.Command;
import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.Description;
import com.lenis0012.pluginutils.command.api.Permission;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WiredCommand {
    private final CommandSource<?> source;
    private final CommandPath path;
    private final Method method;
    private final WiredResolver[] resolvers;
    private final WiredCompletion completion;
    @Getter
    private final String description;
    @Getter
    private final String permission;

    public void execute(CommandContext context) {
        Object[] args = new Object[resolvers.length];
        for(int i = 0; i < args.length; i++) {
            if(resolvers[i] == null) {
                continue;
            }
            args[i] = resolvers[i].resolve(context);
        }

        try {
            method.invoke(source.getInstance(), args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException("Unexpected error occurred while executing command", cause);
            }
        }
    }

    public List<String> complete(CommandContext context) {
        return completion != null ? completion.complete(context) : Collections.emptyList();
    }

    public static WiredCommand create(CommandSource<?> source, Method method, CommandPath path, Command command, WiredResolver[] parameterResolvers, WiredCompletion completion) {
        String description = command.description();
        if(description.isEmpty() && method.isAnnotationPresent(Description.class)) {
            description = method.getAnnotation(Description.class).value();
        }
        String permission = method.isAnnotationPresent(Permission.class) ? method.getAnnotation(Permission.class).value() : null;
        return new WiredCommand(source, path.concat(command.value()), method, parameterResolvers, completion, description, permission);
    }

    public CommandPath getPath() {
        return path;
    }
}
