package erozic.optimisation.applications.gui_utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComponent;

import erozic.optimisation.extras.Location2D;

/**
 * This is a graphics component that draws the towns of a 2D TSP problem with accurate
 * relative distance relations and draws a given path between them that is a solution to
 * the TSP problem.
 * 
 * @author Eugen Rožić
 *
 */
public class TSPVisualisation extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private List<Location2D> towns = null;
	
	private int[] path = null;
	
	private int leftPadding = 50;
	private int rightPadding = 50;
	private int topPadding = 50;
	private int bottomPadding = 50;
	
	/**
	 * Sets a new set of towns to draw and caluclates and sets their relative distances.
	 * 
	 * It also invalidates the current {@link #path} (sets it to null), so 
	 * {@link #setPath(int[])} should always be called after this method.
	 */
	public void setTowns(List<Location2D> towns) {
		
		this.towns = towns;
		
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		
		for (Location2D town : towns) {
			if (town.getX() < xMin)
				xMin = town.getX();
			if (town.getX() > xMax)
				xMax = town.getX();
			if (town.getY() < yMin)
				yMin = town.getY();
			if (town.getY() > yMax)
				yMax = town.getY();
		}
		for (Location2D town : towns){
			town.setxRel(xMin, xMax);
			town.setyRel(yMin, yMax);
		}
		this.repaint();
	}
	
	/**
	 * Sets a new path to draw and schedules a redraw as soon as possible.
	 */
	public void setPath(int[] path) {
		this.path = path;
		this.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		
		Insets insets = this.getInsets();
		
		int leftOffset = leftPadding + insets.left;
		int rightOffset = rightPadding + insets.right;
		int topOffset = topPadding + insets.top;
		int bottomOffset = bottomPadding + insets.bottom;

		int usedWidth = this.getWidth() - leftOffset - rightOffset;
		int usedHeight = this.getHeight() - topOffset - bottomOffset;

		if (towns == null)
			return;

		// draw towns
		g.setColor(Color.BLACK);
		for (Location2D grad : towns) {
			g.fillRect((int)(grad.getxRel()*usedWidth + leftOffset) - 3, (int)(grad.getyRel()*usedHeight + topOffset) - 3, 6, 6);
		}
		
		if (path == null)
			return;

		// draw lines between towns
		Location2D lastTown = towns.get(path[path.length-1]);
		g.setColor(Color.BLACK);
		for (int i = 0; i < path.length; i++) {
			
			Location2D currentTown = towns.get(path[i]);
			
			g.drawLine((int)(lastTown.getxRel()*usedWidth + leftOffset), (int)(lastTown.getyRel()*usedHeight + topOffset),
					(int)(currentTown.getxRel()*usedWidth + leftOffset), (int)(currentTown.getyRel()*usedHeight + topOffset));
			
			lastTown = currentTown;
		}
	}
}