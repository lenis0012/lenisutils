package com.lenis0012.pluginutils.command.api.message;

public interface Message {

    String getTemplate();

    String name();

    default boolean isHelpMessage() {
        return false;
    }

    default boolean isErrorMessage() {
        return name().startsWith("ERROR_");
    }

    static Message ofNamed(String name, String template) {
        return new NamedMessage(name, template);
    }

    class NamedMessage implements Message {
        private final String name;
        private final String template;

        private NamedMessage(String name, String template) {
            this.name = name;
            this.template = template;
        }

        @Override
        public String getTemplate() {
            return template;
        }

        @Override
        public String name() {
            return name;
        }
    }
}
