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
 * 
 * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
 */
package nl.lxtreme.test.view.renderer;


import java.awt.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;


/**
 * Renders the "flag" of a cursor, containing the cursor's label and time
 * information.
 */
public class CursorFlagRenderer extends BaseRenderer
{
  // CONSTANTS

  private static final int PADDING_TOP = 2;
  private static final int PADDING_LEFT = 3;

  private static final int PADDING_WIDTH = 2 * PADDING_LEFT;
  private static final int PADDING_HEIGHT = 2 * PADDING_TOP;

  // VARIABLES

  private int cursorIdx;
  private long cursorTimestamp;
  private SignalDiagramController controller;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public Rectangle render( final Graphics2D aCanvas, final Rectangle aClip1 )
  {
    final Rectangle clip = aCanvas.getClipBounds();

    final SampleDataModel dataModel = this.controller.getDataModel();
    final ScreenModel screenModel = this.controller.getScreenModel();

    final Color flagColor = screenModel.getCursorColor( this.cursorIdx );
    final double sampleRate = dataModel.getSampleRate();

    final String timeStr = String.format( "%s: %s", screenModel.getCursorLabel( this.cursorIdx ),
        Utils.displayTime( this.cursorTimestamp / sampleRate ) );

    final FontMetrics fm = aCanvas.getFontMetrics();
    final int flagWidth = fm.stringWidth( timeStr ) + PADDING_WIDTH;
    final int flagHeight = fm.getHeight() + PADDING_HEIGHT;

    final Rectangle result = new Rectangle();
    result.width = flagWidth;
    result.height = clip.height;

    aCanvas.setColor( flagColor );

    if ( clip.contains( result.x, result.y, result.width, flagHeight - 1 ) )
    {
      aCanvas.fillRect( result.x, result.y, result.width, flagHeight - 1 );

      final int textXpos = result.x + PADDING_LEFT;
      final int textYpos = result.y + fm.getLeading() + fm.getAscent() + PADDING_TOP;

      final Color textColor = Utils.getContrastColor( flagColor );
      aCanvas.setColor( textColor );

      aCanvas.drawString( timeStr, textXpos, textYpos );
    }

    aCanvas.setColor( flagColor );

    drawCursorLine( aCanvas, clip, result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    if ( ( aParameters == null ) || ( aParameters.length < 3 ) )
    {
      throw new IllegalArgumentException( "Expected a Controller, Integer & Long parameter!" );
    }
    this.controller = ( ( SignalDiagramController )aParameters[0] );
    this.cursorIdx = ( ( Integer )aParameters[1] ).intValue();
    this.cursorTimestamp = ( ( Long )aParameters[2] ).longValue();
  }

  /**
   * Draws the cursor line itself.
   * 
   * @param aCanvas
   * @param aClip
   * @param aResult
   */
  private void drawCursorLine( final Graphics2D aCanvas, final Rectangle aClip, final Rectangle aResult )
  {
    int x = Math.max( aResult.x, aClip.x );
    int y1 = Math.max( aResult.y, aClip.y );
    int y2 = Math.max( aResult.y + aResult.height, aClip.y + aClip.height );
    aCanvas.drawLine( x, y1, x, y2 );
  }
}
