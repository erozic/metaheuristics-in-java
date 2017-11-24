package erozic.optimisation.applications;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import erozic.optimisation.algorithms.OptimisationAlgorithm.OptimisationAlgorithmListener;
import erozic.optimisation.algorithms.metaheuristics.ParticleSwarmOptimisation;
import erozic.optimisation.applications.gui_utils.OptimisationAlgorithmGUI;
import erozic.optimisation.applications.gui_utils.PSOVisualisation;
import erozic.optimisation.functions.Function;
import erozic.optimisation.functions.RastriginFunction;
import erozic.optimisation.functions.SchwefelFunction;
import erozic.optimisation.solutions.VectorParticle;
import erozic.optimisation.solutions.VectorSolution;
import erozic.optimisation.utils.VectorUtil;

/**
 * This class represents an application for finding the minimum of a function using
 * the PSO algorithm.
 * It has both a GUI and a console interface and supports multiple functions. The GUI
 * interface is fixed to solve the problem only in 2D because of the visualisation
 * of the swarm state as the algorithm runs.
 * 
 * @author Eugen Rožić
 *
 */
public class PSO_function_minimiser extends OptimisationAlgorithmGUI {

	private static final long serialVersionUID = 1L;

	// ---------------------------------------------
	// --- graphics components ---------------------
	// ---------------------------------------------
	private JDialog functionDialog;
	private PSOVisualisation solutionVisualisationComponent;
	
	private ButtonGroup functionChoice;
	
	private JCheckBoxMenuItem velocitiesCheckBox;
	private JCheckBoxMenuItem neighbourhoodCheckBox;
	// ---------------------------------------------
	
	/** Holds actions that will not be disabled when the algorithm is running */
	private Map<String, Action> permanentActions;
	
	private Function function = null;

	/**
	 * Initiates and shows the GUI.
	 */
	public PSO_function_minimiser() {
		super();
		
		createFunctionDialog();
		
		initVisualisationComponent();
		
		createMenu();

		this.setVisible(true);
	}
	
	@Override
	protected void createParametersDialog() {
		// TODO
	}
	
	private void createFunctionDialog() {
		// TODO
	}

	private void initVisualisationComponent() {

		// TODO get domain boundaries from functionDialog
		solutionVisualisationComponent = new PSOVisualisation(function, -10, 10);
		
		solutionVisualisationComponent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(solutionVisualisationComponent, BorderLayout.CENTER);
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void createActions() {
		
		permanentActions = new HashMap<String, Action>();
		
		//------------------------------------------------------------------
		
		Action actionSave = new AbstractAction("Save solution...") {
			public void actionPerformed(ActionEvent event) {
				saveToFile();
			}
		};
		actionSave.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		actionSave.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		actionSave.putValue(Action.SHORT_DESCRIPTION, "Saves the best solution to a file");
		actions.put("save", actionSave);
		
		//------------------------------------------------------------------
		
		Action actionExit = new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		};
		actionExit.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
		actionExit.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		actionExit.putValue(Action.SHORT_DESCRIPTION, "Exits the application");
		actions.put("exit", actionExit);
		
		//------------------------------------------------------------------
		
		Action actionSelectRastrigin = new AbstractAction(RastriginFunction.class.getSimpleName()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				function = new RastriginFunction(Function.MINIMISE);
				solutionVisualisationComponent.changeFunction(function);
			}
		};
		actions.put("select_rastrigin", actionSelectRastrigin);
		
		//------------------------------------------------------------------
		
