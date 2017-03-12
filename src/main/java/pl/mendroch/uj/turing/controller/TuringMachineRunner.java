package pl.mendroch.uj.turing.controller;

import com.sun.javafx.application.PlatformImpl;
import lombok.extern.java.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Log
public class TuringMachineRunner implements Runnable {
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final OperatingTuringMachine machine;
    private CountDownLatch manualLatch = new CountDownLatch(1);
    private CountDownLatch runnerLatch = new CountDownLatch(1);

    public TuringMachineRunner(OperatingTuringMachine machine) {
        this.machine = machine;
        machine.initialize();
    }

    @Override
    public void run() {
        log.info("Turing machine started");
        try {
            while (running.get()) {
                log.info("loop begin");
                if (machine.isManual()) {
                    log.info("manual wait");
                    manualLatch.await();
                    manualLatch = new CountDownLatch(1);
                }
                if (running.get()) {
                    PlatformImpl.runAndWait(machine::step);
                }
                if (!machine.isManual()) {
                    log.info("sleep");
                    runnerLatch.await((long) machine.getStepTime().get(), TimeUnit.MILLISECONDS);
                    runnerLatch = new CountDownLatch(1);
                }
            }
        } catch (InterruptedException e) {
            log.warning(e.getMessage());
        }
    }

    public void stop() {
        log.info("stop");
        running.set(false);
        manualLatch.countDown();
        runnerLatch.countDown();
    }

    public void stepBack() {
        log.info("step back");
        PlatformImpl.runAndWait(machine::stepBack);
    }

    public void step() {
        log.info("step");
        manualLatch.countDown();
    }

    public void stepRunner() {
        log.info("step runner");
        manualLatch.countDown();
    }
}
