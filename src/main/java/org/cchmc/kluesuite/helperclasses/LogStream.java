package org.cchmc.kluesuite.helperclasses;

import org.cchmc.kluesuite.TimeTotals;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;

/**
 * Created by jwc on 3/8/18.
 */
public class LogStream {


    public PrintStream prnt;
    public FileWriter fw;
    public BufferedWriter writer;

    private boolean logging = false;
    TimeTotals tt;

    public static LogStream stdout = null;
    public static LogStream stderr = null;

    /**
     * Path should end with "/" or "." when a filename
     * @param path
     */
    public static void startStdStreams(String path) throws IOException {
//        String stamp = (new Timestamp(System.currentTimeMillis())).toString();
//        stamp = stamp.replace(' ','_');
//        stdout = LogStream.MessageClassBuilder(stamp+".logfile.out.txt", System.out);
//        stderr = LogStream.MessageClassBuilder(stamp+".logfile.err.txt", System.err);


        stdout = LogStream.MessageClassBuilder(path+"logfile.out.txt", System.out);
        stderr = LogStream.MessageClassBuilder(path+"logfile.err.txt", System.err);
    }

    //LogStream.stdout and Logstream.stderr always on
    static {
        startStdStreams();
    }

    /**
     * Path should end with "/" or "." when a filename
     * @param path
     */
    public static void startStdStreamsTimeStamped(String path, String header) throws IOException {
        String stamp = (new Timestamp(System.currentTimeMillis())).toString();
        String stamp2 = stamp.replace(' ','_');
        stdout = LogStream.MessageClassBuilder(path+stamp2+".logfile.out.txt", System.out);
        stderr = LogStream.MessageClassBuilder(path+stamp2+".logfile.err.txt", System.err);
        stdout.println(header);
        stdout.println("STDOUT log started\t"+stamp);
        stderr.println(header);
        stderr.println("STDOUT err started\t"+stamp);
    }


    /**
     * without logging
     */
    public static void startStdStreams(){
        TimeTotals tz = new TimeTotals();
        tz.start();

        if (stdout == null) {
            stdout = logStreamNoLoggingBuilder(System.out, tz);
        }
        if (stderr==null){
            stderr = logStreamNoLoggingBuilder(System.err, tz);
        }
//        int debugger1;
    }


    public static LogStream getStdout(){
        return stdout;
    }

    public static LogStream getStderr(){
        return stderr;
    }


    public LogStream(){


    }

    public static LogStream logStreamNoLoggingBuilder(PrintStream ps, TimeTotals tz){
        LogStream mc = new LogStream();
        mc.logging = false;
        mc.prnt=ps;
        mc.tt = tz;
        return mc;
    }

    public static LogStream MessageClassBuilder(String logfile, PrintStream ps ) throws IOException {

        LogStream mc = new LogStream();
        mc.logging = true;
        mc.prnt=ps;
        mc.fw = new FileWriter(logfile);
        mc.writer = new BufferedWriter(mc.fw);
        return mc;
    }


    //MEMBER FUNCTIONS

    public void printTimeStamped(String s) {
        prnt.print(tt.toHMSuS()+"\t"+s);
        try {
        if (logging){
                writer.write(s);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printlnTimeStamped(String s){
        s += "\n";
        printTimeStamped(s);
    }

    public void println(String s){
        s += "\n";
        print(s);
    }

    public void print(String s) {
        prnt.print(s);
        try {
            if (logging) {
                writer.write(s);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
