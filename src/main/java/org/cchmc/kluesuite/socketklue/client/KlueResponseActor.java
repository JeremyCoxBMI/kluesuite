package org.cchmc.kluesuite.socketklue.client;

/**
 * Created by joe on 3/8/17.
 */
public interface KlueResponseActor<T> {
    void handleUnsupported();
    void handleException(String s);
    void handleWrongOpcode(int receivedOpcode);
    void handleResponse(T value);
}
