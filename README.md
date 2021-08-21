lenis' utils
============
lenisutils is a library for Minecraft-related development.
It has been created for internal use, but is fully open-source.
It is focussed on solving a set of common problems in Bukkit/spigot and Bungeecord plugins
and various quality-of-life improvements that make development easier.

It consists of a set of modules intended to be individually shaded in using maven on an individual basis.
The modules are trimmed to be as lite as possible so plugin jars can stay small 
and shaded packages can be safely relocated without bloating the classpath.

lenisutils currently contains the following modules:
- **lenisutils-module-system** 
  - provides system to split your project up into individual modules
- **lenisutils-config**
  - provides a configuration module that can load config.yml files and map them to java objects
  - allows for per-key headers to provide extra explanation
- **lenisutils-command**
  - provides a module that can register high-level commands with support for subcommands
  - automatically handles formatting of help and errors
- **lenisutils-packet** (sending only)
  - provides a module that can send packets to players  
  - Disclaimer: vastly inferior to ProtocolLib, should only be used as a fallback
- **lenisutils-task**
  - A system for dealing with thread synchronization by switching seamlessly between sync and async tasks.
  - Powered by CompletableFuture. supports bukkit as well as bungeecord.
- **lenisutils-sql** (WIP)
  - A module that makes SQL easier to integrate and use
  - Thread-safe single-connection pool
  - Migration runner that updates MySQL and SQLite schemas.
  - ResultSet object mapper that converts sql results to java objects
  - Repository generator for generating repositories and queries (inspired by Spring Data)

### Table of contents
- [Installation](#installation)
- [Modules](#modules)

## Installation
lenisutils is hosted on the [CodeMC build server](https://ci.codemc.io/). 
You will have to add it's repository to your maven (or gradle) project to use the modules.
```xml
<repositories>
    ...your other repos
    <repository>
        <id>codemc-repo</id>
        <url>https://repo.codemc.io/repository/maven-public/</url>
    </repository>
</repositories>
```
Then add compile dependencies for each module you would like to use.
```xml
<dependencies>
    ...your other dependencies
    <!-- Module system -->
    <dependency>
        <groupId>com.lenis0012.pluginutils</groupId>
        <artifactId>lenisutils-module-system</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>
    <!-- Configuration module -->
    <dependency>
        <groupId>com.lenis0012.pluginutils</groupId>
        <artifactId>lenisutils-config/artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>
    <!-- Async task manager -->
    <dependency>
        <groupId>com.lenis0012.pluginutils</groupId>
        <artifactId>lenisutils-task</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>
    <!-- SQL tools -->
    <dependency>
        <groupId>com.lenis0012.pluginutils</groupId>
        <artifactId>lenisutils-sql</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>
    etc..
</dependencies>
```

It is crucial to use the maven-shade-plugin to relocate the lenisutils packages 
to include the library and avoid conflicts with other plugins.
```xml
<build>
  <plugins>
    ...other plugins
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <executions>
        <execution>
          <id>shade</id>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <relocations>
          <!-- Relocate lenisutils -->
          <relocation>
            <pattern>com.lenis0012.pluginutils</pattern>
            <!-- replace ${groupId}.${artifactId} with your root package, such as me.myname.myproject -->
            <shadedPattern>${groupId}.${artifactId}.libs.pluginutils</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Modules