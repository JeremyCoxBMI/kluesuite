package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.TableEntry;

/**
 * Created by jwc on 11/14/17.
 */
public class TableEntry2 extends TableEntry{
    public int fastScore;

    /**
     * Move is a ghetto ENUM type C-style, see TableEntry
     * @param move
     * @param score
     * @param fastScore
     */
    public TableEntry2(int move, int score, int fastScore) {
        this.move = move;
        this.score = score;
        this.fastScore = fastScore;
    }

    public TableEntry2(){
        super();
        this.fastScore=0;
    }
}
