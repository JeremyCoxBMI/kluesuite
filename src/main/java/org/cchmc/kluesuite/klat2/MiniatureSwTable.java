package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klue.DNAcodes;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * Created by jwc on 4/11/18.
 *
 * Issue #97 :: TODO
 *
 * This is basically a minimal pointer struct to represent a table for more efficient memory access speed
 */
public class MiniatureSwTable {

    int[][] moves;
    int[][] scores;
    int[][] fastScores;
    int[][] initialized;
    int first_row_col;

    boolean winnersCalculated;
    ArrayList<PairRC> winners;
    int num_rows;
    int num_cols;

    public MiniatureSwTable(int rows, int cols, int firstRowCol){
        moves = new int[rows][cols];
        scores = new int[rows][cols];
        fastScores = new int[rows][cols];
        initialized = new int[rows][cols];  //defaults to 0, so false
        first_row_col = firstRowCol;
        winners = new ArrayList<PairRC>();
        winnersCalculated = false;
        num_rows = rows;
        num_cols = cols;
    }

    /**
     * No value returns  TableEntry2.EMPTY
     * @param r
     * @param c
     * @return
     */
    public Integer getMove(int r, int c){
        if (r<0 || c < 0)   return TableEntry2.EMPTY;


        if (initialized[r][c] > 0){
            return moves[r][c];
        } else {
            return null;
        }
//        return TableEntry2.EMPTY;
    }

    public Integer getScore(int r, int c){

        if (r<0 || c < 0)   return 0;

        //uninitialized, return null

        if (initialized[r][c] > 0){
            return scores[r][c];
        } else {
            return null;
        }
//        return Integer.MIN_VALUE;
    }

    public Integer getFastScore(int r, int c){
        if (r<0 || c < 0)   return 0;


        if (initialized[r][c] > 0){
            return fastScores[r][c];
        } else {
            if (r < first_row_col || c < first_row_col)
                return 0;
            else
                return null;
        }
//        return Integer.MIN_VALUE;
    }

    public void setEntry(int r, int c, int move, int score, int fastScore){
        moves[r][c] = move;
        scores[r][c] = score;
        fastScores[r][c] = fastScore;
        initialized[r][c] = 1;
    }


    public void setScore(int r, int c, int i) {
        scores[r][c] = i;
    }
//
//    public void setFastScore(int r, int c, int i) {
//        fastScores[r][c] = i;
//    }
//
//    public void setMove(int r, int c, int i) {
//        moves[r][c] = i;
//    }






    /**
     *
     * @param k     row coordinate
     * @param j     col coordinate
     * @param LAST_ROW_CALCULATE    from construct table; determines proper edge
     * @param LAST_COL_CALCULATE    from construct table; determines proper edge
     * @param leftCap   is first column & first row calculated (left most sub-table)
     * @param rightCap  is last column &  last  row calculated (right most sub-table)
     * @return
     */
    public void writeBestMove(int k, int j, int LAST_ROW_CALCULATE, int LAST_COL_CALCULATE, boolean match, boolean leftCap, boolean rightCap){
        //pre-existing values
        //return null if non-existant
        Integer diagonal = null;
        Integer down = null;
        Integer right = null;

        diagonal = getScore(k-1,j-1);
        down = getScore(k - 1, j);
        right = getScore(k, j - 1);

        //convert from relative 0 coordinates to actual query/reference String coordinates
        if (diagonal != null) {
            //DNAcodes.equals(cols.charAt(j + scol), rows.charAt(k + srow))
            if (match) {
                //ends of strings cannot be a match
                if ((!leftCap && (k == 0) && (j == 0))
                        ||
                        (!rightCap && (k == LAST_ROW_CALCULATE) && (j == LAST_COL_CALCULATE))
                        ) {
                    diagonal += SmithWatermanTruncated2.MISMATCH;
                } else {
                    diagonal += SmithWatermanTruncated2.MATCH;
                }
            } else {
                diagonal += SmithWatermanTruncated2.MISMATCH;
            }
        } else {
            //illegal move; path must connect to top left
            diagonal = Integer.MIN_VALUE;  //will not win
        }
        if (down != null) {
            down += SmithWatermanTruncated2.GAP;
        } else {
            //illegal move; path must connect to top left
            down = Integer.MIN_VALUE;  //will not win
        }
        if (right != null) {
            right += SmithWatermanTruncated2.GAP;
        } else {
            //illegal move; path must connect to top left
            right = Integer.MIN_VALUE;  //will not win
        }

        writeBestMove(k, j, match, diagonal, down, right);
    }


