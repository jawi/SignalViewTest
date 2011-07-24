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
package nl.lxtreme.test.view.model;


import java.beans.*;
import java.util.*;

import nl.lxtreme.test.model.*;
import nl.lxtreme.test.model.ScreenModel.*;
import nl.lxtreme.test.view.*;


/**
 * The main model for the {@link SignalDiagramComponent}.
 */
public class SignalDiagramModel extends AbstractViewModel
{
  // CONSTANTS

  /** The absolute maximum number of cursors. */
  private static final int MAX_CURSORS = 10;

  /**
   * Defines the area around each cursor in which the mouse cursor should be in
   * before the cursor can be moved.
   */
  private static final int CURSOR_SENSITIVITY_AREA = 5;

  // VARIABLES

  private int[] values;
  private long[] timestamps;
  private int sampleRate;
  private String[] channelLabels;
  private int sampleWidth;

  private int signalHeight;
  private int channelHeight;
  private SignalAlignment signalAlignment;

  private final Long[] cursors;
  private final String[] cursorLabels;

  private final PropertyChangeSupport pcs = new PropertyChangeSupport( this );

  // CONSTRUCTORS

  /**
   * Creates a new SignalDiagramModel instance.
   */
  public SignalDiagramModel( final SignalDiagramController aController )
  {
    super( aController );

    this.cursors = new Long[MAX_CURSORS];
    this.cursorLabels = new String[MAX_CURSORS];
  }

