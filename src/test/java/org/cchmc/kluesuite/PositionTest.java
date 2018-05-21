package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.Position;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by osboxes on 15/08/16.
 */
public class PositionTest {

    @Test
    public void testConstructor(){
        Position monkey = new Position(128, 148, "01010010");
        System.out.println(monkey);
        Assert.assertEquals("{KID 128, POS 148, FLAGS 01010010}", monkey.toString());
        monkey = new Position(1128, 1148);
        System.out.println(monkey);
        Assert.assertEquals("{KID 1128, POS 1148, FLAGS 00000000}", monkey.toString());
    }
}
