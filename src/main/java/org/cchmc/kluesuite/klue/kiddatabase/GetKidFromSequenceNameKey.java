package org.cchmc.kluesuite.klue.kiddatabase;

import java.io.UnsupportedEncodingException;

/**
 * Created by jwc on 4/12/18.
 */
public class GetKidFromSequenceNameKey {

    private byte[] b;
    private static String SHORT_KEY_ADD = "||break";

    public GetKidFromSequenceNameKey(byte[] bs) {
        b = bs;
    }

    public static GetKidFromSequenceNameKey Builder(String s) throws UnsupportedEncodingException {
        if (s == null) return null;

        if (s.length() < 9) {
            s = s + SHORT_KEY_ADD;
        }

        return new GetKidFromSequenceNameKey(s.getBytes("UTF-8"));
    }

    public String getString() throws UnsupportedEncodingException {
        String r = new String(b, "UTF-8");
        int z = r.length();
        if (z < 16) {
            if (r.substring(8).equals(SHORT_KEY_ADD)) {
                r = r.substring(0, 8);
            }
        }

        return r;
    }

//    public static byte[] toBytes(String s){
//        byte[] r = null;
//        return null;
//    }

    public byte[] toBytes() {
        return b;
    }

}
