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


import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.event.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.model.Cursor;
import nl.lxtreme.test.view.*;


/**
 * The main model for the {@link SignalDiagramComponent}.
 */
public class SignalDiagramModel
{
  // INNER TYPES

  public static enum HelpTextDisplay
  {
    INVISIBLE, TOOLTIP, LABEL;
  }

  /**
   * @author jawi
   */
  public static enum SignalAlignment
  {
    TOP, BOTTOM, CENTER;
  }

  // CONSTANTS

  /** The tick increment (in pixels). */
  private static final int TIMELINE_INCREMENT = 5;

  private static final int CURSORS_VISIBLE = ( 1 << 0 );
  private static final int SNAP_CURSOR_MODE = ( 1 << 1 );
  private static final int MEASUREMENT_MODE = ( 1 << 2 );

  private static final Color[] SALEAE_COLORS = { //
  Utils.parseColor( "000000" ), //
      Utils.parseColor( "8B4513" ), //
      Utils.parseColor( "FF0000" ), //
      Utils.parseColor( "FFA500" ), //
      Utils.parseColor( "FFFF00" ), //
      Utils.parseColor( "00FF00" ), //
      Utils.parseColor( "0000FF" ), //
      Utils.parseColor( "A020F0" ), //
      Utils.parseColor( "CDC9C9" ) //
  };
  private static final Color[] OLS_COLORS = { //
  Utils.parseColor( "000000" ), //
      Utils.parseColor( "FFFFFF" ), //
      Utils.parseColor( "00FF00" ), //
      Utils.parseColor( "FF0000" ), //
      Utils.parseColor( "0000FF" ), //
      Utils.parseColor( "00FF00" ), //
      Utils.parseColor( "FFFF00" ), //
      Utils.parseColor( "0000FF" ), //
      Utils.parseColor( "FF0000" ) //
  };
  private static final Color[] DARK_COLORS = { //
  Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ), //
      Utils.parseColor( "7bf9dd" ) //
  };

  // VARIABLES

  private double zoomFactor;
  private boolean zoomAll;
  private int signalHeight;
  private int channelHeight;
  private int visibleMask;
  private int mode;
  private int[] virtualRowMapping;
  private Color[] colors;
  private String[] channelLabels;
  private SignalAlignment signalAlignment;
  private HelpTextDisplay helpTextDisplay;

  private int[] values;
  private long[] timestamps;
  private Cursor[] cursors;
  private int sampleRate;
  private int sampleWidth;

  private final SignalDiagramController controller;
  private final EventListenerList eventListeners;
  private final PropertyChangeSupport propertyChangeSupport;

  // CONSTRUCTORS

  /**
   * Creates a new SignalDiagramModel instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public SignalDiagramModel( final SignalDiagramController aController )
  {
    this.controller = aController;

    this.eventListeners = new EventListenerList();
    this.propertyChangeSupport = new PropertyChangeSupport( this );

    this.signalHeight = 20;
    this.channelHeight = 40;
    this.zoomFactor = 0.01;

    this.signalAlignment = SignalAlignment.CENTER;
    this.helpTextDisplay = HelpTextDisplay.TOOLTIP;

    this.mode = 0;

    this.visibleMask = 0x555;

    this.virtualRowMapping = new int[0];
    this.colors = new Color[0];
    this.channelLabels = new String[0];
    this.cursors = new Cursor[0];
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
   * Moves an element from a "old" position to a "new" position, shifting all
   * other elements.
   * <p>
   * NOTE: the given array's contents will be mutated!
   * </p>
   * 
   * @param aInput
   *          the input array to move the elements from, cannot be
   *          <code>null</code>;
   * @param aOldIdx
   *          the index of the element to move;
   * @param aNewIdx
   *          the index to move the element to.
   */
  static final void shiftElements( final int[] aInput, final int aOldIdx, final int aNewIdx )
  {
    final int length = aInput.length;

    final int moved = aInput[aOldIdx];
    // Delete element from array...
    System.arraycopy( aInput, aOldIdx + 1, aInput, aOldIdx, length - 1 - aOldIdx );
    // Make space for new element...
    System.arraycopy( aInput, aNewIdx, aInput, aNewIdx + 1, length - 1 - aNewIdx );
    // Set actual (inserted) element...
    aInput[aNewIdx] = moved;
  }

  /**
   * Adds a cursor change listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addCursorChangeListener( final ICursorChangeListener aListener )
  {
    this.eventListeners.add( ICursorChangeListener.class, aListener );
  }

  /**
   * Adds a data model change listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addDataModelChangeListener( final IDataModelChangeListener aListener )
  {
    this.eventListeners.add( IDataModelChangeListener.class, aListener );
  }

  /**
   * Adds a measurement listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addMeasurementListener( final IMeasurementListener aListener )
  {
    this.eventListeners.add( IMeasurementListener.class, aListener );
  }

  /**
   * Adds a property change listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addPropertyChangeListener( final PropertyChangeListener aListener )
  {
    this.propertyChangeSupport.addPropertyChangeListener( aListener );
  }

  /**
   * {@inheritDoc}
   */
  public int findCursor( final long aTimestamp, final double aSensitivityArea )
  {
    for ( Cursor cursor : this.cursors )
    {
      if ( cursor.inArea( aTimestamp, aSensitivityArea ) )
      {
        return cursor.getIndex();
      }
    }

    return -1;
  }

  /**
   * @param aPoint
   */
  public void fireMeasurementEvent( final SignalHoverInfo signalHover )
  {
    final IMeasurementListener[] listeners = this.eventListeners.getListeners( IMeasurementListener.class );
    for ( IMeasurementListener listener : listeners )
    {
      if ( listener.isListening() )
      {
        listener.handleMeasureEvent( signalHover );
      }
    }
  }

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
   * Returns the absolute height of the screen.
   * 
   * @return a screen height, in pixels, >= 0 && < {@value Integer#MAX_VALUE}.
   */
  public int getAbsoluteScreenHeight()
  {
    return getChannelHeight() * getSampleWidth();
  }

  /**
   * Returns the absolute width of the screen.
   * 
   * @return a screen width, in pixels, >= 0 && < {@value Integer#MAX_VALUE}.
   */
  public int getAbsoluteScreenWidth()
  {
    final double result = getAbsoluteLength() * getZoomFactor();
    if ( result > Integer.MAX_VALUE )
    {
      return Integer.MAX_VALUE;
    }
    return ( int )result;
  }

  /**
   * {@inheritDoc}
   */
  public double getCaptureLength()
  {
    return getAbsoluteLength() / ( double )getSampleRate();
  }

  /**
   * @return the colors
   */
  public Color getChannelColor( final int aChannelIdx )
  {
    return this.colors[aChannelIdx];
  }

  /**
   * @return
   */
  public int getChannelHeight()
  {
    return this.channelHeight;
  }

  /**
   * @param aChannelIdx
   * @return
   */
  public String getChannelLabel( final int aChannelIdx )
  {
    return this.channelLabels[aChannelIdx];
  }

  /**
   * {@inheritDoc}
   */
  public Cursor getCursor( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    return this.cursors[aCursorIdx];
  }

  /**
   * Returns the time interval displayed by the current view.
   * 
   * @return a time interval, in seconds.
   */
  public double getDisplayedTimeInterval()
  {
    final Dimension visibleRect = this.controller.getSignalDiagram().getVisibleViewSize();
    if ( visibleRect == null )
    {
      return 0.0;
    }
    return visibleRect.width / ( double )getSampleRate();
  }

  /**
   * Returns how the time line help text is to be displayed.
   * 
   * @return a {@link HelpTextDisplay} value, never <code>null</code>.
   */
  public HelpTextDisplay getHelpTextDisplayMode()
  {
    return this.helpTextDisplay;
  }

  /**
   * Determines the maximum zoom level that we can handle without causing
   * display problems.
   * <p>
   * It appears that the maximum width of a component can be
   * {@link Short#MAX_VALUE} pixels wide.
   * </p>
   * 
   * @return a maximum zoom level.
   */
  public double getMaxZoomLevel()
  {
    final double end = this.timestamps[this.timestamps.length - 1] + 1;
    final double start = this.timestamps[0];
    return Math.floor( Integer.MAX_VALUE / ( end - start ) );
  }

  /**
   * {@inheritDoc}
   */
  public int getSampleCount()
  {
    return this.values.length;
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
  public int getSampleWidth()
  {
    return this.sampleWidth;
  }

  /**
   * @return
   */
  public SignalAlignment getSignalAlignment()
  {
    return this.signalAlignment;
  }

  /**
   * @return
   */
  public int getSignalHeight()
  {
    return this.signalHeight;
  }

  /**
   * Returns the hover area of the signal under the given coordinate (= mouse
   * position).
   * 
   * @param aPoint
   *          the mouse coordinate to determine the signal rectangle for, cannot
   *          be <code>null</code>.
   * @return the rectangle of the signal the given coordinate contains,
   *         <code>null</code> if not found.
   */
  public final SignalHoverInfo getSignalHover( final Point aPoint )
  {
    // Calculate the "absolute" time based on the mouse position, use a
    // "over sampling" factor to allow intermediary (between two time stamps)
    // time value to be shown...
    final double refTime = ( ( SignalHoverInfo.TIMESTAMP_FACTOR * aPoint.x ) / this.zoomFactor )
        / ( SignalHoverInfo.TIMESTAMP_FACTOR * this.sampleRate );

    final int virtualChannel = ( aPoint.y / this.channelHeight );
    if ( ( virtualChannel < 0 ) || ( virtualChannel > ( this.sampleWidth - 1 ) ) )
    {
      // Trivial reject: invalid virtual channel...
      return null;
    }

    final int realChannel = toRealRow( virtualChannel );
    final String channelLabel = getChannelLabel( realChannel );

    if ( !isChannelVisible( realChannel ) )
    {
      // Trivial reject: real channel is invisible...
      return new SignalHoverInfo( realChannel, channelLabel, refTime );
    }

    final long[] timestamps = getTimestamps();

    long ts = -1L;
    long tm = -1L;
    long te = -1L;
    long th = -1L;
    int middleXpos = -1;

    // find the reference time value; which is the "timestamp" under the
    // cursor...
    final int refIdx = locationToSampleIndex( aPoint );
    final int[] values = getValues();
    if ( ( refIdx >= 0 ) && ( refIdx < values.length ) )
    {
      final int mask = ( 1 << realChannel );
      final int refValue = ( values[refIdx] & mask );

      int idx = refIdx;
      do
      {
        idx--;
      }
      while ( ( idx >= 0 ) && ( ( values[idx] & mask ) == refValue ) );

      // convert the found index back to "screen" values...
      final int tm_idx = Math.max( 0, idx + 1 );
      tm = timestamps[tm_idx];

      // Search for the original value again, to complete the pulse...
      do
      {
        idx--;
      }
      while ( ( idx >= 0 ) && ( ( values[idx] & mask ) != refValue ) );

      // convert the found index back to "screen" values...
      final int ts_idx = Math.max( 0, idx + 1 );
      ts = timestamps[ts_idx];

      idx = refIdx;
      do
      {
        idx++;
      }
      while ( ( idx < values.length ) && ( ( values[idx] & mask ) == refValue ) );

      // convert the found index back to "screen" values...
      final int te_idx = Math.min( idx, timestamps.length - 1 );
      te = timestamps[te_idx];

      // Determine the width of the "high" part...
      if ( ( values[ts_idx] & mask ) != 0 )
      {
        th = Math.abs( tm - ts );
      }
      else
      {
        th = Math.abs( te - tm );
      }
    }

    final Rectangle rect = new Rectangle();
    rect.x = ( int )( this.zoomFactor * ts );
    rect.width = ( int )( this.zoomFactor * ( te - ts ) );
    rect.y = ( virtualChannel * this.channelHeight ) + getSignalOffset();
    rect.height = this.signalHeight;

    // The position where the "other" signal transition should be...
    middleXpos = ( int )( this.zoomFactor * tm );

    final double timeHigh = th / ( double )this.sampleRate;
    final double timeTotal = ( te - ts ) / ( double )this.sampleRate;

    return new SignalHoverInfo( realChannel, channelLabel, rect, ts, te, refTime, timeHigh, timeTotal, middleXpos );
  }

  /**
   * Returns the signal offset.
   * 
   * @return a signal offset, >= 0.
   * @see #getSignalAlignment()
   */
  public int getSignalOffset()
  {
    final int signalOffset;
    if ( SignalAlignment.BOTTOM.equals( getSignalAlignment() ) )
    {
      signalOffset = ( this.channelHeight - this.signalHeight ) - 2;
    }
    else if ( SignalAlignment.CENTER.equals( getSignalAlignment() ) )
    {
      signalOffset = ( int )( ( this.channelHeight - this.signalHeight ) / 2.0 );
    }
    else
    {
      signalOffset = 1;
    }
    return signalOffset;
  }

  /**
   * Returns the increment of pixels per timeline tick.
   * 
   * @return a tick increment, >= 1.0.
   * @see #getTimebase()
   */
  public double getTickIncrement()
  {
    return Math.max( 1.0, getTimebase() / TIMELINE_INCREMENT );
  }

  /**
   * Determines the time base for the given absolute time (= total time
   * displayed).
   * 
   * @return a time base, as power of 10.
   */
  public double getTimebase()
  {
    final Dimension visibleViewSize = this.controller.getSignalDiagram().getVisibleViewSize();
    final double absoluteTime = visibleViewSize.width / getZoomFactor();
    return Math.pow( 10, Math.round( Math.log10( absoluteTime ) ) );
  }

  /**
   * Returns the increment of pixels per unit of time.
   * 
   * @return a time increment, >= 0.1.
   * @see #getTimebase()
   */
  public double getTimeIncrement()
  {
    return Math.max( 0.1, getTimebase() / ( 10.0 * TIMELINE_INCREMENT ) );
  }

  /**
   * Returns the time interval displayed by a single tick in the time line.
   * 
   * @return a time interval, in seconds.
   */
  public double getTimeInterval()
  {
    return getTimeIncrement() / getSampleRate();
  }

  /**
   * {@inheritDoc}
   */
  public int getTimestampIndex( final long aValue )
  {
    final int length = this.timestamps.length;
    return binarySearch( this.timestamps, 0, length, aValue );
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
   * @return
   */
  public double getZoomFactor()
  {
    return this.zoomFactor;
  }

  /**
   * @param aChannelIdx
   * @return
   */
  public boolean isChannelVisible( final int aChannelIdx )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= this.virtualRowMapping.length ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }

    int mask = ( 1 << aChannelIdx );
    return ( this.visibleMask & mask ) != 0;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isCursorDefined( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    return this.cursors[aCursorIdx].isDefined();
  }

  /**
   * @return the cursorMode
   */
  public boolean isCursorMode()
  {
    return ( this.mode & CURSORS_VISIBLE ) != 0;
  }

  /**
   * @return
   */
  public boolean isMeasurementMode()
  {
    return ( this.mode & MEASUREMENT_MODE ) != 0;
  }

  /**
   * @return the snapCursor
   */
  public boolean isSnapCursor()
  {
    return ( this.mode & SNAP_CURSOR_MODE ) != 0;
  }

  /**
   * Returns whether or not the time line help text is to be displayed.
   * 
   * @return <code>true</code> if the time line help text is to be displayed,
   *         <code>false</code> otherwise.
   */
  public boolean isTimeLineHelpTextDisplayed()
  {
    return HelpTextDisplay.INVISIBLE != this.helpTextDisplay;
  }

  /**
   * @return the zoomAll
   */
  public boolean isZoomAll()
  {
    return this.zoomAll;
  }

  /**
   * Converts the given coordinate to the corresponding sample index.
   * 
   * @param aCoordinate
   *          the coordinate to convert to a sample index, cannot be
   *          <code>null</code>.
   * @return a sample index, >= 0, or -1 if no corresponding sample index could
   *         be found.
   */
  public int locationToSampleIndex( final Point aCoordinate )
  {
    final long timestamp = locationToTimestamp( aCoordinate );
    final int idx = getTimestampIndex( timestamp );
    if ( idx < 0 )
    {
      return -1;
    }
    final int sampleCount = getSampleCount() - 1;
    if ( idx > sampleCount )
    {
      return sampleCount;
    }

    return idx;
  }

  /**
   * Converts the given coordinate to the corresponding sample index.
   * 
   * @param aCoordinate
   *          the coordinate to convert to a sample index, cannot be
   *          <code>null</code>.
   * @return a sample index, >= 0, or -1 if no corresponding sample index could
   *         be found.
   */
  public long locationToTimestamp( final Point aCoordinate )
  {
    final long timestamp = ( long )Math.ceil( aCoordinate.x / getZoomFactor() );
    if ( timestamp < 0 )
    {
      return -1;
    }
    return timestamp;
  }

  /**
   * Moves the given "old" row index to the new row index position.
   * 
   * @param aOldRowIdx
   *          the old (virtual) row to move;
   * @param aNewRowIdx
   *          the new (virtual) row to insert the "old" row to.
   */
  public void moveRows( final int aOldRowIdx, final int aNewRowIdx )
  {
    final int dataWidth = this.virtualRowMapping.length;
    if ( ( aOldRowIdx < 0 ) || ( aOldRowIdx >= dataWidth ) )
    {
      throw new IllegalArgumentException( "Moved row invalid!" );
    }
    if ( ( aNewRowIdx < 0 ) || ( aNewRowIdx >= dataWidth ) )
    {
      throw new IllegalArgumentException( "Insert row invalid!" );
    }

    if ( aOldRowIdx == aNewRowIdx )
    {
      return;
    }

    final int row = toRealRow( aOldRowIdx );
    final int newRow = toRealRow( aNewRowIdx );

    shiftElements( this.virtualRowMapping, row, newRow );
  }

  /**
   * @param aCursorIdx
   */
  public void removeCursor( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }

    final Cursor cursor = this.cursors[aCursorIdx];
    if ( !cursor.isDefined() )
    {
      // Nothing to do; the cursor is not defined...
      return;
    }

    final Cursor oldCursor = cursor.clone();

    cursor.clear();

    ICursorChangeListener[] listeners = this.eventListeners.getListeners( ICursorChangeListener.class );
    for ( ICursorChangeListener listener : listeners )
    {
      listener.cursorRemoved( oldCursor );
    }
  }

  /**
   * Removes a cursor change listener.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeCursorChangeListener( final ICursorChangeListener aListener )
  {
    this.eventListeners.remove( ICursorChangeListener.class, aListener );
  }

  /**
   * Removes a data model change listener.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeDataModelChangeListener( final IDataModelChangeListener aListener )
  {
    this.eventListeners.remove( IDataModelChangeListener.class, aListener );
  }

  /**
   * Removes the given measurement listener from the list of listeners.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeMeasurementListener( final IMeasurementListener aListener )
  {
    this.eventListeners.remove( IMeasurementListener.class, aListener );
  }

  /**
   * Removes a property change listener.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removePropertyChangeListener( final PropertyChangeListener aListener )
  {
    this.propertyChangeSupport.removePropertyChangeListener( aListener );
  }

  /**
   * @param aChannelHeight
   */
  public void setChannelHeight( final int aChannelHeight )
  {
    this.channelHeight = aChannelHeight;
  }

  /**
   * @param aChannelIdx
   * @param aVisible
   */
  public void setChannelVisible( final int aChannelIdx, final boolean aVisible )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= this.virtualRowMapping.length ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }

    int mask = ( 1 << aChannelIdx );
    if ( aVisible )
    {
      this.visibleMask |= mask;
    }
    else
    {
      this.visibleMask &= ~mask;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setCursor( final int aCursorIdx, final long aTimestamp )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx > this.cursors.length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }

    final Cursor cursor = this.cursors[aCursorIdx];
    final Cursor oldCursor = cursor.clone();

    // Update the time stamp of the cursor...
    cursor.setTimestamp( aTimestamp );

    ICursorChangeListener[] listeners = this.eventListeners.getListeners( ICursorChangeListener.class );
    for ( ICursorChangeListener listener : listeners )
    {
      if ( !oldCursor.isDefined() )
      {
        listener.cursorAdded( cursor );
      }
      else
      {
        listener.cursorChanged( oldCursor, cursor );
      }
    }
  }

  /**
   * Returns the color for a cursor with the given index.
   * 
   * @param aCursorIndex
   *          the index of the cursor to retrieve the color for.
   * @return a cursor color, never <code>null</code>.
   */
  public void setCursorColor( final int aCursorIndex, final Color aColor )
  {
    final Cursor cursor = getCursor( aCursorIndex );
    Color oldColor = cursor.getColor();
    cursor.setColor( aColor );

    this.propertyChangeSupport.fireIndexedPropertyChange( "cursorColor", aCursorIndex, oldColor, aColor );
  }

  /**
   * Enables or disables the cursors.
   * 
   * @param aSelected
   *          <code>true</code> to enable the cursors, <code>false</code> to
   *          disable the cursors.
   */
  public void setCursorMode( final boolean aCursorMode )
  {
    if ( aCursorMode )
    {
      this.mode |= CURSORS_VISIBLE;
    }
    else
    {
      this.mode &= ~CURSORS_VISIBLE;
    }

    ICursorChangeListener[] listeners = this.eventListeners.getListeners( ICursorChangeListener.class );
    for ( ICursorChangeListener listener : listeners )
    {
      if ( aCursorMode )
      {
        listener.cursorsVisible();
      }
      else
      {
        listener.cursorsInvisible();
      }
    }
  }

  /**
   * Sets the data model for this controller.
   * 
   * @param aDataModel
   *          the dataModel to set, cannot be <code>null</code>.
   */
  public void setDataModel( final SampleDataModel aDataModel )
  {
    if ( aDataModel == null )
    {
      throw new IllegalArgumentException( "Parameter DataModel cannot be null!" );
    }

    final int[] dmValues = aDataModel.getValues();
    this.values = Arrays.copyOf( dmValues, dmValues.length );

    final long[] dmTimestamps = aDataModel.getTimestamps();
    this.timestamps = Arrays.copyOf( dmTimestamps, dmTimestamps.length );

    this.sampleRate = aDataModel.getSampleRate();
    this.sampleWidth = aDataModel.getWidth();

    this.virtualRowMapping = new int[this.sampleWidth];
    for ( int i = 0; i < this.sampleWidth; i++ )
    {
      this.virtualRowMapping[i] = i;
    }

    final Cursor[] dmCursors = aDataModel.getCursors();
    this.cursors = Arrays.copyOf( dmCursors, dmCursors.length );

    this.colors = new Color[this.sampleWidth];
    int value = 2;
    if ( value == 0 )
    {
      for ( int i = 0; i < this.sampleWidth; i++ )
      {
        int idx = ( i % ( OLS_COLORS.length - 1 ) ) + 1;
        this.colors[i] = OLS_COLORS[idx];
      }
    }
    else if ( value == 1 )
    {
      for ( int i = 0; i < this.sampleWidth; i++ )
      {
        int idx = ( i % ( SALEAE_COLORS.length - 1 ) ) + 1;
        this.colors[i] = SALEAE_COLORS[idx];
      }
    }
    else if ( value == 2 )
    {
      for ( int i = 0; i < this.sampleWidth; i++ )
      {
        int idx = ( i % ( DARK_COLORS.length - 1 ) ) + 1;
        this.colors[i] = DARK_COLORS[idx];
      }
    }

    this.channelLabels = new String[this.sampleWidth];
    for ( int i = 0; i < this.channelLabels.length; i++ )
    {
      this.channelLabels[i] = String.format( "Channel %c", Integer.valueOf( i + 'A' ) );
    }

    final IDataModelChangeListener[] listeners = this.eventListeners.getListeners( IDataModelChangeListener.class );
    for ( IDataModelChangeListener listener : listeners )
    {
      listener.dataModelChanged( aDataModel );
    }
  }

  /**
   * Sets how the time line help text is to be displayed.
   * 
   * @param aHelpTextDisplay
   *          the {@link HelpTextDisplay} to set, cannot be <code>null</code>.
   */
  public void setHelpTextDisplayMode( final HelpTextDisplay aHelpTextDisplay )
  {
    this.helpTextDisplay = aHelpTextDisplay;
  }

  /**
   * @param aEnabled
   */
  public void setMeasurementMode( final boolean aEnabled )
  {
    if ( aEnabled )
    {
      this.mode |= MEASUREMENT_MODE;
    }
    else
    {
      this.mode &= ~MEASUREMENT_MODE;
    }

    IMeasurementListener[] listeners = this.eventListeners.getListeners( IMeasurementListener.class );
    for ( IMeasurementListener listener : listeners )
    {
      if ( aEnabled )
      {
        listener.enableMeasurementMode();
      }
      else
      {
        listener.disableMeasurementMode();
      }
    }
  }

  /**
   * @param aSignalAlignment
   */
  public void setSignalAlignment( final SignalAlignment aSignalAlignment )
  {
    this.signalAlignment = aSignalAlignment;
  }

  /**
   * @param aSignalHeight
   */
  public void setSignalHeight( final int aSignalHeight )
  {
    this.signalHeight = aSignalHeight;
  }

  /**
   * @param aSnapCursor
   *          the snapCursor to set
   */
  public void setSnapCursor( final boolean aSnapCursor )
  {
    if ( aSnapCursor )
    {
      this.mode |= SNAP_CURSOR_MODE;
    }
    else
    {
      this.mode &= ~SNAP_CURSOR_MODE;
    }
  }

  /**
   * @param aZoomAll
   *          the zoomAll to set
   */
  public void setZoomAll( final boolean aZoomAll )
  {
    this.zoomAll = aZoomAll;
  }

  /**
   * @param aZoomFactor
   */
  public void setZoomFactor( final double aZoomFactor )
  {
    double oldFactor = this.zoomFactor;

    this.zoomFactor = aZoomFactor;

    this.propertyChangeSupport.firePropertyChange( "zoomFactor", oldFactor, aZoomFactor );
  }

  /**
   * @param aRowIdx
   * @return
   */
  public int toRealRow( final int aRowIdx )
  {
    return this.virtualRowMapping[aRowIdx];
  }

  /**
   * @param aRowIdx
   * @return
   */
  public int toVirtualRow( final int aRowIdx )
  {
    for ( int i = 0; i < this.virtualRowMapping.length; i++ )
    {
      if ( this.virtualRowMapping[i] == aRowIdx )
      {
        return i;
      }
    }
    return -1;
  }
}
