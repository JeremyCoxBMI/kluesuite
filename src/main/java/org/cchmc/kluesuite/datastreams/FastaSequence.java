package org.cchmc.kluesuite.datastreams;

import org.cchmc.kluesuite.klue.SuperString;

/**
 * Created by jwc on 8/3/17.
 */
public class FastaSequence {

    public String query;
    public SuperString sequence;


    public FastaSequence(){
        query = null;
        sequence= new SuperString();
    }

    public void seqQuery(String q){
        query = q;
    }

    public void addLine(String line){
        sequence.addAndTrim(line);  //filters out endl
    }
}
