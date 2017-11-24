package erozic.optimisation.algorithms.metaheuristics.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import erozic.optimisation.algorithms.metaheuristics.ScheduleOSGA;
import erozic.optimisation.solutions.Schedule;
import erozic.optimisation.utils.ScheduleUtil;

/**
 * An abstract genetic algorithm for handling
 * {@link optjava.os_ga.models.Schedule}s as individuals in the search for the
 * optimal schedule.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class ScheduleOSGAConcurrent extends ScheduleOSGA {

	private int numThreads;
	
	private ExecutorService threadPool;
	
	private List<Callable<Schedule>> jobPool;
	
	/** The amount of children created in the current iteration */
	private AtomicInteger currentEffort = new AtomicInteger(0);
	
	/**
	 * Calls the other constructor with maxSteps = Integer.MAX_Value.
	 */
	public ScheduleOSGAConcurrent(int numThreads, ScheduleUtil scheduleHandler, int populationSize, int maxSelPressure,
			double compFactor, double successRatio, int kTour, double mutationRate) {
		super(scheduleHandler, populationSize, maxSelPressure, compFactor, successRatio, kTour, mutationRate,
				Integer.MAX_VALUE);
		this.numThreads = numThreads;
	}

	/**
	 * Sets the number of threads.
	 * 
	 * @see ScheduleOSGA
	 */
	public ScheduleOSGAConcurrent(int numThreads, ScheduleUtil scheduleHandler, int populationSize, int maxSelPressure, double compFactor,
			double successRatio, int kTour, double mutationRate, int maxSteps) {
		super(scheduleHandler, populationSize, maxSelPressure, compFactor, successRatio, kTour, mutationRate, maxSteps);
		this.numThreads = numThreads;
	}
	
	@Override
	protected void algorithmStart() {
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " started on " + numThreads + " threads with parameters:" + " populationSize = " + populationSize
				+ ", maxSelPressure = " + maxSelPressure + ", compFactor = " + compFactor + ", successRatio = "
				+ successRatio + ", kTour = " + kTour + ", mutationRate = " + mutationRate);

		generateInitialPopulation();

		findAndSaveBestSolution();
		
		threadPool = Executors.newFixedThreadPool(numThreads);
		jobPool = prepareJobs();
		
		currSelPressure = 1;
	}
	
	/**
	 * Prepares (populationSize*successRatio) jobs for the threads to execute
	 * asynchronously, guaranteeing that many children in the
	 * {@link #nextGeneration}.
	 */
	private List<Callable<Schedule>> prepareJobs() {

		List<Callable<Schedule>> jobPool = new ArrayList<Callable<Schedule>>(populationSize);
		for (int i = 0; i < populationSize * successRatio; i++) {
			jobPool.add(() -> {
				// this is the job that is done
				return makeAGoodChildIfPossible();
			});
		}
		return jobPool;
	}

	/**
	 * Tries to make a 'good' child until the
	 * {@link OffspringSelection#maxSelPressure} limit isn't reached.
	 */
	private Schedule makeAGoodChildIfPossible() {

		while (true) {

			Schedule[] parents = selection();
			Schedule[] children = crossover(parents[0], parents[1]);

			mutation(children[0]);
			mutation(children[1]);

			scheduleHandler.evaluate(children[0]);
			scheduleHandler.evaluate(children[1]);

			// this is why the parents array has to be sorted
			double minimumFitness = parents[1].getFitness()
					+ (parents[0].getFitness() - parents[1].getFitness()) * compFactor;

			Schedule betterChild;

			if (children[0].getFitness() > children[1].getFitness()) {
				betterChild = children[0];
				synchronized (badChildrenPool) {
					badChildrenPool.add(children[1]);
				}
			} else {
				betterChild = children[1];
				synchronized (badChildrenPool) {
					badChildrenPool.add(children[0]);
				}
			}

			currentEffort.getAndAdd(2);

			if (betterChild.getFitness() > minimumFitness || currentEffort.get() > maxSelPressure * populationSize) {
				return betterChild;
			} else {
				synchronized (badChildrenPool) {
					badChildrenPool.add(betterChild);
				}
			}
		}
	}
	
	@Override
	protected void algorithmStep() {
		
		Random random = ThreadLocalRandom.current();
		
		currentEffort.set(0);

		nextGeneration = new ArrayList<Schedule>(populationSize);
		badChildrenPool = new ArrayList<Schedule>(populationSize);

		try {
			threadPool.invokeAll(jobPool).stream().map(future -> {
				try {
					return future.get();
				} catch (Exception e) {
					throw new IllegalStateException("Something broke in one of the jobs:\n"+ e.getMessage());
				}
			}).forEach(schedule -> {
				nextGeneration.add(schedule);
			});
		} catch (InterruptedException e) {
			throw new IllegalStateException("Something broke while invoking the jobs:\n" + e.getMessage());
		}

		/* the old way (before Java 8 streams and such)
		 * 
		List<Future<Schedule>> runningJobs = null;
		try {
			runningJobs = threadPool.invokeAll(jobPool);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Something broke while invoking the jobs:\n" + e.getMessage());
		}
		for (Future<Schedule> f : runningJobs) {
			try {
				nextGeneration.add(f.get());
			} catch (Exception e) {
				System.err.println("Something broke (" + e.getMessage() + "). Exiting...");
				System.exit(-1);
			}
		}
		*/

		currSelPressure = (double) (nextGeneration.size() + badChildrenPool.size()) / populationSize;
		adjustCompFactor();

		// populate the remainder of the next generation with 'bad' children
		while (nextGeneration.size() < populationSize) {
			nextGeneration.add(badChildrenPool.get(random.nextInt(badChildrenPool.size())));
		}

		activePopulation = nextGeneration;
		
		findAndSaveBestSolution();
		
		fireStateChanged(activePopulation);
		
		if (currSelPressure >= maxSelPressure) {
			this.stop();
		}
	}
	
	@Override
	protected void algorithmEnd() {
		
		threadPool.shutdown();
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}
}
