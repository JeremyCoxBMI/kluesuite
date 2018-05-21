package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.Position;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jwc on 2/6/18.
 */
public class TestPosition {

    @Test
    public void testText() {

        //Rocks16Klue.makeChunks();
        Position p = new Position(33, 4001);
        p.setFlag(0, true);
        p.setFlag(1, true);
        p.setFlag(2, true);
        Position r = new Position(33, 4001, true);
        System.out.println(p.toBinaryString());
        System.out.println(r.toBinaryString());
        System.out.println(p.toLong());
        System.out.println(r.toLong());
        System.out.println(p);
        System.out.println(r);
        System.out.println(Position.MAX_KID);
        System.out.println("BIT SHIFT");
        System.out.println(1 << 28);
        System.out.println(1 << 28+2000);

    }

    @Test
    public void testConstructorToString() {

        //Rocks16Klue.makeChunks();
        Position p = new Position(33, 4001);
        p.setFlag(0, true);
        p.setFlag(1, true);
        p.setFlag(2, true);
        Position r = new Position(33, 4001, true);
        String ps = "{KID 33, POS 4001, FLAGS 00000111}";
        String rs = "{KID 33, POS 4001, FLAGS 10000000}";

        Assert.assertEquals(p.toString(),ps);
        Assert.assertEquals(r.toString(),rs);
    }


    @Test
    public void testReverseBit() {
        Position p = new Position(33, 4001);
        Position r = new Position(33, 4001, true);
        Position p2 = new Position(r);
        p2.setFlag(Position.REVERSE,false);
        Assert.assertNotEquals(p.toLong(), r.toLong());
        Assert.assertEquals(p.toLong(), p2.toLong());
    }

    @Test
    public void testToBinaryString(){
        Position p = new Position(33, 4001);
        Position r = new Position(33, 4001, true);
        Position p2 = new Position(r);
        p2.setFlag(Position.REVERSE,false);
        Assert.assertNotEquals(p.toBinaryString(), r.toBinaryString());
        Assert.assertEquals(p.toBinaryString(), p2.toBinaryString());

        //String indexes from left to right -- reverse of bits
//        System.out.println(p.toBinaryString().substring(1,64));
//        System.out.println(r.toBinaryString().substring(1,64));
        Assert.assertEquals(p.toBinaryString().substring(1,64), r.toBinaryString().substring(1,64));
    }

    @Test
    public void testFlagsToBinaryString(){
        Position p = new Position(33, 4001);
        Position r = new Position(33, 4001, true);
        Position p2 = new Position(r);
        p2.setFlag(Position.REVERSE,false);

        String ps=p.flagsToBinaryString();
        String p2s=p2.flagsToBinaryString();
        String rs=r.flagsToBinaryString();

        String noFlags = "00000000";
        String noFlagsRev = "10000000";
        Assert.assertEquals(ps, noFlags);
        Assert.assertEquals(p2s, noFlags);
        Assert.assertEquals(rs, noFlagsRev);
    }

    @Test
    public void testBinaryStringConstructor(){
        //public Position( int kid, int pos, String binStr)
        Position p = new Position(33, 4001);
        p.setFlag(0, true);
        p.setFlag(1, true);
        p.setFlag(2, true);
        Position p2 = new Position(33, 4001,p.flagsToBinaryString());
        Position r = new Position(p2);
        r.setFlag(Position.REVERSE,true);


        String noFlags = "00000111";
        String ps=p.toBinaryString();
        String p2s = p2.toBinaryString();
        String noFlagsRev = "10000111";

        Assert.assertEquals(ps, p2s);
        Assert.assertEquals(p.flagsToBinaryString(), noFlags);
        Assert.assertEquals(p2.flagsToBinaryString(), noFlags);
        Assert.assertEquals(r.flagsToBinaryString(), noFlagsRev);


    }

    @Test
    public void testExtractBitsFromNumber(){

    }

    @Test
    public void testIsFlagBit(){

    }

