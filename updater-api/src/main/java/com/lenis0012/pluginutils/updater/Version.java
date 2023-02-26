package com.lenis0012.pluginutils.updater;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Version {
    VersionNumber versionNumber;
    String downloadUrl;
    String changelogUrl;
    UpdateChannel channel;

    VersionNumber minMinecraftVersion;
    VersionNumber maxMinecraftVersion;
}
