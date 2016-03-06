package com.lenis0012.pluginutils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Registry {
    private final Map<Class<? extends Module>, Module> moduleMap = Maps.newConcurrentMap();
    private final PluginHolder plugin;
    private final ClassLoader classLoader;

    public Registry(PluginHolder plugin, ClassLoader classLoader) {
        this.plugin = plugin;
        this.classLoader = classLoader;
    }

    public void registerModules(String path) {
        List<Class<? extends Module>> classes = Lists.newArrayList();
        try {
            ClassPath classPath = ClassPath.from(classLoader);
            for(ClassInfo info : classPath.getTopLevelClassesRecursive(path)) {
                Class<?> clazz = Class.forName(info.getName());
                if(Module.class.isAssignableFrom(clazz)) {
                    classes.add((Class<? extends Module>) clazz);
                }
            }
        } catch(Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to scan for modules", e);
        }
        registerModules(classes.toArray(new Class[0]));
    }

    public void registerModules(Class<? extends Module>... modules) {
        registerModules(false, modules);
    }

    protected void registerModules(boolean local, Class<? extends Module>... modules) {
        for(Class<? extends Module> moduleClass : modules) {
            try {
                Module instance = (Module) moduleClass.getConstructors()[0].newInstance(plugin);
                instance.local = local;
                moduleMap.put(moduleClass, instance);
            } catch(Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to init module", e);
            }
        }
    }

    protected void enableModules(boolean local) {
        for(Module module : new ArrayList<>(moduleMap.values())) {
            if(module.local != local) continue;
            module.enable();
        }

        for(Module module : new ArrayList<>(moduleMap.values())) {
            if(module.local != local) continue;
            module.enable();
        }
    }

    protected void disableModules(boolean local) {
        for(Module module : new ArrayList<>(moduleMap.values())) {
            if(module.local != local) continue;
            module.disable();
        }

        for(Module module : new ArrayList<>(moduleMap.values())) {
            if(module.local != local) continue;
            module.disable();
        }
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        return moduleClass.cast(moduleMap.get(moduleClass));
    }
}
