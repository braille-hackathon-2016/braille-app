package com.hackathon.westhill.hackathonbrailleinput;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amish on 04/08/2016.
 */
public class BrailleMap {

    private static HashMap<String, String> map;

    public static void init() {
        map = new HashMap<String, String>();
        map.put("100000", "A");
        map.put("101000", "B");
        map.put("110000", "C");
        map.put("110100", "D");
        map.put("100100", "E");
        map.put("111000", "F");
        map.put("111100", "G");
        map.put("101100", "H");
        map.put("011000", "I");
        map.put("011100", "J");
        map.put("100010", "K");
        map.put("101010", "L");
        map.put("110010", "M");
        map.put("110110", "N");
        map.put("100110", "O");
        map.put("111010", "P");
        map.put("111110", "Q");
        map.put("101110", "R");
        map.put("011010", "S");
        map.put("011110", "T");
        map.put("100011", "U");
        map.put("101011", "V");
        map.put("011101", "W");
        map.put("110011", "X");
        map.put("110111", "Y");
        map.put("100111", "Z");
    }

    public static String resolve(String chars) {
        return map.get(chars);
    }

}
