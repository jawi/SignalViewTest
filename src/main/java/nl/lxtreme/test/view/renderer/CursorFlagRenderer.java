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

  private volatile int cursorIdx;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void render( final Graphics2D aCanvas )
  {
    final SignalDiagramController controller = getController();

    final SampleDataModel dataModel = controller.getDataModel();

    final Long cursorTimestamp = dataModel.getCursor( this.cursorIdx );
    if ( cursorTimestamp == null )
    {
      return;
    }

    final int sampleRate = dataModel.getSampleRate();

    final ScreenModel screenModel = controller.getScreenModel();

    final String timeStr = String.format( "%s: %s", screenModel.getCursorLabel( this.cursorIdx ),
        Utils.displayTime( cursorTimestamp.doubleValue() / sampleRate ) );

    final FontMetrics fm = aCanvas.getFontMetrics();

    final int w = fm.stringWidth( timeStr ) + PADDING_WIDTH;
    final int h = fm.getHeight() + PADDING_HEIGHT;

    final int x1 = controller.toScaledScreenCoordinate( cursorTimestamp.longValue() ).x;
    final int y1 = -h;

    final Color flagColor = screenModel.getCursorColor( this.cursorIdx );
    aCanvas.setColor( flagColor );

    aCanvas.fillRect( x1, y1, w, h - 1 );

    final int textXpos = x1 + PADDING_LEFT;
    final int textYpos = y1 + fm.getLeading() + fm.getAscent() + PADDING_TOP;

    final Color textColor = Utils.getContrastColor( flagColor );
    aCanvas.setColor( textColor );

    aCanvas.drawString( timeStr, textXpos, textYpos );
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
