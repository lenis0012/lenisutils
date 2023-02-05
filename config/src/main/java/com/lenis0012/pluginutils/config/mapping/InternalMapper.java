package com.lenis0012.pluginutils.config.mapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lenis0012.pluginutils.config.AutoSavePolicy;
import com.lenis0012.pluginutils.config.CommentConfiguration;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

public class InternalMapper {
    private final Map<Class<?>, SettingsHolder> holders = Maps.newConcurrentMap();

    public void registerSettingsClass(Class<?> settingsClass, CommentConfiguration config, AutoSavePolicy autoSave) {
        SettingsHolder holder = new SettingsHolder(config, autoSave, settingsClass);
        holders.put(settingsClass, holder);
    }

    public void loadSettings(Class<?> byClass, boolean writeDefaults) {
        SettingsHolder holder = holders.get(byClass);
        if(holder == null) {
            throw new IllegalArgumentException("Specified settings not registered");
        }

        holder.load(writeDefaults);
    }

    public void saveSettings(Class<?> byClass) {
        SettingsHolder holder = holders.get(byClass);
        if(holder == null) {
            throw new IllegalArgumentException("Specified settings not registered");
        }

        holder.save();
    }

    public void shutdown() {
        for(SettingsHolder holder : holders.values()) {
            if(holder.getAutoSave() != AutoSavePolicy.ON_SHUTDOWN) {
                continue;
            }

            holder.save();
        }
    }

    protected static class SettingsHolder {
        private List<ConfigOption> options = Lists.newArrayList();
        private final CommentConfiguration config;
        private final AutoSavePolicy autoSave;

        public SettingsHolder(CommentConfiguration config, AutoSavePolicy autoSave, Class<?> settingsClass) {
            this.config = config;
            this.autoSave = autoSave;
            registerOptions(settingsClass);
        }

        public AutoSavePolicy getAutoSave() {
            return autoSave;
        }

        @SneakyThrows
        private void registerOptions(Class<?> settingsClass) {
            final String seperator = Character.toString(config.options().pathSeparator());
            for(Field field : settingsClass.getFields()) {
                if(ConfigOption.class.isAssignableFrom(field.getType())) {
                    // Validate
                    ConfigOption<?> value = (ConfigOption<?>) field.get(null);
                    if(value == null) {
                        continue;
                    }

                    // Register
                    value.setHolder(this);
                    options.add(value);

                    // Header
                    ConfigHeader header = field.getAnnotation(ConfigHeader.class);
                    if(header != null) {
                        String path = header.path().isEmpty() ? value.getPath(seperator) : header.path();
                        config.header(path, header.value());
                    }
                }
            }
        }

        public void load(boolean writeDefaults) {
            config.reload();
            for(ConfigOption<?> option : options) {
                option.loadFromConfig(config);
            }

            if(writeDefaults) {
                // Clear old settings
                for(String key : config.getKeys(false)) {
                    config.set(key, null);
                }

                save(); // Save default values
            }
        }

        public void save() {
            for(ConfigOption<?> option : options) {
                option.saveToConfig(config);
            }
            config.save();
        }
    }
}