  /**
   * Creates a new SignalDiagramModel instance.
   * 
   * @param aDataModel
   *          the data model to wrap in this model, cannot be <code>null</code>;
   * @param aScreenModel
   *          the screen model to wrap in this model, cannot be
   *          <code>null</code>.
   */
  public SignalDiagramModel( final SignalDiagramController aController, final SampleDataModel aDataModel,
      final ScreenModel aScreenModel )
  {
    this( aController );

    int[] v = aDataModel.getValues();
    this.values = Arrays.copyOf( v, v.length );
    long[] t = aDataModel.getTimestamps();
    this.timestamps = Arrays.copyOf( t, t.length );
    this.sampleRate = aDataModel.getSampleRate();

    setSampleWidth( aDataModel.getWidth() );

    this.channelLabels = new String[aDataModel.getWidth()];

    System.arraycopy( aDataModel.getCursors(), 0, this.cursors, 0, this.cursors.length );
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
    if ( mid < ( aToIndex - 1 ) )
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

  /**
   * Clears the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the cursor to clear, >= 0.
   * @throws IllegalArgumentException
   *           in case the given cursor index was invalid.
   */
  public void clearCursor( final int aCursorIdx )
  {
    setCursor( aCursorIdx, null );
  }

  /**
   * Finds the cursor that is "near" the given timestamp.
   * 
   * @param aTimestamp
   *          the timestamp to find the cursor for.
   * @return a cursor index, or <code>null</code> if no cursor could be found.
   */
  public Integer findCursor( final long aTimestamp )
  {
    final double snapArea = CURSOR_SENSITIVITY_AREA / getZoomFactor();

    for ( int i = 0, count = getMaxCursors(); i < count; i++ )
    {
      if ( !isCursorDefined( i ) )
      {
        continue;
      }

      long cursorValue = getCursor( i ).longValue();
      final double min = cursorValue - snapArea;
      final double max = cursorValue + snapArea;

      if ( ( aTimestamp >= min ) && ( aTimestamp <= max ) )
      {
        return Integer.valueOf( i );
      }
    }

    return null;
  }

  /**
   * @param aSignalHover
   */
  public void fireMeasurementEvent( final SignalHoverInfo aSignalHover )
  {
    this.pcs.firePropertyChange( "measurementInfo", null, aSignalHover );
  }

  /**
   * Returns the maximum time stamp present in this model.
   * 
   * @return a maximum time stamp, >= 0.
   */
  public long getAbsoluteLength()
  {
    int idx = this.timestamps.length - 1;
    if ( idx >= 0 )
    {
      return this.timestamps[idx] + 1;
    }
    return 0;
  }

  /**
   * Returns the label for the channel with the given index.
   * 
   * @param aChannelIdx
   *          a channel index, >= 0 && < {@link #getSampleWidth()}.
   * @return a channel label, or <code>null</code> if no channel label is set.
   * @throws IllegalArgumentException
   *           in case the given channel index was invalid.
   */
  public String getChannelLabel( final int aChannelIdx )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= getSampleWidth() ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }
    return this.channelLabels[aChannelIdx];
  }

  /**
   * Returns the cursor value for the given index.
   * 
   * @param aCursorIdx
   *          the index of the cursor to return, >= 0.
   * @return a cursor timestamp value, or <code>null</code> if the cursor is
   *         undefined.
   * @throws IllegalArgumentException
   *           in case the given cursor index was invalid.
   */
  public Long getCursor( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    return this.cursors[aCursorIdx];
  }

  /**
   * Returns the cursor label for the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the index of the cursor to return the label for, >= 0.
   * @return a cursor label, can be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given cursor index was invalid.
   */
  public String getCursorLabel( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    return this.cursorLabels[aCursorIdx];
  }

  /**
   * Returns the maximum number of available cursors.
   * 
   * @return a cursor count, > 0.
   */
  public int getMaxCursors()
  {
    return SampleDataModel.MAX_CURSORS;
  }

  /**
   * Returns the number of samples.
   * 
   * @return a sample count, >= 0.
   */
  public int getSampleCount()
  {
    return this.values.length;
  }

  /**
   * Returns the sample rate.
   * 
   * @return a sample rate, in Hertz.
   */
  public int getSampleRate()
  {
    return this.sampleRate;
  }

  /**
   * Returns the width of a sample.
   * 
   * @return a sample width, in bits.
   */
  public final int getSampleWidth()
  {
    return this.sampleWidth;
  }

  /**
   * Returns the index where the given time stamp can be found in the array of
   * time stamps.
   * 
   * @param aValue
   *          a time stamp value.
   * @return an array index, >= 0.
   */
  public int getTimestampIndex( final long aValue )
  {
    final int length = this.timestamps.length;
    return binarySearch( this.timestamps, 0, length, aValue );
  }

  /**
   * Returns the time stamps of the individual samples.
   * 
   * @return the time stamps as array of long values, never <code>null</code>.
   */
  public long[] getTimestamps()
  {
    return this.timestamps;
  }

  /**
   * Returns the individual sample values.
   * 
   * @return the sample values as array of int values, never <code>null</code>.
   */
  public int[] getValues()
  {
    return this.values;
  }

  /**
   * Returns whether the cursor denoted by the given index is defined.
   * 
   * @param aCursorIdx
   *          the index of the cursor to check.
   * @return <code>true</code> if the cursor with the given index is defined,
   *         <code>false</code> otherwise.
   */
  public boolean isCursorDefined( final int aCursorIdx )
  {
    return getCursor( aCursorIdx ) != null;
  }

  /**
   * Sets the channel height on screen.
   * 
   * @param aChannelHeight
   *          the channelHeight to set, in pixels, > 0.
   */
  public void setChannelHeight( final int aChannelHeight )
  {
    if ( aChannelHeight <= 0 )
    {
      throw new IllegalArgumentException( "Invalid channel height!" );
    }
    int oldValue = this.channelHeight;
    this.channelHeight = aChannelHeight;
    this.pcs.firePropertyChange( "channelHeight", oldValue, aChannelHeight );
  }

  /**
   * Sets the label for the channel with the given index.
   * 
   * @param aChannelIdx
   *          a channel index, >= 0 && < {@link #getSampleWidth()};
   * @param aLabel
   *          the label to set for the channel, can be <code>null</code>.
   * @return a channel label, or <code>null</code> if no channel label is set.
   * @throws IllegalArgumentException
   *           in case the given channel index was invalid.
   */
  public void setChannelLabel( final int aChannelIdx, final String aLabel )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= getSampleWidth() ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }
    String oldValue = this.channelLabels[aChannelIdx];
    this.channelLabels[aChannelIdx] = ( aLabel == null ) ? null : aLabel.trim();
    this.pcs.fireIndexedPropertyChange( "channelLabels", aChannelIdx, oldValue, this.channelLabels[aChannelIdx] );
  }

  /**
   * Sets the cursor with the given index to the given timestamp.
   * 
   * @param aCursorIdx
   *          the cursor index to set, >= 0;
   * @param aTimestamp
   *          the new timestamp of the cursor.
   * @return the old cursor value, can be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given cursor index was invalid.
   */
  public void setCursor( final int aCursorIdx, final long aTimestamp )
  {
    setCursor( aCursorIdx, Long.valueOf( aTimestamp ) );
  }

  /**
   * Sets the cursor label for the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the index of the cursor to return the label for, >= 0;
   * @param aLabel
   *          the label to set, can be <code>null</code>.
   * @return a cursor label, can be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given cursor index was invalid.
   */
  public void setCursorLabel( final int aCursorIdx, final String aLabel )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    String oldValue = this.cursorLabels[aCursorIdx];
    this.cursorLabels[aCursorIdx] = ( aLabel == null ) ? null : aLabel.trim();
    this.pcs.fireIndexedPropertyChange( "cursorLabels", aCursorIdx, oldValue, this.cursorLabels[aCursorIdx] );
  }

  /**
   * Sets the sample data for this model.
   * 
   * @param aSampleValues
   *          the sample values, as array of int values, cannot be
   *          <code>null</code>;
   * @param aTimestamps
   *          the timestamps, as array of long values, cannot be
   *          <code>null</code>;
   * @param aSampleRate
   *          the sample rate, in Hertz, > 0;
   * @param aSampleWidth
   *          the sample width, in bits, > 0.
   * @throws IllegalArgumentException
   *           in case one of the parameters was invalid (<code>null</code> or
   *           (less than) zero).
   */
  public void setSampleData( final int[] aSampleValues, final long[] aTimestamps, final int aSampleRate,
      final int aSampleWidth )
  {
    if ( aSampleValues == null )
    {
      throw new IllegalArgumentException( "Parameter sample values cannot be null!" );
    }
    if ( aTimestamps == null )
    {
      throw new IllegalArgumentException( "Parameter timestamps cannot be null!" );
    }
    if ( aSampleRate <= 0 )
    {
      throw new IllegalArgumentException( "Parameter sample rate should be greater than zero!" );
    }
    if ( aSampleWidth <= 0 )
    {
      throw new IllegalArgumentException( "Parameter sample width should be greater than zero!" );
    }

    int[] oldValues = this.values;
    this.values = Arrays.copyOf( aSampleValues, aSampleValues.length );
    this.pcs.firePropertyChange( "values", oldValues, this.values );

    long[] oldTimestamps = this.timestamps;
    this.timestamps = Arrays.copyOf( aTimestamps, aTimestamps.length );
    this.pcs.firePropertyChange( "timestamps", oldTimestamps, this.timestamps );

    int oldSampleRate = this.sampleRate;
    this.sampleRate = aSampleRate;
    this.pcs.firePropertyChange( "sampleRate", oldSampleRate, this.sampleRate );

    String[] oldChannelLabels = this.channelLabels;
    this.channelLabels = new String[aSampleWidth];
    this.pcs.firePropertyChange( "channelLabels", oldChannelLabels, this.channelLabels );

    setSampleWidth( aSampleWidth );

    // resetChannelMapping();
  }

  /**
   * Sets signal alignment on screen.
   * 
   * @param aSignalAlignment
   *          the signal alignment to set, cannot be <code>null</code>.
   */
  public void setSignalAlignment( final SignalAlignment aSignalAlignment )
  {
    if ( aSignalAlignment == null )
    {
      throw new IllegalArgumentException( "Invalid signal alignment!" );
    }
    SignalAlignment oldValue = this.signalAlignment;
    this.signalAlignment = aSignalAlignment;
    this.pcs.firePropertyChange( "signalAlignment", oldValue, aSignalAlignment );
  }

  /**
   * Sets the signal height on screen.
   * 
   * @param aSignalHeight
   *          the new signal height to set, > 0.
   * @throws IllegalArgumentException
   *           in case the given signal height was invalid.
   */
  public void setSignalHeight( final int aSignalHeight )
  {
    if ( aSignalHeight <= 0 )
    {
      throw new IllegalArgumentException( "Invalid signal height!" );
    }
    int oldValue = this.signalHeight;
    this.signalHeight = aSignalHeight;
    this.pcs.firePropertyChange( "signalHeight", oldValue, aSignalHeight );
  }

  /**
   * Sets sampleWidth to the given value.
   * 
   * @param aSampleWidth
   *          the sampleWidth to set.
   */
  protected final void setSampleWidth( final int aSampleWidth )
  {
    int oldSampleWidth = this.sampleWidth;
    this.sampleWidth = aSampleWidth;

    this.pcs.firePropertyChange( "sampleWidth", oldSampleWidth, this.sampleWidth );
  }

  /**
   * Sets the cursor with the given index to the given timestamp.
   * 
   * @param aCursorIdx
   *          the cursor index to set, >= 0;
   * @param aTimestamp
   *          the new timestamp of the cursor.
   * @return the old cursor value, can be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given cursor index was invalid.
   */
  private void setCursor( final int aCursorIdx, final Long aTimestamp )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    final Long oldValue = this.cursors[aCursorIdx];
    this.cursors[aCursorIdx] = aTimestamp;

    this.pcs.fireIndexedPropertyChange( "cursors", aCursorIdx, oldValue, aTimestamp );
  }
}
