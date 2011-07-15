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
import nl.lxtreme.test.view.model.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * 
 */
public class SignalUI extends ComponentUI implements IMeasurementListener
{
  // CONSTANTS

  /** XXX The threshold when we're going to draw a bit more sloppy... */
  private static final int SLOPPY_THRESHOLD = 1000000;

  // VARIABLES

  private final Renderer cursorRenderer = new CursorFlagRenderer();
  private final Renderer arrowRenderer = new ArrowRenderer();

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
    final SignalViewModel model = view.getModel();

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createSignalRenderingHints() );

      canvas.setBackground( model.getBackgroundColor() );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final int[] values = model.getDataValues();
      final long[] timestamps = model.getTimestamps();
      final int dataWidth = model.getDataWidth();

      final int startIdx = model.getStartIndex( clip );
      final int endIdx = model.getEndIndex( clip, values.length );

      final int signalHeight = model.getSignalHeight();
      final int channelHeight = model.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = model.getSignalOffset();
      final double zoomFactor = model.getZoomFactor();

      // Determine which bits of the actual signal should be drawn...
      int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      int endBit = ( int )Math.min( dataWidth, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

      for ( int b = 0; b < dataWidth; b++ )
      {
        final int virtualRow = model.toVirtualRow( b );
        if ( ( virtualRow < startBit ) || ( virtualRow > endBit ) )
        {
          // Trivial reject: we don't have to paint this row, as it is not asked
          // from us (due to clip boundaries)!
          continue;
        }

        final Color channelColor = model.getChannelColor( b );

        final int mask = ( 1 << b );
        // determine where we really should draw the signal...
        final int dy = signalOffset + ( channelHeight * virtualRow );

        int p;

        if ( !model.isChannelVisible( b ) )
        {
          canvas.setColor( channelColor );
          // Make sure we always start with time 0...
          long timestamp = ( startIdx == 0 ) ? 0 : timestamps[startIdx];

          // Forced zero'd channel is *very* easy to draw...
          canvas.translate( 0, dy + signalHeight );
          canvas.drawLine( ( int )( zoomFactor * timestamp ), 0, ( int )( zoomFactor * timestamps[endIdx] ), 0 );
          canvas.translate( 0, -( dy + signalHeight ) );
        }
        else
        {
          // "Normal" data set; draw as accurate as possible...
          canvas.setColor( channelColor );

          // Make sure we always start with time 0...
          long timestamp = ( startIdx == 0 ) ? 0 : timestamps[startIdx];
          int prevSampleValue = 1 - ( ( values[startIdx] & mask ) >>> b );

          int xValue = ( int )( zoomFactor * timestamp );
          int yValue = signalHeight * prevSampleValue;

          x[0] = xValue;
          y[0] = yValue;
          p = 1;

          for ( int sampleIdx = startIdx; sampleIdx < endIdx; sampleIdx++ )
          {
            timestamp = timestamps[sampleIdx];
            int sampleValue = 1 - ( ( values[sampleIdx] & mask ) >>> b );

            xValue = ( int )( zoomFactor * timestamp );

            if ( prevSampleValue != sampleValue )
            {
              x[p] = xValue;
              y[p] = ( signalHeight * prevSampleValue );
              p++;
            }

            x[p] = xValue;
            y[p] = ( signalHeight * sampleValue );
            p++;

            prevSampleValue = sampleValue;
          }

          canvas.translate( 0, dy );
          canvas.drawPolyline( x, y, p );
          canvas.translate( 0, -dy );
        }
      }

      // Draw the cursor "flags"...
      if ( model.isCursorMode() )
      {
        paintCursorFlags( model, view, canvas, clip );
      }

      // Draw the measurement stuff...
      if ( model.isMeasurementMode() && ( this.signalHoverInfo != null ) )
      {
        renderMeasurementInfo( model, canvas, this.signalHoverInfo );
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
  private RenderingHints createSignalRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
    hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
    hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
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
  private void paintCursorFlags( final SignalViewModel aModel, final JComponent aView, final Graphics2D aCanvas,
      final Rectangle aClip )
  {
    // Tell Swing how we would like to render ourselves...
    aCanvas.setRenderingHints( createCursorRenderingHints() );

    for ( int i = 0; i < SampleDataModel.MAX_CURSORS; i++ )
    {
      int x = aModel.getCursorScreenCoordinate( i );
      int y = getYposition( aView );

      if ( ( x < 0 ) || !aClip.contains( x, 0 ) )
      {
        // Trivial reject: don't paint undefined cursors, or cursors outside the
        // clip boundaries...
        continue;
      }

      aCanvas.setColor( aModel.getCursorColor( i ) );
      aCanvas.setFont( aModel.getCursorFlagFont() );

      this.cursorRenderer.setContext( aModel.getCursorFlagText( i ) );

      this.cursorRenderer.render( aCanvas, x, y );
    }
  }

  /**
   * @param aModel
   * @param aCanvas
   * @param aSignalHover
   */
  private void renderMeasurementInfo( final SignalViewModel aModel, final Graphics2D aCanvas,
      final SignalHoverInfo aSignalHover )
  {
    if ( aSignalHover.isEmpty() )
    {
      return;
    }

    Rectangle signalHoverRect = aSignalHover.getRectangle();
    int x = signalHoverRect.x;
    int y = ( int )signalHoverRect.getCenterY();
    int w = signalHoverRect.width;
    int middlePos = aSignalHover.getMidSamplePos().intValue() - x;

    // Tell Swing how we would like to render ourselves...
    aCanvas.setRenderingHints( createArrowRenderingHints() );

    aCanvas.setColor( aModel.getMeasurementArrowColor() );

    this.arrowRenderer.setContext( Integer.valueOf( w ), Integer.valueOf( middlePos ) );
    this.arrowRenderer.render( aCanvas, x, y );
  }
}
