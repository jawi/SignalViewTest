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
package nl.lxtreme.test.view.renderer;


import java.awt.*;


/**
 * @author jawi
 */
public class ChannelInsertionPointRenderer extends BaseRenderer
{
  // CONSTANTS

  private static final int CHANNEL_ROW_MARKER_WIDTH = 100;

  private static final Stroke INDICATOR_STROKE = new BasicStroke( 1.5f );

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    // NO-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Rectangle render( final Graphics2D aCanvas )
  {
    aCanvas.setStroke( INDICATOR_STROKE );

    aCanvas.drawLine( 0, 0, CHANNEL_ROW_MARKER_WIDTH, 0 );

    return new Rectangle( -2, -2, CHANNEL_ROW_MARKER_WIDTH + 4, 4 );
  }
}
