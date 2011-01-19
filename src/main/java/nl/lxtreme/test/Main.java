/**
 * 
 */
package nl.lxtreme.test;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * @author jajans
 * 
 */
public class Main
{
	/**
	 * @author jajans
	 * 
	 */
	final class MyMouseListener extends MouseAdapter
	{
		private volatile int lastCursor = -1;
		private volatile boolean showing = false;

		@Override
		public void mouseDragged(final MouseEvent aEvent)
		{
			if (this.lastCursor < 0)
			{
				return;
			}

			dragCursor(this.lastCursor, aEvent.getPoint());
			aEvent.consume();
		}

		@Override
		public void mouseMoved(final MouseEvent aEvent)
		{
			final Point point = aEvent.getPoint();

			if (!this.showing)
			{
				if (aEvent.isControlDown())
				{
					this.showing = showHover(point);
					aEvent.consume();
				}
			}
			else
			{
				if (!aEvent.isControlDown())
				{
					this.showing = hideHover();
					aEvent.consume();
				}
				else
				{
					moveHover(point);
					aEvent.consume();
				}
			}
		}

		@Override
		public void mousePressed(final MouseEvent aEvent)
		{
			this.lastCursor = findCursor(aEvent.getPoint());
			aEvent.consume();
		}

		@Override
		public void mouseReleased(final MouseEvent aEvent)
		{
			if (this.lastCursor < 0)
			{
				return;
			}
			this.lastCursor = -1;
			aEvent.consume();
		}
	}

	/**
	 * @author jajans
	 * 
	 */
	final class MyResizeListener extends ComponentAdapter
	{
		@Override
		public void componentResized(final ComponentEvent aEvent)
		{
			resize();
		}
	}

	private Model model;

	private ModelView modelView;
	private CursorView cursorView;
	private ArrowView arrowView;

	private JFrame mainFrame;

	/**
	 * @param args
	 */
	public static void main(final String[] aArgs)
	{
		final Main main = new Main();
		main.init();
		main.build();
		main.run();
	}

	/**
	 * @return
	 */
	public Model getModel()
	{
		return this.model;
	}

	/**
	 * @return
	 */
	public Dimension getPreferredSize()
	{
		return this.mainFrame.getPreferredSize();
	}

	/**
	 * @param aCursorIdx
	 * @param aPoint
	 */
	final void dragCursor(final int aCursorIdx, final Point aPoint)
	{
		final Point point = convertToPoint(this.cursorView, aPoint);
		this.cursorView.moveCursor(aCursorIdx, point);
	}

	/**
	 * @param aPoint
	 * @return
	 */
	final int findCursor(final Point aPoint)
	{
		final Point point = convertToPoint(this.cursorView, aPoint);

		final int[] cursors = getModel().getCursors();
		for (int i = 0; i < cursors.length; i++)
		{
			if ((point.x > (cursors[i] - 5)) && (point.x < (cursors[i] + 5)))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * @param aPoint
	 * @return
	 */
	final boolean hideHover()
	{
		this.arrowView.hideHover();
		this.arrowView.hideRect();
		return false;
	}

	/**
	 * @param aPoint
	 */
	final void moveHover(final Point aPoint)
	{
		final Point point = convertToPoint(this.arrowView, aPoint);

		final Rectangle signalHover = this.modelView.getSignalHover(convertToPoint(this.modelView, aPoint));
		this.arrowView.moveHover(point);
		this.arrowView.moveRect(signalHover);
	}

	/**
	 * 
	 */
	final void resize()
	{
		final Dimension dims = this.mainFrame.getSize();
		final Insets insets = this.mainFrame.getInsets();

		final Rectangle rect = new Rectangle();
		rect.x = insets.left;
		rect.y = insets.top;
		rect.width = dims.width - insets.left - insets.right;
		rect.height = dims.height - insets.top - insets.bottom;

		this.modelView.setBounds(rect);
		this.arrowView.setBounds(rect);
		this.cursorView.setBounds(rect);
	}

	/**
	 * @param aPoint
	 * @return
	 */
	final boolean showHover(final Point aPoint)
	{
		final Point point = convertToPoint(this.arrowView, aPoint);

		final Rectangle signalHover = this.modelView.getSignalHover(convertToPoint(this.modelView, aPoint));

		this.arrowView.showHover(point);
		this.arrowView.showRect(signalHover);

		return true;
	}

	/**
	 * 
	 */
	private void build()
	{
		final JLayeredPane layeredPane = this.mainFrame.getLayeredPane();
		layeredPane.setPreferredSize(getPreferredSize());

		layeredPane.add(this.arrowView, Integer.valueOf(3));
		layeredPane.add(this.cursorView, Integer.valueOf(2));
		layeredPane.add(this.modelView, Integer.valueOf(1));

		final MyMouseListener listener = new MyMouseListener();
		layeredPane.addMouseListener(listener);
		layeredPane.addMouseMotionListener(listener);

		this.mainFrame.pack();
		resize();
	}

	/**
	 * @param aDestination
	 * @param aOriginal
	 * @return
	 */
	private Point convertToPoint(final Component aDestination, final Point aOriginal)
	{
		return SwingUtilities.convertPoint(this.mainFrame.getLayeredPane(), aOriginal, aDestination);
	}

	/**
	 * 
	 */
	private void init()
	{
		this.model = new Model(1024);

		final Dimension dims = new Dimension(640, 480);

		this.mainFrame = new JFrame("JLayeredPane test");
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.mainFrame.setPreferredSize(dims);
		this.mainFrame.setSize(dims);

		this.mainFrame.addComponentListener(new MyResizeListener());

		this.modelView = new ModelView(this);
		this.cursorView = new CursorView(this);
		this.arrowView = new ArrowView(this);
	}

	/**
	 * 
	 */
	private void run()
	{
		this.mainFrame.setVisible(true);
	}
}
