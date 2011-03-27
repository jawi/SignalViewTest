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
  public static final int TIMELINE_INCREMENT = 10;
  /** The height of this component. */
  public static final int TIMELINE_HEIGHT = 40;

  private static final int SHORT_TICK_HEIGHT = 4;
  private static final int PADDING_Y = 1;

  private static final int BASE_TICK_Y_POS = TIMELINE_HEIGHT - PADDING_Y;
  private static final int SHORT_TICK_Y_POS = TIMELINE_HEIGHT - PADDING_Y - SHORT_TICK_HEIGHT;
  private static final int LONG_TICK_Y_POS = TIMELINE_HEIGHT - PADDING_Y - 2 * SHORT_TICK_HEIGHT;

  // VARIABLES

  private final SignalDiagramController controller;

  private final Font majorTickFont;
  private final Font minorTickFont;

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

    setBackground( Utils.parseColor( "#1E2126" ) );

    final Font baseFont = ( Font )UIManager.get( "Label.font" );
    this.minorTickFont = baseFont.deriveFont( baseFont.getSize() * 0.8f );
    this.majorTickFont = baseFont.deriveFont( baseFont.getSize() * 0.9f );
  }

  // METHODS

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setColor( getBackground() );
      canvas.fillRect( clip.x, clip.y, clip.width, clip.height );

      final SampleDataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();

      final FontMetrics majorFM = canvas.getFontMetrics( this.majorTickFont );
      final int majorFontHeight = majorFM.getHeight();

      final FontMetrics minorFM = canvas.getFontMetrics( this.minorTickFont );
      final int minorFontHeight = minorFM.getHeight();

      final long[] timestamps = dataModel.getTimestamps();
      final double zoomFactor = screenModel.getZoomFactor();
      final double sampleRate = dataModel.getSampleRate();

      final int startIdx = getStartIndex( clip );
      final int endIdx = getEndIndex( clip, timestamps.length );

      final long startTimeStamp = timestamps[startIdx];
      final long endTimeStamp = timestamps[endIdx];

      final long timeline = endTimeStamp - startTimeStamp;
      final double timebase = getTimebase( timeline );

      double tickIncr = Math.max( 1.0, timebase / TIMELINE_INCREMENT );
      double timeIncr = Math.max( 1.0, timebase / ( 10.0 * TIMELINE_INCREMENT ) );

      double timestamp = ( Math.ceil( startTimeStamp / tickIncr ) * tickIncr );
      double majorTimestamp = timestamp;

      for ( ; timestamp <= endTimeStamp; timestamp += timeIncr )
      {
        int relXpos = ( int )( timestamp * zoomFactor );

        if ( ( timestamp % tickIncr ) == 0 )
        {
          boolean major = ( ( timestamp % timebase ) == 0 );

          final String time;
          final int textWidth;
          final int textHeight;

          if ( major )
          {
            majorTimestamp = timestamp;
            final double tickTime = majorTimestamp / sampleRate;
            time = Utils.displayTime( tickTime, 3, "", true /* aIncludeUnit */);

            canvas.setFont( this.majorTickFont );
            textWidth = majorFM.stringWidth( time ) + 2;
            textHeight = 2 * minorFontHeight;
          }
          else
          {
            final double tickTime = ( timestamp - majorTimestamp ) / sampleRate;
            time = "+" + Utils.displayTime( tickTime, 1, "", true /* aIncludeUnit */);

            canvas.setFont( this.minorTickFont );
            textWidth = minorFM.stringWidth( time ) + 2;
            textHeight = minorFontHeight;
          }

          int textXpos = Math.max( 1, ( int )( relXpos - ( textWidth / 2.0 ) ) );
          int textYpos = Math.max( 1, ( BASE_TICK_Y_POS - textHeight ) );

          canvas.setColor( Color.LIGHT_GRAY );

          canvas.drawString( time, textXpos, textYpos );

          if ( major )
          {
            canvas.setColor( Color.LIGHT_GRAY.brighter() );
          }

          canvas.drawLine( relXpos, BASE_TICK_Y_POS, relXpos, LONG_TICK_Y_POS );
        }
        else
        {
          canvas.setColor( Color.DARK_GRAY );

          canvas.drawLine( relXpos, BASE_TICK_Y_POS, relXpos, SHORT_TICK_Y_POS );
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
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    return new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
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

  /**
   * @param aTimeline
   * @return
   */
  private double getTimebase( final double aTimeline )
  {
    double result = Math.pow( 10, Math.round( Math.log10( aTimeline ) ) );
    return result;
  }
}
