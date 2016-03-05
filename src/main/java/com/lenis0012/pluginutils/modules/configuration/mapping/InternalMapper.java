package com.lenis0012.pluginutils.modules.configuration.mapping;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lenis0012.pluginutils.misc.Reflection;
import com.lenis0012.pluginutils.misc.Reflection.ClassReflection;
import com.lenis0012.pluginutils.modules.configuration.AutoSavePolicy;
import com.lenis0012.pluginutils.modules.configuration.Configuration;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class InternalMapper {
    private final Map<Class<?>, SettingsHolder> holders = Maps.newConcurrentMap();

    public void registerSettingsClass(Class<?> settingsClass, Configuration config, AutoSavePolicy autoSave) {
        ClassReflection reflection = new ClassReflection(settingsClass);
        SettingsHolder holder = new SettingsHolder(config, autoSave, reflection);
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
        private final Set<ConfigOption> options = Sets.newConcurrentHashSet();
        private final Configuration config;
        private final AutoSavePolicy autoSave;

        public SettingsHolder(Configuration config, AutoSavePolicy autoSave, ClassReflection reflection) {
            this.config = config;
            this.autoSave = autoSave;
            registerOptions(reflection);
        }

        public AutoSavePolicy getAutoSave() {
            return autoSave;
        }

        private void registerOptions(ClassReflection reflection) {
            final String seperator = Character.toString(config.options().pathSeparator());
            for(Field field : reflection.getFields()) {
                if(ConfigOption.class.isAssignableFrom(field.getType())) {
                    // Validate
                    ConfigOption<?> value = Reflection.getFieldValue(field, null, ConfigOption.class);
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
