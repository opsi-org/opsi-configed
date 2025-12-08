/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.uib.configed.share.logging.Logging;

/**
 * Provides utility methods to execute tasks in parallel and wait for their
 * completion. This class allows tasks to be executed asynchronously and ensures
 * that the calling thread can wait for all parallel tasks to finish before
 * continuing.
 */
public class ParallelTaskExecutor {
	private static volatile boolean newTasksAllowed = true;
	private static final List<ParallelTaskExecutor> executors = Collections.synchronizedList(new ArrayList<>());

	private final List<CompletableFuture<Void>> futures = new ArrayList<>();
	private ExecutorService executorService = Executors.newCachedThreadPool();

	public ParallelTaskExecutor() {
		executors.add(this);
	}

	/**
	 * Executes a given task in parallel. The task will be executed
	 * asynchronously, allowing for concurrent execution of multiple tasks. The
	 * task will be tracked, and the caller can later wait for its completion.
	 *
	 * @param task The {@link Runnable} task to be executed in parallel.
	 */
	public void runInParallel(Runnable task) {
		if (!newTasksAllowed || executorService.isShutdown()) {
			return;
		}
		CompletableFuture<Void> future = CompletableFuture.runAsync(task, executorService);
		futures.add(future);
	}

	/**
	 * Waits for all parallel tasks to complete. This method blocks the calling
	 * thread until the longest-running task has finished.
	 */
	public void waitForCompletion() {
		if (!futures.isEmpty()) {
			CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
			try {
				allOf.join();
			} catch (CompletionException e) {
				if (e.getCause() instanceof CancellationException) {
					Logging.info("One or more tasks were cancelled as expected.");
				} else {
					throw e;
				}
			}
		}
		executors.remove(this);
		executorService.shutdownNow();
	}

	/**
	 * Cancels all submitted tasks in this executor. Attempts to cancel each
	 * task and initiate an immediate shutdown of the executor service.
	 */
	public void cancelAllTasks() {
		for (CompletableFuture<Void> future : futures) {
			future.cancel(true);
		}
		executorService.shutdownNow();
	}

	/**
	 * Static method to cancel all tasks in all recorded ParallelTaskExecutor
	 * instances.
	 */
	public static void cancelAllExecutorsTasks() {
		synchronized (executors) {
			for (ParallelTaskExecutor executor : executors) {
				executor.cancelAllTasks();
			}
			executors.clear();
		}
		newTasksAllowed = false;
	}

	/**
	 * Re-enables new tasks to be accepted.
	 */
	public static void allowNewTasks(boolean allow) {
		newTasksAllowed = allow;
	}

	/**
	 * Checks if the executor is currently accepting new tasks.
	 * 
	 * @return true if new tasks are allowed, false otherwise.
	 */
	public static boolean isNewTasksAllowed() {
		return newTasksAllowed;
	}
}
