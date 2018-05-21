package org.cchmc.kluesuite.socketklue.proto;

import io.netty.buffer.ByteBuf;
import lombok.Value;

/**
 * Created by joe on 3/3/17.
 */
@Value
public class NumberedInPacket {
    long requestNumber;
    ByteBuf data;
}
