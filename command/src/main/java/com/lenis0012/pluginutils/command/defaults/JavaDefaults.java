package com.lenis0012.pluginutils.command.defaults;

import com.lenis0012.pluginutils.command.api.CommandException;
import com.lenis0012.pluginutils.command.api.Resolver;

public class JavaDefaults {

    @Resolver(Boolean.class)
    public Boolean resolveBoolean(String input) {
        return Boolean.parseBoolean(input);
    }

    @Resolver(String.class)
    public String resolveString(String input) {
        return input;
    }

    @Resolver(Integer.class)
    public Integer resolveInteger(String input) {
        try {
            return Integer.parseInt(input);
        } catch(NumberFormatException e) {
            throw new CommandException(CommandErrorMessage.INVALID_NUMBER, input);
        }
    }

    @Resolver(Double.class)
    public Double resolveDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch(NumberFormatException e) {
            throw new CommandException(CommandErrorMessage.INVALID_NUMBER, input);
        }
    }
}
