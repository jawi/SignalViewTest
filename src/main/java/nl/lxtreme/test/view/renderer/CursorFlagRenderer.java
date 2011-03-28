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
  // VARIABLES

  private volatile int cursorIdx;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void render( final Graphics2D aCanvas, final int aXpos, final int aYpos )
  {
    final SignalDiagramController controller = getController();
    final Rectangle clip = getClipBounds();

    final SampleDataModel dataModel = controller.getDataModel();

    final Long cursorTimestamp = dataModel.getCursors()[this.cursorIdx];
    if ( cursorTimestamp == null )
    {
      return;
    }

    final int sampleRate = dataModel.getSampleRate();

    final ScreenModel screenModel = controller.getScreenModel();

    final String timeStr = String.format( "%s: %s", screenModel.getCursorLabel( this.cursorIdx ),
        Utils.displayTime( cursorTimestamp.doubleValue() / sampleRate ) );

    final FontMetrics fm = aCanvas.getFontMetrics();

    // Move the canvas to the requested position...
    aCanvas.translate( aXpos, aYpos );

    final int w = fm.stringWidth( timeStr ) + 6;
    final int h = fm.getHeight() + 4;

    final int x1 = controller.toScaledScreenCoordinate( cursorTimestamp.longValue() ).x;
    final int y1 = clip.height - h;

    if ( clip.contains( x1, y1 ) || clip.contains( x1 + w, y1 + h ) )
    {
      aCanvas.setColor( screenModel.getCursorColor( this.cursorIdx ) );

      aCanvas.drawRect( x1, y1, w, h - 1 );

      final int textXpos = x1 + 3;
      final int textYpos = y1 + fm.getLeading() + fm.getAscent() + 2;

      aCanvas.drawString( timeStr, textXpos, textYpos );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    if ( ( aParameters == null ) || ( aParameters.length < 1 ) )
    {
      throw new IllegalArgumentException( "Expected an Integer parameter!" );
    }
    this.cursorIdx = ( ( Integer )aParameters[0] ).intValue();
  }
}
