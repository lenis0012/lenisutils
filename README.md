lenisutils
==========

## Overview
Lenisutils is a collection of utilities for bukkit plugins. 
Its goal is to provide embedded functionality that is very commonly needed for bukkit development.

Lenisutils is split up into modules, each module has its own purpose and can be used independently of each other.
Each module is designed to be lightweight, targeting a jar size of less than 20kb.

### Modules
* [Module system](#module-system) - A simple module system with support for service discovery, lifecycle management ~~and dependency injection~~
* [Config](#config) - An annotation-driven configuration API with support for inline comments retention
* [Updater](#updater) - A platform-agnostic update checker with a wide range of features
* [SQL](#sql) - Utilities for setting up and working with MySQL and SQLite databases using a connection pool

### Disclaimers
The `packet` module is old, deprecated and broken on newer versions. The `command` module is being reworked.
These modules are currently not recommended for use.

Lenisutils will not be expanded with modules for thngs already covered by other libraries or more suited to plugins.  
Such modules include but are not limited to: packet framework, inventory/GUI framework, scoreboards, placeholders, etc.

This project is designed to be lightweight. Pull requests are welcome but are subject to rejection if they unnecessarily bloat the jar.

## Installation

### Maven
To use lenisutils in your maven project, make sure that the codemc repository is added to your pom.xml:
```xml
<repositories>
    <!-- other repositories... -->
    <repository>
        <id>codemc-repo</id>
        <url>https://repo.codemc.io/repository/maven-public/</url>
    </repository>
</repositories>
```

Then add the dependency of the module(s) you want to use:
```xml
<dependency>
    <groupId>com.lenis0012</groupId>
    <artifactId>lenisutils-config</artifactId>
    <version>2.3.0</version>
</dependency>
```

You should shade the utils into your plugin to prevent conflicts with other plugins.
It is also recommended to configure the services resource transformer which is 
required for some modules (such as the updater) to function.
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <configuration>
        <relocations>
            <!-- other relocations... -->
            <relocation>
                <pattern>com.lenis0012.pluginutils</pattern>
                <shadedPattern>${project.groupId}.lenisutils</shadedPattern>
            </relocation>
        </relocations>
        <!-- Transform meta files used for service discovery in updater module -->
        <transformers>
            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
        </transformers>
    </configuration>
</plugin>
```

### Gradle
I don't use gradle for bukkit development but I do have some pointers on how you may configure it.  
You should first of all include the dependency as an implementation dependency:
```groovy
repositories{
    maven{ url = "https://repo.codemc.io/repository/maven-public/" }
}

dependencies {
    implementation 'com.lenis0012:lenisutils-config:2.3.0'
}
```

Then you should shade the utils into your plugin using [Shadow](https://imperceptiblethoughts.com/shadow/).  
This can be done by changing your plugins at the top of your `build.gradle` file:
```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.0'
    id 'java'
}
```

Then you should configure the shadow plugin to relocate the utils and transform the services resource:
```groovy
shadowJar {
    relocate 'com.lenis0012.pluginutils', "my.package.lenisutils"
    mergeServiceFiles()
}
```

You can now get your distribution jar by running `gradle shadowJar`.