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
public class ModelView extends JComponent
{
	private static final long serialVersionUID = 1L;

	private final Main controller;

	/**
	 * @param aMain
	 */
	public ModelView(final Main aMain)
	{
		this.controller = aMain;

		setOpaque(true);

		setBackground(Color.BLACK);
	}

	/**
	 * @param aOriginalPoint
	 * @return
	 */
	public Rectangle getSignalHover(final Point aOriginalPoint)
	{
		final Rectangle rect = new Rectangle();

		final Model model = this.controller.getModel();
		final int signalWidth = model.getWidth();

		// XXX 20 = initial dy; 30 = spacing between signals
		final int y = (aOriginalPoint.y - 20) / 30;
		if ((y < 0) || (y > (signalWidth - 1)))
		{
			return null;
		}

		rect.x = rect.width = 0;
		rect.y = (y * 30) + 20;
		rect.height = 20;

		// find timevalue...
		final int[] values = model.getValues();

		final int xPos = aOriginalPoint.x;
		if ((xPos >= 0) && (xPos < values.length))
		{
			final int mask = (1 << y);

			final int refValue = (values[xPos] & mask);

			rect.x = xPos;
			do
			{
				rect.x--;
			} while ((rect.x >= 0) && ((values[rect.x] & mask) == refValue));

			rect.width = aOriginalPoint.x;
			do
			{
				rect.width++;
			} while ((rect.width < values.length) && ((values[rect.width] & mask) == refValue));
			// correct to actual width...
			rect.width -= rect.x;
		}

		return rect;
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

		g2d.setColor(getBackground());
		g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

		final Model model = this.controller.getModel();
		final int[] values = model.getValues();
		final int[] timestamps = model.getTimestamps();
		final int width = model.getWidth();

		final int size = values.length;

		final int[] x = new int[size];
		final int[] y = new int[size];

		int dy = 20;

		for (int b = 0; b < width; b++)
		{
			final int mask = (1 << b);

			for (int i = 0; i < size; i++)
			{
				final int value = (values[i] & mask) == 0 ? 0 : 20;
				final int timestamp = timestamps[i];

				x[i] = timestamp;
				y[i] = dy + value;
			}

			g2d.setColor(Color.BLUE);
			g2d.drawPolyline(x, y, size);

			dy += 30;
		}
	}
}
