/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;


/**
 * Provides a glass pane for use while dragging channels around. This glass pane
 * will show a marker where the drop location of the channel will be.
 */
public class GhostGlassPane extends JPanel
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final int INDICATOR_WIDTH = 100;
  private static final int INDICATOR_HEIGHT = 2;

  private static final Stroke INDICATOR_STROKE = new BasicStroke( INDICATOR_HEIGHT );

  // VARIABLES

  private Point droppedRow;
  private Point oldDroppedRow;

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
   * Repaints only the affected areas of this glass pane.
   * 
   * @see #setDropChannelPoint(Point)
   */
  public void repaintPartially()
  {
    int width = INDICATOR_WIDTH + 2;
    int height = INDICATOR_HEIGHT + 2;

    if ( this.oldDroppedRow != null )
    {
      int x = this.oldDroppedRow.x - 1;
      int y = this.oldDroppedRow.y - 1;
      repaint( x, y, width, height );
    }
    if ( this.droppedRow != null )
    {
      int x = this.droppedRow.x - 1;
      int y = this.droppedRow.y - 1;
      repaint( x, y, width, height );
    }
  }

  /**
   * Sets the location where the channel might be dropped. This location is used
   * to draw a small marker indicating the drop point.
   * 
   * @param aLocation
   *          the location where the channel might be dropped, cannot be
   *          <code>null</code>.
   */
  public void setDropChannelPoint( final Point aLocation )
  {
    this.oldDroppedRow = ( aLocation == null ) ? null : this.droppedRow;
    this.droppedRow = aLocation;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( ( this.droppedRow == null ) || !isVisible() )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, this.alpha ) );
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

      int x = this.droppedRow.x;
      int y = this.droppedRow.y;

      g2d.setColor( Color.YELLOW );
      g2d.setStroke( INDICATOR_STROKE );
      g2d.drawLine( x, y, x + INDICATOR_WIDTH, y );
    }
    finally
    {
      g2d.dispose();
    }
  }
}
