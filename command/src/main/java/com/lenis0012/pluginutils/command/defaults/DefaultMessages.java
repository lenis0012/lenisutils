package com.lenis0012.pluginutils.command.defaults;

import com.lenis0012.pluginutils.command.api.Message;

public enum DefaultMessages implements Message {
    INVALID_PLAYER("Player '%s' is not online"),
    INVALID_OFFLINE_PLAYER("Player %s does not exist"),
    INVALID_MATERIAL("Material '%s' does not exist"),
    INVALID_WORLD("World '%s' does not exist"),
    INVALID_NUMBER("'%s' is not a valid number"),
    INVALID_ARGUMENT("Unexpected argument '%s'. Type %s for help"),
    INTERNAL_ERROR("An internal error occurred while executing this command"),
    NO_PERMISSION("You do not have permission to execute this command"),
    ;

    private final String template;

    DefaultMessages(String template) {
        this.template = template;
    }

    @Override
    public String getTemplate() {
        return template;
    }
}
