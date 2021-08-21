package com.lenis0012.pluginutils.task;

import java.util.concurrent.Executor;

public interface TaskExecutor {

    Executor sync();

    Executor async();

    TaskExecutor BLOCKING = new TaskExecutor() {
        @Override
        public Executor sync() {
            return Runnable::run;
        }

        @Override
        public Executor async() {
            return Runnable::run;
        }
    };

}
