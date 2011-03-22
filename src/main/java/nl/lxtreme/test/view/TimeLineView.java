/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * @author jawi
 */
class TimeLineView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  /** The tick increment (in pixels). */
  public static final int TIMELINE_INCREMENT = 20;
  /** The height of this component. */
  public static final int TIMELINE_HEIGHT = 30;

  private static final int LONG_TICK_INTERVAL = 10;

  private static final int SHORT_TICK_HEIGHT = 4;
  private static final int PADDING_Y = 1;

  // VARIABLES

  private final SignalDiagramController controller;
  private final Font labelFont;

  // CONSTRUCTORS

  /**
   * Creates a new {@link TimeLineView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public TimeLineView( final SignalDiagramController aController )
  {
    this.controller = aController;

    setOpaque( true );
    setBackground( Color.BLACK );

    this.labelFont = ( Font )UIManager.get( "Label.font" );
  }

  // METHODS

  @Override
  public Dimension getPreferredSize()
  {
    Dimension result = super.getPreferredSize();
    return new Dimension( result.width, TIMELINE_HEIGHT );
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    Graphics canvas = aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();

      canvas.setColor( getBackground() );
      canvas.fillRect( clip.x, clip.y, clip.width, clip.height );

      final SampleDataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();

      final FontMetrics fm = canvas.getFontMetrics( this.labelFont );

      final long[] timestamps = dataModel.getTimestamps();
      final double zoomFactor = screenModel.getZoomFactor();

      final int startIdx = getStartIndex( clip );
      final int endIdx = getEndIndex( clip, timestamps.length );

      final double tickIncr = ( TIMELINE_INCREMENT / zoomFactor );

      final double pixelTimeInterval = ( zoomFactor / dataModel.getSampleRate() );
      System.out.println( "tickInterval = " + Utils.displayTime( pixelTimeInterval ) + ", " + tickIncr );

      System.out.println( "start = " + startIdx + " => " + endIdx );

      canvas.setColor( Color.GRAY );

      final int y1 = TIMELINE_HEIGHT - PADDING_Y - SHORT_TICK_HEIGHT;
      final int y2 = TIMELINE_HEIGHT - PADDING_Y;

      for ( int i = clip.x; i < clip.width; i += TIMELINE_INCREMENT )
      {
        int relXpos = ( i - clip.x );

        // System.out.println( "relXpos[" + i + "] = " + Utils.displayTime( i /
        // (
        // double )dataModel.getSampleRate() ) );

        canvas.drawLine( relXpos, y1, relXpos, y2 );
        if ( i % 100 == 0 )
        {
          canvas.drawString( Utils.displayTime( i * pixelTimeInterval ), relXpos, y1 );
        }
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;
    }
  }

  /**
   * @param aClip
   * @return
   */
  private int getEndIndex( final Rectangle aClip, final int aLength )
  {
    final Point location = new Point( aClip.x + aClip.width, 0 );
    return Math.min( this.controller.toTimestampIndex( location ) + 1, aLength - 1 );
  }

  /**
   * @param aClip
   * @return
   */
  private int getStartIndex( final Rectangle aClip )
  {
    final Point location = aClip.getLocation();
    return Math.max( this.controller.toTimestampIndex( location ) - 1, 0 );
  }

}
