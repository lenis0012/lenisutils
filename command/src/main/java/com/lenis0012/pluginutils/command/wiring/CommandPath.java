package com.lenis0012.pluginutils.command.wiring;

public class CommandPath {
    private final String path;

    public CommandPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }

    public CommandPath concat(String path) {
        if(path.isEmpty()) {
            return this;
        }
        return new CommandPath((this.path + " " + path).trim());
    }

    // check if two strings match using wildcard
    public static boolean matches(String path, String input) {
        if(path.equals("**")) {
            return true;
        }

        String[] pathParts = path.split(" ");
        String[] inputParts = input.split(" ");

        if(pathParts.length != inputParts.length && !path.startsWith("**") && !path.endsWith("**")) {
            return false;
        }

        return compareParts(pathParts, inputParts, 0);
    }

    private static boolean compareParts(String[] pathParts, String[] inputParts, int offset) {
        for(int i = 0; i < pathParts.length; i++) {
            String pathPart = pathParts[i];
            if(pathPart.equals("**")) {
                if(i == 0) continue;
                if(i >= pathParts.length - 1) return true;
            }

            if(i + offset >= inputParts.length) {
                return false;
            }
            if(pathPart.equals("*") || pathPart.equalsIgnoreCase(inputParts[i + offset])) {
                continue;
            }

            if(pathParts[0].equals("**")) {
                return compareParts(pathParts, inputParts, offset + 1);
            }
            return false;
        }

        return false;
    }
}
