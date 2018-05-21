package org.cchmc.kluesuite.socketklue.proto;

import lombok.Value;

/**
 * Created by joe on 3/3/17.
 */
@Value
public class NumberedOutPacket {
    long requestNumber;
    KlueSocketPacket packet;
}
