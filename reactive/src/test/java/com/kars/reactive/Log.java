package com.kars.reactive;

import java.util.*;

public class Log {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String rang = scanner.nextLine();
        List<Integer> target = dealRang(rang);
        int steps = scanner.nextInt();
        while (steps-- >= 0){
            int logCount = scanner.nextInt();
            Map<Integer, String> logMap = new TreeMap<>();
            for (int i = 0; i < logCount; i++) {
                String log = scanner.nextLine();
                String[] split = log.split("\\.");
                String a1 = split[0];
                String a2 = split[1];
                logMap.put(Integer.valueOf(a1), log);
                logMap.put(Integer.valueOf(a2), log);
            }
            bfs(logMap, target);
        }
    }

    private static void bfs(Map<Integer, String> logMap, List<Integer> target) {

    }


    private static List<Integer> dealRang(String rang) {
        String[] split = rang.split("\\.");
        int count = Integer.parseInt(split[1]) - Integer.parseInt(split[0]);
        for (int i = 0; i < count; i++) {
        }
        return null;
    }

}
