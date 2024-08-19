package co.casterlabs.caffeinated.core.launcher;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import co.casterlabs.caffeinated.core.launcher.AppInterface.DummySaucerRunnable;
import co.casterlabs.commons.async.queue.ExecutionQueue;
import co.casterlabs.saucer.Saucer;
import lombok.Getter;

@SuppressWarnings("unchecked")
public class MainThread implements ExecutionQueue {
    private static @Getter MainThread instance;

    private Saucer currentWebview;

    public final Thread thread;
    private final Object logicLock = new Object();
    private final Deque<Runnable> taskQueue = new ArrayDeque<>();

    MainThread(Runnable continueAt) {
        assert instance == null : "Cannot create another main thread.";
        instance = this;

        this.thread = Thread.currentThread();
        this.execute(continueAt);

        while (true) {
            while (!this.taskQueue.isEmpty()) {
                Runnable popped = this.taskQueue.pop();

                if (popped instanceof DummySaucerRunnable) {
                    try {
                        this.currentWebview = ((DummySaucerRunnable) popped).saucer;
                        this.taskQueue.forEach(this.currentWebview.window()::dispatch); // Submit everything to the webview.
                        this.taskQueue.clear(); // Clear the backlog, since we'll no longer have access to this thread.

                        Saucer.run();
                    } finally {
                        this.currentWebview.close();
                        this.currentWebview = null;
                        AppInterface.onUIClose();
                    }
                    break;
                } else {
                    try {
                        popped.run();
                    } catch (Throwable t) {
                        System.err.println("An exception occurred whilst processing task in the queue:");
                        t.printStackTrace();
                    }
                }
            }

            synchronized (this.logicLock) {
                try {
                    Thread.yield(); // The thread may lie dormant for a while.
                    this.logicLock.wait(); // Sleep until we get another task.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void execute(Runnable task) {
        if (task instanceof DummySaucerRunnable) {
            if (this.currentWebview != null) {
                throw new IllegalStateException("You cannot run more than one webview.");
            }
            this.taskQueue.push(task); // avoid wrapping as a Supplier<Void>.
        } else {
            ExecutionQueue.super.execute(task);
        }
    }

    @Override
    public <T> T execute(Supplier<T> task) {
        // Yucky pointer code.
        Object[] $t = new Object[1];

        if (this.currentWebview == null) {
            this.taskQueue.push(() -> {
                $t[0] = task.get();
            });
            synchronized (this.logicLock) {
                this.logicLock.notifyAll();
            }
        } else {
            this.currentWebview.window().dispatch(() -> {
                $t[0] = task.get();
            });
        }

        return (T) $t[0];
    }

}
