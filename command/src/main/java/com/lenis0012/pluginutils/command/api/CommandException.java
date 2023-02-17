package com.lenis0012.pluginutils.command.api;

public class CommandException extends RuntimeException {
    private final Message message;
    private final Object[] args;

    public CommandException(Message message, Object... args) {
        super(String.format(message.getTemplate(), args));
        this.message = message;
        this.args = args;
    }

    public Message getUserMessage() {
        return message;
    }

    public Object[] getArgs() {
        return args;
    }
}
