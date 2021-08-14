package com.lenis0012.pluginutils.sql.pipeline;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResultMapper<T> {
    private final Class<T> type;
    private final List<Field> fields;

    public ResultMapper(Class<T> type) {
        this.type = type;
        this.fields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> !Modifier.isTransient(field.getModifiers()))
                .peek(Field::trySetAccessible)
                .collect(Collectors.toList());
    }

    public T map(ResultSet resultSet) {
        for(Field field : fields) {
            if(resultSet.getMetaData().getColumnName)
            Object value = resultSet.getObject(field.getName());
        }
    }
}
