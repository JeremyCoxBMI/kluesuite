package org.cchmc.kluesuite.zDevelopmentPrograms;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.SuperString;

import java.util.zip.DataFormatException;

/**
 * Created by osboxes on 25/09/16.
 *
 * Believe there is a bug related to constructing DnaBitString with length multiple 32.
 *
 */
public class testRocksDbKidDbPart32 {

    public static void main(String[] args) {
        SuperString ss = new SuperString();
        ss.addAndTrim("ATTTGTAGAATATTTGGATGTTCCAGAAAAAAGTAGCATTTTGACCTCTGTTTTCTATGGTCGTCACAGTGCGACATTCA");
        ss.addAndTrim("ATCGCTTGAATCTCTT");
        System.out.println("Verify length = 96 (3 * 32) :: "+ss.length());
        DnaBitString dns = null;
        try {
            dns = new DnaBitString(ss);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        System.out.println("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        System.out.println(dns.toString());
        System.out.println(ss.strings.get(0)+ss.strings.get(1));


        ss = new SuperString();
        ss.addAndTrim("ATTTGTAGAATATTTGGATGTTCCAGAAAAAAGTAGCATTTTGACCTCTGTTTTCTATGGTCGTCACAGTGCGACATTCA");
        ss.addAndTrim("XNATCGCTTGAATCTCTT");
        System.out.println("Verify length = 98 :: "+ss.length());
        try {
            dns = new DnaBitString(ss);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        System.out.println("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        System.out.println(dns.toString());
        System.out.println(ss.strings.get(0)+ss.strings.get(1));

    }

}
