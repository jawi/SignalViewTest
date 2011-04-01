/**
 * 
 */
package nl.lxtreme.test.view.renderer;


import java.awt.*;


/**
 * @author jawi
 */
public class ChannelInsertionPointRenderer extends BaseRenderer
{
  // CONSTANTS

  private static final int CHANNEL_ROW_MARKER_WIDTH = 100;

  private static final Stroke INDICATOR_STROKE = new BasicStroke( 1.5f );

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    // NO-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Rectangle render( final Graphics2D aCanvas )
  {
    aCanvas.setStroke( INDICATOR_STROKE );

    aCanvas.drawLine( 0, 0, CHANNEL_ROW_MARKER_WIDTH, 0 );

    return new Rectangle( -2, -2, CHANNEL_ROW_MARKER_WIDTH + 4, 4 );
  }
}