    /**
     * records the best move for the position indicated by coordinate (r,c) to the tableEntry
     * constructs the tableEntry2, using branching logic to determine which is best
     *
     * @param r
     * @param c
     * @param match
     * @param diagonal
     * @param down
     * @param right
     */
    public void writeBestMove(int r, int c, boolean match, int diagonal, int down, int right) {
        //DEFAULT MOVE IS DOWN
        //IN CASE OF TIE, use diagonal move for score -- could be a match
        int move, score, fastScore;
        Integer tmp;
//        if (right == -3)
//            "DEBUG".equals("HERE");
        if (diagonal == down && down == right) {
            //three way tie
            move = TableEntry2.TIE;
            score = diagonal;
            tmp = getFastScore(r - 1, c - 1);
            if (tmp != null)
                fastScore = tmp;
            else {
                System.out.println("WARN "+r+" "+c);
                fastScore = 0; //minimum
            }
            if (match) {
                fastScore++;  //+1 for match
                move = TableEntry2.DIAGONAL;
            }

        } else if (diagonal >= down && diagonal >= right) {
            move = TableEntry2.DIAGONAL;
            score = diagonal;
            tmp = getFastScore(r - 1, c - 1);  //+1 for match
            if (tmp != null)
                fastScore = tmp;
            else
                fastScore = 0; //minimum
            if (match) {
                fastScore++;
            }

        } else {

//            diagonal = getScore(k-1,j-1);
//            down = getScore(k - 1, j);
//            right = getScore(k, j - 1);


            //then down or right is winner
            if (right == down) {
                move = TableEntry2.DOWN;
                score = right;
                tmp = getFastScore(r-1, c);
            } else if (right > down) {
                move = TableEntry2.RIGHT;
                score = right;
                tmp = getFastScore(r, c-1);
            } else {  // right < down
                move = TableEntry2.DOWN;
                score = down;
                tmp = getFastScore(r-1, c);

            }
            if (tmp != null)
                fastScore = tmp;
            else {
                System.out.println("WARN " + r + " " + c);
                fastScore = 0; //minimum
            }
        }

        setEntry(r, c, move, score, fastScore);
    }

    /**
     * Only when the last position is open to debate (that is, rightmost Box2 in alignment) should this be called
     * @return
     */
    public ArrayList<PairRC> getWinners(int srow, int scol){
        int maxSW = Integer.MIN_VALUE;
        if (!winnersCalculated){
            int r,c;
            c = num_cols -1;
            for (r = 0; r < num_rows; r++){
                if (initialized[r][c] > 0 && (scores[r][c] > maxSW)){
                    maxSW = scores[r][c];
                }
            }
            r = num_rows - 1;
            for (c=0; c < num_cols;c++){
                maxSW = scores[r][c];
            }

            c = num_cols -1;
            for (r = 0; r < num_rows; r++){
                if (initialized[r][c] > 0 && (scores[r][c] == maxSW)){
                    winners.add(new PairRC(r,c));
                }
            }
            r = num_rows - 1;
            for (c=0; c < num_cols;c++){
                if (initialized[r][c] > 0 && (scores[r][c] == maxSW)){
                    winners.add(new PairRC(r+srow,c+scol));
                }
            }

            winnersCalculated = true;
        }
        return winners;
    }


}
