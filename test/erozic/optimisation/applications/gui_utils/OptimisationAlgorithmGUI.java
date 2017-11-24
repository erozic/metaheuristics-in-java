package erozic.optimisation.applications.gui_utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import erozic.optimisation.algorithms.OptimisationAlgorithm;

/**
 * This is a generic JFrame extension for the main frame of a
 * GUI application for an OptimisationAlgorithm implementation
 * 
 * @author Eugen Rožić
 *
 */
abstract public class OptimisationAlgorithmGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/** The algorithm instance to run */
	protected OptimisationAlgorithm<?> algorithm = null;
	
	//---------------------------------------------
	//--- graphics components ---------------------
	//---------------------------------------------
	
	/** A dialog to set and save algorithm parametres in */
	protected ParametersDialog parametersDialog = null;
	/** A menu holding the controlActions */
	protected JMenu menuControls = null;
	
	protected JButton buttonStart = null;
	protected JButton buttonStop = null;
	protected JButton buttonPause = null;
	protected JButton buttonResume = null;
	
	//---------------------------------------------
	
	private enum ControlAction {
		START,
		STOP,
		PAUSE,
		RESUME
	}
	
	private Map<String, Action> controlActions = new HashMap<String, Action>();
	
	/** A map to hold all actions that are disabled when the algorithm is running */
	protected Map<String, Action> actions = new HashMap<String, Action>();
	
	protected Preferences prefs = Preferences.userNodeForPackage(this.getClass());

	/**
	 * Initiates and shows the GUI.
	 */
	public OptimisationAlgorithmGUI() {
		
		this.setTitle(this.getClass().getSimpleName());
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		
		createControlActions();
		createControlButtons();
		createControlMenu();
		
		createParametersDialog();
		createActions();
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int screenWidth = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
		this.setBounds(screenWidth / 4, screenHeight / 4, screenWidth / 2, screenHeight / 2);
		this.setMinimumSize(new Dimension(200, 200));
	}
	
	/**
	 * Creates all actions used in the menu, button, etc.
	 */
	@SuppressWarnings("serial")
	private void createControlActions() {

		Action actionStart = new AbstractAction("Start") {
			public void actionPerformed(ActionEvent event) {
				doStartAlgorithm();
			}
		};
		actionStart.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
		actionStart.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		actionStart.putValue(Action.SHORT_DESCRIPTION, "Starts the algorithm execution");
		controlActions.put(ControlAction.START.name(), actionStart);
		actionStart.setEnabled(true);

		//-------------------------------------------------------------------------------

		Action actionStop = new AbstractAction("Stop") {
			public void actionPerformed(ActionEvent event) {
				doStopAlgorithm();
			}
		};
		actionStop.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
		actionStop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		actionStop.putValue(Action.SHORT_DESCRIPTION, "Stops the algorithm execution");
		controlActions.put(ControlAction.STOP.name(), actionStop);
		actionStop.setEnabled(false);

		//-------------------------------------------------------------------------------

		Action actionPause = new AbstractAction("Pause") {
			public void actionPerformed(ActionEvent event) {
				doPauseAlgorithm();
			}
		};
		actionPause.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		actionPause.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		actionPause.putValue(Action.SHORT_DESCRIPTION, "Pauses the algorithm execution");
		controlActions.put(ControlAction.PAUSE.name(), actionPause);
		actionPause.setEnabled(false);

		//-------------------------------------------------------------------------------

		Action actionResume = new AbstractAction("Resume") {
			public void actionPerformed(ActionEvent event) {
				doResumeAlgorithm();
			}
		};
		actionResume.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		actionResume.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		actionResume.putValue(Action.SHORT_DESCRIPTION, "Resumes the algorithm execution");
		controlActions.put(ControlAction.RESUME.name(), actionResume);
		actionResume.setEnabled(false);

	}

	private void createControlMenu() {
		menuControls = new JMenu("Controls");
		menuControls.add(new JMenuItem(controlActions.get(ControlAction.START.name())));
		menuControls.add(new JMenuItem(controlActions.get(ControlAction.STOP.name())));
		menuControls.add(new JMenuItem(controlActions.get(ControlAction.PAUSE.name())));
		menuControls.add(new JMenuItem(controlActions.get(ControlAction.RESUME.name())));
	}
	
	private void createControlButtons() {
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		buttonStart = new JButton(controlActions.get(ControlAction.START.name()));
		buttonPanel.add(buttonStart);
		
		buttonStop = new JButton(controlActions.get(ControlAction.STOP.name()));
		buttonPanel.add(buttonStop);
		
		buttonPause = new JButton(controlActions.get(ControlAction.PAUSE.name()));
		buttonPanel.add(buttonPause);
		
		buttonResume = new JButton(controlActions.get(ControlAction.RESUME.name()));
		buttonPanel.add(buttonResume);
	}
	
	/**
	 * Creates a dialog for setting the parameters of the algorithms. 
	 */
	abstract protected void createParametersDialog();
	
	/**
	 * Populates the {@link #actions} map with menu and other actions.
	 */
	abstract protected void createActions();
	
	//--- action implementations -------------------------------------- 
	
	private void doStartAlgorithm() {
		
		controlActions.get(ControlAction.START.name()).setEnabled(false);
		
		if (algorithm != null && !algorithm.hasStopped()) {
			controlActions.get(ControlAction.STOP.name()).setEnabled(true);
			return;
		}
		
		initAlgorithm();
		if (algorithm == null) {
			controlActions.get(ControlAction.START.name()).setEnabled(true);
			return;
		}
		Thread thread = new Thread(algorithm);
		thread.setDaemon(true);
		thread.start();
		
		controlActions.get(ControlAction.STOP.name()).setEnabled(true);
		controlActions.get(ControlAction.PAUSE.name()).setEnabled(true);
		controlActions.get(ControlAction.RESUME.name()).setEnabled(false);
		disableAllOtherActions();
	}
	
	private void doStopAlgorithm() {
		
		controlActions.get(ControlAction.STOP.name()).setEnabled(false);
		
		algorithm.stop();
		
		controlActions.get(ControlAction.PAUSE.name()).setEnabled(false);
		controlActions.get(ControlAction.RESUME.name()).setEnabled(false);
		controlActions.get(ControlAction.START.name()).setEnabled(true);
		enableAllOtherActions();
	}
	
	private void doPauseAlgorithm() {
		
		controlActions.get(ControlAction.PAUSE.name()).setEnabled(false);
		
		if (algorithm.pause()) {
			controlActions.get(ControlAction.RESUME.name()).setEnabled(true);
		}
	}
	
	private void doResumeAlgorithm() {
		
		controlActions.get(ControlAction.RESUME.name()).setEnabled(false);
		
		algorithm.resume();
		
		controlActions.get(ControlAction.PAUSE.name()).setEnabled(true);
	}
	
	//-----------------------------------------------------------------------
	
	/**
	 * Disables all actions in the {@link #actions} map.
	 */
	protected void disableAllOtherActions() {
		for (Action action : actions.values()) {
			action.setEnabled(false);
		}
	}
	
	/**
	 * Enables all actions in the {@link #actions} map.
	 */
	protected void enableAllOtherActions() {
		for (Action action : actions.values()) {
			action.setEnabled(true);
		}
	}
	
	/**
	 * Initiates a new algorithm instance with parameters from the parameterDialog.
	 */
	abstract protected void initAlgorithm();
}