    @Test
    public void testGetMyKID(){
        Position p = new Position(33, 4001);
        Position r = new Position(33, 4001, true);
        Position big = new Position(268435455, 400104);
        Position rbig = new Position(-268435455, 400104);

        Assert.assertEquals(p.getMyKID(),33);
        Assert.assertEquals(r.getMyKID(),33);
        Assert.assertEquals(big.getMyKID(),268435455);

        Position TOO_BIG = new Position(268435456, 400104);
        Assert.assertNotEquals(TOO_BIG.getMyKID(), 268435456);
        Assert.assertNotEquals(r.getMyKID(), -268435455);
        Assert.assertNotEquals(r.getMyKID(), 268435455);

//        Position nextOffset = new Position( 1 << 28 + 2000, 101010101);
        Position x = new Position( (1 << 28)+2000, 101010101);

        System.out.println("Testing myKid() stress conditions");
        System.out.println(x.getMyKID());
        System.out.println((1<<28)+2000);
        Assert.assertNotEquals(x.getMyKID(), (1 << 28) + 2000);
        Assert.assertEquals(x.getMyKID(),2000);


    }

    @Test
    public void testGetPosition(){
        Position p = new Position(33, 4001);
        Position r = new Position(33, 4001, true);
        Assert.assertEquals(p.getPosition(),r.getPosition());
        Assert.assertEquals(p.getPosition(),4001);
    }


    @Test
    public void testCompare(){
        Position p = new Position(33, 4006);
        Position r = new Position(33, 4006, true);
//        Assert.assertEquals(p.compareTo(r), 0);
        Position s = new Position(33, 4006);

        p.setFlagsBinaryString("10101011");  //reverses p

        Assert.assertTrue(p.equals(r));
        p.setFlag(Position.REVERSE, false);
        Assert.assertTrue(p.equals(r));

        Position t = new Position(34, 4006);
        Position u = new Position(33, 4);

        Assert.assertTrue( t.compareTo(p) > 0);
        Assert.assertTrue( u.compareTo(p) < 0);
        Assert.assertTrue( p.compareTo(u) > 0);
        Assert.assertTrue( u.compareTo(u) == 0);
    }


//    @Test
//    public void testCopyBits(){
//      //already tested by other functions above
//    }


    @Test
    public void testSetFlagsBinaryString(){
        Position s = new Position(33, 4006);
        String flags = "10101011";
        s.setFlagsBinaryString(flags);
        Assert.assertEquals(flags,s.flagsToBinaryString());
    }


    @Test
    public void testLexicographicOrder(){

        Position p = new Position(33, 4006);
        Position r = new Position(33, 4006, true);
        Position s = new Position(33, 4006);
        Position t = new Position(34, 4006);
        Position u = new Position(33, 4);

        ArrayList<Position> pl = new ArrayList<>();
        pl.add(r);
        pl.add(s);
        pl.add(p);
        pl.add(u);
        pl.add(t);
        Collections.sort(pl);
        System.out.println("Sorted list");
        for (Position item : pl){
            System.out.println(item);
        }
        Assert.assertTrue(pl.get(0).equals(u));
        Assert.assertTrue(pl.get(1).equals(r));
        Assert.assertTrue(pl.get(2).equals(p));
        Assert.assertTrue(pl.get(3).equals(p));
        Assert.assertTrue(pl.get(4).equals(t));


        pl = new ArrayList<>();
        pl.add(s);
        pl.add(p);
        pl.add(u);
        pl.add(t);
        pl.add(r);  //sort is not dependent on reverse flag

        Collections.sort(pl);
        System.out.println("Sorted list");
        for (Position item : pl){
            System.out.println(item);
        }

        Assert.assertTrue(pl.get(0).equals(u));
        Assert.assertTrue(pl.get(1).equals(s));
        Assert.assertTrue(pl.get(2).equals(p));
        Assert.assertTrue(pl.get(3).equals(r));//sort is not dependent on reverse flag, lower in list as started lower
        Assert.assertTrue(pl.get(4).equals(t));
    }

}
