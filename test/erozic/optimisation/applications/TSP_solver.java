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
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.algorithms.OptimisationAlgorithm.OptimisationAlgorithmListener;
import erozic.optimisation.algorithms.metaheuristics.AntColonySystemTSP;
import erozic.optimisation.algorithms.metaheuristics.ClonAlgTSP;
import erozic.optimisation.applications.gui_utils.OptimisationAlgorithmGUI;
import erozic.optimisation.applications.gui_utils.TSPVisualisation;
import erozic.optimisation.solutions.TSPSolution;
import erozic.optimisation.utils.TSPUtil;

/**
 * This class represents an application for solving the TSP problem. It has
 * both a GUI and a console interface and supports multiple algorithms which the
 * user can choose for solving a specific TSP problem which is loaded from a
 * (.tsp) file.
 * 
 * @author Eugen Rožić
 *
 */
public class TSP_solver extends OptimisationAlgorithmGUI {

	private static final long serialVersionUID = 1L;
	
	private TSPUtil tspUtil = null;
	
	//---------------------------------------------
	//--- graphics components ---------------------
	//---------------------------------------------
	private JFileChooser tspFileChooser = null;
	
	private ButtonGroup algorithmChoice = null;
	
	private TSPVisualisation solutionVisualisationComponent = null;
	//---------------------------------------------
	
	private Class<? extends OptimisationAlgorithm<TSPSolution>> algorithmClass = null;

	/**
	 * Initiates and shows the GUI.
	 */
	public TSP_solver() {
		super();
		
		createFileChooser();
		
		createMenu();
		
		solutionVisualisationComponent = new TSPVisualisation();
		this.getContentPane().add(solutionVisualisationComponent, BorderLayout.CENTER);
		
		this.setVisible(true);
	}
	
