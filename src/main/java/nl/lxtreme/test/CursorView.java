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

  private final ScreenController controller;
  private boolean snapMode;

  private Point lastPoint;

  // CONSTRUCTORS

  /**
   * Creates a new CursorView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public CursorView( final ScreenController aController )
  {
    this.controller = aController;

    setOpaque( false );

    this.controller.setCursorView( this );
  }

  // METHODS

  private static boolean inArea( final Point aPoint, final int aX )
  {
    if ( aPoint == null )
    {
      return false;
    }
    return ( aX >= aPoint.x - 5 ) && ( aX <= aPoint.x + 5 );
  }

  /**
   * @return
   */
  public boolean isSnapModeEnabled()
  {
    return this.snapMode;
  }

  /**
   * Moves the cursor with the given index to the given coordinate.
   * 
   * @param aCursorIdx
   *          the index of the cursor to move;
   * @param aPoint
   *          the coordinate to move the cursor to, cannot be <code>null</code>;
   * @param aSnap
   *          if <code>true</code> snaps to the signal value, <code>false</code>
   *          otherwise.
   */
  public void moveCursor( final int aCursorIdx, final Point aPoint )
  {
    repaintPartially( aCursorIdx );

    final int[] cursors = this.controller.getDataModel().getCursors();
    cursors[aCursorIdx] = this.controller.toUnscaledScreenCoordinate( aPoint );

    if ( this.snapMode )
    {
      this.lastPoint = aPoint;
    }
    else
    {
      this.lastPoint = null;
    }

    repaintPartially( aCursorIdx );
  }

  /**
   * @param aSnapMode
   */
  public synchronized void setSnapMode( final boolean aSnapMode )
  {
    this.snapMode = aSnapMode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    final Graphics2D g2d = ( Graphics2D )aGraphics.create();

    final Rectangle clip = aGraphics.getClipBounds();

    final int y1 = clip.y;
    final int y2 = clip.y + clip.height;

    final int[] cursors = this.controller.getDataModel().getCursors();
    final Color[] colors = new Color[] { Color.RED, Color.GREEN };

    for ( int i = 0; i < cursors.length; i++ )
    {
      final int x = this.controller.toScaledScreenCoordinate( cursors[i] ).x;

      if ( clip.contains( x, y1 ) || clip.contains( x, y2 ) )
      {
        g2d.setColor( colors[i] );
        g2d.drawLine( x, y1, x, y2 );

        if ( this.snapMode && inArea( this.lastPoint, x ) )
        {
          g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
          g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );

          g2d.drawOval( x - 4, this.lastPoint.y - 4, 8, 8 );
        }
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
    int x, y, w, h;

    final int[] cursors = this.controller.getDataModel().getCursors();
    x = this.controller.toScaledScreenCoordinate( cursors[aCursorIdx] ).x - 1;
    y = 0;
    w = 2;
    h = getHeight();

    repaint( x, y, w, h );

    if ( this.lastPoint != null )
    {
      x = this.lastPoint.x - 5;
      y = this.lastPoint.y - 5;
      w = 10;
      h = 10;

      repaint( x, y, w, h );
    }
  }
}
