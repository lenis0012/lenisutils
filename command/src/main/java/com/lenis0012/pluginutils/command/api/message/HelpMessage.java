package com.lenis0012.pluginutils.command.api.message;

public class HelpMessage implements Message {
    @Override
    public String getTemplate() {
        return "";
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public boolean isHelpMessage() {
        return true;
    }

    public static HelpMessage ofCurrent() {
        return new HelpMessage();
    }
}
