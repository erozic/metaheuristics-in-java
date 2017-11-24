package erozic.optimisation.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;

import erozic.optimisation.solutions.Solution;

/**
 * This (abstract) class offers the possibility for an (optimisation) algorithm
 * to be run in a separate thread while offering it the ability to be paused,
 * continued and stopped in a thread-safe and meaningful (algorithm-wise) way.
 * 
 * It also offers the possibility for an algorithm to be monitored while
 * executing and for it to notify via listeners about the current (after each
 * step) and final best solutions. These possibilities are like the
 * "publish/process" and "done" functionalities of the SwingWorker class, but
 * their implementation relies not only on the implementation of listeners by
 * the caller but also on the implementation of the abstract methods by the
 * inheriting class where it should call the
 * {@link #fireCurrentBestSolutionUpdated(Object)} and
 * {@link #fireFinalSolutionSet(Object)} methods as the equivalent of
 * implementing the "publish" and "call" (instead of just "run") methods.
 * 
 * It can be used as a kind of SwingWorker if needed for a GUI-based
 * application, but it can also be used for a console application or any other
 * kind of used interaction, but it is also appropriate to use without any user
 * interaction or multi-threading since the overhead for those functionalities
 * is very minimal.
 * 
 * The template class T should be the model of the solution, for example an
 * "Individual" for a Genetic algorithm or an "Ant" for an ACO algorithm etc.
 * 
 * @author Eugen Rožić
 *
 */
public abstract class OptimisationAlgorithm<T extends Solution> implements Runnable {
	
	/** The maximum number of steps the algorithm will run through */
	protected int maxSteps = -1;
	/** The current step */
	protected int currentStep;
	
	protected T currentBestSolution = null;
	
	public T getCurrentBestSolution() {
		return currentBestSolution;
	}
	
	volatile private boolean started = false;
	volatile private boolean stopped = false;
	volatile private boolean paused = false;
	
	/**
	 * @param maxSteps The maximum number of steps before the algorithm stops regardless
	 * of whether {@link #stop()} was called or some other criterion is reached.
	 */
	public OptimisationAlgorithm(int maxSteps) {
		this.maxSteps = maxSteps;
	}
	
	/**
	 * Implements the general structure of the running of an optimisation algorithm. This
	 * should NOT be overriden in the inheriting class unless one knows exactly what one is
	 * doing. Instead, the protected methods {@link #algorithmStart()},
	 * {@link #algorithmStep()} and {@link #algorithmEnd()} should implement all the
	 * specifics of a concrete algorithm.
	 */
	@Override
	public void run() {
		started = true;
		algorithmStart();
		
		currentStep = 0;
		while ( !stopped && currentStep < maxSteps){
			currentStep++;
			algorithmStep();
			waitIfPaused();
		}
		algorithmEnd();
		stopped = true;
	}
	
	/**
	 * Whatever needs to be done before the algorithm starts with its steps, e.g.
	 * setting some initial values, finding an initial best solution, writing out something
	 * to the user ...
	 */
	abstract protected void algorithmStart();
	
	/**
	 * This is where the body of the algorithm should be implemented, along with any
	 * reporting to the user such as using the {@link #fireCurrentBestSolutionUpdated(Solution)}
	 * method etc.
	 */
	abstract protected void algorithmStep();
	
	/**
	 * Whatever should be done after the algorithm has ended, such as finding the best
	 * solution and setting it via {@link #fireFinalSolutionFound(Solution)} which would notify
	 * all the listeners.
	 */
	abstract protected void algorithmEnd();
	
