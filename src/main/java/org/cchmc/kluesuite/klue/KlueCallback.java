package org.cchmc.kluesuite.klue;

/**
 * Created by joe on 4/17/17.
 */
public interface KlueCallback<T> {
    void callback(T value);
    void exception(Exception e);
}
