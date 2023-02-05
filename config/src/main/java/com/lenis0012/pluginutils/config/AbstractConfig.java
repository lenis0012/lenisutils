package com.lenis0012.pluginutils.config;

import com.lenis0012.pluginutils.config.mapping.ConfigHeader;
import com.lenis0012.pluginutils.config.mapping.ConfigKey;
import com.lenis0012.pluginutils.config.mapping.ConfigMapper;
import com.lenis0012.pluginutils.config.mapping.ConfigSection;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractConfig {
    private final Map<Class<?>, List<Field>> dataFields = new HashMap<>();
    private final ConfigMapper mapper;
    private final CommentConfiguration config;
    private final Logger logger;
    private boolean clearOnSave = false;

    protected AbstractConfig(ConfigurationModule module) {
        this.mapper = getClass().getAnnotation(ConfigMapper.class);
        this.config = module.getConfiguration(mapper.fileName());
        this.logger = module.logger();
        loadSectionKeys(getClass(), "");
    }

    private void loadSectionKeys(Class<?> source, String basePath) {
        List<Field> dataFields = new ArrayList<>();
        for(Field field : source.getDeclaredFields()) {
            ConfigKey key = field.getAnnotation(ConfigKey.class);
            if(key == null) {
                continue;
            }

            // Headers
            String keyPath = key.path().isEmpty() ? toConfigString(field.getName()) : key.path();
            ConfigHeader header = field.getAnnotation(ConfigHeader.class);
            if(header != null) {
                String path = header.path().isEmpty() ? keyPath : header.path();
                config.header(basePath + path, header.value());
            }

            if(field.getType().isAnnotationPresent(ConfigSection.class)) {
                loadSectionKeys(field.getType(), basePath + keyPath + ".");
            }

            field.setAccessible(true);
            dataFields.add(field);
        }
        this.dataFields.put(source, dataFields);
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
            config.mainHeader(mapper.header());
        }

        // Load values
        reloadSection(config, this);
    }

    @SneakyThrows
    private void reloadSection(ConfigurationSection source, Object target) {
        for(Field field : dataFields.get(target.getClass())) {
            ConfigKey key = field.getAnnotation(ConfigKey.class);
            String path = key.path().isEmpty() ? toConfigString(field.getName()) : key.path();
            if(!config.contains(path)) {
                continue;
            }

            Object value = source.get(path);

            if(value instanceof ConfigurationSection && field.getType().isAnnotationPresent(ConfigSection.class)) {
                try {
                    Object result = field.getType().getDeclaredConstructor().newInstance();
                    reloadSection((ConfigurationSection) value, result);
                    field.set(target, result);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to load property \"" + path + "\" from " + mapper.fileName());
                }
            } else {
                field.set(target, value);
            }
        }
    }

    public void save() {
        // Set values
        saveSection(config, this);
        config.save();
    }

    @SneakyThrows
    private void saveSection(ConfigurationSection target, Object source) {
        for(Field field : dataFields.get(source.getClass())) {
            ConfigKey key = field.getAnnotation(ConfigKey.class);
            String path = key.path().isEmpty() ? toConfigString(field.getName()) : key.path();

            if(field.getType().isAnnotationPresent(ConfigSection.class)) {
                ConfigurationSection section = target.getConfigurationSection(path);
                if(section == null) {
                    section = target.createSection(path);
                }
                Object sourceValue = field.get(source);
                saveSection(section, sourceValue);
            } else {
                target.set(path, field.get(source));
            }
        }
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
