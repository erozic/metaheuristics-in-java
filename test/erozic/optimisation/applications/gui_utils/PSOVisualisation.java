package erozic.optimisation.applications.gui_utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;

import erozic.optimisation.functions.Function;

/**
 * This is a graphics component that draws the background function and the particles
 * which are trying to locate its extremum (in red), along with their current velocities
 * (distances from previous locations; in yellow) and their neighbours (in magenta).
 * 
 * The function is assumed to have a 2D domain and is drawn in greyscale where black is
 * the minumum and white the maximum.
 * 
 * @author Eugen Rožić
 *
 */
public class PSOVisualisation extends FunctionVisualisation {

	private static final long serialVersionUID = 1L;
	
	private boolean velocities = false;
	private boolean neighbourhood = false;
	
	private double[][] lastOfToDraw = null;
	
	/**
	 * @see FunctionVisualisation#FunctionVisualisation(Function, double, double)
	 */
	public PSOVisualisation(Function function, double minValue, double maxValue) {
		super(function, minValue, maxValue);
	}
	
	/**
	 * Sets the solutions (particles) to draw and schedules the repaint to show the changes.
	 */
	public void setToDraw(double[][] toDraw, double[][] lastOfToDraw) {
		this.lastOfToDraw = lastOfToDraw;
		setToDraw(toDraw);
	}
	
	/**
	 * Sets whether to draw velocities (as yellow lines from last to current position for
	 * each particle). This only applies to VectorPartices for PSO.
	 */
	public void drawVelocities(boolean velocities) {
		this.velocities = velocities;
	}
	
	/**
	 * Sets whether to draw lines between neighbours (in magenta).
	 * This only applies to VectorPartices for PSO.
	 */
	public void drawNeighbourhood(boolean neighbourhood) {
		this.neighbourhood = neighbourhood;
	}

	@Override 
	protected void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		if (lastOfToDraw == null)
			return;
		
		Insets insets = this.getInsets();
		
		int leftOffset = leftPadding + insets.left;
		int rightOffset = rightPadding + insets.right;
		int topOffset = topPadding + insets.top;
		int bottomOffset = bottomPadding + insets.bottom;

		int usedWidth = this.getWidth() - leftOffset - rightOffset;
		int usedHeight = this.getHeight() - topOffset - bottomOffset;
		
		double rescaleFactorX = usedWidth / (this.maxValue - this.minValue);
		double rescaleFactorY = usedHeight / (this.maxValue - this.minValue);

		int xLast = (int) ((toDraw[toDraw.length - 1][0] - this.minValue) * rescaleFactorX);
		int yLast = (int) ((toDraw[toDraw.length - 1][1] - this.minValue) * rescaleFactorY);

		for (int i = 0; i < toDraw.length; i++) {

			int x = (int) ((toDraw[i][0] - this.minValue) * rescaleFactorX);
			int y = (int) ((toDraw[i][1] - this.minValue) * rescaleFactorY);

			if (velocities) {

				int xOld = (int) ((lastOfToDraw[i][0] - this.minValue) * rescaleFactorX);
				int yOld = (int) ((lastOfToDraw[i][1] - this.minValue) * rescaleFactorY);

				g.setColor(Color.YELLOW);
				g.drawLine(xOld + leftOffset, yOld + topOffset, x + leftOffset, y + topOffset);
			}
			if (neighbourhood) {

				g.setColor(Color.MAGENTA);
				g.drawLine(xLast + leftOffset, yLast + topOffset, x + leftOffset, y + topOffset);

			}
			xLast = x;
			yLast = y;
		}
	}
}