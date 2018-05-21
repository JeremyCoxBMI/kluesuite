package org.cchmc.kluesuite.datastreams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by jwc on 8/3/17.
 */
public class FastaFile {

    String filename;

    public FastaFile(String filename){this.filename = filename;}

    public Iterator<FastaSequence> sequenceIterator(){
        return new myIterator(filename);
    }

    public static final class myIterator implements Iterator<FastaSequence> {
        String nextQuery;
        BufferedReader br;
        FastaSequence next;
        String line;

        public myIterator(String filename) {

            try {
                br = new BufferedReader(new FileReader(filename));
                nextQuery = br.readLine();
                if (nextQuery != null) nextQuery=nextQuery.trim();
            } catch (IOException e) {
                e.printStackTrace();
            }

            pullNext();
        }

        //sets up next for next iteration
        private void pullNext() {

            next = new FastaSequence();
            next.query = nextQuery;


            //if line is null, no worries, still null
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (line != null && !line.substring(0,1).equals(">")) {
                next.sequence.addAndTrim(line.trim());
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //nextQuery = br.readLine();
            nextQuery = line;
            if (nextQuery != null) nextQuery=nextQuery.trim();


            return; //pullNext()
        }

        @Override
        public boolean hasNext() {
            return next.query != null;
        }

        @Override
        public FastaSequence next() {
            FastaSequence result = next;
            pullNext();
            return result;
        }

        @Override
        public void remove() {

        }
    }

}
