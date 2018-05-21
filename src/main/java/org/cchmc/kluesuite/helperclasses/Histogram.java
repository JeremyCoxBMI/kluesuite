package org.cchmc.kluesuite.helperclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jwc on 4/27/18.
 */
public class Histogram {

    Map<MyInteger,MyInteger> map;

    public Histogram(){
        map = new HashMap<MyInteger, MyInteger>();
    }

    public void put(int frequency){
        MyInteger a = new MyInteger(frequency);
        if (map.containsKey(a)){
            map.put(a, map.get(a).addOne());
        } else {
            map.put(a, new MyInteger(1));
        }
    }

    public int get(int frequency){
        return map.get(new MyInteger(frequency)).toInt();
    }

    public int get(MyInteger frequency){
        return map.get(frequency).toInt();
    }

    public Iterator<MyInteger> freqIterator(){
        return new ArrayList(map.keySet()).iterator();
    }

}
