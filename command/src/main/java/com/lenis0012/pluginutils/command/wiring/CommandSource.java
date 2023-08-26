package com.lenis0012.pluginutils.command.wiring;

import lombok.Value;

import java.util.function.Supplier;

@Value
public class CommandSource<T> {
    Class<T> type;
    Supplier<T> instantiator;

    public T getInstance() {
        return instantiator.get();
    }
}
