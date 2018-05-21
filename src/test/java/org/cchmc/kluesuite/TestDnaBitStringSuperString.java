package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.SuperString;
import org.junit.Test;

import java.util.zip.DataFormatException;

/**
 * Created by osboxes on 25/09/16.
 */
public class TestDnaBitStringSuperString {

    @Test
    public void testBoth(){
        SuperString test = new SuperString();
        test.addAndTrim("AAAAATTTTTCCCCCGGGGG");
        test.addAndTrim("XR");
        test.addAndTrim("CCCCCGGGGGAAAAATTTTT");

        try {
            DnaBitString dns;
            dns = new DnaBitString(test);
            System.out.println("Expected String :");
            for (String s : test.strings )  System.out.print(s);
            System.out.println("\nDnaBitString :");
            System.out.println(dns.toString());
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
    }
}
