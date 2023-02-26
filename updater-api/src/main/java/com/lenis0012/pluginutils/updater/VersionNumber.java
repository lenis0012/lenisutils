package com.lenis0012.pluginutils.updater;

import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionNumber {
    private final int[] parts;

    private VersionNumber(int[] parts) {
        this.parts = parts;
    }

    public boolean greaterThan(VersionNumber other) {
        for(int i = 0; i < parts.length && i < other.parts.length; i++) {
            if(parts[i] > other.parts[i]) {
                return true;
            } else if(parts[i] < other.parts[i]) {
                return false;
            }
        }
        return false;
    }

    public boolean greaterThanOrEqual(VersionNumber other) {
        return greaterThan(other) || equals(other);
    }

    public boolean lessThan(VersionNumber other) {
        return !other.greaterThanOrEqual(this);
    }

    public boolean lessThanOrEqual(VersionNumber other) {
        return !other.greaterThan(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionNumber that = (VersionNumber) o;
        return Arrays.equals(parts, that.parts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }

    public static VersionNumber of(String versionString) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+(\\.\\d)*");
        Matcher matcher = pattern.matcher(versionString);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version string: " + versionString);
        }

        String version = matcher.group();
        int[] parts = Arrays.stream(version.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
        return new VersionNumber(parts);
    }

    public static VersionNumber ofBukkit() {
        return of(Objects.requireNonNull(Bukkit.getServer().getBukkitVersion(), "Bukkit version is null"));
    }
}
