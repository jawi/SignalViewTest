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
package nl.lxtreme.test.model;


import java.util.*;

import nl.lxtreme.test.*;


/**
 * @author jajans
 */
public class SampleDataModel
{
  // INNER TYPES

  public static interface SampleDataProvider
  {
    /**
     * @param aValues
     * @param aTimestamps
     * @param aSize
     * @return the sample rate, in Hertz.
     */
    int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize );
  }

  // CONSTANTS

  public static final int MAX_CURSORS = 10;

  // VARIABLES

  private final int[] values;
  private final long[] timestamps;
  private final Long[] cursors;
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

    System.out.println( "Data sample rate  : " + Utils.displayFrequency( this.sampleRate ) );
    System.out.println( "Data size         : " + aSize + " samples." );
    System.out.println( "Total sample time : " + Utils.displayTime( aSize / ( double )this.sampleRate ) );

    this.cursors = new Long[MAX_CURSORS];
    this.cursors[0] = Long.valueOf( 100 );
    this.cursors[1] = Long.valueOf( 200 );
  }

  // METHODS

  /**
   * Provides a binary search for arrays of long-values.
   * <p>
   * This implementation is directly copied from the JDK
   * {@link Arrays#binarySearch(long[], long)} implementation, slightly modified
   * to only perform a single comparison-action.
   * </p>
   * 
   * @param aArray
   *          the array of long values to search in;
   * @param aFromIndex
   *          the from index to search from;
   * @param aToIndex
   *          the to index to search up and until;
   * @param aKey
   *          the value to search for.
   * @return the index of the given key, which is either the greatest index of
   *         the value less or equal to the given key.
   * @see Arrays#binarySearch(long[], long)
   */
  static final int binarySearch( final long[] aArray, final int aFromIndex, final int aToIndex, final long aKey )
  {
    int mid = -1;
    int low = aFromIndex;
    int high = aToIndex - 1;

    while ( low <= high )
    {
      mid = ( low + high ) >>> 1;
      final long midVal = aArray[mid];

      final int c = ( aKey < midVal ? -1 : ( aKey == midVal ? 0 : 1 ) );
      if ( c > 0 )
      {
        low = mid + 1;
      }
      else if ( c < 0 )
      {
        high = mid - 1;
      }
      else
      {
        return mid; // key found
      }
    }

    if ( mid < 0 )
    {
      return low;
    }

    // Determine the insertion point, avoid crossing the array boundaries...
    if ( mid < aToIndex - 1 )
    {
      // If the searched value is greater than the value of the found index,
      // insert it after this value, otherwise before it (= the last return)...
      if ( aKey > aArray[mid] )
      {
        return mid + 1;
      }
    }

    return mid;
  }

  public long getAbsoluteLength()
  {
    int idx = this.timestamps.length - 1;
    if ( idx >= 0 )
    {
      return this.timestamps[idx] + 1;
    }
    return -1;
  }

  /**
   * @param aCursorIdx
   * @return
   */
  public Long getCursor( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException();
    }
    return this.cursors[aCursorIdx];
  }

  public Long[] getCursors()
  {
    return this.cursors;
  }

  public int getSampleRate()
  {
    return this.sampleRate;
  }

  public int getSize()
  {
    return this.values.length;
  }

  public int getTimestampIndex( final long aValue )
  {
    final int length = this.timestamps.length;
    return binarySearch( this.timestamps, 0, length, aValue );
  }

  public long[] getTimestamps()
  {
    return this.timestamps;
  }

  public int[] getValues()
  {
    return this.values;
  }

  public int getWidth()
  {
    return 9;
  }

  /**
   * @param aCursorIdx
   * @return the old cursor value, can be <code>null</code>.
   */
  public Long setCursor( final int aCursorIdx, final Long aTimestamp )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException();
    }
    final Long oldValue = this.cursors[aCursorIdx];
    this.cursors[aCursorIdx] = aTimestamp;
    return oldValue;
  }
}
