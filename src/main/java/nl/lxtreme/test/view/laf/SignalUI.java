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
public class SignalUI extends ComponentUI implements IMeasurementListener
{
  // CONSTANTS

  public static final String COMPONENT_BACKGROUND_COLOR = "channellabels.color.background";
  public static final String LABEL_FONT = "measurement.label.font";

  /** XXX The threshold when we're going to draw a bit more sloppy... */
  private static final int SLOPPY_THRESHOLD = 1000000;

  // VARIABLES

  private final Renderer cursorRenderer = new CursorFlagRenderer();
  private final Renderer arrowRenderer = new ArrowRenderer();

  private Color backgroundColor;
  private Font textFont;

  private volatile boolean listening = true;
  private volatile SignalHoverInfo signalHoverInfo;
  private volatile Rectangle measurementRect;

  private static final int[] x = new int[2 * SLOPPY_THRESHOLD];
  private static final int[] y = new int[2 * SLOPPY_THRESHOLD];

  // METHODS

  /**
   * Returns the current value of measurementRect.
   * 
   * @return the measurementRect
   */
  public Rectangle getMeasurementRect()
  {
    return this.measurementRect;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMeasureEvent( final SignalHoverInfo aEvent )
  {
    this.signalHoverInfo = aEvent;

    if ( aEvent != null )
    {
      this.measurementRect = new Rectangle( aEvent.getRectangle() );
      this.measurementRect.grow( ArrowRenderer.HEAD_WIDTH, ArrowRenderer.HEAD_HEIGHT );
    }
    else
    {
      this.measurementRect = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    final SignalView view = ( SignalView )aComponent;

    final IUserInterfaceSettingsProvider settingsProvider = view.getSettingsProvider();

    this.backgroundColor = settingsProvider.getColor( COMPONENT_BACKGROUND_COLOR );
    if ( this.backgroundColor == null )
    {
      this.backgroundColor = Utils.parseColor( "#1E2126" );
    }

    final Font baseFont = ( Font )UIManager.get( "Label.font" );

    this.textFont = settingsProvider.getFont( LABEL_FONT );
    if ( this.textFont == null )
    {
      this.textFont = baseFont.deriveFont( baseFont.getSize2D() * 0.9f );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isListening()
  {
    return this.listening;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    this.listening = false;

    final SignalView view = ( SignalView )aComponent;

    final SignalDiagramController controller = view.getController();

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setBackground( this.backgroundColor );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final SampleDataModel dataModel = controller.getDataModel();
      final ScreenModel screenModel = controller.getScreenModel();

      final int[] values = dataModel.getValues();
      final long[] timestamps = dataModel.getTimestamps();

      final int startIdx = getStartIndex( controller, clip );
      final int endIdx = getEndIndex( controller, clip, values.length );
      final int size = Math.min( values.length - 1, ( endIdx - startIdx ) + 1 );

      final int signalHeight = screenModel.getSignalHeight();
      final int channelHeight = screenModel.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = screenModel.getSignalOffset();
      final double zoomFactor = screenModel.getZoomFactor();

      final int dataWidth = dataModel.getWidth();

      // Determine which bits of the actual signal should be drawn...
      int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      int endBit = ( int )Math.min( dataWidth, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

      for ( int b = 0; b < dataWidth; b++ )
      {
        final int virtualRow = screenModel.toVirtualRow( b );
        if ( ( virtualRow < startBit ) || ( virtualRow > endBit ) )
        {
          // Trivial reject: we don't have to paint this row, as it is not asked
          // from us (due to clip boundaries)!
          continue;
        }

        final Color channelColor = screenModel.getChannelColor( b );

        final int mask = ( 1 << b );
        // determine where we really should draw the signal...
        final int dy = signalOffset + ( channelHeight * virtualRow );

        int p;

        if ( !screenModel.isChannelVisible( b ) )
        {
          canvas.setColor( channelColor.darker().darker() );

          long timestamp = ( startIdx == 0 ) ? 0 : timestamps[startIdx];

          // Forced zero'd channel is *very* easy to draw...
          x[0] = ( int )( zoomFactor * timestamp );
          y[0] = dy + signalHeight;

          x[1] = ( int )( zoomFactor * timestamps[endIdx] );
          y[1] = dy + signalHeight;
          p = 2;

          canvas.drawPolyline( x, y, p );
        }
        else
        {
          if ( size > SLOPPY_THRESHOLD )
          {
            canvas.setColor( channelColor.darker() );

            // Large data set; be a bit more sloppy in the drawing to gain some
            // performance...
            int incr = ( int )Math.floor( 1.0 / ( 3 * zoomFactor ) );

            float mean = 0.0f;
            for ( int sampleIdx = startIdx; sampleIdx < endIdx; sampleIdx++ )
            {
              mean = mean + ( ( values[sampleIdx] & mask ) >> b );
            }
            mean = mean / size;

            for ( int sampleIdx = startIdx; sampleIdx < endIdx; sampleIdx += incr )
            {
              final int nextSampleIdx = Math.min( endIdx, sampleIdx + incr - 1 );

              float blkMean = 0.0f;
              for ( int i = sampleIdx; i < nextSampleIdx; i++ )
              {
                blkMean = blkMean + ( ( values[i] & mask ) >> b );
              }
              blkMean = blkMean / incr;

              long timestamp = ( startIdx == 0 ) ? 0 : timestamps[startIdx];

              int x1 = ( int )( zoomFactor * timestamp );
              int x2 = ( int )( zoomFactor * timestamps[nextSampleIdx - 1] );
              int y1 = signalHeight * ( blkMean < mean ? 0 : 1 );

              canvas.drawRect( x1, dy, x2 - x1, y1 );
            }
          }
          else
          {
            canvas.setColor( channelColor );

            // "Normal" data set; draw as accurate as possible...
            long timestamp = ( startIdx == 0 ) ? 0 : timestamps[startIdx];
            int prevSampleValue = 1 - ( ( values[startIdx] & mask ) >> b );

            x[0] = ( int )( zoomFactor * timestamp );
            y[0] = dy + ( signalHeight * prevSampleValue );
            p = 1;

            for ( int sampleIdx = startIdx; sampleIdx < endIdx; sampleIdx++ )
            {
              timestamp = timestamps[sampleIdx];
              int sampleValue = 1 - ( ( values[sampleIdx] & mask ) >> b );

              if ( prevSampleValue != sampleValue )
              {
                x[p] = ( int )( zoomFactor * timestamp );
                y[p] = dy + ( signalHeight * prevSampleValue );
                p++;
              }

              x[p] = ( int )( zoomFactor * timestamp );
              y[p] = dy + ( signalHeight * sampleValue );
              p++;

              prevSampleValue = sampleValue;
            }

            canvas.drawPolyline( x, y, p );
          }
        }
      }

      // Draw the cursor "flags"...
      if ( controller.isCursorMode() )
      {
        paintCursorFlags( controller, view, canvas, clip );
      }

      // Draw the measurement stuff...
      if ( controller.isMeasurementMode() && ( this.signalHoverInfo != null ) )
      {
        paintMeasurementInfo( canvas, this.signalHoverInfo );
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;

      this.listening = true;
    }
  }

  /**
   * Creates the rendering hints for this the drawing of arrows.
   */
  private RenderingHints createArrowRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
    hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
    return hints;
  }

  /**
   * Creates the rendering hints for this view.
   */
  private RenderingHints createCursorRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
  }

  /**
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    return new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
  }

  /**
   * @param aClip
   * @return
   */
  private int getEndIndex( final SignalDiagramController aController, final Rectangle aClip, final int aLength )
  {
    final Point location = new Point( aClip.x + aClip.width, 0 );
    int index = aController.locationToSampleIndex( location );
    return Math.min( index + 1, aLength - 1 );
  }

  /**
   * @param aClip
   * @return
   */
  private int getStartIndex( final SignalDiagramController aController, final Rectangle aClip )
  {
    final Point location = aClip.getLocation();
    int index = aController.locationToSampleIndex( location );
    return Math.max( index - 1, 0 );
  }

  /**
   * Returns the Y-position where the cursor (+ flag) should be drawn.
   * 
   * @return a Y-position, in the coordinate space of this component.
   */
  private int getYposition( final JComponent aComponent )
  {
    int result = 0;
    if ( SwingUtilities.getAncestorOfClass( JViewport.class, aComponent ) != null )
    {
      // negative in order to ensure the flag itself is hidden
      result = -40;
    }
    return result;
  }

  /**
   * Paints the cursors on this time line.
   * 
   * @param aCanvas
   *          the canvas to paint the cursor (flags) on;
   * @param aClip
   *          the clip boundaries.
   */
  private void paintCursorFlags( final SignalDiagramController aController, final JComponent aView,
      final Graphics2D aCanvas, final Rectangle aClip )
  {
    final ScreenModel screenModel = aController.getScreenModel();

    // Tell Swing how we would like to render ourselves...
    aCanvas.setRenderingHints( createCursorRenderingHints() );

    for ( int i = 0; i < SampleDataModel.MAX_CURSORS; i++ )
    {
      int x = aController.getCursorScreenCoordinate( i );
      int y = getYposition( aView );

      if ( ( x < 0 ) || !aClip.contains( x, 0 ) )
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

  /**
   * @param aCanvas
   * @param aSignalHover
   */
  private void paintMeasurementInfo( final Graphics2D aCanvas, final SignalHoverInfo aSignalHover )
  {
    Rectangle signalHoverRect = aSignalHover.getRectangle();

    int x = signalHoverRect.x;
    int y = ( int )signalHoverRect.getCenterY();
    int w = signalHoverRect.width;
    int middlePos = aSignalHover.getMiddleXpos() - x;

    // Tell Swing how we would like to render ourselves...
    aCanvas.setRenderingHints( createArrowRenderingHints() );

    aCanvas.setColor( Color.YELLOW );

    this.arrowRenderer.setContext( Integer.valueOf( w ), Integer.valueOf( middlePos ) );
    this.arrowRenderer.render( aCanvas, x, y );
  }
}
