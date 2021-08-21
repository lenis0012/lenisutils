package com.lenis0012.pluginutils.sql;

import java.util.Optional;

public interface CrudRepository<T, ID> {

    Optional<T> findById(ID id);

    Iterable<T> findAll();

    void insert(T entity);

    boolean update(T entity);

    boolean deleteById(ID id);
}
