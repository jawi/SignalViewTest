/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;

import javax.swing.JComponent;


/**
 * @author jajans
 */
public class CursorView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final Main controller;

  // CONSTRUCTORS

  /**
   * Creates a new CursorView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public CursorView( final Main aController )
  {
    this.controller = aController;

    setOpaque( false );
  }

  // METHODS

  /**
   * Finds the cursor under the given point.
   * 
   * @param aPoint
   *          the coordinate of the potential cursor, cannot be
   *          <code>null</code>.
   * @return the cursor index, or -1 if not found.
   */
  public int findCursor( final Point aPoint )
  {
    final int[] cursors = this.controller.getModel().getCursors();
    for ( int i = 0; i < cursors.length; i++ )
    {
      if ( ( aPoint.x > ( cursors[i] - 5 ) ) && ( aPoint.x < ( cursors[i] + 5 ) ) )
      {
        return i;
      }
    }

    return -1;
  }

  /**
   * Moves the cursor with the given index to the given coordinate.
   * 
   * @param aCursorIdx
   *          the index of the cursor to move;
   * @param aPoint
   *          the coordinate to move the cursor to, cannot be <code>null</code>.
   */
  public void moveCursor( final int aCursorIdx, final Point aPoint )
  {
    repaintPartially( aCursorIdx );

    final int[] cursors = this.controller.getModel().getCursors();
    cursors[aCursorIdx] = aPoint.x;

    repaintPartially( aCursorIdx );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    final Graphics2D g2d = ( Graphics2D )aGraphics;

    final Rectangle clip = aGraphics.getClipBounds();

    final int y1 = clip.y;
    final int y2 = clip.y + clip.height;

    final int[] cursors = this.controller.getModel().getCursors();
    final Color[] colors = new Color[] { Color.RED, Color.GREEN };

    for ( int i = 0; i < cursors.length; i++ )
    {
      final int x = cursors[i];

      if ( clip.contains( x, y1 ) || clip.contains( x, y2 ) )
      {
        g2d.setColor( colors[i] );
        g2d.drawLine( x, y1, x, y2 );
      }
    }
  }

  /**
   * Repaints the areas that were affected by the last paint() call.
   * 
   * @param aCursorIdx
   *          the cursor index of the cursor to repaint.
   */
  private void repaintPartially( final int aCursorIdx )
  {
    final int[] cursors = this.controller.getModel().getCursors();

    final int x = cursors[aCursorIdx] - 1;
    final int y = 0;
    final int w = 2;
    final int h = getHeight();

    repaint( x, y, w, h );
  }
}
