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
package nl.lxtreme.test.model;


/**
 * @author jajans
 */
public class SampleDataModel
{
  // VARIABLES

  private final int[] values;
  private final long[] timestamps;
  private final Cursor[] cursors;
  private final int sampleRate;

  // CONSTRUCTORS

  /**
   * @param aSize
   */
  public SampleDataModel( final int aSize, final SampleDataProvider aProvider )
  {
    if ( aSize <= 0 )
    {
      throw new IllegalArgumentException();
    }
    this.values = new int[aSize];
    this.timestamps = new long[aSize];

    this.sampleRate = aProvider.getSampleData( this.values, this.timestamps, aSize );

    this.cursors = Cursor.createCursors();
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  public long getAbsoluteLength()
  {
    int idx = this.timestamps.length - 1;

    long length = -1L;
    if ( idx > 0 )
    {
      length = ( this.timestamps[idx] + 1 ) - this.timestamps[0];
    }
    else if ( idx == 0 )
    {
      length = this.timestamps[0];
    }

    return length;
  }

  /**
   * {@inheritDoc}
   */
  public double getCaptureLength()
  {
    return getAbsoluteLength() / ( double )getSampleRate();
  }

  /**
   * {@inheritDoc}
   */
  public Cursor getCursor( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException();
    }
    return this.cursors[aCursorIdx];
  }

  /**
   * {@inheritDoc}
   */
  public Cursor[] getCursors()
  {
    return this.cursors;
  }

  /**
   * {@inheritDoc}
   */
  public int getSampleRate()
  {
    return this.sampleRate;
  }

  /**
   * {@inheritDoc}
   */
  public int getSize()
  {
    return this.values.length;
  }

  /**
   * {@inheritDoc}
   */
  public long[] getTimestamps()
  {
    return this.timestamps;
  }

  /**
   * {@inheritDoc}
   */
  public int[] getValues()
  {
    return this.values;
  }

  /**
   * {@inheritDoc}
   */
  public int getWidth()
  {
    return 32;
  }

  /**
   * {@inheritDoc}
   */
  public void setCursor( final int aCursorIdx, final long aTimestamp )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException();
    }
    this.cursors[aCursorIdx].setTimestamp( aTimestamp );
  }
}
