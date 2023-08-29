# Updater
The updater module is a platform-agnostic update checker with a wide range of features.  

To use the updater, the central API module must be included, as well as one or more implementations.  
When creating an updater, an instance is created by finding the most suitable implementation
using service loading.  
Additionally, you can create multiple distributions that contain different backing implementations,
allowing you to use different profiles that contain only the implementation compatible with the target platform.

## Implementations

* `updater-manifest` - Manifest implementation. This uses a standardized file format that 
  can be loaded from any URL.
* `updater-bukkit` - dev.bukkit.org implementation.
* No-Op - A fallback used if no others are available

## Updater selection
An instance of the updater can be created via the `UpdaterFactory` class.  
This will find the most suitable backing implementation by comparing the available implementations
based on their supported capabilities and a running a compatability check.

Each updater factory provides a set of `Capabilities` that it supports and a `isCompatible`
method to check whether it is compatible with the current plugin installation.

Capabilities are used to determine the load order / priority. This is backed by
the `Capability` enum which is ordered by priority.
This means that an implementation with a higher capability will be picked over others.
For example, an implementation that only supports `Capability.VERSION_CHECK` will always be picked over
one that doesn't, even if the other one supports all other capabilities. 
Unless the former returns false from the `isCompatible` method.

## Installation

First, add the required dependencies to your project:
```xml
<!-- Main API module -->
<dependency>
    <groupId>com.lenis0012.pluginutils</groupId>
    <artifactId>lenisutils-updater-api</artifactId>
    <version>${lenisutils.version}</version>
</dependency>
<!-- One or more backing implementations -->
<dependency>
    <groupId>com.lenis0012.pluginutils</groupId>
    <artifactId>lenisutils-updater-manifest</artifactId>
    <version>${lenisutils.version}</version>
</dependency>
```

Don't forget to shade the dependencies into your plugin jar and merge the service files
according to the [main README](../README.md#installation).

# Getting started

## Creating an updater
To create an updater, use the `UpdaterFactory` class.
```java
// Auto-select best implementation (can be no-op)
UpdaterFactory updaterFactory = UpdaterFactory.provideBest(plugin);
Updater updater = updaterFactory.getUpdater(plugin);
```
Once the updater is created, it will automatically start checking for updates asynchronously in the background.
The checking will be done according to the update interval specified in the updater factory
and default to once every 4 hours.

You can change the update interval, amongst other properties, prior to creation:
```java
Updater updater = updaterFactory.provideBest(plugin)
        .withFrequency(Duration.ofHours(1)) // Check every hour
        .getUpdater(plugin);
```

## Notifying players
To notify players when an update is available, you can use the `Updater` class.
```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    if(event.getPlayer().hasPermission("myplugin.update")) {
        updater.notifyIfUpdateAvailable(event.getPlayer());
    }
}
```
`notifyIfUpdateAvailable` will not perform a blocking check, but use the information from
the last asynchronous update check performed by the updater.
This means it is safe to call this method from the main thread.

# Configuration
The updater implementations require some configuration to work properly.
After all, they need to know where to pull the updates from.
These settings are implementation-specific and are specified in the `plugin.yml` file.

If the configuration is missing or invalid, the updater will be incompatible and a different
implementation will be used automatically.
This allows you to include multiple implementations in your plugin and let the updater
automatically pick the best one.

## Manifest
The manifest updater requires a URL pointing to your manifest file.

plugin.yml:
```yml
name: MyPlugin
...
manifest-url: 'https://raw.githubusercontent.com/me/myplugin/master/version_manifest.json'
```

The manifest has the following format:
```json5
[
  {
    "version": "1.0.0",
    "changelogUrl": "<ur>", // optional
    "downloadUrl": "<url>", // optional
    "channel": "STABLE", // optional, defaults to STABLE
    "minMcVersion": "1.16", // optional - Only notify if server is running this version or higher
    "maxMcVersion": "1.20.1" // optional - Only notify if server is running this version or lower
  }
]
```

## Bukkit
The bukkit updater requires a project ID. This can be found on the sidebar of your project page on dev.bukkit.org.

plugin.yml:
```yml
name: MyPlugin
...
devbukkit-project: 12345
```
