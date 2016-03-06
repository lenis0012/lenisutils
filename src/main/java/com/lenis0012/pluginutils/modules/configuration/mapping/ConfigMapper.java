package com.lenis0012.pluginutils.modules.configuration.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigMapper {
    /**
     * @return Name of config file
     */
    String fileName() default "config.yml";

    /**
     * @return main header
     */
    String[] header() default {};
}
