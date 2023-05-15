package com.lenis0012.pluginutils.command.wiring;

import com.lenis0012.pluginutils.command.api.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WiringContext {
    private final Annotation[] annotations;

    private WiringContext(Annotation[] annotations) {
        this.annotations = annotations;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(annotations);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof WiringContext)) {
            return false;
        }
        return Arrays.equals(annotations, ((WiringContext) obj).annotations);
    }

    @Override
    public String toString() {
        return Arrays.stream(annotations)
                .map(Annotation::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public boolean isPresent() {
        return annotations.length > 0;
    }

    public static WiringContext ofParameter(Parameter parameter) {
        Annotation[] annotations = Arrays.stream(parameter.getAnnotations())
            .filter(annotation -> annotation.annotationType() == Context.class ||
                annotation.annotationType().isAnnotationPresent(Context.class))
            .toArray(Annotation[]::new);
        return new WiringContext(annotations);
    }

    public static WiringContext ofResolver(Method resolver) {
        Annotation[] annotations = Arrays.stream(resolver.getAnnotations())
            .filter(annotation -> annotation.annotationType() == Context.class ||
                annotation.annotationType().isAnnotationPresent(Context.class))
            .toArray(Annotation[]::new);
        return new WiringContext(annotations);
    }
}
