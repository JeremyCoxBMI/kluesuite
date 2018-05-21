package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.MyFixedBitSet;
import org.junit.Test;

import java.io.*;

import static java.lang.System.exit;

/**
 * Created by osboxes on 13/09/16.
 */
public class MyFixedBitSetTest {

    @Test
    public void testSerializor(){
        MyFixedBitSet input = new MyFixedBitSet(64, 1987123L);
        MyFixedBitSet output = new MyFixedBitSet(0);

        byte[] value = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(input);
            value = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing compressed DNA to database");
            exit(1);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(value);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bis);

            // the serializer is different now
            output = (MyFixedBitSet) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        System.out.println("Compare input then output");
        System.out.println(input.toBinaryString());
        System.out.println(output.toBinaryString());

    }

}
