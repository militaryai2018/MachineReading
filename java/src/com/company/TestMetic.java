package com.company;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TestMetic {

    @Test
    public void testMetricThread() {
        List<Thread> threadList = new ArrayList<>();
        int THREAD_SIZE = 100;
        for (int i = 0; i < THREAD_SIZE; i++) {
            ScoreThread rt = new ScoreThread("thread " + i);
            Thread thread = new Thread(rt);
            threadList.add(thread);
            thread.start();
        }
        for (int i = 0; i < THREAD_SIZE; i++) {
            Thread rt = threadList.get(i);
            try {
                rt.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finish All Score!!!!!");
    }
}

class ScoreThread implements Runnable {
    private Thread thread;
    private String threadName;

    ScoreThread(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void run() {
        System.out.println("Running " + threadName);

        File refFile = new File("src/data/question.json");
        File candFile = new File("src/data/answer.json");
        ConcurrentHashMap<String, Double> scores = null;
        try {
            scores = Metric.getScore(refFile, candFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        if (scores != null) {
            System.out.println("bleu score: " + scores.get(Metric.BLEU_SCORE));
            System.out.println("rouge score: " + scores.get(Metric.ROUGE_SCORE));
        }

        System.out.println("Exiting " + threadName);
    }
}