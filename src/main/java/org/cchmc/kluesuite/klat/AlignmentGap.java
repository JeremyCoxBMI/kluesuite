package org.cchmc.kluesuite.klat;

/**
 * Created by osboxes on 15/08/16.
 *
 * Basic struct for alignment gaps
 */
public class AlignmentGap extends Gap {

    /**
     * either gap is in the query string (true)  or the reference sequence string (false)
     */
    public boolean gapISquery;


    /**
     *
     * @param position  position in the alignment;
     *                  if alignment portion is length 9, in this case, this ranges 0 to 8
     * @param size
     * @param gapISquery
     */
    public AlignmentGap( int position, int size, boolean gapISquery ){
        pos = position;
        length = size;
        this.gapISquery = gapISquery;
    }

    public String toString(){
        String result;
        if(gapISquery) result = " Query ";
        else result = "RefSeq ";
        result += "at "+pos+" length "+length;
        return result;
    }

}