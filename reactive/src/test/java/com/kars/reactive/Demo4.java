package com.kars.reactive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo4 {
    public static void main(String[] args) {
        //[ [A,B,C,E], [S,F,C,S], [A,D,E,E] ],"ABCCED"
        char [][] board = new char[][]{{'A','B','C','E'}, {'S','F','C','S'},{'A','D','E','E'}};
        var word = "ABCCED";
        int i = count_word_occurrences(board, word);
        System.out.println(i);
    }

    public static int count_word_occurrences (char[][] board, String word) {
        // write code here
        // 1.找到网络中开始字母位置 (k,v) 集合可能多个，需要实现一个查找函数，入参数坐标，下一目标k，出参数 空数组 or 下一目标坐标
        // 2.上下左右路径查询符合 target[step] 目标步数的值，完全路径查找。
        // 3.统计完成全部路径个数
        char[] targetChar = word.toCharArray();
        // 查询全部符合开始条件的坐标
        Map<Integer, Integer> match_start_map = findMatchStart(board, targetChar[0]);
        if (match_start_map.isEmpty()){
            return 0;
        }
        AtomicInteger count = new AtomicInteger();
        match_start_map.forEach((k,v)-> {
            Integer search_step = 1;
            deep_search_route(board,k,v,search_step, targetChar);
            if (search_step == word.length() - 1){
                count.getAndIncrement();
            }
        });
        return count.get();
    }

    // 路径探查上下左右，上：(x-1,y) 下：（x+1,y）左: (x, y-1) 右 (x,y+1)
    private static void deep_search_route(char[][] board, Integer x, Integer y, Integer target_char_index, char[] targetChar) {
        if (target_char_index > targetChar.length - 1){
            return;
        }
        if (x-1 >= 0){
            char c = board[x - 1][y];
            if (c == targetChar[target_char_index]){
                target_char_index ++ ;
                deep_search_route(board, x-1,y,target_char_index, targetChar);
            }
        }else if (x + 1 < board.length){
            char c = board[x + 1][y];
            if (c == targetChar[target_char_index]){
                target_char_index ++ ;
                deep_search_route(board, x+1,y,target_char_index, targetChar);
            }
        }
        else if (y - 1 > 0){
            char c = board[x][y - 1];
            if (c == targetChar[target_char_index]){
                target_char_index ++ ;
                deep_search_route(board, x,y - 1,target_char_index, targetChar);
            }
        }
        else if (y + 1 < board[0].length){
            char c = board[x][y + 1];
            if (c == targetChar[target_char_index]){
                target_char_index ++ ;
                deep_search_route(board, x,y + 1,target_char_index, targetChar);
            }
        }
    }

    private static Map<Integer, Integer> findMatchStart(char[][] board, char c) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            char[] chars = board[i];
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == c){
                    map.put(i, j);
                }
            }
        }
        return map;
    }
}
