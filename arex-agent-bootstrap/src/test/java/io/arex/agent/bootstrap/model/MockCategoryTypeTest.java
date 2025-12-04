package io.arex.agent.bootstrap.model;

import io.arex.agent.bootstrap.util.Assert;
import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MockCategoryTypeTest {

    private static volatile boolean isError = false;

    @Test
    public void test_Create() {
        CyclicBarrier barrier = new CyclicBarrier(10);
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new Task(barrier, "test" + i));
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {

            }
        }
        assertFalse(isError, "MockCategoryType create method should not throw exception");
    }

    static class Task implements Runnable {
        private final CyclicBarrier barrier;
        private final String name;

        public Task(CyclicBarrier barrier, String name) {
            this.barrier = barrier;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                barrier.await();
                MockCategoryType.create(name, true, false);
            } catch (InterruptedException | BrokenBarrierException e) {

            } catch (ConcurrentModificationException e) {
                isError = true;
            }
        }
    }
}