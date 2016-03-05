package com.lenis0012.pluginutils.modules.configuration;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.lenis0012.pluginutils.misc.Reflection;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigKey;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigMapper;

import java.lang.reflect.Field;
import java.util.Set;

public class AbstractConfig {
    private final Set<Field> dataFields = Sets.newConcurrentHashSet();
    private final ConfigMapper mapper;
    private final Configuration config;
    private boolean clearOnSave = false;

    protected AbstractConfig(ConfigurationModule module) {
        this.mapper = getClass().getAnnotation(ConfigMapper.class);
        this.config = module.getConfiguration(mapper.fileName());
        for(Field field : getClass().getDeclaredFields()) {
            if(!field.isAnnotationPresent(ConfigKey.class)) {
                continue;
            }

            field.setAccessible(true);
            dataFields.add(field);
        }
    }

    /**
     * @return Whether or not values that are not registered in class should be removed from config
     */
    protected boolean isClearOnSave() {
        return clearOnSave;
    }

    /**
     * Set whether or not values that are not registered in class should be removed from config.
     *
     * @param flag Flag
     */
    protected void setClearOnSave(boolean flag) {
        this.clearOnSave = flag;
    }

    public void reload() {
        config.reload();
        if(mapper.header().length > 0) {
            config.options().header(Joiner.on('\n').join(mapper.header()));
        }

        // Load values
        for(Field field : dataFields) {
            ConfigKey key = field.getAnnotation(ConfigKey.class);
            String path = key.path().isEmpty() ? toConfigString(field.getName()) : key.path();
            if(!config.contains(path)) {
                continue;
            }

            Reflection.setFieldValue(field, this, config.get(path));
        }
    }

    public void save() {
        // Set values
        for(Field field : dataFields) {
            ConfigKey key = field.getAnnotation(ConfigKey.class);
            String path = key.path().isEmpty() ? toConfigString(field.getName()) : key.path();
            config.set(path, Reflection.getFieldValue(field, this));
        }

        config.save();
    }

    private String toConfigString(String value) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if(Character.isUpperCase(c)) {
                builder.append('-').append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
