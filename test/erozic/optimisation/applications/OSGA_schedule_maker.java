package erozic.optimisation.applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.algorithms.OptimisationAlgorithm.OptimisationAlgorithmListener;
import erozic.optimisation.algorithms.metaheuristics.ScheduleOSGA;
import erozic.optimisation.algorithms.metaheuristics.concurrent.ScheduleOSGAConcurrent;
import erozic.optimisation.solutions.Schedule;
import erozic.optimisation.utils.ScheduleUtil;

/**
 * A console application for finding the optimal schedule using an
 * implementation of the Offpsring Selection Genetic Algorithm.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class OSGA_schedule_maker {

	/**
	 * Starts an instance of an Offspring Selection genetic algorithm for
	 * finding the optimal schedule for teams with the given constraint of
	 * available times and places (with limited capacity) and previous team
	 * member obligations.
	 * 
	 * @param args
	 *            command line arguments; has to contain paths to three files of
	 *            a given format in the following order: file defining the
	 *            teams, file defining the available terms, file listing
	 *            students' unavailabilities.<br>
	 *            Optional arguments are:
	 *            <ul>
	 *            <li>--concurrent: a switch indicating whether to use the
	 *            multithreaded implementation</li>
	 *            <li>--threads <small positive integer>: the number of threads
	 *            to use if using the multithreaded implementation</li>
	 *            </ul>
	 */
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println(usage());
			System.exit(-1);
		}
		
		// TODO better console args handling...

		List<String> argsList = new ArrayList<String>(Arrays.asList(args));

		File teamsFile = checkFileArgument(argsList);
		File termsFile = checkFileArgument(argsList);
		File student_unavailabilityFile = checkFileArgument(argsList);

		ScheduleUtil util = new ScheduleUtil(teamsFile, termsFile, student_unavailabilityFile);

		OptimisationAlgorithm<Schedule> algorithm = null;

		if (argsList.contains("--concurrent")) {
			argsList.remove("--concurrent");
			int threads = Runtime.getRuntime().availableProcessors();
			if (argsList.contains("--threads")) {
				try {
					int index = argsList.indexOf("--threads");
					threads = Integer.parseInt(argsList.get(index + 1));
					argsList.remove(index);
					argsList.remove(index);
				} catch (Exception e) {
					System.out.println(usage());
					System.err.println("ERROR: Wronly entered arguments!\n"
							+ "Immediately after '--threads' there should be a (small) positive integer that determines how "
							+ "many threads will execute the algorithm (default is determined by the number of processors).");
					System.exit(-1);
				}
			}
			algorithm = new ScheduleOSGAConcurrent(threads, util, 100, 25, 0., 0.6, 3, 0.05);
		} else {
			algorithm = new ScheduleOSGA(util, 100, 25, 0., 0.6, 3, 0.05);
		}
		
		ScheduleOSGA osga = (ScheduleOSGA) algorithm;

		if (!argsList.isEmpty()) {
			System.out.println(usage());
			System.err.println(
					"WARNING: There are some extra or redundant arguments passed that are ignored: " + argsList);
		}
		
		algorithm.addAlgorithmListener(new OptimisationAlgorithmListener<Schedule>() {
			@Override
			public void stateChanged(Collection<Schedule> state, int currentStep) {
				// not necessary
			}
			@Override
			public void currentBestSolutionUpdated(Schedule solution, int currentStep) {
				
				System.out.println(currentStep + ". step; selection pressure = " + osga.getCurrSelPressure()
				+ ", compFactor = "	+ osga.getCompFactor());
				System.out.println("new best solution: " + solution + "\n");
			}
			@Override
			public void finalSolutionFound(Schedule solution, int currentStep) {
				
				System.out.println(currentStep + ". step; Final solution: \n" + solution.fullOutput());
			}
		});

		new Thread(algorithm).start();

		System.out.println("Press [enter] to pause/resume execution:");

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				input.readLine();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			if (algorithm.isPaused()) {
				algorithm.resume();
			} else {
				algorithm.pause();
			}
		}
	}

	/**
	 * @return the File wrapper of the first argument in the list and removes it
	 * from the list, IF it is a valid, existing, readable file, otherwise it
	 * exists the program.
	 */
	private static File checkFileArgument(List<String> argsList) {
		File file = new File(argsList.get(0));

		if (!file.isFile() || !file.canRead()) {
			System.out.println(usage());
			System.err
					.println("ERROR: The file at " + argsList.get(0) + " is not a file or cannot be read! Exiting...");
			System.exit(-1);
		}

		argsList.remove(0);
		return file;
	}

	/**
	 * @return a String containing the description of proper usage
	 */
	public static String usage() {

		StringBuilder sb = new StringBuilder();

		sb.append("\nThis program uses the Offspring Selection genetic algorithm to solve a scheduling problem.\n\n");
		sb.append("The program expects 3 positional arguments first:\n");
		sb.append("\t 1) path to a textual file defining the teams,\n");
		sb.append("\t 2) path to a textual file defining the available terms,\n");
		sb.append("\t 3) path to a textual file listing students' unavailability times.\n");
		sb.append("Optional, unpositional arguments are:\n");
		sb.append("\t --concurrent: a switch indicating whether to use the multithreaded implementation,\n");
		sb.append("\t --threads <integer>: the number of threads to use if using the multithreaded implementation.");

		return sb.toString();
	}

}