package com.kars.reactive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Demo3 {

    public static void main(String[] args) {
        int [] [] arr = new int[] []{{150, 165, 3333}};
        int[][] ints = billing_system(arr);
        System.out.println(Arrays.asList(ints));
    }

    public static int[][] billing_system (int[][] recs) {
        // write code here
        // 房号 费用
        Map<Integer,Integer> map = new HashMap<>();
        for (int[] item : recs) {
            int start = item[0];
            int end = item[1];
            int room = item[2];
            int count = end - start;
            int fee = 0;
            if (count > 12) {
                fee = new BigDecimal(count - 12).divide(new BigDecimal(12), RoundingMode.UP).intValue();
            }
            if (map.containsKey(room)) {
                map.put(room, map.get(room) + fee);
            } else {
                map.put(room, fee);
            }
        }
        int [][] res = new int[map.size()][2];
        List<Map.Entry<Integer, Integer>> setList = new ArrayList<>(map.entrySet());
        for (int i = 0; i < setList.size(); i++) {
            Map.Entry<Integer, Integer> entry = setList.get(i);
            res[i] = new int[]{entry.getKey(), entry.getValue()};
        }
        return res;
    }
}
