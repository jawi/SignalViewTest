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
public class CursorView extends JComponent
{
	private static final long serialVersionUID = 1L;

	private final Main controller;

	/**
	 * @param aMain
	 */
	public CursorView(final Main aMain)
	{
		this.controller = aMain;

		setOpaque(false);
	}

	/**
	 * @param aCursorIdx
	 * @param aPoint
	 */
	public void moveCursor(final int aCursorIdx, final Point aPoint)
	{
		repaintPartially(aCursorIdx);
		final int[] cursors = this.controller.getModel().getCursors();
		cursors[aCursorIdx] = aPoint.x;
		repaintPartially(aCursorIdx);
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

		final int y1 = clip.y + insets.top;
		final int y2 = clip.y + clip.height - insets.bottom;

		final int[] cursors = this.controller.getModel().getCursors();

		final Color[] colors = new Color[] { Color.RED, Color.GREEN };

		for (int i = 0; i < 2; i++)
		{
			final int c = cursors[i];

			g2d.setColor(colors[i]);
			g2d.drawLine(c, y1, c, y2);
		}
	}

	private void repaintPartially(final int aCursorIdx)
	{
		final int[] cursors = this.controller.getModel().getCursors();

		final int x = cursors[aCursorIdx] - 1;
		final int y = 0;
		final int w = 2;
		final int h = getHeight();

		repaint(25L, x, y, w, h);
	}
}
