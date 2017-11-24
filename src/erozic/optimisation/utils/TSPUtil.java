package erozic.optimisation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.extras.Location2D;
import erozic.optimisation.solutions.TSPSolution;

/**
 * Keeps all data related to the TSP problem being solved (towns, distances,
 * etc.)
 * 
 * @author Eugen Rožić
 *
 */
public class TSPUtil implements SolutionUtil<TSPSolution> {

	/** A list of towns to visit */
	private List<Location2D> towns;
	/** The number of towns (a convenience) */
	private int numTowns;
	/** The absolute distances between towns */
	private double[][] distances;

	/** The path that one gets using a greedy, closest neighbour algorithm */
	private int[] greedyPath;
	/** The absolute length of the greedy path */
	private double greedyPathLength;

	/**
	 * Sets all the object fields to appropriate values.
	 * 
	 * @throws IOException if something is wrong with the file
	 */
	public TSPUtil(File file) throws IOException {
		parseTowns(file);
		calculateAndSetDistances();
		calculateAndSetGreedyPath();
	}

	/**
	 * Reads town information from the given file, determines the maximal and
	 * minimal absolute coordinates among all towns and populates the list of
	 * {@link #towns}.
	 */
	private void parseTowns(File file) throws IOException {

		towns = new ArrayList<Location2D>();

		BufferedReader fileInput = new BufferedReader(new FileReader(file));
		String line;

		System.out.print("Parsing file "+file.getPath()+" ... ");
		while ((line = fileInput.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			String[] elems = line.split(" ");
			if (elems.length == 3 && !elems[1].equals(":")) {
				double x = Double.parseDouble(elems[1]);
				double y = Double.parseDouble(elems[2]);
				towns.add(new Location2D(elems[0].trim(), x, y));
			}
		}
		fileInput.close();

		numTowns = towns.size();

		System.out.println("Done!");
	}

	private void calculateAndSetDistances() {

		distances = new double[towns.size()][towns.size()];

		for (int i = 0; i < towns.size(); i++) {
			distances[i][i] = 0;
			Location2D a = towns.get(i);
			for (int j = i + 1; j < towns.size(); j++) {
				Location2D b = towns.get(j);
				distances[i][j] = distances[j][i] = a.distanceTo(b);
			}
		}
	}

	private void calculateAndSetGreedyPath() {

		greedyPathLength = 0;
		greedyPath = new int[towns.size()];

		int[] unvisited = new int[towns.size()];
		for (int i = 0; i < towns.size(); i++)
			unvisited[i] = i;

		greedyPath[0] = unvisited[0];

		for (int step = 1; step < towns.size()-1; step++) {

			int lastTown = greedyPath[step-1];

			double shortestPath = Double.POSITIVE_INFINITY;
			int closestUnvisitedTownIndex = -1;

			for (int i = step; i < towns.size(); i++) {
				int candidate = unvisited[i];
				if (distances[lastTown][candidate] < shortestPath) {
					shortestPath = distances[lastTown][candidate];
					closestUnvisitedTownIndex = i;
				}
			}
			greedyPath[step] = unvisited[closestUnvisitedTownIndex];
			greedyPathLength += shortestPath;

			if (closestUnvisitedTownIndex > step){
				int temp = unvisited[step];
				unvisited[step] = unvisited[closestUnvisitedTownIndex];
				unvisited[closestUnvisitedTownIndex] = temp;
			}
		}
		greedyPath[towns.size()-1] = unvisited[towns.size()-1];
		greedyPathLength += distances[towns.size()-2][towns.size()-1];
		greedyPathLength += distances[towns.size()-1][0];
	}

	public List<Location2D> getTowns() {
		return towns;
	}

	public int getNumTowns() {
		return numTowns;
	}

	public double[][] getDistances() {
		return distances;
	}

	public double getGreedyPathLength() {
		return greedyPathLength;
	}

	public int[] getGreedyPath() {
		return greedyPath;
	}

	/**
	 * Calculates the lenght of the ant's current path and sets it.
	 * 
	 * @return a reference to the passed parameter, for convenience
	 */
	public TSPSolution evaluate(TSPSolution solution) {

		double pathLength = 0;
		for (int i = 1; i < numTowns; i++)
			pathLength += distances[solution.getPath()[i-1]][solution.getPath()[i]];

		pathLength += distances[solution.getPath()[numTowns-1]][solution.getPath()[0]];
		solution.setPathLength(pathLength);
		return solution;
	}

	/**
	 * @return A new and evaluated {@link TSPSolution} with a random path.
	 */
	public TSPSolution generateRandom(){
		
		TSPSolution newSolution = new TSPSolution(numTowns);
		
		for (int i = 0; i < numTowns; i++)
			newSolution.getPath()[i] = i;
		
		shuffleArray(newSolution.getPath());

		return evaluate(newSolution);
	}

	/**
	 * Fisher-Yates shuffle of the given array.
	 */
	public void shuffleArray(int[] polje) {
		
		Random random = ThreadLocalRandom.current();
		
		for (int i = polje.length - 1; i > 0; i--) {
			int randomIndex = random.nextInt(i+1);
			if (randomIndex==i)
				continue;
			int temp = polje[i];
			polje[i] = polje[randomIndex];
			polje[randomIndex] = temp;
		}
	}

}
