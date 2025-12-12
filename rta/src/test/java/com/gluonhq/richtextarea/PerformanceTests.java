/*
 * Copyright (C) 2025 Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gluonhq.richtextarea;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author johan
 */
public class PerformanceTests {

    Stage stage;

    public static void main(String[] args) throws InterruptedException {
        PerformanceTests pt = new PerformanceTests();
        pt.insertMany();
    }

    /**
     * This test measures performance of the combination {add character; render UI}
     * It is important to measure the combination, as a performance enhancements in
     * one part could lead to a degradation in the other part.
     * In this test, after a warmup of 1000 characters, we add 1000 characters (to the
     * already existing 1000 ones). After a character is added, a Pulse is requested.
     * Ideally, we render at 60 FPS and a character insert takes much less than 16ms,
     * so that not every requestPulse results in a separate pulse.
     * We use a prelayoutListener to keep track of how many pulses are executed, and use
     * that to calculate the FPS.
     * We also calculate the average time that it takes to add and render a character. This
     * number needs to be interpreted with caution, as it strongly depends on the performance of the
     * individual parts, and on the number of additions that can be done within 16ms.
     *
     * Note that currently lots of Nodes are created in both parts, and GC activity is expected,
     * which can influence the measured values.
     *
     * Run this test before and after a PR, to check for regression.
     * @throws InterruptedException
     */
    public void insertMany() throws InterruptedException {
        AtomicInteger pulseCounter = new AtomicInteger(0);
        Runnable prelayout = () -> pulseCounter.incrementAndGet();
        final int WARMUP_CNT = 1000; // how many chars to warmup
        final int TEST_CNT = 1000; // how many chars to test
        final int SLEEP_MS = 5000; // sleep between warmup and test
        CountDownLatch cdl = new CountDownLatch(1);
        Platform.startup(() -> {
            this.stage = new Stage();
            cdl.countDown();
        });
        cdl.await(1, TimeUnit.SECONDS);
        RichTextArea rta = new RichTextArea();
        CountDownLatch cdl2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            Scene scene = new Scene(new StackPane(rta));
            stage.setScene(scene);
            stage.show();
            cdl2.countDown();
        });
        cdl2.await(1, TimeUnit.SECONDS);
        RichTextAreaSkin skin = (RichTextAreaSkin) rta.getSkin();
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        System.err.println("Will test inserts of " + TEST_CNT + " after warmup of " + WARMUP_CNT + " chars, and sleep of " + SLEEP_MS + " ms.");

        long startTime = System.nanoTime();
        for (int i = 0; i < WARMUP_CNT; i++) {
            char c = (char) ('a' + tlr.nextInt(26));
            if (tlr.nextInt(10) > 8) {
                c = ' ';
            }
            String k = String.valueOf(c);
            KeyEvent evt = new KeyEvent(KeyEvent.KEY_TYPED, k, "", KeyCode.UNDEFINED, false, false, false, false);
            CountDownLatch testRun = new CountDownLatch(1);
            Platform.runLater(() -> {
                skin.keyTypedListener(evt);
                Platform.requestNextPulse();
                testRun.countDown();
            });
            boolean await = testRun.await(1, TimeUnit.SECONDS);
            if (!await) {
                System.err.println("ERROR warming up");
                System.exit(1);
            }
        }
        CountDownLatch cdl3 = new CountDownLatch(1);
        Platform.runLater(() -> cdl3.countDown());
        cdl3.await(1, TimeUnit.SECONDS);
        long endTime = System.nanoTime();
        long dur = endTime - startTime;
        System.err.println("warmup: total time = " + dur + ", average = " + (dur / (1e6 * WARMUP_CNT)) + ", now sleep for " + SLEEP_MS);
        Thread.sleep(SLEEP_MS);
        System.err.println("resume");
        pulseCounter.set(0);
        Platform.runLater(() -> rta.getScene().addPreLayoutPulseListener(prelayout));

        startTime = System.nanoTime();
        final int cnt = TEST_CNT;
        for (int i = 0; i < cnt; i++) {
            char c = (char) ('a' + tlr.nextInt(26));
            if (tlr.nextInt(10) > 8) {
                c = ' ';
            }
            String k = String.valueOf(c);
            KeyEvent evt = new KeyEvent(KeyEvent.KEY_TYPED, k, "", KeyCode.UNDEFINED, false, false, false, false);
            CountDownLatch prodRun = new CountDownLatch(1);
            Platform.runLater(() -> {
                skin.keyTypedListener(evt);
                Platform.requestNextPulse();
                prodRun.countDown();
            });
            boolean await = prodRun.await(1, TimeUnit.SECONDS);
            if (!await) {
                System.err.println("ERROR running test");
                System.exit(1);
            }
        }
        CountDownLatch cdl4 = new CountDownLatch(1);
        Platform.runLater(() -> cdl4.countDown());
        cdl4.await(10, TimeUnit.SECONDS);
        endTime = System.nanoTime();
        dur = endTime - startTime;
<<<<<<< HEAD
        long mem1 = getUsedMemory();
        System.err.println("total time = " + dur + ", average = " + (dur / (1e6 * cnt)) + " and used mem = " + (mem1 - mem0));
        Thread.sleep(SLEEP_MS);
        Platform.exit();
    }

    long getUsedMemory() {
        System.gc();
        try {
            Thread.sleep(100);
            System.gc();
            Thread.sleep(1400);
        } catch (InterruptedException ex) {
            System.getLogger(PerformanceTests.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        Runtime runtime = Runtime.getRuntime();
        long val = runtime.totalMemory() - runtime.freeMemory();
        return val;
    }

=======
        System.err.println("RefreshRate = "+1.e9*pulseCounter.get()/dur +" FPS");
        System.err.println("Average duration of a character addition = " + (dur / (1e6 * cnt))+"ms");
        Platform.exit();
    }

>>>>>>> main
}
