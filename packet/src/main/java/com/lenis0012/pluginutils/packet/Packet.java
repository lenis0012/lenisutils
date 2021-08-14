package com.lenis0012.pluginutils.packet;

import com.lenis0012.pluginutils.modules.misc.Reflection.ClassReflection;

public class Packet {
    private final ClassReflection reflection;
    private final Object handle;

    protected Packet(ClassReflection reflection, Object handle) {
        this.reflection = reflection;
        this.handle = handle;
    }

    public String getName() {
        return handle.getClass().getSimpleName();
    }

    public void write(String field, Object value) {
        reflection.setFieldValue(field, handle, value);
    }

    public <T> T read(String field, Class<T> type) {
        return reflection.getFieldValue(field, handle, type);
    }

    protected Object getHandle() {
        return handle;
    }
}
