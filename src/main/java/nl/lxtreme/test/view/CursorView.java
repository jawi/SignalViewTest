/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.model.*;


/**
 * @author jajans
 */
class CursorView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;
  private Point lastPoint;

  // CONSTRUCTORS

  /**
   * Creates a new CursorView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public CursorView( final SignalDiagramController aController )
  {
    this.controller = aController;

    setOpaque( false );
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
    final Long[] cursors = getCursors();

    repaintPartially( cursors, aCursorIdx );

    cursors[aCursorIdx] = this.controller.toUnscaledScreenCoordinate( aPoint );

    if ( this.controller.isSnapModeEnabled() )
    {
      this.lastPoint = aPoint;
    }
    else
    {
      this.lastPoint = null;
    }

    repaintPartially( cursors, aCursorIdx );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( !this.controller.isCursorMode() )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = aGraphics.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      g2d.setRenderingHints( createRenderingHints() );

      final int y1 = clip.y;
      final int y2 = clip.y + clip.height;

      final ScreenModel screenModel = this.controller.getScreenModel();
      final Long[] cursors = getCursors();

      for ( int i = 0; i < cursors.length; i++ )
      {
        final Long cursorTimestamp = cursors[i];
        if ( cursorTimestamp == null )
        {
          continue;
        }

        final int x = this.controller.toScaledScreenCoordinate( cursorTimestamp ).x;

        if ( clip.contains( x, y1 ) || clip.contains( x, y2 ) )
        {
          g2d.setColor( screenModel.getCursorColor( i ) );
          g2d.drawLine( x, y1, x, y2 );

          if ( this.controller.isSnapModeEnabled() && inArea( this.lastPoint, x ) )
          {
            g2d.drawOval( x - 4, this.lastPoint.y - 4, 8, 8 );
          }
        }
      }
    }
    finally
    {
      g2d.dispose();
    }
  }

  /**
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
  }

  /**
   * @return
   */
  private Long[] getCursors()
  {
    return this.controller.getDataModel().getCursors();
  }

  /**
   * Repaints the areas that were affected by the last paint() call.
   * 
   * @param aCursorIdx
   *          the cursor index of the cursor to repaint.
   */
  private void repaintPartially( final Long[] aCursors, final int aCursorIdx )
  {
    int x, y, w, h;

    final Long cursorTimestamp = aCursors[aCursorIdx];
    if ( cursorTimestamp != null )
    {
      x = this.controller.toScaledScreenCoordinate( cursorTimestamp ).x - 1;
      y = 0;
      w = 2;
      h = getHeight();

      repaint( x, y, w, h );
    }

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
