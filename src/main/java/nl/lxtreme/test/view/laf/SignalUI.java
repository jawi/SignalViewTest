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

import nl.lxtreme.test.model.Cursor;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.model.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * 
 */
public class SignalUI extends ComponentUI
{
  // CONSTANTS

  /** XXX The threshold when we're going to draw a bit more sloppy... */
  private static final int SLOPPY_THRESHOLD = 1000000;

  private static final int PADDING_X = 2;
  private static final int PADDING_Y = 2;

  // VARIABLES

  private final Renderer arrowRenderer = new ArrowRenderer();

  private volatile boolean listening = true;
  private volatile SignalHoverInfo signalHoverInfo;
  private volatile Rectangle measurementRect;

  private static final int[] x = new int[2 * SLOPPY_THRESHOLD];
  private static final int[] y = new int[2 * SLOPPY_THRESHOLD];

  // METHODS

  /**
   * Creates the rendering hints for this the drawing of arrows.
   */
  private static RenderingHints createArrowRenderingHints()
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
  private static RenderingHints createCursorRenderingHints()
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
  private static RenderingHints createSignalRenderingHints()
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

    try
    {
      Graphics2D canvas = ( Graphics2D )aGraphics.create();

      try
      {
        final Rectangle clip = canvas.getClipBounds();

        final SignalElement[] signalElements = model.getSignalElements( clip.y, clip.height );
        if ( signalElements.length > 0 )
        {
          paintSignals( canvas, model, signalElements );
        }
      }
      finally
      {
        canvas.dispose();
        canvas = null;
      }

      // Use the *original* graphics object, as the one defined above is
      // translated to some unknown coordinate system...
      canvas = ( Graphics2D )aGraphics;

      // Draw the cursor "flags"...
      if ( model.isCursorMode() )
      {
        paintCursorFlags( canvas, model, view );
      }

      // Draw the measurement stuff...
      if ( model.isMeasurementMode() && SignalHoverInfo.isDefined( this.signalHoverInfo ) )
      {
        paintMeasurementArrow( canvas, model, this.signalHoverInfo );
      }
    }
    finally
    {
      this.listening = true;
    }
  }

  /**
   * Paints the cursors on this time line.
   * 
   * @param aCanvas
   *          the canvas to paint the cursor (flags) on;
   * @param aClip
   *          the clip boundaries.
   */
  private void paintCursorFlags( final Graphics2D aCanvas, final SignalViewModel aModel, final JComponent aView )
  {
    final Rectangle clip = aCanvas.getClipBounds();

    // Tell Swing how we would like to render ourselves...
    aCanvas.setRenderingHints( createCursorRenderingHints() );

    final int viewYpos = aView.getVisibleRect().y;
    for ( int i = 0; i < Cursor.MAX_CURSORS; i++ )
    {
      int cursorXpos = aModel.getCursorScreenCoordinate( i );

      if ( ( cursorXpos < 0 ) || !clip.contains( cursorXpos, viewYpos ) )
      {
        // Trivial reject: don't paint undefined cursors, or cursors outside the
        // clip boundaries...
        continue;
      }

      aCanvas.setColor( aModel.getCursorColor( i ) );

      aCanvas.drawLine( cursorXpos, clip.y, cursorXpos, clip.y + clip.height );
    }
  }

  /**
   * Renders the measurement information arrows.
   * 
   * @param aCanvas
   *          the canvas to paint the measurement arrows on, cannot be
   *          <code>null</code>;
   * @param aModel
   *          the model to use, cannot be <code>null</code>;
   * @param aSignalHover
   *          the signal hover information, cannot be <code>null</code> or
   *          empty.
   */
  private void paintMeasurementArrow( final Graphics2D aCanvas, final SignalViewModel aModel,
      final SignalHoverInfo aSignalHover )
  {
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

  /**
   * Paints the individual signal channels, group bytes and analogue scope
   * signals.
   * 
   * @param aCanvas
   *          the canvas to paint on, cannot be <code>null</code>;
   * @param aModel
   *          the model to use, cannot be <code>null</code>;
   * @param aSignalElements
   *          the signal elements to draw, cannot be <code>null</code> or empty!
   */
  private void paintSignals( final Graphics2D aCanvas, final SignalViewModel aModel,
      final SignalElement[] aSignalElements )
  {
    final Rectangle clip = aCanvas.getClipBounds();

    // Tell Swing how we would like to render ourselves...
    aCanvas.setRenderingHints( createSignalRenderingHints() );

    aCanvas.setBackground( aModel.getBackgroundColor() );
    aCanvas.clearRect( clip.x, clip.y, clip.width, clip.height );

    final int[] values = aModel.getDataValues();
    final long[] timestamps = aModel.getTimestamps();

    final int startIdx = aModel.getStartIndex( clip );
    final int endIdx = aModel.getEndIndex( clip, values.length );

    final int signalHeight = aModel.getSignalHeight();
    // Where is the signal to be drawn?
    final int signalOffset = aModel.getSignalOffset();
    final double zoomFactor = aModel.getZoomFactor();

    // Start drawing at the correct position in the clipped region...
    aCanvas.translate( 0, aSignalElements[0].getYposition() + signalOffset );

    final int sampleIncr = ( int )Math.max( 1.0, ( 1.0 / zoomFactor ) );
    // System.out.printf( "Sample incr = %d px\n", sampleIncr ); // XXX

    for ( SignalElement signalElement : aSignalElements )
    {
      aCanvas.setColor( signalElement.getColor() );

      if ( signalElement.isSignalGroup() )
      {
        // Draw nothing...
        aCanvas.translate( 0, signalElement.getHeight() );
      }

      if ( signalElement.isDigitalSignal() )
      {
        if ( !signalElement.isEnabled() )
        {
          // Forced zero'd channel is *very* easy to draw...
          aCanvas.drawLine( clip.x, signalHeight, clip.x + clip.width, signalHeight );
        }
        else
        {
          // "Normal" data set; draw as accurate as possible...
          final int mask = signalElement.getMask();

          // Make sure we always start with time 0...
          long timestamp = timestamps[startIdx];
          int prevSampleValue = ( values[startIdx] & mask );

          int xValue = ( int )( zoomFactor * timestamp );
          int yValue = ( prevSampleValue == 0 ? signalHeight : 0 );

          x[0] = xValue;
          y[0] = yValue;
          int p = 1;

          for ( int sampleIdx = startIdx + 1; sampleIdx < endIdx; sampleIdx++ )
          {
            timestamp = timestamps[sampleIdx];
            int sampleValue = ( values[sampleIdx] & mask );

            xValue = ( int )( zoomFactor * timestamp );

            if ( prevSampleValue != sampleValue )
            {
              x[p] = xValue;
              y[p] = ( prevSampleValue == 0 ? signalHeight : 0 );
              p++;
            }

            x[p] = xValue;
            y[p] = ( sampleValue == 0 ? signalHeight : 0 );
            p++;

            prevSampleValue = sampleValue;
          }

          aCanvas.drawPolyline( x, y, p );
        }

        // Advance to the next channel...
        aCanvas.translate( 0, signalElement.getHeight() );
      }

      // remove the signal offset...
      aCanvas.translate( 0, -signalOffset );

      if ( signalElement.isGroupSummary() )
      {
        int mask = signalElement.getMask();

        int prevSampleValue = values[startIdx] & mask;
        int prevX = ( int )( zoomFactor * timestamps[startIdx] );

        for ( int sampleIdx = startIdx + 1; sampleIdx < endIdx; sampleIdx += sampleIncr )
        {
          int sampleValue = ( values[sampleIdx] & mask );

          if ( sampleValue != prevSampleValue )
          {
            int x = ( int )( zoomFactor * timestamps[sampleIdx] );

            String text = String.format( "%x", Integer.valueOf( prevSampleValue ) );
            FontMetrics fm = aCanvas.getFontMetrics();

            int textWidth = fm.stringWidth( text ) + ( 2 * PADDING_X );
            int cellWidth = x - prevX;
            if ( textWidth < cellWidth )
            {
              int textXpos = prevX + ( int )( ( cellWidth - textWidth ) / 2.0 );
              int textYpos = fm.getHeight();

              aCanvas.drawString( text, textXpos, textYpos );
            }

            // draw a small line...
            aCanvas.drawLine( x, PADDING_Y, x, signalElement.getHeight() - PADDING_Y );

            prevX = x;
          }

          prevSampleValue = sampleValue;
        }

        aCanvas.translate( 0, signalElement.getHeight() );
      }

      if ( signalElement.isAnalogSignal() )
      {
        int mask = signalElement.getMask();
        final int trailingZeros = Integer.numberOfTrailingZeros( mask );
        final int onesCount = Integer.SIZE - Integer.numberOfLeadingZeros( mask ) - trailingZeros;
        final int maxValue = ( int )( 1L << onesCount );
        double scaleFactor = signalElement.getHeight() / ( maxValue + 1.0 );

        // Make sure we always start with time 0...
        int p = 0;
        for ( int sampleIdx = startIdx + sampleIncr; sampleIdx < endIdx; sampleIdx += sampleIncr )
        {
          long timestamp = timestamps[sampleIdx - sampleIncr];
          int sampleValue = 0;
          for ( int i = sampleIdx - sampleIncr; i < sampleIdx; i++ )
          {
            sampleValue += ( ( values[sampleIdx] & mask ) >> trailingZeros );
          }
          sampleValue = maxValue - ( sampleValue / sampleIncr );

          x[p] = ( int )( zoomFactor * timestamp );
          y[p] = ( int )( scaleFactor * sampleValue );
          p++;
        }

        aCanvas.drawPolyline( x, y, p );

        aCanvas.translate( 0, signalElement.getHeight() );
      }

      // remove the signal offset...
      aCanvas.translate( 0, signalOffset );
    }
  }
}
