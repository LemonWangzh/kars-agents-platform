package com.kars.reactive;

import java.util.*;

public class Demo2 {

    public static void main(String[] args) {
        int [] arr = new int[]{1,2,3,4,5,6,7,8,5,4,3,2,1};
        long l = maxSumForMilk(arr, 4);
        System.out.println(l);
    }

    public static long maxSumForMilk (int[] numbers, int k) {
        // write code here
        if (k == 0) {
            return 0;
        }
        if (k >= numbers.length){
            return Arrays.stream(numbers).sum();
        }else {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i <= numbers.length - k; i++) {
                int sum = 0;
                for (int j = 0; j < k; j++) {
                    sum = numbers[i+j] + sum;
                }
                map.put(i, sum);
            }
            List<Integer> list = new ArrayList<>();
            map.forEach((s,v)-> list.add(v));
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(i) > list.get(j)){
                        int temp = list.get(i);
                        list.set(i, list.get(j));
                        list.set(j, temp);
                    }
                }
            }
            return list.get(list.size() - 1);
        }
    }
}
