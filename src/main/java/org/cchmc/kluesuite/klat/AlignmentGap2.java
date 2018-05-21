package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.SuperString;

/**
 * Created by jwc on 8/27/17.
 */
public class AlignmentGap2 extends AlignmentGap {

    public int refPos;
    public int qPos;

    public AlignmentGap2(int position, int refPosition, int queryPosition, int size, boolean gapISquery ) {
        super(position, size, gapISquery);
        refPos = refPosition;
        qPos = queryPosition;
    }

    @Override
    public String toString(){
        SuperString result = new SuperString();
        result.add( "AlignmentKLAT1 pos ");
        result.add(Integer.toString(pos));
        result.add(" length ");
        result.add(Integer.toString(length));
        if(gapISquery){
            result.add(" Query ");
            result.add(Integer.toString(qPos));
        }
        else{
            result.add(" RefSeq ");
            result.add(Integer.toString(refPos));
        }

        return result.toString();
    }
}
