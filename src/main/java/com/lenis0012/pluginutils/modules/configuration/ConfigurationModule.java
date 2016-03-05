package com.lenis0012.pluginutils.modules.configuration;

import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.mapping.InternalMapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Level;

public class ConfigurationModule extends Module {
    private InternalMapper mapper;

    public ConfigurationModule(PluginHolder plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        this.mapper = new InternalMapper();
    }

    @Override
    public void disable() {
        mapper.shutdown();
    }

    /**
     * Load configuration from file in data folder.
     *
     * @param fileName Name of file
     * @return Configuration
     */
    public Configuration getConfiguration(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        file.getParentFile().mkdirs();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to create config file", e);
            }
        }
        return new Configuration(file);
    }

    public <T extends AbstractConfig> T createCustomConfig(Class<T> configClass) {
        try {
            Constructor<T> constructor = configClass.getConstructor(ConfigurationModule.class);
            return constructor.newInstance(this);
        } catch(Exception e) {
            throw new IllegalArgumentException("Invalid config class", e);
        }
    }

    public void registerSettings(Class<?> settingsClass) {
        registerSettings(settingsClass, "config.yml");
    }

    public void registerSettings(Class<?> settingsClass, String fileName) {
        registerSettings(settingsClass, fileName, AutoSavePolicy.DISABLED);
    }

    public void registerSettings(Class<?> settingsClass, String fileName, AutoSavePolicy autoSave) {
        mapper.registerSettingsClass(settingsClass, getConfiguration(fileName), autoSave);
    }

    public void reloadSettings(Class<?> settingsClass) {
        reloadSettings(settingsClass, true);
    }

    public void reloadSettings(Class<?> settingsClass, boolean writeDefaults) {
        mapper.loadSettings(settingsClass, writeDefaults);
    }

    public void saveSettings(Class<?> settingsClass) {
        mapper.saveSettings(settingsClass);
    }
}
