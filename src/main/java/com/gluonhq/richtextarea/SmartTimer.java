package com.gluonhq.richtextarea;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

class SmartTimer {

    private Timer timer;
    private final Runnable task;
    private final long delay;
    private final long period;

    public SmartTimer( Runnable task, long delay, long period) {
        this.task = Objects.requireNonNull(task);
        this.delay = delay;
        this.period = period;
    }

    public void pause() {
        if ( timer != null ) {
            timer.cancel();
            timer = null;
        }
    }

    public void start( ) {
        if ( timer == null ) {
            timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    task.run();
                }
            };
            timer.scheduleAtFixedRate( timerTask, delay, period);
        }
    }


}
