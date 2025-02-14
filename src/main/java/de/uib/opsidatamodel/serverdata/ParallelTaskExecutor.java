/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides utility methods to execute tasks in parallel and wait for their
 * completion. This class allows tasks to be executed asynchronously and ensures
 * that the calling thread can wait for all parallel tasks to finish before
 * continuing.
 */
public class ParallelTaskExecutor {
	private final List<CompletableFuture<Void>> futures = new ArrayList<>();
	private ExecutorService executorService = Executors.newCachedThreadPool();

	/**
	 * Executes a given task in parallel. The task will be executed
	 * asynchronously, allowing for concurrent execution of multiple tasks. The
	 * task will be tracked, and the caller can later wait for its completion.
	 *
	 * @param task The {@link Runnable} task to be executed in parallel.
	 */
	public void runInParallel(Runnable task) {
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
			allOf.join();
		}
		executorService.shutdown();
	}
}
