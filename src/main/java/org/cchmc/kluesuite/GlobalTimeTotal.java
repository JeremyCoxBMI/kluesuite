package org.cchmc.kluesuite;

/**
 * Gives stopwatch functionality (start()/end()) as well as secondary timer for specific tasks (total time spent in pause()).
 *
 * 2017-06-22   Created global class
 */
public class GlobalTimeTotal {

    private static TimeTotals tt;

    static {
        tt = new TimeTotals();
    }

    public static String toHMSuS() {
        return tt.toHMSuS();
    }

    public static String toHMS() {
        return tt.toHMS();
    }



    public static void start() {
        tt.start();
    }

    public static void stop() {
        tt.stop();
    }

    public static void pause() {
        tt.pause();
    }

    public static void unPause() {
        tt.unPause();
    }

    public static String asString() {
        return tt.toString();
    }

    public static String string() {
        return tt.toHMSuS();
    }

    public static void systemOutPrintln() {
        System.out.println(asString());
    }

    public static long timePassed() {
        return tt.timePassed();
    }

    public static long timePassedFromStart() {
        return tt.timePassedFromStart();
    }

}
