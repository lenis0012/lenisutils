package com.lenis0012.pluginutils.config.mapping;

import com.lenis0012.pluginutils.config.AutoSavePolicy;
import com.lenis0012.pluginutils.config.mapping.InternalMapper.SettingsHolder;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigOption<T> {
    private final String path;
    private T value;
    private SettingsHolder holder;

    /**
     * @param path Relative path to option
     */
    public ConfigOption(String path) {
        this(path, null);
    }

    /**
     * @param path Relative path to option
     * @param defaultValue Default value, used if option not in config
     */
    public ConfigOption(String path, T defaultValue) {
        this.path = path;
        this.value = defaultValue;
    }

    protected String getPath(String seperator) {
        return path;
    }

    protected void setHolder(SettingsHolder holder) {
        this.holder = holder;
    }

    protected void loadFromConfig(ConfigurationSection section) {
        if(!section.contains(path)) return;
        this.value = (T) section.get(path);
    }

    protected void saveToConfig(ConfigurationSection section) {
        section.set(path, value);
    }

    /**
     * Set option to value.
     *
     * @param value to set to
     */
    public void set(T value) {
        this.value = value;
        if(holder != null && holder.getAutoSave() == AutoSavePolicy.ON_CHANGE) {
            holder.save();
        }
    }

    /**
     * @return Option value
     */
    public T value() {
        return value;
    }
}
