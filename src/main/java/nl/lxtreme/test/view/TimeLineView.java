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
  public static final int TIMELINE_HEIGHT = 30;

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

    setBackground( Utils.parseColor( "#1E2126" ) );

    final Font baseFont = ( Font )UIManager.get( "Label.font" );
    this.labelFont = baseFont.deriveFont( baseFont.getSize() * 0.9f );
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

      final FontMetrics fm = canvas.getFontMetrics( this.labelFont );

      final long[] timestamps = dataModel.getTimestamps();
      final double zoomFactor = screenModel.getZoomFactor();
      final double sampleRate = dataModel.getSampleRate();

      final int startIdx = getStartIndex( clip );
      final int endIdx = getEndIndex( clip, timestamps.length );

      final long startTimeStamp = timestamps[startIdx];
      final long endTimeStamp = timestamps[endIdx];

      final double timebase = getTimebase( ( endTimeStamp - startTimeStamp ), sampleRate );
      final double tickIncr = ( timebase / TIMELINE_INCREMENT );

      final int y1 = TIMELINE_HEIGHT - PADDING_Y;
      final int y2 = TIMELINE_HEIGHT - PADDING_Y - SHORT_TICK_HEIGHT;
      final int y3 = TIMELINE_HEIGHT - PADDING_Y - 2 * SHORT_TICK_HEIGHT;

      double timestamp = ( startTimeStamp / TIMELINE_INCREMENT ) * TIMELINE_INCREMENT;
      double majorTimestamp = timestamp;
      while ( timestamp < endTimeStamp )
      {
        int relXpos = ( int )( timestamp * zoomFactor );

        if ( ( ( timestamp - startTimeStamp ) % TIMELINE_INCREMENT ) == 0 )
        {
          boolean major = ( ( ( timestamp - startTimeStamp ) % timebase ) == 0 );

          final String time;
          if ( major )
          {
            majorTimestamp = timestamp;
            time = Utils.displayTime( majorTimestamp / sampleRate, 3, "", true /* aIncludeUnit */);
          }
          else
          {
            time = Utils.displayTime( timestamp / sampleRate, 0, "", major /* aIncludeUnit */);
          }
          int w = fm.stringWidth( time );

          int textXpos = ( int )( relXpos - ( w / 2.0 ) );
          if ( textXpos < 1 )
          {
            textXpos = 1;
          }

          int textYpos = major ? y1 - 20 : y1 - 10;
          if ( textYpos < 1 )
          {
            textYpos = 1;
          }

          canvas.setColor( Color.GRAY );

          canvas.setFont( this.labelFont );
          canvas.drawString( time, textXpos, textYpos );

          canvas.setColor( Color.YELLOW );

          canvas.drawLine( relXpos, y1, relXpos, y3 );
        }
        else
        {
          canvas.setColor( Color.GRAY );

          canvas.drawLine( relXpos, y1, relXpos, y2 );
        }

        timestamp += tickIncr;
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
  private double getTimebase( final double aTimeline, final double aSampleRate )
  {
    double result = Math.pow( 10, Math.ceil( Math.log10( aTimeline / aSampleRate ) ) - 1 );
    return result * aSampleRate;
  }
}
