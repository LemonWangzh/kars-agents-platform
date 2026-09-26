package com.kars.reactive;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {


    public static void main(String[] args) throws InterruptedException {
    }
    static class Solution {

        private final int maxCount; // 最大数字

        private final AtomicInteger atomicInteger = new AtomicInteger(0);
        private final AtomicBoolean aBoolean = new AtomicBoolean(true);

        public Solution(int maxCount) {
            this.maxCount = maxCount;
        }

        // 开启两个线程分别打印奇数和偶数
        public void start() throws InterruptedException {
            // 线程 OddThread 调用 printOdd();
            Thread oddThread = new Thread(() -> {
                try {
                    while (maxCount > atomicInteger.get()){
                        synchronized (aBoolean){
                            if (atomicInteger.get()%2 == 1){
                                printOdd();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "OddThread");

            // 线程 EvenThread 调用 printEven();
            Thread evenThread = new Thread(() -> {
                try {
                    while (maxCount > atomicInteger.get()){
                        synchronized (aBoolean){
                            if (atomicInteger.get()%2 == 0){
                                printEven();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "EvenThread");

            // 启动两个线程
            oddThread.start();
            evenThread.start();

            // 等待子线程执行完成
            oddThread.join();
            evenThread.join();
        }

        public void printOdd() throws InterruptedException {
            int i = atomicInteger.incrementAndGet();
            System.out.println(Thread.currentThread().getName() + i);
        }

        public void printEven() throws InterruptedException {
            int i = atomicInteger.incrementAndGet();
            System.out.println(Thread.currentThread().getName() + i);
        }
    }
}
