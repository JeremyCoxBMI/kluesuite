package org.cchmc.kluesuite.socketklue.client;

import lombok.Value;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/8/17.
 */
@Value
public class RequestWrapper {
    KlueSocketPacket packet;
    KlueResponseHandler handler;
}
