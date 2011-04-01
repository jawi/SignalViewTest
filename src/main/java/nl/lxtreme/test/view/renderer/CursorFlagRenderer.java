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

  private String cursorFlagText;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    if ( ( aParameters == null ) || ( aParameters.length < 1 ) )
    {
      throw new IllegalArgumentException( "Expected a String parameter!" );
    }
    this.cursorFlagText = ( ( String )aParameters[0] );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Rectangle render( final Graphics2D aCanvas )
  {
    final Rectangle clip = aCanvas.getClipBounds();

    final FontMetrics fm = aCanvas.getFontMetrics();
    final int flagWidth = fm.stringWidth( this.cursorFlagText ) + PADDING_WIDTH;
    final int flagHeight = fm.getHeight() + PADDING_HEIGHT;

    final Rectangle result = new Rectangle();
    result.width = flagWidth;
    result.height = clip.height;

    final Color flagColor = aCanvas.getColor();

    if ( clip.contains( result.x, result.y, result.width, flagHeight - 1 ) )
    {
      aCanvas.fillRect( result.x, result.y, result.width, flagHeight - 1 );

      final int textXpos = result.x + PADDING_LEFT;
      final int textYpos = result.y + fm.getLeading() + fm.getAscent() + PADDING_TOP;

      final Color textColor = Utils.getContrastColor( flagColor );
      aCanvas.setColor( textColor );

      aCanvas.drawString( this.cursorFlagText, textXpos, textYpos );
    }

    aCanvas.setColor( flagColor );

    drawCursorLine( aCanvas, clip, result );

    return result;
  }

  /**
   * Draws the cursor line itself.
   * 
   * @param aCanvas
   *          the canvas to draw on;
   * @param aClip
   *          the clip boundaries;
   * @param aFlagArea
   *          the outer rectangle denoting the cursor boundaries (+ flag).
   */
  private void drawCursorLine( final Graphics2D aCanvas, final Rectangle aClip, final Rectangle aFlagArea )
  {
    int x = aFlagArea.x;

    int y1 = aFlagArea.y;
    if ( y1 < aClip.y )
    {
      y1 = aClip.y;
    }
    int y2 = aFlagArea.y + aFlagArea.height;
    if ( y2 > aClip.y + aClip.height )
    {
      y2 = aClip.y + aClip.height;
    }

    aCanvas.drawLine( x, y1, x, y2 );
  }
}