	@Override
	protected void createParametersDialog() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Creates a JFileChooser for the .tsp files.
	 */
	private void createFileChooser() {
		
		FileFilter tspFiles = new FileNameExtensionFilter("TSP plaintext files (*.tsp)", "tsp");
		FileFilter allFiles = new FileFilter() {
			public String getDescription() {
				return "All files";
			}	
			public boolean accept(File f) {
				return true;
			}
		};
		
		tspFileChooser = new JFileChooser(prefs.get("tsp_directory", null));
		tspFileChooser.addChoosableFileFilter(tspFiles);
		tspFileChooser.addChoosableFileFilter(allFiles);
		tspFileChooser.setAcceptAllFileFilterUsed(false);
		tspFileChooser.setFileFilter(tspFiles);		
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void createActions() {
		
		Action actionLoad = new AbstractAction("Load a TSP...") {
			public void actionPerformed(ActionEvent event) {
				initTSP();
			}
		};
		actionLoad.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
		actionLoad.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		actionLoad.putValue(Action.SHORT_DESCRIPTION, "Loads a TSP description from a file");
		actions.put("load", actionLoad);
		
		//------------------------------------------------------------------
		
		Action actionSave = new AbstractAction("Save solution...") {
			public void actionPerformed(ActionEvent event) {
				saveToFile();
			}
		};
		actionSave.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		actionSave.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		actionSave.putValue(Action.SHORT_DESCRIPTION, "Saves the solution of the TSP to a file");
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
		
		Action actionSelectAnts = new AbstractAction(AntColonySystemTSP.class.getSimpleName()) {
			public void actionPerformed(ActionEvent e) {
				algorithmClass = AntColonySystemTSP.class;
			}
		};
		actions.put("select_ants", actionSelectAnts);
		
		//------------------------------------------------------------------
		
		Action actionSelectAIS = new AbstractAction(ClonAlgTSP.class.getSimpleName()) {
			public void actionPerformed(ActionEvent e) {
				algorithmClass = ClonAlgTSP.class;
			}
		};
		actions.put("select_ais", actionSelectAIS);
		
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
		
		//--------------------------------------------------------------------------
		
		JMenu menuFile = new JMenu("File");
		menuFile.add(new JMenuItem(actions.get("load")));
		menuFile.add(new JMenuItem(actions.get("save")));
		menuFile.addSeparator();
		menuFile.add(new JMenuItem(actions.get("exit")));
		menuBar.add(menuFile);
		
		//--------------------------------------------------------------------------
		
		algorithmChoice = new ButtonGroup();
		
		JRadioButtonMenuItem antsAlgRadio = new JRadioButtonMenuItem(actions.get("select_ants"));
		antsAlgRadio.doClick(); //initial selection
		algorithmChoice.add(antsAlgRadio);
		
		JRadioButtonMenuItem clonAlgRadio = new JRadioButtonMenuItem(actions.get("select_ais"));
		algorithmChoice.add(clonAlgRadio);
		
		JMenu menuAlgorithm = new JMenu("Algorithm");
		menuAlgorithm.add(antsAlgRadio);
		menuAlgorithm.add(clonAlgRadio);
		menuAlgorithm.addSeparator();
		menuAlgorithm.add(new JMenuItem(actions.get("set_params")));
		menuBar.add(menuAlgorithm);
		
		//--------------------------------------------------------------------------
		
		menuBar.add(menuControls);
		
		//--------------------------------------------------------------------------
		
		this.setJMenuBar(menuBar);
	}
	
	/**
	 * Opens a file chooser for the user to pick a file containing a TSP problem and
	 * prepares the application to run the algorithm on it.
	 */
	private void initTSP(){
		
		File tspFile = null;

		switch (tspFileChooser.showOpenDialog(this)){
		case JFileChooser.APPROVE_OPTION:
			tspFile = tspFileChooser.getSelectedFile();
			break;
		case JFileChooser.CANCEL_OPTION:
			return;
		case JFileChooser.ERROR_OPTION:
		default:
			String message = "An unknown error occured!\nPlease try again.";
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		prefs.put("tsp_directory",tspFile.getParent());

		try {
			tspUtil = new TSPUtil(tspFile);
		} catch (IOException e) {
			String message = "Error reading file " + tspFile.getPath() + "\nException message:\n";
			JOptionPane.showMessageDialog(this, message+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		solutionVisualisationComponent.setTowns(tspUtil.getTowns());
		solutionVisualisationComponent.setPath(tspUtil.getGreedyPath());
	}
	
	@Override
	protected void initAlgorithm() {
		
		if (tspUtil == null){
			JOptionPane.showMessageDialog(this, "No TSP problem loaded yet!");
			return;
		}
		
		if (algorithmClass.equals(AntColonySystemTSP.class)) {
			
			// TODO get parameters from the parametersDialog
			int colonySize = 50;
			double alpha = 1.;
			double beta = 2.;
			double rho = 0.5;
			
			algorithm = new AntColonySystemTSP(tspUtil, colonySize, alpha, beta, rho);
			
		} else if (algorithmClass.equals(ClonAlgTSP.class)) {
			
			// TODO get parameters from the parametersDialog
			int populationSize = 200;
			double selectFraction = 1.;
			double birthFraction = 0.2;
			double beta = 10.;
			double rho = 0.4;
			
			algorithm = new ClonAlgTSP(tspUtil, populationSize, selectFraction, birthFraction, beta, rho);
		
		} else {
			JOptionPane.showMessageDialog(this, "Unknown algorithm (" + algorithmClass.getSimpleName() + ") specified!",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		OptimisationAlgorithm<TSPSolution> TSPAlgorithm = (OptimisationAlgorithm<TSPSolution>) algorithm;

		TSPAlgorithm.addAlgorithmListener(new OptimisationAlgorithmListener<TSPSolution>() {
			@Override
			public void stateChanged(Collection<TSPSolution> state, int step) {
				// nothing to do here
			}
			@Override
			public void currentBestSolutionUpdated(TSPSolution solution, int step) {
				SwingUtilities.invokeLater(() -> {
					solutionVisualisationComponent.setPath(solution.getPath());
				});
				System.out.println(step + ". step: shortest path = " + solution.getPathLength());
			}
			@Override
			public void finalSolutionFound(TSPSolution solution, int step) {
				// no need for anything to do here
			}
		});
	}
	
	/**
	 * Opens a filechooser for the user to choose a file to which to save the best solution
	 * the algorithm found.
	 */
	private void saveToFile() {
		
		if (algorithm==null || !algorithm.hasStopped()) {
			JOptionPane.showMessageDialog(TSP_solver.this, "No solution to save yet!");
			return;
		}
		
		File saveFile = null;
		
		switch(tspFileChooser.showSaveDialog(TSP_solver.this)){
		case JFileChooser.APPROVE_OPTION:
			saveFile = tspFileChooser.getSelectedFile();
			break;
		case JFileChooser.CANCEL_OPTION:
			return;
		case JFileChooser.ERROR_OPTION:
		default:
			String message = "An unknown error occured!\nPlease try again.";
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		PrintStream toFile = null;
		try {
			toFile = new PrintStream(new FileOutputStream(saveFile, false));
			String message = "A solution by " + algorithm.getClass().getSimpleName() + " on "
					+ ((TSPSolution) algorithm.getCurrentBestSolution()).getPath().length + " towns:";
			
			printSolution(tspUtil, message, (TSPSolution) algorithm.getCurrentBestSolution(), toFile);
		
		} catch (IOException e) {
			String message = "Could not write to file " + saveFile.getPath() + "!";
			JOptionPane.showMessageDialog(TSP_solver.this, message, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} finally {
			toFile.close();
		}
	}
	
	//---------------------------------------------------------------------------------------------
	//--- static, non GUI stuff -------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------

	/**
	 * Starts an instance of an Ant Colony System ACO algorithm for solving the
	 * TSP problem.
	 * 
	 * TODO
	 */
	public static void main(String[] args) {

		// TODO check options for GUI (--gui) or console options
		
		SwingUtilities.invokeLater(() -> {
			new TSP_solver();
		});

		//consoleRun(args);
	}

	private static void consoleRun(String[] args) throws IOException {

		File tspFile = new File(args[0]);

		if (!tspFile.isFile() || !tspFile.canRead()) {
			System.out.println(usage());
			System.err.println("ERROR: The file at " + args[0] + " is not a file or cannot be read! Exiting...");
			System.exit(-1);
		}
		TSPUtil tspUtil = new TSPUtil(tspFile);
		
		int colonySize = 50;
		double alpha = 1;
		double beta = 2;
		double rho = 0.5;
		
		// TODO what about a CLONALG console run ...

		AntColonySystemTSP algorithm = new AntColonySystemTSP(tspUtil, colonySize, alpha, beta, rho);
		
		algorithm.addAlgorithmListener(new OptimisationAlgorithmListener<TSPSolution>() {
			@Override
			public void currentBestSolutionUpdated(TSPSolution solution, int step) {
				System.out.println("step: " + step + ", best solution length = " + solution.getPathLength());
			}
			@Override
			public void finalSolutionFound(TSPSolution solution, int step) {
				printSolution(tspUtil, "(step " + step + ") Full final solution:", solution, System.out);
			}
			@Override
			public void stateChanged(Collection<TSPSolution> state, int step) {
				// no need to do anything here
			}
		});

		System.out.println("Initial (greedy) solution:");
		System.out.println(algorithm.getCurrentBestSolution());
		user_pause();

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
	private static String usage() {

		StringBuilder sb = new StringBuilder();

		sb.append("\nThis program uses an Ant Colony System ACO algorithm to solve a TSP problem.\n\n");
		sb.append("The program expects 1 argument: a path to a '.tsp' file.\n");

		return sb.toString();
	}

	private static void printSolution(TSPUtil tspUtil, String message, TSPSolution ant, PrintStream out) {
		out.println(message);
		out.println();
		out.println("Path length = " + ant.getPathLength());
		out.println();
		for (int townIndex : ant.getPath()) {
			out.print(tspUtil.getTowns().get(townIndex).toString() + " --> ");
		}
		out.println(tspUtil.getTowns().get(ant.getPath()[0]).toString());
		
	}

	/**
	 * Pauses until the user presses [enter].
	 */
	private static void user_pause() {
		System.out.println("Press [enter] to continue");
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		try {
			input.readLine();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
