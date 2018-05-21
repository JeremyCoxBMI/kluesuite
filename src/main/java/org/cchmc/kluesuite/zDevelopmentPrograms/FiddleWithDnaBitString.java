package org.cchmc.kluesuite.zDevelopmentPrograms;

import org.cchmc.kluesuite.klue.DnaBitString;

/**
 * Created by osboxes on 23/09/16.
 */
public class FiddleWithDnaBitString{

    public static long addUpSequence(String test){
        long temp = 0;
        for(int k=0; k<test.length(); k++){
            temp = temp << 2;
//            if (test.charAt(k) == 'A')  temp += 3;
//            else if (test.charAt(k) == 'T')  temp += 0;
//            else if (test.charAt(k) == 'C')  temp += 1;
//            else if (test.charAt(k) == 'G')  temp += 2;
            switch(test.charAt(k)){
                case 'A':
                case 'a':
                    temp += 3;
                    break;
                case 'T':
                case 't':
                    temp += 0;
                    break;
                case 'C':
                case 'c':
                    temp += 1;
                    break;
                case 'G':
                case 'g':
                    temp += 2;
                    break;
            }
        }
        return temp;
    }


    public static void main(String[] args) {

        DnaBitString dns;


        String tests[] = {"A","AT","ATC","ATCG"};

        for (String test : tests) {
            dns = new DnaBitString(test);
            System.out.println(test);
            System.out.println(dns.toBinaryString());
        }


        String test = tests[3];
        long temp = 0;
        for(int k=0; k<test.length(); k++){
            temp = temp << 2;
            if (test.charAt(k) == 'A')  temp += 3;
            else if (test.charAt(k) == 'T')  temp += 0;
            else if (test.charAt(k) == 'C')  temp += 1;
            else if (test.charAt(k) == 'G')  temp += 2;
        }
        dns = new DnaBitString(temp,4);
        System.out.println(test);
        System.out.println(dns.toBinaryString());

        //ONLY PROBLEM HERE is that LONG is not unsigned
        //WILL it work?
        String chump32 = "ACCCCGCCTGGGCTCACCTCCTAGGCTCAGTA";
        temp = addUpSequence(chump32);
        dns = new DnaBitString(temp,32);
        System.out.println(chump32);
        System.out.println(dns.toString());

        chump32 = "TCCCCGCCTGGGCTCACCTCCTAGGCTCAGTA";
        temp = addUpSequence(chump32);
        dns = new DnaBitString(temp,32);
        System.out.println(chump32);
        System.out.println(dns.toString());

    }
}
