/**
 * 
 */
package nl.lxtreme.test;

import java.awt.*;

import javax.swing.*;

/**
 * @author jajans
 */
public class ModelView extends JComponent implements Scrollable
{
	// CONSTANTS

	private static final long serialVersionUID = 1L;

	// VARIABLES

	private final ScreenController controller;

	// CONSTRUCTORS

	/**
	 * Creates a new ModelView instance.
	 * 
	 * @param aController
	 *          the controller to use, cannot be <code>null</code>.
	 */
	public ModelView(final ScreenController aController)
	{
		this.controller = aController;

		setOpaque(true);
		setBackground(Color.BLACK);

		this.controller.setModelView(this);
	}

	// METHODS

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getScrollableBlockIncrement(final Rectangle aVisibleRect, final int aOrientation, final int aDirection)
	{
		if (aOrientation == SwingConstants.HORIZONTAL)
		{
			return aVisibleRect.width - 50;
		}
		else
		{
			return aVisibleRect.height - this.controller.getScreenModel().getChannelHeight();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getScrollableUnitIncrement(final Rectangle aVisibleRect, final int aOrientation, final int aDirection)
	{
		int currentPosition = 0;
		final int maxUnitIncrement;
		if (aOrientation == SwingConstants.HORIZONTAL)
		{
			currentPosition = aVisibleRect.x;
			maxUnitIncrement = 50;
		}
		else
		{
			currentPosition = aVisibleRect.y;
			maxUnitIncrement = this.controller.getScreenModel().getChannelHeight();
		}

		// Return the number of pixels between currentPosition
		// and the nearest tick mark in the indicated direction.
		if (aDirection < 0)
		{
			final int newPosition = currentPosition - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		}
		else
		{
			return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement - currentPosition;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(final Graphics aGraphics)
	{
		final Graphics2D g2d = (Graphics2D) aGraphics;

		final Rectangle clip = aGraphics.getClipBounds();

		g2d.setColor(getBackground());
		g2d.fillRect(clip.x, clip.y, clip.width, clip.height);

		final DataModel dataModel = this.controller.getDataModel();

		final int[] values = dataModel.getValues();
		final int[] timestamps = dataModel.getTimestamps();

		final int startIdx = Math.max(0, this.controller.toTimestampIndex(clip.getLocation()) - 1);
		final int endIdx = Math.min(this.controller.toTimestampIndex(new Point(clip.x + clip.width, 0)) + 1, values.length - 1);

		final int size = (endIdx - startIdx);
		final int[] x = new int[size];
		final int[] y = new int[size];

		final ScreenModel screenModel = this.controller.getScreenModel();
		int dy = screenModel.getSignalHeight();

		final int width = dataModel.getWidth();
		for (int b = 0; b < width; b++)
		{
			final int mask = (1 << b);

			for (int i = 0; i < size; i++)
			{
				final int sampleIdx = i + startIdx;
				final int value = (values[sampleIdx] & mask) == 0 ? 0 : screenModel.getSignalHeight();
				final int timestamp = timestamps[sampleIdx];

				x[i] = this.controller.toScaledScreenCoordinate(timestamp).x;
				y[i] = dy + value;
			}

			g2d.setColor(Color.GREEN.darker().darker());
			g2d.drawPolyline(x, y, size);

			dy += screenModel.getChannelHeight();
		}
	}
}
