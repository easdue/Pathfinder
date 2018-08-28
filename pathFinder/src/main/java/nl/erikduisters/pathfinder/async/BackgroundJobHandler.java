/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.async;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

@Singleton
public class BackgroundJobHandler {
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;

    private final Map<BackgroundJob, Future<?>> jobMap = new HashMap<>();
    private final Object jobMapLock = new Object();

    private final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger threadNum = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "BGThread #" + threadNum.getAndIncrement());
        }
    };

    private final BlockingQueue<Runnable> poolWorkQueue = new LinkedBlockingQueue<>(10);

    private class MyThreadPoolExecutor extends ThreadPoolExecutor {
        MyThreadPoolExecutor(BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);

            if (!(r instanceof Future)) {
                return;
            }

            Future<?> future = (Future<?>) r;

            if (t == null) {
                try {
                    future.get();
                } catch (CancellationException ce) {
                    Timber.d("afterExecute got an CancellationException");
                    //Thread.currentThread().interrupt();
                } catch (ExecutionException ee) {
                    t = ee;
                } catch (InterruptedException ie) {
                    Timber.d("afterExecute got an InterruptedException");
                    Thread.currentThread().interrupt();    // ignore/reset
                }
            }

            if (t != null) {
                BackgroundJobHandler.this.handleUncaughtException(future, t);
            }
        }
    }

    //TODO: Inject
    private final ThreadPoolExecutor threadPool = new MyThreadPoolExecutor(poolWorkQueue, threadFactory);
    private Handler handler;

    @Inject
    BackgroundJobHandler(@Named("MainLooper") Handler handler) {
        this.handler = handler;
    }
    /**
     * Run a new background job
     *
     * @param bgJob The job to be run
     * @return true if the job was excepted for execution, false if not
     */
    //TODO: Call onError instead of returning true of false
    public boolean runJob(BackgroundJob bgJob) {
        Future<?> f;

        bgJob.setBackgroundJobHandler(this);

        try {
            synchronized (jobMapLock) {
                f = threadPool.submit(bgJob);
                jobMap.put(bgJob, f);
            }
        } catch (RejectedExecutionException e) {
            Timber.d("threadPool.submit rejected a background job: %s", e.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Checks if the job is running.
     *
     * @param job The name of the background job
     * @return true if a job with the provided name is already running, false otherwise
     */
    public boolean isRunning(BackgroundJob job) {
        boolean result;

        synchronized (jobMapLock) {
            result = jobMap.containsKey(job);
        }

        return result;
    }

    /**
     * Cancel a job
     *
     * @param job The job to cancel
     * @return true if the job was cancelled successfully, false otherwise
     */
    public boolean cancelJob(BackgroundJob job) {
        boolean res = false;

        synchronized (jobMapLock) {
            if (jobMap.containsKey(job)) {
                Future<?> f = jobMap.get(job);

                res = f.cancel(true);
                threadPool.purge();

                if (!res) {
                    // The job has probably crashed so remove it from the list
                    jobMap.remove(job);
                }
            }
            Timber.d("cancel returned: %s", String.valueOf(res));
        }

        return res;
    }

    private void handleUncaughtException(Future<?> ft, Throwable t) {
        synchronized (jobMapLock) {
            for (Map.Entry<BackgroundJob, Future<?>> pairs : jobMap.entrySet()) {
                Future<?> future = pairs.getValue();

                if (future == ft) {
                    pairs.getKey().onError(t);
                    break;
                }
            }
        }
    }

    void onFinished(BackgroundJob job) {
        synchronized (jobMapLock) {
            jobMap.remove(job);
        }
    }

    void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
