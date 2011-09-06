package com.shadowcraft.android;

import core.util.Tuple_2;

public class Stat extends Tuple_2<Object, Object>{

    public Stat (int first, double second) {
        super(first, second);
    }

    public int getId() {
        return (Integer) super.getFirst();
    }

    public double getValue() {
        return  (Double) super.getSecond();
    }

}