	/**
	 * Causes the algorithm to temporarily stop at the end of the current step
	 * (it will finish the current one it's in) until the {@link #resume()} method is
	 * called.
	 * 
	 * @return true if paused, false if not
	 */
	synchronized public boolean pause(){
		if (!started || stopped) {
			System.err.println("WARNING: pause() called on a not yet started algorithm or already stopped algorithm!");
			return false;
		}
		if (!paused) {
			paused = true;
		} else {
			System.err.println("WARNING: pause() was called when the algorithm was already paused!");
		}
		return paused;
	}
	/**
	 * Causes the algorithm to resume execution if it was paused, otherwise has no effect.
	 */
	synchronized public void resume(){
		if (paused){
			paused = false;
			this.notify();
		} else {
			System.err.println("WARNING: resume() was called when the algorithm wasn't paused!");
		}
	}
	/**
	 * Causes the algorithm to make this, current step its final step (even if paused).
	 * 
	 * @return true if stopped, false if not
	 */
	synchronized public boolean stop(){
		if (started && !stopped) {
			stopped = true;
			if (paused){
				resume();
			}
		} else {
			System.err.println("WARNING: stop() called on a not yet started algorithm or already stopped algorithm!");
		}
		return stopped;
	}
	
	synchronized public boolean hasStarted() {
		return started;
	}
	
	synchronized public boolean isPaused() {
		return paused;
	}
	
	synchronized public boolean hasStopped() {
		return stopped;
	}
	
	synchronized private void waitIfPaused(){
		if (paused)
			try {
				System.out.println("[ "+Thread.currentThread().getName() + " ] waiting on pause...");
				this.wait();
				System.out.println("[ "+Thread.currentThread().getName() + " ] resuming...");
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
	}
	
	//-----------------------------------------------------------------------------------
	//--- event stuff -------------------------------------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * A listener for relevant events in the course of the execution of the
	 * algorithm.
	 */
	public interface OptimisationAlgorithmListener<T> extends EventListener {

		/**
		 * Should be executed when the state has changed in a relevant way, e.g.
		 * at the end of an iteration/step of the algorithm.
		 * 
		 * @param state
		 *            The current population/swarm/colony/... reference. Should
		 *            NOT be altered or even used in other threads. If needed (for
		 *            GUI purposes, for example) the user should make a copy.
		 */
		public void stateChanged(Collection<T> state, int currentStep);

		/**
		 * This can be viewed as a substitution of the "process" method of the
		 * SwingWorker class.
		 */
		public void currentBestSolutionUpdated(T solution, int currentStep);

		/**
		 * This can be viewed as a substitution of the "done" method of the
		 * SwingWorker class.
		 */
		public void finalSolutionFound(T solution, int currentStep);
	}

	private final List<OptimisationAlgorithmListener<T>> algorithmListeners = 
			new ArrayList<OptimisationAlgorithmListener<T>>(1);

	
	public void addAlgorithmListener(OptimisationAlgorithmListener<T> listener) {
		synchronized (algorithmListeners) {
			algorithmListeners.add(listener);
		}
	}

	public void removeAlgorithmListener(OptimisationAlgorithmListener<T> listener) {
		synchronized (algorithmListeners) {
			algorithmListeners.remove(listener);
		}
	}

	/**
	 * This can be viewed as a substitution of the "publish" method of the
	 * SwingWorker class. It is the responsibilty of the inheriting class to
	 * call it somewhere appropriately.
	 */
	protected void fireCurrentBestSolutionUpdated(T newBestSolution) {
		for (OptimisationAlgorithmListener<T> listener : algorithmListeners) {
			listener.currentBestSolutionUpdated(newBestSolution, currentStep);
		}
	}

	/**
	 * This can be viewed as a substitution of the "call"/"get" mechanism of the
	 * Callable/Future classes. It is the responsibility of the inheriting class
	 * to call this method, which is the equivalent of implementing a "call()"
	 * instead of a "run()".
	 */
	protected void fireFinalSolutionFound(T finalSolution) {
		for (OptimisationAlgorithmListener<T> listener : algorithmListeners) {
			listener.finalSolutionFound(finalSolution, currentStep);
		}
	}

	/**
	 * A reference should be given because of speed, it should be the users
	 * responsibility to make a copy if necessary.
	 */
	protected void fireStateChanged(Collection<T> state) {
		for (OptimisationAlgorithmListener<T> listener : algorithmListeners) {
			listener.stateChanged(state, currentStep);
		}
	}
}
