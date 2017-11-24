package erozic.optimisation.applications.gui_utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JComponent;

import erozic.optimisation.functions.Function;

/**
 * This is a graphics component that draws the background function and the particles
 * which are trying to locate its extremum (in red).
 * 
 * The function is assumed to have a 2D domain and is drawn in greyscale where black is
 * the minumum and white the maximum.
 * 
 * @author Eugen Rožić
 *
 */
public class FunctionVisualisation extends JComponent {

	private static final long serialVersionUID = 1L;
	
	protected Function function = null;
	protected double maxValue;
	protected double minValue;
	
	private BufferedImage background = null;
	
	protected double[][] toDraw = null;
	
	protected int leftPadding = 0;
	protected int rightPadding = 0;
	protected int topPadding = 0;
	protected int bottomPadding = 0;
	
	/**
	 * @param function The function to draw in the background.
	 */
	public FunctionVisualisation(Function function, double minValue, double maxValue) {
		
		this.function = function;
		this.maxValue = maxValue;
		this.minValue = minValue;
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				//createBackground();
				super.componentResized(e);
			}
		});
	}
	
	/**
	 * Changes the function to draw and repaints removing the particles.
	 */
	public void changeFunction(Function function) {
		this.function = function;
		background = null;
		toDraw = null;
		this.repaint();
	}
	
	/**
	 * Changes the domain boundaries of the function and repaints removing the particles.
	 */
	public void changeDomainBoundaries(double minValue, double maxValue) {
		this.maxValue = maxValue;
		this.minValue = minValue;
		background = null;
		toDraw = null;
		this.repaint();
	}
	
	/**
	 * Sets the solutions (particles) to draw and schedules the repaint to show the changes.
	 */
	public void setToDraw(double[][] toDraw) {
		this.toDraw = toDraw;
		this.repaint();
	}
	
	private void createBackground() {
		
		Insets insets = this.getInsets();
		
		int leftOffset = leftPadding + insets.left;
		int rightOffset = rightPadding + insets.right;
		int topOffset = topPadding + insets.top;
		int bottomOffset = bottomPadding + insets.bottom;

		int usedWidth = this.getWidth() - leftOffset - rightOffset;
		int usedHeight = this.getHeight() - topOffset - bottomOffset;
		
		double rescaleFactorX = (this.maxValue - this.minValue) / usedWidth;
		double rescaleFactorY = (this.maxValue - this.minValue) / usedHeight;
		
		double[][] values = new double[usedWidth][usedHeight];
		double maxVal = Double.NEGATIVE_INFINITY;
		double minVal = Double.POSITIVE_INFINITY;
		double[] domainPoint = new double[2];
		
		for (int x = 0; x < usedWidth; x++) {
			for (int y = 0; y < usedHeight; y++) {
				
				domainPoint[0] = this.minValue + (x * rescaleFactorX);
				domainPoint[1] = this.maxValue - (y * rescaleFactorY);
				
				values[x][y] = function.value(domainPoint);
				if (values[x][y] > maxVal) {
					maxVal = values[x][y];
				} else if (values[x][y] < minVal) {
					minVal = values[x][y];
				}
			}
		}
		
		double valueRange = maxVal - minVal;
		
		background = new BufferedImage(usedWidth, usedHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = background.getRaster();
		int[] pixel = {0};
		
		for (int x = 0; x < usedWidth; x++) {
			for (int y = 0; y < usedHeight; y++) {
				pixel[0] = (int)(255 * (values[x][y] - minVal) / valueRange);
				raster.setPixel(x, y, pixel);
			}
		}
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
		
		//if (background == null)
			createBackground();
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(background, null, leftOffset, topOffset);
		
		if (toDraw == null)
			return;
		
		double rescaleFactorX = usedWidth / (this.maxValue - this.minValue);
		double rescaleFactorY = usedHeight / (this.maxValue - this.minValue);
		
		for (double[] point : toDraw) {

			int x = (int) ((point[0] - this.minValue) * rescaleFactorX);
			int y = (int) ((point[1] - this.minValue) * rescaleFactorY);

			g.setColor(Color.RED);
			g.fillRect(x + leftOffset - 1, y + topOffset - 1, 3, 3);
		}
	}
}