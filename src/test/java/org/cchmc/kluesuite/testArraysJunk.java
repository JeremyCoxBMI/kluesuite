package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.SuperString;
import org.junit.Test;

import java.util.Random;

/**
 * Created by jwc on 3/7/18.
 */
public class testArraysJunk {

    public static void aFunction(long[] a){
        long x = a[0];
        a[0] = a[3];
        a[3] = x;
    }

    static void shuffleArray(long[] ar, Random rnd)
    {
        long a;
        int index;
        for (int i = ar.length - 1; i > 0; i--) {
            index = rnd.nextInt(i);
            // Simple swap
            a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private static void printArray(long[] arr, int m) {
        SuperString ss = new SuperString();

        ss.add('{');
        ss.add(Long.toString(arr[0]));
        for(int k=1; k<m;k++){
            ss.add(',');
            ss.add(Long.toString(arr[k]));
        }
        ss.add('}');

        System.out.println(ss.toString());

    }

    @Test
    public void testPassArray(){
        long[] a ={10L, 20L, 30L, 40L};
        printArray(a,4);

        aFunction(a);
        printArray(a,4);

        a = new long[]{10L, 20L, 30L, 40L};
        shuffleArray(a, new Random());
        printArray(a,4);
    }
}
