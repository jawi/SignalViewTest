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


import static java.awt.RenderingHints.*;
import static nl.lxtreme.test.Utils.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.model.Cursor;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.model.*;


/**
 * 
 */
public class TimeLineUI extends ComponentUI
{
  // CONSTANTS

  private static final int PADDING_TOP = 2;
  private static final int PADDING_LEFT = 3;

  private static final int PADDING_WIDTH = 2 * PADDING_LEFT;
  private static final int PADDING_HEIGHT = 2 * PADDING_TOP;

  /** The horizontal padding for all texts. */
  private static final int TEXT_PADDING_X = 2;
  /** The vertical padding (in px) of the timeline view. */
  private static final int VERTICAL_PADDING = 1;
  /** The (fixed) Y position of the cursors. */
  private static final int CURSOR_Y_POS = 12;

  // METHODS

  /**
   * Creates the rendering hints for this view.
   */
  private static RenderingHints createRenderingHints()
  {
    RenderingHints hints = new RenderingHints( KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    hints.put( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
    hints.put( KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY );
    hints.put( KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY );
    hints.put( KEY_RENDERING, VALUE_RENDER_QUALITY );
    return hints;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    final TimeLineView view = ( TimeLineView )aComponent;
    final TimeLineViewModel model = view.getModel();

    final int baseTickYpos = model.getTimeLineHeight() - VERTICAL_PADDING;
    final int tickYpos = baseTickYpos - model.getTickHeight();
    final int majorTickYpos = baseTickYpos - model.getMajorTickHeight();
    final int minorTickYpos = baseTickYpos - model.getMinorTickHeight();

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setBackground( model.getBackgroundColor() );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final Rectangle visibleRect = view.getVisibleRect();

      final double zoomFactor = model.getZoomFactor();
      final double sampleRate = model.getSampleRate();
      final double timebase = model.getTimebase();
      final double tickIncr = model.getTickIncrement();
      final double timeIncr = model.getTimeIncrement();

      final long startTimeStamp = model.getStartTimestamp( visibleRect );
      final long endTimeStamp = model.getEndTimestamp( visibleRect );

      final FontMetrics majorFM = canvas.getFontMetrics( model.getMajorTickLabelFont() );
      final FontMetrics minorFM = canvas.getFontMetrics( model.getMinorTickLabelFont() );
      final int minorFontHeight = minorFM.getHeight();

      double timestamp = Math.floor( startTimeStamp / tickIncr ) * tickIncr;
      double majorTimestamp = Math.round( startTimeStamp / timebase ) * timebase;

      while ( timestamp <= endTimeStamp )
      {
        int relXpos = ( int )( zoomFactor * timestamp );

        if ( ( timestamp % tickIncr ) != 0 )
        {
          canvas.setColor( model.getTickColor() );

          canvas.drawLine( relXpos, baseTickYpos, relXpos, tickYpos );
        }
        else
        {
          boolean major = ( ( timestamp % timebase ) == 0 );

          final String time;
          final int textWidth;
          final int textHeight;
          final int tickHeight;

          if ( major )
          {
            majorTimestamp = timestamp;

            final double tickTime = ( majorTimestamp / sampleRate );
            time = displayTime( tickTime, 3, "", true /* aIncludeUnit */);

            canvas.setFont( model.getMajorTickLabelFont() );

            textWidth = majorFM.stringWidth( time ) + TEXT_PADDING_X;
            textHeight = 2 * minorFontHeight;

            canvas.setColor( model.getMajorTickColor() );

            tickHeight = majorTickYpos;
          }
          else
          {
            final double tickTime = ( timestamp - majorTimestamp ) / sampleRate;
            time = displayTime( tickTime, 1, "", true /* aIncludeUnit */);

            canvas.setFont( model.getMinorTickLabelFont() );
            textWidth = minorFM.stringWidth( time ) + TEXT_PADDING_X;
            textHeight = minorFontHeight;

            canvas.setColor( model.getMinorTickColor() );

            tickHeight = minorTickYpos;
          }

          canvas.drawLine( relXpos, baseTickYpos, relXpos, tickHeight );

          int textXpos = Math.max( 0, ( int )( relXpos - ( textWidth / 2.0 ) ) ) + 1;
          int textYpos = Math.max( 1, ( baseTickYpos - textHeight ) );

          canvas.setColor( model.getTextColor() );

          canvas.drawString( time, textXpos, textYpos );
        }

        // make sure we're rounding to two digits after the comma...
        timestamp = Math.round( 100.0 * ( timestamp + timeIncr ) ) / 100.0;
      }

      // Draw the cursor "flags"...
      if ( model.isCursorMode() )
      {
        paintCursorFlags( model, canvas, aComponent );
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;
    }
  }

  /**
   * Paints the cursors on this timeline.
   * 
   * @param aCanvas
   *          the canvas to paint the cursor (flags) on;
   */
  private void paintCursorFlags( final TimeLineViewModel aModel, final Graphics2D aCanvas, final JComponent aComponent )
  {
    for ( int i = 0; i < Cursor.MAX_CURSORS; i++ )
    {
      int x = aModel.getCursorScreenCoordinate( i );
      int y = CURSOR_Y_POS;

      if ( x < 0 )
      {
        // Trivial reject: don't paint undefined cursors...
        continue;
      }

      aCanvas.setFont( aModel.getCursorFlagFont() );

      final String flagText = aModel.getCursorFlagText( i );

      final FontMetrics fm = aCanvas.getFontMetrics();
      final int flagWidth = fm.stringWidth( flagText ) + PADDING_WIDTH;
      final int flagHeight = fm.getHeight() + PADDING_HEIGHT;

      final int centerX = ( int )( flagWidth / 2.0 );
      final int x1 = x - centerX;
      final int x2 = x + centerX;

      int[] poly_x = new int[] { x1, x2, x2, x + 4, x, x - 4, x1 };
      int[] poly_y = new int[] { y, y, y + flagHeight, y + flagHeight, aComponent.getHeight(), y + flagHeight,
          y + flagHeight };

      aCanvas.setColor( Color.BLACK ); // XXX
      aCanvas.fillPolygon( poly_x, poly_y, poly_x.length );

      aCanvas.setColor( aModel.getCursorColor( i ) );
      aCanvas.drawPolygon( poly_x, poly_y, poly_x.length );

      final int textXpos = x1 + PADDING_LEFT;
      final int textYpos = y + fm.getLeading() + fm.getAscent() + PADDING_TOP;

      aCanvas.setColor( aModel.getCursorTextColor( i ) );
      aCanvas.drawString( flagText, textXpos, textYpos );
    }
  }
}
