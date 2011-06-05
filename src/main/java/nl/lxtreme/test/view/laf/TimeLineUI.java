/*
 * OpenBench LogicSniffer / SUMP project 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2010-2011 - J.W. Janssen, <http://www.lxtreme.nl>
 */
package nl.lxtreme.test.view.laf;


import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * 
 */
public class TimeLineUI extends ComponentUI
{
  // CONSTANTS

  public static final String COMPONENT_BACKGROUND_COLOR = "timeline.color.background";
  public static final String MAJOR_TICK_LABEL_FONT = "timeline.majortick.label.font";
  public static final String MINOR_TICK_LABEL_FONT = "timeline.minortick.label.font";

  /** The tick increment (in pixels). */
  private static final int TIMELINE_INCREMENT = 10;
  /** The height of this component. */
  public static final int TIMELINE_HEIGHT = 40;

  private static final int SHORT_TICK_HEIGHT = 4;
  private static final int PADDING_Y = 1;

  private static final int BASE_TICK_Y_POS = TIMELINE_HEIGHT - PADDING_Y;
  private static final int SHORT_TICK_Y_POS = TIMELINE_HEIGHT - PADDING_Y - SHORT_TICK_HEIGHT;
  private static final int LONG_TICK_Y_POS = TIMELINE_HEIGHT - PADDING_Y - 2 * SHORT_TICK_HEIGHT;

  // VARIABLES

  private final Renderer cursorRenderer = new CursorFlagRenderer();

  private Color backgroundColor;
  private Font majorTickFont;
  private Font minorTickFont;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    final TimeLineView view = ( TimeLineView )aComponent;

    final IUserInterfaceSettingsProvider settingsProvider = view.getSettingsProvider();

    final Font baseFont = ( Font )UIManager.get( "Label.font" );

    this.backgroundColor = settingsProvider.getColor( COMPONENT_BACKGROUND_COLOR );
    if ( this.backgroundColor == null )
    {
      this.backgroundColor = Utils.parseColor( "#1E2126" );
    }

    this.minorTickFont = settingsProvider.getFont( MINOR_TICK_LABEL_FONT );
    if ( this.minorTickFont == null )
    {
      this.minorTickFont = baseFont.deriveFont( baseFont.getSize() * 0.8f );
    }

    this.majorTickFont = settingsProvider.getFont( MAJOR_TICK_LABEL_FONT );
    if ( this.majorTickFont == null )
    {
      this.majorTickFont = baseFont.deriveFont( baseFont.getSize() * 0.9f );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    final TimeLineView view = ( TimeLineView )aComponent;

    final SignalDiagramController controller = view.getController();

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setBackground( this.backgroundColor );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final FontMetrics majorFM = canvas.getFontMetrics( this.majorTickFont );
      final FontMetrics minorFM = canvas.getFontMetrics( this.minorTickFont );
      final int minorFontHeight = minorFM.getHeight();

      final SampleDataModel dataModel = controller.getDataModel();
      final ScreenModel screenModel = controller.getScreenModel();

      final double zoomFactor = screenModel.getZoomFactor();
      final double sampleRate = dataModel.getSampleRate();

      final double timebase = getTimebase( aComponent, zoomFactor );

      double tickIncr = Math.max( 1.0, timebase / TIMELINE_INCREMENT );
      double timeIncr = Math.max( 1.0, timebase / ( 10.0 * TIMELINE_INCREMENT ) );

      final Rectangle visibleRect = view.getVisibleRect();

      final long startTimeStamp = getStartTimestamp( controller, visibleRect );
      final long endTimeStamp = getEndTimestamp( controller, visibleRect );

      double timestamp = ( Math.ceil( startTimeStamp / tickIncr ) * tickIncr );
      double majorTimestamp = timestamp;

      for ( ; timestamp <= endTimeStamp; timestamp += timeIncr )
      {
        int relXpos = ( int )( zoomFactor * timestamp );

        // XXX where does the 16 come from???
        if ( ( relXpos < ( clip.x - 16 ) ) || ( relXpos > ( 16 + clip.x + clip.width ) ) )
        {
          // System.out.println( "SKIP!" );
          // continue;
        }

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
      if ( controller.isCursorMode() )
      {
        paintCursorFlags( controller, canvas, clip );
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
  private long getEndTimestamp( final SignalDiagramController aController, final Rectangle aClip )
  {
    final Point location = new Point( aClip.x + aClip.width, 0 );
    final int idx = aController.locationToSampleIndex( location );
    if ( idx < 0 )
    {
      return 0L;
    }

    final SampleDataModel dataModel = aController.getDataModel();
    final long[] timestamps = dataModel.getTimestamps();

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
  private long getStartTimestamp( final SignalDiagramController aController, final Rectangle aClip )
  {
    final Point location = aClip.getLocation();
    final int idx = aController.locationToSampleIndex( location );
    if ( idx < 0 )
    {
      return 0L;
    }

    final SampleDataModel dataModel = aController.getDataModel();
    final long[] timestamps = dataModel.getTimestamps();

    return idx == 0 ? 0 : timestamps[idx];
  }

  /**
   * Determines the time base for the given absolute time (= total time
   * displayed).
   * 
   * @param aZoomFactor
   *          the current zoom factor.
   * @return a time base, as power of 10.
   */
  private double getTimebase( final JComponent aView, final double aZoomFactor )
  {
    final double absoluteTime = aView.getVisibleRect().width / aZoomFactor;
    return Math.pow( 10, Math.round( Math.log10( absoluteTime ) ) );
  }

  /**
   * Paints the cursors on this timeline.
   * 
   * @param aCanvas
   *          the canvas to paint the cursor (flags) on;
   * @param aClip
   *          the clip boundaries.
   */
  private void paintCursorFlags( final SignalDiagramController aController, final Graphics2D aCanvas,
      final Rectangle aClip )
  {
    final ScreenModel screenModel = aController.getScreenModel();

    for ( int i = 0; i < SampleDataModel.MAX_CURSORS; i++ )
    {
      int x = aController.getCursorScreenCoordinate( i );
      int y = 4;

      if ( x < 0 )
      {
        // Trivial reject: don't paint undefined cursors, or cursors outside the
        // clip boundaries...
        continue;
      }

      aCanvas.setColor( screenModel.getCursorColor( i ) );

      this.cursorRenderer.setContext( aController.getCursorFlagText( i ) );

      this.cursorRenderer.render( aCanvas, x, y );
    }
  }
}
