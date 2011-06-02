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
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.test.view.renderer;


import java.awt.*;

import nl.lxtreme.test.model.*;


/**
 * Renders a constant low signal.
 */
public class ConstantSignalRenderer extends BaseRenderer implements SignalRenderer
{
  // VARIABLES

  private final long[] timestamps;
  private final double zoomFactor;
  private final int signalHeight;

  private final int[] x;
  private final int[] y;

  private final int startIdx;
  private final int size;

  // CONSTRUCTORS

  /**
   * Creates a new ConstantSignalRenderer instance.
   */
  public ConstantSignalRenderer( final SampleDataModel aDataModel, final ScreenModel aScreenModel,
      final int aStartIndex, final int aSize )
  {
    this.timestamps = aDataModel.getTimestamps();
    this.zoomFactor = aScreenModel.getZoomFactor();
    this.signalHeight = aScreenModel.getSignalHeight();

    this.startIdx = aStartIndex;
    this.size = aSize;

    this.x = new int[2];
    this.y = new int[2];
  }

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
    this.x[0] = ( int )( this.zoomFactor * this.timestamps[this.startIdx] );
    this.y[0] = this.signalHeight;

    final int maxT = Math.min( this.startIdx + this.size, this.timestamps.length - 1 );

    this.x[1] = ( int )( this.zoomFactor * this.timestamps[maxT] );
    this.y[1] = this.signalHeight;

    aCanvas.drawPolyline( this.x, this.y, 2 );

    return null;
  }
}
