package net.exylia.exyliaHoppers.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaHoppers.config.ConfigManager;

import java.util.concurrent.*;

@Singleton
public class ThreadPoolManager {
    @Getter
    private final ForkJoinPool forkJoinPool;
    @Getter
    private final ScheduledExecutorService scheduledExecutor;
    @Getter
    private final ExecutorService asyncExecutor;

    @Inject
    public ThreadPoolManager(ConfigManager configManager) {
        int parallelism = configManager.getConfig().getForkJoinParallelism();
        if (parallelism == -1) {
            parallelism = Runtime.getRuntime().availableProcessors();
        }

        this.forkJoinPool = new ForkJoinPool(parallelism);
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, this::createThread);
        this.asyncExecutor = new ThreadPoolExecutor(
                configManager.getConfig().getCorePoolSize(),
                configManager.getConfig().getMaxPoolSize(),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                this::createThread
        );
    }

    private Thread createThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("ExyliaHoppers-Worker-" + thread.getId());
        return thread;
    }

    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, asyncExecutor);
    }

    public <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, asyncExecutor);
    }

    public void shutdown() {
        try {
            scheduledExecutor.shutdownNow();
            asyncExecutor.shutdownNow();
            forkJoinPool.shutdownNow();

            if (!scheduledExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            if (!asyncExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
            if (!forkJoinPool.awaitTermination(2, TimeUnit.SECONDS)) {
                forkJoinPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            asyncExecutor.shutdownNow();
            forkJoinPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
