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
 * Renders the actual signal of a single channel.
 */
public class ActualSignalRenderer extends BaseRenderer implements SignalRenderer
{
  // VARIABLES

  private final long[] timestamps;
  private final int[] values;
  private final double zoomFactor;
  private final int signalHeight;

  private static int[] x;
  private static int[] y;

  private final int startIdx;
  private final int size;
  private int mask;

  // CONSTRUCTORS

  /**
   * Creates a new ActualSignalRenderer instance.
   */
  public ActualSignalRenderer( final SampleDataModel aDataModel, final ScreenModel aScreenModel, final int aStartIndex,
      final int aSize )
  {
    this.timestamps = aDataModel.getTimestamps();
    this.values = aDataModel.getValues();
    this.zoomFactor = aScreenModel.getZoomFactor();
    this.signalHeight = aScreenModel.getSignalHeight();
    this.startIdx = aStartIndex;
    this.size = aSize;

    if ( ( x == null ) || ( x.length < 2 * this.size ) )
    {
      System.out.println( "RESIZE FROM " + ( x == null ? 0 : x.length ) + " TO " + ( 2 * this.size ) );
      x = null;
      x = new int[2 * this.size];
    }
    if ( ( y == null ) || ( y.length < 2 * this.size ) )
    {
      y = null;
      y = new int[2 * this.size];
    }
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    if ( ( aParameters == null ) || ( aParameters.length < 1 ) )
    {
      throw new IllegalArgumentException( "Expected one integer parameter: mask!" );
    }

    this.mask = ( ( Integer )aParameters[0] ).intValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Rectangle render( final Graphics2D aCanvas )
  {
    long timestamp = this.timestamps[this.startIdx];
    int prevSampleValue = ( ( this.values[this.startIdx] & this.mask ) == 0 ) ? 1 : 0;

    x[0] = ( int )( this.zoomFactor * timestamp );
    y[0] = ( this.signalHeight * prevSampleValue );
    int p = 1;

    for ( int sampleIdx = this.startIdx + 1; sampleIdx < this.size; sampleIdx++ )
    {
      int sampleValue = ( ( this.values[sampleIdx] & this.mask ) == 0 ) ? 1 : 0;
      timestamp = this.timestamps[sampleIdx];

      if ( prevSampleValue != sampleValue )
      {
        x[p] = ( int )( this.zoomFactor * timestamp );
        y[p] = ( this.signalHeight * prevSampleValue );
        p++;
      }

      x[p] = ( int )( this.zoomFactor * timestamp );
      y[p] = ( this.signalHeight * sampleValue );
      p++;

      prevSampleValue = sampleValue;
    }

    aCanvas.drawPolyline( x, y, p );

    return null;
  }
}
