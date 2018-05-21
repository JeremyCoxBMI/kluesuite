package org.cchmc.kluesuite.klat;

/**
 * Helper class to SmithWatermanOriginal and also SmithWatermanAdvanced
 *
 * 2016-08-15   v2.0    Imported without modification from v1.6.
 *
 * Struct to store ideal movement entry in alignment table.
 * (Two pieces of data)
 */
public class TableEntry {

    public static int EMPTY = 0;
    public static int RIGHT = 1;
    public static int DOWN = 2;
    public static int DIAGONAL = 3;
    public static int TIE = RIGHT;     //by this program's convention, longest is across top, thus tie moves right

    public int move;
    public int score;

    public TableEntry() {
        move = DIAGONAL;
        score = 0;
    }

    public TableEntry(int move) {
        this.move = move;
        score = 0;
    }
}
