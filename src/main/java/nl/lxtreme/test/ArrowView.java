/**
 * 
 */
package nl.lxtreme.test;

import java.awt.*;

import javax.swing.JComponent;

/**
 * @author jajans
 * 
 */
public class ArrowView extends JComponent
{
	private static final long serialVersionUID = 1L;

	private final Main controller;

	private volatile Point mousePoint;
	private volatile Rectangle rectangle;

	/**
	 * @param aMain
	 */
	public ArrowView(final Main aMain)
	{
		this.controller = aMain;

		setOpaque(false);
	}

	/**
	 * @param aPoint
	 */
	public void hideHover()
	{
		repaintPartially();
		this.mousePoint = null;
	}

	/**
	 * 
	 */
	public void hideRect()
	{
		repaintPartially();
		this.rectangle = null;
	}

	/**
	 * @param aPoint
	 */
	public void moveHover(final Point aPoint)
	{
		repaintPartially();
		this.mousePoint = aPoint;
		repaintPartially();
	}

	public void moveRect(final Rectangle aRectangle)
	{
		repaintPartially();
		this.rectangle = aRectangle;
		repaintPartially();
	}

	public void showHover(final Point aPoint)
	{
		this.mousePoint = aPoint;
		repaintPartially();
	}

	public void showRect(final Rectangle aRectangle)
	{
		this.rectangle = aRectangle;
		repaintPartially();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics aG)
	{
		final Graphics2D g2d = (Graphics2D) aG;

		final Rectangle clip = aG.getClipBounds();
		final Insets insets = getInsets();

		final Rectangle rect = new Rectangle();
		rect.x = insets.left + clip.x;
		rect.y = insets.top + clip.y;
		rect.width = clip.width - insets.left - insets.right;
		rect.height = clip.height - insets.top - insets.bottom;

		final int[] cursors = this.controller.getModel().getCursors();

		if (this.mousePoint != null)
		{
			final int x1 = cursors[0];
			final int y1 = this.mousePoint.y;
			final int x2 = cursors[1];
			final int y2 = this.mousePoint.y;

			g2d.setColor(Color.YELLOW);
			g2d.drawLine(x1, y1, x2, y2);
		}

		if (this.rectangle != null)
		{
			final double yOffset = this.rectangle.getCenterY();
			final int x1 = this.rectangle.x + 2;
			final int x2 = this.rectangle.x + this.rectangle.width - 2;
			final int y = (int) (yOffset);

			g2d.setColor(Color.CYAN);
			drawDoubleHeadedArrow(g2d, x1, y, x2, y);
		}
	}

	/**
	 * @param aG2D
	 * @param aXpos
	 * @param aYpos
	 * @param aFactor
	 */
	private void drawArrowHead(final Graphics aG, final int aXpos, final int aYpos, final int aFactor)
	{
		final Polygon arrowHead = new Polygon();
		arrowHead.addPoint(aXpos, aYpos);
		arrowHead.addPoint(aXpos + (aFactor * 6), aYpos - 3);
		arrowHead.addPoint(aXpos + (aFactor * 6), aYpos + 3);

		final Graphics2D g = (Graphics2D) aG.create();
		g.fill(arrowHead);
		g.dispose();
	}

	/**
	 * @param aG
	 * @param aX1
	 * @param aY1
	 * @param aX2
	 * @param aY2
	 */
	private void drawDoubleHeadedArrow(final Graphics aG, final int aX1, final int aY1, final int aX2, final int aY2)
	{
		drawDoubleHeadedArrow(aG, aX1, aY1, aX2, aY2, 4, 4);
	}

	/**
	 * @param aG
	 * @param aX1
	 * @param aY1
	 * @param aX2
	 * @param aY2
	 * @param aArrowWidth
	 * @param aArrowHeight
	 */
	private void drawDoubleHeadedArrow(final Graphics aG, final int aX1, final int aY1, final int aX2, final int aY2, final int aArrowWidth, final int aArrowHeight)
	{
		final int lineWidth = Math.abs(aX2 - aX1);
		if (lineWidth > 8)
		{
			drawArrowHead(aG, aX1, aY1, 1);
		}
		aG.drawLine(aX1, aY1, aX2, aY2);
		if (lineWidth > 8)
		{
			// why x2 needs to be shifted by one pixel is beyond me...
			drawArrowHead(aG, aX2 + 1, aY2, -1);
		}
	}

	/**
	 * 
	 */
	private void repaintPartially()
	{
		if (this.rectangle != null)
		{
			final int x = this.rectangle.x - 1;
			final int y = this.rectangle.y - 1;
			final int w = this.rectangle.width + 2;
			final int h = this.rectangle.height + 2;

			repaint(25L, x, y, w, h);
		}

		if (this.mousePoint != null)
		{
			final int[] cursors = this.controller.getModel().getCursors();
			final int x = cursors[0] - 1;
			final int y = this.mousePoint.y - 1;
			final int w = (cursors[1] - cursors[0]) + 5;
			final int h = 2;

			repaint(25L, x, y, w, h);
		}
	}
}
