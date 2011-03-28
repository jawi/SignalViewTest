/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * Provides a time line view, displaying ticks at regular intervals along with
 * timing information.
 */
final class TimeLineView extends JComponent
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

      final FontMetrics majorFM = canvas.getFontMetrics( this.majorTickFont );
      final FontMetrics minorFM = canvas.getFontMetrics( this.minorTickFont );
      final int minorFontHeight = minorFM.getHeight();

      final SampleDataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();

      final double zoomFactor = screenModel.getZoomFactor();
      final double sampleRate = dataModel.getSampleRate();

      final long startTimeStamp = getStartTimestamp( clip );
      final long endTimeStamp = getEndTimestamp( clip );

      final double timebase = getTimebase( endTimeStamp - startTimeStamp );

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

      // Draw the cursor "flags"...
      if ( !this.controller.isCursorMode() )
      {
        return;
      }

      canvas.setFont( this.minorTickFont );

      final int h = minorFM.getHeight() + 4;
      final int y1 = clip.height - h;

      final long[] cursors = this.controller.getDataModel().getCursors();
      for ( int i = 0; i < cursors.length; i++ )
      {
        final long cursorTime = cursors[i];

        final int x1 = this.controller.toScaledScreenCoordinate( cursorTime ).x;

        final String timeStr = String.format( "%s: %s", screenModel.getCursorLabel( i ),
            Utils.displayTime( cursorTime / sampleRate ) );
        final int w = minorFM.stringWidth( timeStr ) + 6;

        if ( clip.contains( x1, y1 ) || clip.contains( x1 + w, y1 + h ) )
        {
          canvas.setColor( screenModel.getCursorColor( i ) );

          canvas.drawRect( x1, y1, w, h - 1 );

          final int textXpos = x1 + 3;
          final int textYpos = y1 + minorFM.getLeading() + minorFM.getAscent() + 2;

          canvas.drawString( timeStr, textXpos, textYpos );
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
   * Determines the ending time stamp until which the time line should be drawn
   * given the clip boundaries of this component.
   * 
   * @param aClip
   *          the clip boundaries of this component, cannot be <code>null</code>
   *          .
   * @return the ending time stamp, as long value.
   */
  private long getEndTimestamp( final Rectangle aClip )
  {
    final Point location = new Point( aClip.x + aClip.width, 0 );

    final SampleDataModel dataModel = this.controller.getDataModel();
    final long[] timestamps = dataModel.getTimestamps();

    final int idx = Math.min( this.controller.toTimestampIndex( location ) + 1, timestamps.length - 1 );
    return timestamps[idx] + 1;
  }

  /**
   * Determines the starting time stamp from which the time line should be drawn
   * given the clip boundaries of this component.
   * 
   * @param aClip
   *          the clip boundaries of this component, cannot be <code>null</code>
   *          .
   * @return the starting time stamp, as long value.
   */
  private long getStartTimestamp( final Rectangle aClip )
  {
    final Point location = aClip.getLocation();

    final SampleDataModel dataModel = this.controller.getDataModel();
    final long[] timestamps = dataModel.getTimestamps();

    final int idx = Math.max( this.controller.toTimestampIndex( location ) - 1, 0 );
    return timestamps[idx];
  }

  /**
   * Determines the time base for the given absolute time (= total time
   * displayed).
   * 
   * @param aAbsoluteTime
   *          the absolute time displayed by this component, that is the ending
   *          time stamp - starting time stamp.
   * @return a time base, as power of 10.
   */
  private double getTimebase( final double aAbsoluteTime )
  {
    return Math.pow( 10, Math.round( Math.log10( aAbsoluteTime ) ) );
  }
}
