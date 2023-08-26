package com.lenis0012.pluginutils.updater;

import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return !greaterThanOrEqual(other);
    }

    public boolean lessThanOrEqual(VersionNumber other) {
        return !greaterThan(other);
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

    @Override
    public String toString() {
        return Arrays.stream(parts).mapToObj(String::valueOf).collect(Collectors.joining("."));
    }

    public static VersionNumber of(String versionString) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+(\\.\\d)*");
        Matcher matcher = pattern.matcher(versionString);
        if(!matcher.find()) {
            throw new IllegalArgumentException("Invalid version string: " + versionString);
        }

        String version = versionString.substring(matcher.start(), matcher.end());
        int[] parts = Arrays.stream(version.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
        return new VersionNumber(parts);
    }

    public static VersionNumber ofBukkit() {
        return of(Objects.requireNonNull(Bukkit.getServer().getBukkitVersion(), "Bukkit version is null"));
    }

    public static Comparator<VersionNumber> comparator() {
        return (a, b) -> {
            for(int i = 0; i < a.parts.length && i < b.parts.length; i++) {
                if(a.parts[i] > b.parts[i]) {
                    return 1;
                } else if(a.parts[i] < b.parts[i]) {
                    return -1;
                }
            }
            return 0;
        };
    }
}
