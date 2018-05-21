package org.cchmc.kluesuite.klue;

import java.util.ArrayList;

/**
 * Created by osboxes on 24/09/16.
 *
 * Prevents massive dynamic resizing for giant input sequences.
 */
public class SuperString {

    public ArrayList<String> strings;
    public ArrayList<Integer> sizes;
    int runningTotal;
    int currRow;

    public SuperString() {
        strings = new ArrayList<String>();
        sizes = new ArrayList<Integer>();
        runningTotal = 0;
        currRow = 0;
    }

    public SuperString(String line) {
        strings = new ArrayList<String>();
        sizes = new ArrayList<Integer>();
        runningTotal = 0;
        currRow = 0;
        strings.add(line);
    }

    /**
     * trims whitespace (leading and trailing) and adds a line
     *
     * @param line
     */
    public void addAndTrim(String line) {
        strings.add(line.trim());
        sizes.add(runningTotal);
        runningTotal += line.length();

    }

    /**
     * Add any char (including whitespace)
     *
     * @param c
     */
    public void add(char c) {
        add(String.valueOf(c));
    }

    /**
     * does not trim whitespace, such as want to keep '\n'
     *
     * @param line
     */
    public void add(String line) {
        strings.add(line);
        sizes.add(runningTotal);
        runningTotal += line.length();
    }

    private int seekIndexRow(int index) {
        int result = -1;
        if (index < runningTotal && index >= 0) {
            int k;
            for (k = 0; k < sizes.size(); k++) {
                if (index < sizes.get(k))
                    return (k - 1);
            }
            //already did boundary checking; it is in the string, last row
            return k;
        } else {
            return -1;
        }
    }

    public char charAt(int index) {
        int subindex = -1;
        if (inRow(index, currRow)) {
            subindex = index - sizes.get(currRow);
        } else if ((currRow + 1) < sizes.size() && inRow(index, currRow + 1)) {
            currRow += 1;
            subindex = index - sizes.get(currRow);
        } else {
            currRow = seekIndexRow(index);
            subindex = index - sizes.get(currRow);
        }

        if (subindex == -1) return (char) 0;
        else return strings.get(currRow).charAt(subindex);
    }


//    public void writeToStream(PrintStream os){
//        for (String s : strings){
//            os.print(s);
//        }
//    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s);
        }
        return builder.toString();
    }

    public String toReverseString() {
        //left = new StringBuilder(left).reverse().toString();
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s);
        }
        return builder.reverse().toString();
    }

    public String get(int from, int to) {
        StringBuilder builder = new StringBuilder();
        for (int k = from; k < to; k++) {
            builder.append(charAt(k));
        }
        return builder.toString();
    }


    private boolean inRow(int index, int row) {
        boolean result = false;
        int maxRow = sizes.size() - 1;
        if (row == maxRow) {
            if (sizes.get(row) <= index && index < runningTotal) {
                result = true;
            }
        } else if (0 <= row && row < maxRow) {
            int currSize = sizes.get(row);
            int nextSize = sizes.get(row + 1);
            if (currSize <= index && index < nextSize) {
                result = true;
            }
        }
        return result;
    }

    public int length() {
        return runningTotal;
    }

}
