lenisutils
==========

## Installation (maven)
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
    <version>2.1.9</version>
</dependency>
```

You should shade the utils into your plugin to prevent conflicts with other plugins.
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
    </configuration>
</plugin>
```