package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.Kmer31;

/**
 * Gives stopwatch functionality (start()/end()) as well as secondary timer for specific tasks (total time spent in pause()).
 *
 * 2016-08-15   v2.0    Imported from v1.6 without changes.  Name changed from TimeTotals3 to TimeTotals;
 */
public class TimeTotals {


    //startL and stopL correspond to start() and end() functions
    //startP and stopP correspond to pause() and unPause()
    /**
     * time of start (ns)
     */
    long startL;

    /**
     * time stopped (ns)
     */
    long stopL;


    /**
     * time spent in last pause/unpause
     */
    long diffL;

    /**
     * time last pause began
     */
    long startP;

    /**
     * time last pause ended
     */
    long stopP;

    /**
     * number nanoseconds spent whiel paused
     */
    long pauseTotal;


    /**
     * constructor start() the clock
     * as your option, use start() again to begin your desired time
     */
    public TimeTotals() {
        diffL = 0;
        startL=0;
        stopL=0;
        startP=0;
        stopP=0;
        pauseTotal=0;
        start();
    }

    /**
     * restart the time, resetting all times
     */
    public void restart() {
        reset();
        start();

    }

    /**
     * Resets all values to 0, resetting the clocks
     */
    private void reset() {
        diffL = 0;
        startL=0;
        stopL=0;
        startP=0;
        stopP=0;
        pauseTotal=0;
    }

    /**
     * Text formatting for hours mins secs msecs usecs nsecs
     * Helper function
     *
     * @param ns	time as nanoseconds since start time
     * @return
     */
    public String toHMSuS(long ns) {
        String result ="";
        //hours
        long temp = ns / (60L*60L*1000L*1000L*1000L);
        result += String.format("%03d", (temp))+":";
        //minutes
        temp = ns / (60L * 1000L*1000L*1000L) % 60L;
        result += String.format("%02d", (temp))+":";
        //seconds
        temp = ns / (1000L*1000L*1000L) % 60L;
        result += String.format("%02d", (temp))+":";
        //milliseconds
        temp = ns / (1000L*1000L) % (1000L);
        result += String.format("%03d", (temp))+":";
        //microseconds
        temp = ns / (1000L)% (1000L);
        result += String.format("%03d", (temp))+":";
        //nanoseconds
        temp = ns % (1000L);
        result += String.format("%03d", (temp));

        return result;
    }

    /**
    * Text formatting for hours mins secs msecs usecs nsecs
    */
    public String toHMSuS() {
        return toHMSuS(timePassedFromStart());
    }

    /**
     * display hours, minutes, seconds
     * @return
     */
    public String toHMS() {
        String result ="";
        long ns = timePassedFromStart();
        //hours
        long temp = ns / (60L*60L*1000L*1000L*1000L);
        result += String.format("%03d", (temp))+"H:";
        //minutes
        temp = ns / (60L * 1000L*1000L*1000L) % 60L;
        result += String.format("%02d", (temp))+"M:";
        //seconds
        temp = ns / (1000L*1000L*1000L) % 60L;
        result += String.format("%02d", (temp))+"S";    //+":";

        return result;
    }


    /**
     * Start the clock.
     * May be used after construction.
     * If resetting the clock, use restart() or output is erroneous
     *
     */
    public void start() {
        startL = System.nanoTime();
    }

    /**
     * Stop the clock
     */
    public void stop() {
        stopL = System.nanoTime();
    }

    /**
     * Pauses the end watch, which basically creates an offset to subtract from TotalTime elapsed
     */
    public void pause() {
        startP = System.nanoTime();
    }

    public void unPause() {
        stopP = System.nanoTime();
        pauseTotal += (stopP- startP);
    }

    public String asString() {
        String result;
        stop();
        diffL = stopL - startL;
        result = "\t\ttotal  "+toHMSuS( diffL )+"\t\tunPaused  "+toHMSuS(diffL-pauseTotal)+"\t\tPaused  "+toHMSuS(pauseTotal);
        return result;
    }

    public String toString() {
        return toHMSuS(timePassedFromStart());
    }

    private String columnHeader(){
        return "\t\ttotal  HHH:MM:SS:_mS:_uS:_nS\t\tunPaused  HHH:MM:SS:_mS:_uS:_nS\t\tPaused  HHH:MM:SS:_mS:_uS:_nS";
    }

    /**
     * print directly to system out
     */
    public void systemOutPrintln() {
        //System.out.println(columnHeader()); //removed 2016-08-03
        System.out.println(asString());
    }

    /**
     * largely helper function, returns nanoseconds passed since start
     * disregards and pauses
     * @return
     */
    public long timePassed() {
        long result = (stopL - startL);///1000000000;
        return result;
    }

    public long timePassedFromStart() {
        long temp = System.nanoTime();
        return (temp - startL);
    }

    //seconds elapsed to nearest millisecond
    public Double timePassedFromStartSeconds() {
        long temp = System.nanoTime();
        temp -= startL;
        temp /= 1000000;  //to milliseconds, remainder discarded
        Double r = new Double(temp);
        r /= 1000;
        return r;
    }

    public static void main(String[] args) {

        TimeTotals tt = new TimeTotals();
        tt.start();

        //startL = System.currentTimeMillis();
        //System.out.println("execution begins "+new Timestamp( startL ));

        tt.systemOutPrintln();


        Kmer31 tinyTim = new Kmer31("AGCAGGGGGGCTTATTATTACCCCCCCTGCT");

        for (int k=1; k<1000000; k++){
            int j = k*k + 42%k -1;
            if (k%1000 == 0) {
                System.out.print(k%10);
            }
        }
        System.out.println("");
        tt.systemOutPrintln();

        tt.pause();


        Kmer31 bigTim = new Kmer31("AGCAGGGGGGCTTATTATTACCCCCCCTGCT");


        for (int k=1; k<100000; k++){
            int j = k*k + 42%k -1;
            if (k%1000 == 0) {
                System.out.print(k%10);
            }
        }
        System.out.println("");
        tt.unPause();
        tt.systemOutPrintln();

        tt.stop();
        System.out.println(tt.startL);
        System.out.println(tt.stopL);
        System.out.println(tt.startP);
        System.out.println(tt.stopP);
        tt.systemOutPrintln();
    }
}
