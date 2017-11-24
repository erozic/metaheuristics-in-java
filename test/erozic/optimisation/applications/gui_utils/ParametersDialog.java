package erozic.optimisation.applications.gui_utils;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This is dialog that can be used to let the user set a list of named parameters
 * via a GUI that can then be easily retrieved.
 * 
 * The list of parameter names the user can set, and that define the dialog look, are
 * given upon instantiation. 
 * 
 * @author Eugen Rožić
 *
 */
public class ParametersDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private final Map<String, JTextField> namedTextFields = new HashMap<String, JTextField>();
	
	public ParametersDialog(JFrame source, Map<String, String> parametersDefaultValues) {
		super(source, "Algorithm parameters", true);
		
		for (Map.Entry<String, String> parameter: parametersDefaultValues.entrySet()) {
			namedTextFields.put(parameter.getKey(), new JTextField(parameter.getValue()));
		}
		
		this.setLocationRelativeTo(source);
		this.setSize(200, 150);
		this.getContentPane().setLayout(new GridLayout(namedTextFields.size(), 2, 10, 10));
		
		// TODO make this prettier - location, size, border, layout ...
		
		for (Map.Entry<String, JTextField> pair : namedTextFields.entrySet()) {
			this.getContentPane().add(new JLabel(pair.getKey()+": "));
			this.getContentPane().add(pair.getValue());
		}
	}
	
	/**
	 * Returns the contents of the text field corresponding to the given parameter name,
	 * or null there is no parameter with the given name.
	 */
	public String getParameter(String parameterName){
		
		JTextField userInput = namedTextFields.get(parameterName);
		if (userInput == null)
			return null;
		
		return userInput.getText();
	}

}
