package org.cchmc.kluesuite.helperclasses;

import java.util.Comparator;

/**
 * Created by jwc on 4/27/18.
 */
public class MyInteger implements Comparable<MyInteger>,Comparator<MyInteger>{
    int x;

    public MyInteger(int i){
        x=i;
    }

    @Override
    public int compareTo(MyInteger o) {
        return 0;
    }

    @Override
    public int compare(MyInteger o1, MyInteger o2) {
        return 0;
    }

    public int hashCode(){
        return x;
    }

    @Override
    public boolean equals(Object o){
        MyInteger z = (MyInteger) o;
        return x == z.toInt();
    }

    public int toInt(){
        return x;
    }

    public MyInteger addOne(){
        x++;
        return this;
    }
}
