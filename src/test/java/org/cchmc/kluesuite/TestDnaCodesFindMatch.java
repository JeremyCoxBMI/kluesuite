package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.DNAcodes;
import org.junit.Test;

import java.util.HashSet;

/**
 * Created by osboxes on 29/04/17.
 */
public class TestDnaCodesFindMatch {

    @Test
    public void testMatchDebugMode() {
        HashSet<Character> hc = new HashSet<Character>();
        hc.add('u');
        hc.add('a');
        hc.add('g');

        DNAcodes.DEBUG = true;
        DNAcodes.findMatch(hc);

        hc = new HashSet<Character>();
        hc.add('c');
        hc.add('C');
        hc.add('g');
        DNAcodes.findMatch(hc);
    }
}
