package org.cchmc.kluesuite.multithread;

import java.util.Random;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

/**
 * Created by jwc on 8/24/17.
 */
public class TestTask implements Callable<Integer>{

    int answer;
    long seed;
    public static int max = 1000;

    TestTask(long seed){
        answer = new Random(seed).nextInt(max);
        this.seed = seed;
    }

    @Override
    public Integer call() throws Exception {
        sleep(5*answer);
        System.err.println("seed\t"+seed+"\tslept\t"+answer);
        return answer;
    }
}
