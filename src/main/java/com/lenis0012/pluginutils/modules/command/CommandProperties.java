package com.lenis0012.pluginutils.modules.command;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class CommandProperties {
    private final Map<String, Object> properties;

    public CommandProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Get command description.
     *
     * @return Description, empty string if not defined
     */
    public String getDescription() {
        return get("description", "");
    }

    /**
     * Get aliases.
     *
     * @return Aliases, empty list if not defined
     */
    public List<String> getAliases() {
        Object object = properties.get("aliases");
        if(object == null) return Lists.newArrayList();
        if(object instanceof List) {
            return (List<String>) object;
        } else {
            return Lists.newArrayList((String) object);
        }
    }

    /**
     * Get permission
     *
     * @return Permission, null if not defined
     */
    public String getPermission() {
        return get("permission", null);
    }

    /**
     * Get error message when player lacks permission.
     *
     * @return Permission message, empty string if not defined
     */
    public String getPermissionMessage() {
        return get("permission-message", "");
    }

    /**
     * Get command sample usage.
     *
     * @return Usage, empty string if not defined
     */
    public String getUsage() {
        return get("usage", "");
    }

    /**
     * Get a property from this command.
     *
     * @param key Key of property
     * @param type Type of property
     * @param <T> cast
     * @return Property, null if not defined
     */
    public <T> T get(String key, Class<T> type) {
        return type.cast(properties.get(key));
    }

    // internal
    private <T> T get(String key, T def) {
        return properties.containsKey(key) ? (T) properties.get(key) : def;
    }
}
