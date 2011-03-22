/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.dnd.*;


/**
 * Provides a glass pane for use while dragging channels around. This glass pane
 * will show a marker where the drop location of the channel will be.
 */
public class GhostGlassPane extends JPanel
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final int CHANNEL_ROW_MARKER_WIDTH = 100;
  private static final int CHANNEL_ROW_MARKER_HEIGHT = 2;

  private static final int CURSOR_MARKER_WIDTH = 2;
  private static final int CURSOR_MARKER_HEIGHT = Integer.MAX_VALUE;

  private static final Stroke INDICATOR_STROKE = new BasicStroke( 1.5f );

  // VARIABLES

  private volatile Point oldDropPoint;
  private volatile Point dropPoint;
  private volatile DragAndDropContext context;

  private final float alpha = 0.7f;

  // CONSTRUCTORS

  /**
   * Creates a new {@link GhostGlassPane} instance.
   */
  public GhostGlassPane()
  {
    setOpaque( false );
  }

  // METHODS

  /**
   * Clears the location where the channel/cursor might be dropped. This
   * location is used to draw a marker indicating the drop point.
   */
  public void clearDropPoint()
  {
    this.oldDropPoint = null;
    this.dropPoint = null;
  }

  /**
   * Repaints only the affected areas of this glass pane.
   * 
   * @see #setDropPoint(Point)
   */
  public void repaintPartially()
  {
    int x, y, width, height;
    if ( DragAndDropContext.CHANNEL_ROW == this.context )
    {
      width = Math.min( getWidth(), CHANNEL_ROW_MARKER_WIDTH ) + 2;
      height = CHANNEL_ROW_MARKER_HEIGHT + 2;
    }
    else
    {
      width = CURSOR_MARKER_WIDTH + 2;
      height = Math.min( getHeight(), CURSOR_MARKER_HEIGHT ) + 2;
    }

    if ( this.oldDropPoint != null )
    {
      x = Math.max( 0, this.oldDropPoint.x - 1 );
      y = Math.max( 0, this.oldDropPoint.y - 1 );
      repaint( x, y, width, height );
    }
    if ( this.dropPoint != null && !this.dropPoint.equals( oldDropPoint ) )
    {
      x = Math.max( 0, this.dropPoint.x - 1 );
      y = Math.max( 0, this.dropPoint.y - 1 );
      repaint( x, y, width, height );
    }
  }

  /**
   * Sets the location where the channel/cursor might be dropped. This location
   * is used to draw a marker indicating the drop point.
   * 
   * @param aLocation
   *          the location where the channel/cursor might be dropped, cannot be
   *          <code>null</code>;
   * @param aContext
   *          the drag and drop context, cannot be <code>null</code>.
   */
  public void setDropPoint( final Point aLocation, final DragAndDropContext aContext )
  {
    this.oldDropPoint = this.dropPoint;
    this.dropPoint = aLocation;
    this.context = aContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( ( this.dropPoint == null ) || !isVisible() )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, this.alpha ) );
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

      final Rectangle clip = g2d.getClipBounds();

      int x = this.dropPoint.x;
      int y = this.dropPoint.y;

      if ( DragAndDropContext.CHANNEL_ROW == this.context )
      {
        g2d.setColor( Color.YELLOW );
        g2d.setStroke( INDICATOR_STROKE );
        g2d.drawLine( x, y, x + CHANNEL_ROW_MARKER_WIDTH, y );
      }
      else
      {
        g2d.setColor( Color.LIGHT_GRAY );
        g2d.setStroke( INDICATOR_STROKE );
        g2d.drawLine( x, y, x, clip.height );
      }
    }
    finally
    {
      g2d.dispose();
    }
  }
}