		Action actionSelectSchwefel = new AbstractAction(SchwefelFunction.class.getSimpleName()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				function = new SchwefelFunction(Function.MINIMISE);
				solutionVisualisationComponent.changeFunction(function);
			}
		};
		actions.put("select_schwefel", actionSelectSchwefel);
		
		//------------------------------------------------------------------
		
		Action actionSetBoundaries = new AbstractAction("Set boundaries") {
			@Override
			public void actionPerformed(ActionEvent e) {
				functionDialog.setVisible(true);
			}
		};
		actionSetBoundaries.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
		actionSetBoundaries.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		actionSetBoundaries.putValue(Action.SHORT_DESCRIPTION, "Opens a dialoge to set function domain boundaries");
		actions.put("set_boundaries", actionSetBoundaries);
		
		//------------------------------------------------------------------
		
		Action actionVelocities = new AbstractAction("Draw velocities") {
			@Override
			public void actionPerformed(ActionEvent e) {
				solutionVisualisationComponent.drawVelocities(velocitiesCheckBox.isSelected());
			}
		};
		actionVelocities.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
		actionVelocities.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		permanentActions.put("velocities", actionVelocities);

		//------------------------------------------------------------------

		Action actionNeighbourhood = new AbstractAction("Draw neighbourhood") {
			@Override
			public void actionPerformed(ActionEvent e) {
				solutionVisualisationComponent.drawNeighbourhood(neighbourhoodCheckBox.isSelected());
			}
		};
		actionNeighbourhood.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		actionNeighbourhood.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		permanentActions.put("neighbourhood", actionNeighbourhood);
		
		//------------------------------------------------------------------

		Action actionSetParams = new AbstractAction("Set parameters") {
			public void actionPerformed(ActionEvent e) {
				parametersDialog.setVisible(true);	
			}
		};
		actionSetParams.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		actionSetParams.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		actionSetParams.putValue(Action.SHORT_DESCRIPTION, "Opens a dialoge to set algorithm parameters");
		actions.put("set_params", actionSetParams);
	}

	/**
	 * Creates the menu and the menus options.
	 */
	private void createMenu() {

		JMenuBar menuBar = new JMenuBar();
		
		// --------------------------------------------------------------------------
		
		JMenu menuFile = new JMenu("File");
		menuFile.add(new JMenuItem(actions.get("save")));
		menuFile.addSeparator();
		menuFile.add(new JMenuItem(actions.get("exit")));
		menuBar.add(menuFile);
		
		// --------------------------------------------------------------------------
		
		functionChoice = new ButtonGroup();

		JRadioButtonMenuItem rastriginRadio = new JRadioButtonMenuItem(actions.get("select_rastrigin"));
		rastriginRadio.doClick(); //initial selection
		functionChoice.add(rastriginRadio);
		
		JRadioButtonMenuItem schwefelRadio = new JRadioButtonMenuItem(actions.get("select_schwefel"));
		functionChoice.add(schwefelRadio);
		
		JMenu menuFunction = new JMenu("Function");
		menuFunction.add(rastriginRadio);
		menuFunction.add(schwefelRadio);
		menuFunction.addSeparator();
		menuFunction.add(new JMenuItem(actions.get("set_boundaries")));
		menuBar.add(menuFunction);
		
		// --------------------------------------------------------------------------
		
		JMenu menuAlgorithm = new JMenu("Algorithm");
		velocitiesCheckBox = new JCheckBoxMenuItem(permanentActions.get("velocities"));
		menuAlgorithm.add(velocitiesCheckBox);
		neighbourhoodCheckBox = new JCheckBoxMenuItem(permanentActions.get("neighbourhood"));
		menuAlgorithm.add(neighbourhoodCheckBox);
		menuAlgorithm.addSeparator();
		menuAlgorithm.add(new JMenuItem(actions.get("set_params")));
		menuBar.add(menuAlgorithm);
		
		// --------------------------------------------------------------------------
		
		menuBar.add(menuControls);
		
		// --------------------------------------------------------------------------
		
		this.setJMenuBar(menuBar);
	}

	@Override
	protected void initAlgorithm() {
		
		// TODO get domain boundaries from functionDialog
		VectorUtil vectorUtil = new VectorUtil(function, 2, -10, 10);
		
		// TODO get parameters from ParametersDialog
		int swarmSize = 20;
		int neighbourhoodSize = 5;
		double c1 = 2;
		double c2 = 2;
		double wStart = 0.9;
		double wEnd = 0.4;
		int wIterBound = 50;
		double vMaxDiffFraction = 0.1;
		
		algorithm = new ParticleSwarmOptimisation(vectorUtil, swarmSize, neighbourhoodSize, c1, c2, wStart, wEnd,
				wIterBound, vMaxDiffFraction);
		
		ParticleSwarmOptimisation PSOAlgorithm = (ParticleSwarmOptimisation) algorithm;
		
		PSOAlgorithm.addAlgorithmListener(new OptimisationAlgorithmListener<VectorParticle>() {
			@Override
			public void stateChanged(Collection<VectorParticle> swarm, int step) {
				double[][] points = new double[swarm.size()][vectorUtil.getDimensions()];
				double[][] last = new double[swarm.size()][vectorUtil.getDimensions()];
				int i = 0;
				for (VectorParticle particle : swarm) {
					points[i] = particle.getValues();
					last[i++] = particle.getLast().getValues();
				}
				SwingUtilities.invokeLater(() -> {
					solutionVisualisationComponent.setToDraw(points, last);
				});
			}
			@Override
			public void currentBestSolutionUpdated(VectorParticle solution, int step) {
				System.out.println("step: " + step + ", best solution: f"
						+ Arrays.toString(((VectorSolution) solution).getValues()) + " = " + solution.getFitness());
			}
			@Override
			public void finalSolutionFound(VectorParticle solution, int step) {
				// no need for this	
			}
		});
	}
	
	/**
	 * Opens a filechooser for the user to choose a file to which to save the best solution
	 * the algorithm found.
	 */
	private void saveToFile() {
		
		if (algorithm==null || !algorithm.hasStopped()) {
			JOptionPane.showMessageDialog(PSO_function_minimiser.this, "No solution to save yet!");
			return;
		}
		
		File saveFile = null;
		
		JFileChooser fileChooser = new JFileChooser(new File("."));
		switch(fileChooser.showSaveDialog(PSO_function_minimiser.this)){
		case JFileChooser.APPROVE_OPTION:
			saveFile = fileChooser.getSelectedFile();
			break;
		case JFileChooser.CANCEL_OPTION:
			return;
		case JFileChooser.ERROR_OPTION:
		default:
			String message = "An unknown error occured!\nPlease try again.";
			JOptionPane.showMessageDialog(PSO_function_minimiser.this, message, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		PrintStream toFile = null;
		try {
			toFile = new PrintStream(new FileOutputStream(saveFile, false));
			VectorSolution bestSolution = (VectorSolution) algorithm.getCurrentBestSolution();
			
			toFile.println("Best solution (minimum) found:");
			toFile.println();
			toFile.println("f" + Arrays.toString(bestSolution.getValues()) + " = "
					+ function.value(bestSolution.getValues()));

		} catch (IOException e) {
			String message = "Could not write to file " + saveFile.getPath() + "!";
			JOptionPane.showMessageDialog(PSO_function_minimiser.this, message, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		} finally {
			toFile.close();
		}
	}

	// ---------------------------------------------------------------------------------------------
	// --- static, non GUI stuff
	// -------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	/**
	 * Starts an instance of a PSO algorithm for finding the minimum of a
	 * function.
	 * 
	 * TODO
	 */
	public static void main(String[] args) {

		// TODO check options for GUI (--gui) or console options

		SwingUtilities.invokeLater(() -> {
			new PSO_function_minimiser();
		});

		// consoleRun(args);
	}

	private static void consoleRun(String[] args) {

		int dimensions = 0;
		double minValue = 0;
		double maxValue = 0;

		if (args.length != 4) {
			System.out.println(usage());
			System.exit(-1);
		}

		Function function = null;

		if (args[0].equalsIgnoreCase("rastrigin"))
			function = new RastriginFunction(Function.MINIMISE);
		else if (args[0].equalsIgnoreCase("schwefel"))
			function = new SchwefelFunction(Function.MINIMISE);
		else {
			System.out.println(usage());
			System.err.println("ERROR: The first argument has unexpected value!");
			System.exit(-1);
		}

		try {
			dimensions = Integer.parseInt(args[1]);
			if (dimensions <= 0)
				throw new NumberFormatException("Dimensions is " + dimensions + " but has to be a positive integer!");
			minValue = Double.parseDouble(args[2]);
			maxValue = Double.parseDouble(args[3]);
			if (minValue >= maxValue)
				throw new NumberFormatException(
						"Minimum value (" + minValue + ") has to be smaller than maximum value (" + maxValue + ")");
		} catch (NumberFormatException e) {
			System.out.println(usage());
			System.err.println("ERROR: Number Format Exception " + e.getMessage());
			System.exit(-1);
		} catch (Exception e2) {
			System.out.println(usage());
			System.err.println("ERROR: Unexpected exception type: " + e2.getClass().getName() + ": " + e2.getMessage());
			System.exit(-1);
		}

		VectorUtil vectorUtil = new VectorUtil(function, dimensions, minValue, maxValue);

		int swarmSize = 20;
		int neighbourhoodSize = 3;
		double c1 = 2;
		double c2 = 2;
		double wStart = 0.9;
		double wEnd = 0.4;
		int wIterBound = 50;
		double vMaxDiffFraction = 0.1;

		ParticleSwarmOptimisation algorithm = new ParticleSwarmOptimisation(vectorUtil, swarmSize, neighbourhoodSize,
				c1, c2, wStart, wEnd, wIterBound, vMaxDiffFraction, Integer.MAX_VALUE);
		
		algorithm.addAlgorithmListener(new OptimisationAlgorithmListener<VectorParticle>() {
			@Override
			public void currentBestSolutionUpdated(VectorParticle solution, int step) {
				System.out.println("step: " + step + ", best solution: f" + Arrays.toString(solution.getValues()) + " = "
						+ solution.getFitness());
			}
			@Override
			public void finalSolutionFound(VectorParticle solution, int step) {
				System.out.println();
				System.out.println(
						"Final solution: f" + Arrays.toString(solution.getValues()) + " = " + solution.getFitness());
				System.out.println();
			}
			@Override
			public void stateChanged(Collection<VectorParticle> state, int step) {
				// no need for this
			}
		});

		new Thread(algorithm).start();

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Enter 'pause', 'resume' or 'stop':");
			String inputLine = null;
			try {
				inputLine = input.readLine();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			if (inputLine.equals("pause")) {
				algorithm.pause();
			} else if (inputLine.equals("resume")) {
				algorithm.resume();
			} else if (inputLine.equals("stop")) {
				algorithm.stop();
				break;
			}
		}

	}

	/**
	 * @return a String containing description of proper usage
	 */
	public static String usage() {

		StringBuilder sb = new StringBuilder();

		sb.append(
				"\nThis program uses a PSO algorithm to find the minimum of the Rastrigin or Schwefel function in N dimensions.\n\n");
		sb.append("The program expects 4 positional arguments:\n");
		sb.append("\t 1) \"rastrigin\" or \"schwefel\": the choice of function,\n");
		sb.append("\t 2) <int>: the dimensionality of the domain/solution,\n ");
		sb.append("\t 3) <double>: the minimum value in all dimensions,\n");
		sb.append("\t 4) <double>: the maximum value in all dimensions.\n");

		return sb.toString();
	}

}
