package com.lenis0012.pluginutils.modules.configuration.mapping;

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
