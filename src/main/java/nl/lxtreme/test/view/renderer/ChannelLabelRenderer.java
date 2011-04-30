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

import nl.lxtreme.test.*;


/**
 * Renders the channel label + index.
 */
public class ChannelLabelRenderer extends BaseRenderer
{
  // CONSTANTS

  private static final int PADDING_RIGHT = 6;
  private static final float INDEX_RELATIVE_FONT_SIZE = 0.75f;

  // VARIABLES

  private int width;
  private String channelIndex;
  private String channelLabel;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    if ( ( aParameters == null ) || ( aParameters.length < 3 ) )
    {
      throw new IllegalArgumentException( "Expected two Integer & one String parameter!" );
    }
    int index = ( ( Integer )aParameters[0] ).intValue();
    this.width = ( ( Integer )aParameters[1] ).intValue();
    this.channelLabel = ( String )aParameters[2];
    this.channelIndex = Integer.toString( index );

    if ( ( this.channelLabel == null ) || this.channelLabel.trim().isEmpty() )
    {
      this.channelLabel = this.channelIndex;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Rectangle render( final Graphics2D aCanvas )
  {
    Font labelFont = aCanvas.getFont();
    FontMetrics labelFm = aCanvas.getFontMetrics();

    // Derive the index font from the label font...
    Font indexFont = labelFont.deriveFont( Font.PLAIN, labelFont.getSize() * INDEX_RELATIVE_FONT_SIZE );
    FontMetrics indexFm = aCanvas.getFontMetrics( indexFont );

    Color labelBackground = aCanvas.getColor();

    final int labelYpos = ( labelFm.getAscent() + labelFm.getLeading() );
    final int labelXpos = ( this.width - labelFm.stringWidth( this.channelLabel ) - PADDING_RIGHT );

    aCanvas.setColor( Utils.getContrastColor( labelBackground ) );
    aCanvas.drawString( this.channelLabel, labelXpos, labelYpos );

    final int indexYpos = ( labelFm.getHeight() + indexFm.getAscent() + indexFm.getLeading() );
    final int indexXpos = ( this.width - indexFm.stringWidth( this.channelIndex ) - PADDING_RIGHT );

    aCanvas.setFont( indexFont );
    aCanvas.drawString( this.channelIndex, indexXpos, indexYpos );

    return null;
  }

}
