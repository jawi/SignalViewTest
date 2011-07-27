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
package nl.lxtreme.test.view;


import java.awt.*;
import java.beans.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.model.*;


/**
 * Provides the main component controller for the signal diagram component.
 */
public final class SignalDiagramController
{
  // CONSTANTS

  private static final Logger LOG = Logger.getLogger( SignalDiagramController.class.getName() );

  // VARIABLES

  private final DragAndDropTargetController dndTargetController;

  private final EventListenerList eventListeners;
  private final PropertyChangeSupport propertyChangeSupport;

  private SampleDataModel dataModel;
  private ScreenModel screenModel;
  private SignalDiagramComponent signalDiagram;

  // CONSTRUCTORS

  /**
   * @param aModel
   */
  public SignalDiagramController()
  {
    this.dndTargetController = new DragAndDropTargetController( this );

    this.eventListeners = new EventListenerList();
    this.propertyChangeSupport = new PropertyChangeSupport( this );
  }

  // METHODS

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
   * @return
   */
  public SampleDataModel getDataModel()
  {
    return this.dataModel;
  }

  /**
   * @return
   */
  public double getDisplayedTimeInterval()
  {
    final Rectangle visibleRect = this.screenModel.getVisibleRect();
    if ( visibleRect == null )
    {
      return 0.0;
    }
    return visibleRect.width / ( double )this.dataModel.getSampleRate();
  }

  /**
   * @return the dndTargetController
   */
  public final DragAndDropTargetController getDndTargetController()
  {
    return this.dndTargetController;
  }

  /**
   * @return
   */
  public ScreenModel getScreenModel()
  {
    return this.screenModel;
  }

  /**
   * @return the signalDiagram
   */
  public final SignalDiagramComponent getSignalDiagram()
  {
    return this.signalDiagram;
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
    final double sampleRate = this.dataModel.getSampleRate();
    final int signalWidth = this.dataModel.getWidth();
    final int signalHeight = this.screenModel.getSignalHeight();
    final int channelHeight = this.screenModel.getChannelHeight();
    final double zoomFactor = this.screenModel.getZoomFactor();

    // Calculate the "absolute" time based on the mouse position, use a
    // "over sampling" factor to allow intermediary (between two time stamps)
    // time value to be shown...
    final double refTime = ( ( SignalHoverInfo.TIMESTAMP_FACTOR * aPoint.x ) / zoomFactor )
        / ( SignalHoverInfo.TIMESTAMP_FACTOR * sampleRate );

    final int virtualChannel = ( aPoint.y / channelHeight );
    if ( ( virtualChannel < 0 ) || ( virtualChannel > ( signalWidth - 1 ) ) )
    {
      // Trivial reject: invalid virtual channel...
      return null;
    }

    final int realChannel = this.screenModel.toRealRow( virtualChannel );
    final String channelLabel = this.screenModel.getChannelLabel( realChannel );

    if ( !this.screenModel.isChannelVisible( realChannel ) )
    {
      // Trivial reject: real channel is invisible...
      return new SignalHoverInfo( realChannel, channelLabel, refTime );
    }

    final long[] timestamps = this.dataModel.getTimestamps();

    long ts = -1L;
    long tm = -1L;
    long te = -1L;
    long th = -1L;
    int middleXpos = -1;

    // find the reference time value; which is the "timestamp" under the
    // cursor...
    final int refIdx = locationToSampleIndex( aPoint );
    final int[] values = this.dataModel.getValues();
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
    rect.x = ( int )( zoomFactor * ts );
    rect.width = ( int )( zoomFactor * ( te - ts ) );
    rect.y = ( virtualChannel * channelHeight ) + this.screenModel.getSignalOffset();
    rect.height = signalHeight;

    // The position where the "other" signal transition should be...
    middleXpos = ( int )( zoomFactor * tm );

    final double timeHigh = th / sampleRate;
    final double timeTotal = ( te - ts ) / sampleRate;

    return new SignalHoverInfo( realChannel, channelLabel, rect, ts, te, refTime, timeHigh, timeTotal, middleXpos );
  }

  /**
   * @return
   */
  public double getTimeInterval()
  {
    return this.screenModel.getTimeIncrement() / this.dataModel.getSampleRate();
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
    if ( this.dataModel == null )
    {
      return false;
    }
    return this.dataModel.isCursorDefined( aCursorIdx );
  }

  /**
   * @return
   */
  public boolean isCursorMode()
  {
    return this.screenModel.isCursorMode();
  }

  /**
   * @return
   */
  public boolean isMeasurementMode()
  {
    return this.screenModel.isMeasurementMode();
  }

  /**
   * @return
   */
  public boolean isSnapModeEnabled()
  {
    return this.screenModel.isSnapCursor();
  }

  /**
   * @return true if the current zoom factor is 'zoom all', false otherwise.
   */
  public boolean isZoomAll()
  {
    return this.screenModel.isZoomAll();
  }

  /**
   * Drags a cursor with a given index to a given point, possibly snapping to a
   * signal edge.
   * 
   * @param aCursorIdx
   *          the cursor index to move, should be &gt;= 0 && &lt; 10;
   * @param aPoint
   *          the new point of the cursor, in case of snapping, it will use this
   *          point to find the nearest signal edge, cannot be <code>null</code>
   *          .
   */
  public void moveCursor( final int aCursorIdx, final Point aPoint )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx >= this.dataModel.getCursors().length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }

    final SignalView view = getSignalView();

    final Point point = getCursorDropPoint( convertToPointOf( view, aPoint ) );
    final long newCursorTimestamp = locationToTimestamp( point );

    Long oldValue = this.dataModel.setCursor( aCursorIdx, Long.valueOf( newCursorTimestamp ) );

    ICursorChangeListener[] listeners = this.eventListeners.getListeners( ICursorChangeListener.class );
    for ( ICursorChangeListener listener : listeners )
    {
      if ( oldValue == null )
      {
        listener.cursorAdded( aCursorIdx, newCursorTimestamp );
      }
      else
      {
        listener.cursorChanged( aCursorIdx, oldValue.longValue(), newCursorTimestamp );
      }
    }
  }

  /**
   * Recalculates the dimensions of the main view.
   */
  public void recalculateDimensions()
  {
    final JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, getSignalView() );
    if ( scrollPane == null )
    {
      return;
    }

    final Rectangle viewPortSize = scrollPane.getViewport().getVisibleRect();

    int width = ( int )Math.min( getMaxWidth(), getAbsoluteLength() );
    if ( width < viewPortSize.width )
    {
      width = viewPortSize.width;
    }

    int height = ( this.screenModel.getChannelHeight() * this.dataModel.getWidth() );
    if ( height < viewPortSize.height )
    {
      height = viewPortSize.height;
    }

    JComponent signalView = ( JComponent )scrollPane.getViewport().getView();
    signalView.setPreferredSize( new Dimension( width, height ) );
    signalView.revalidate();

    TimeLineView timeline = ( TimeLineView )scrollPane.getColumnHeader().getView();
    // the timeline component always follows the width of the signal view, but
    // with a fixed height...
    timeline.setPreferredSize( new Dimension( width, timeline.getTimeLineHeight() ) );
    timeline.setMinimumSize( signalView.getPreferredSize() );
    timeline.revalidate();

    ChannelLabelsView channelLabels = ( ChannelLabelsView )scrollPane.getRowHeader().getView();
    // the channel label component calculates its own 'optimal' width, but
    // doesn't know squat about the correct height...
    final Dimension minimumSize = channelLabels.getMinimumSize();
    channelLabels.setMinimumSize( new Dimension( minimumSize.width, height ) );
    channelLabels.setPreferredSize( new Dimension( minimumSize.width, height ) );
    channelLabels.revalidate();

    scrollPane.repaint();

    // Update the screen model...
    Rectangle oldVisibleRect = this.screenModel.getVisibleRect();
    this.screenModel.setVisibleRect( viewPortSize );

    this.propertyChangeSupport.firePropertyChange( "visibleRect", oldVisibleRect, viewPortSize );
  }

  /**
   * Removes the cursor denoted by the given index. If the cursor with the given
   * index is <em>undefined</em> this method does nothing (not even call event
   * listeners!).
   * 
   * @param aCursorIdx
   *          the index of the cursor to remove.
   */
  public void removeCursor( final int aCursorIdx )
  {
    if ( ( aCursorIdx < 0 ) || ( aCursorIdx >= this.dataModel.getCursors().length ) )
    {
      throw new IllegalArgumentException( "Invalid cursor index!" );
    }
    if ( !this.dataModel.isCursorDefined( aCursorIdx ) )
    {
      return;
    }

    final Long oldValue = this.dataModel.setCursor( aCursorIdx, null );

    ICursorChangeListener[] listeners = this.eventListeners.getListeners( ICursorChangeListener.class );
    for ( ICursorChangeListener listener : listeners )
    {
      listener.cursorRemoved( aCursorIdx, oldValue.longValue() );
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
   * Turns the visibility of all cursors either on or off.
   * <p>
   * This method does <em>not</em> modify any cursor, only whether they are
   * displayed or not!
   * </p>
   * 
   * @param aVisible
   *          <code>true</code> if the cursors should be made visible,
   *          <code>false</code> if the cursors should be made invisible.
   */
  public void setCursorsVisible( final boolean aVisible )
  {
    this.screenModel.setCursorMode( aVisible );

    ICursorChangeListener[] listeners = this.eventListeners.getListeners( ICursorChangeListener.class );
    for ( ICursorChangeListener listener : listeners )
    {
      if ( aVisible )
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
      throw new IllegalArgumentException();
    }

    this.dataModel = aDataModel;

    setScreenModel( new ScreenModel( aDataModel.getWidth() ) );

    final IDataModelChangeListener[] listeners = this.eventListeners.getListeners( IDataModelChangeListener.class );
    for ( IDataModelChangeListener listener : listeners )
    {
      listener.dataModelChanged( aDataModel );
    }

    zoomOriginal();
  }

  /**
   * Enables or disables the measurement mode.
   * 
   * @param aEnabled
   *          <code>true</code> to enable the measurement mode,
   *          <code>false</code> to disable this mode.
   */
  public void setMeasurementMode( final boolean aEnabled )
  {
    this.screenModel.setMeasurementMode( aEnabled );

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
   * Sets the screen model for this controller.
   * 
   * @param aScreenModel
   *          the screenModel to set, cannot be <code>null</code>.
   */
  public void setScreenModel( final ScreenModel aScreenModel )
  {
    if ( aScreenModel == null )
    {
      throw new IllegalArgumentException();
    }
    this.screenModel = aScreenModel;
  }

  /**
   * Disables the cursor "snap" mode.
   * 
   * @param aSnapMode
   *          <code>true</code> if the snap mode should be enabled,
   *          <code>false</code> otherwise.
   */
  public void setSnapModeEnabled( final boolean aSnapMode )
  {
    this.screenModel.setSnapCursor( aSnapMode );
  }

  /**
   * Zooms to make all data visible in one screen.
   */
  public void zoomAll()
  {
    final double oldFactor = this.screenModel.getZoomFactor();

    try
    {
      Dimension viewSize = getVisibleViewSize();
      this.screenModel.setZoomFactor( viewSize.getWidth() / this.dataModel.getAbsoluteLength() );
    }
    finally
    {
      this.screenModel.setZoomAll( true );
    }

    LOG.log( Level.INFO, "Zoom factor set to " + this.screenModel.getZoomFactor() );

    recalculateDimensions();

    this.propertyChangeSupport.firePropertyChange( "zoomFactor", oldFactor, this.screenModel.getZoomFactor() );
  }

  /**
   * Zooms in with a factor 1.5
   */
  public void zoomIn()
  {
    final double oldFactor = this.screenModel.getZoomFactor();

    zoomRelative( 2.0 );

    recalculateDimensions();

    this.propertyChangeSupport.firePropertyChange( "zoomFactor", oldFactor, this.screenModel.getZoomFactor() );
  }

  /**
   * Zooms to a factor of 1.0.
   */
  public void zoomOriginal()
  {
    final double oldFactor = this.screenModel.getZoomFactor();

    zoomAbsolute( 1.0 );

    recalculateDimensions();

    this.propertyChangeSupport.firePropertyChange( "zoomFactor", oldFactor, this.screenModel.getZoomFactor() );
  }

  /**
   * Zooms out with a factor 1.5
   */
  public void zoomOut()
  {
    final double oldFactor = this.screenModel.getZoomFactor();

    zoomRelative( 0.5 );

    recalculateDimensions();

    this.propertyChangeSupport.firePropertyChange( "zoomFactor", oldFactor, this.screenModel.getZoomFactor() );
  }

  /**
   * @param aComponent
   */
  final void setSignalDiagram( final SignalDiagramComponent aComponent )
  {
    this.signalDiagram = aComponent;
  }

  /**
   * Converts a given point to the coordinate space of a given destination
   * component.
   * 
   * @param aDestination
   *          the destination component to convert the point to, cannot be
   *          <code>null</code>;
   * @param aOriginal
   *          the original point to convert, cannot be <code>null</code>.
   * @return the converted point, never <code>null</code>.
   */
  private Point convertToPointOf( final Component aDestination, final Point aOriginal )
  {
    Component view = SwingUtilities.getAncestorOfClass( JScrollPane.class, aDestination );
    if ( view instanceof JScrollPane )
    {
      view = ( ( JScrollPane )view ).getViewport().getView();
    }
    else
    {
      view = SwingUtilities.getRootPane( aDestination );
    }
    return SwingUtilities.convertPoint( view, aOriginal, aDestination );
  }

  /**
   * Returns the "visual length" of the timeline.
   * 
   * @return a visual length, >= 0.
   */
  private long getAbsoluteLength()
  {
    final long[] timestamps = this.dataModel.getTimestamps();
    final long end = timestamps[timestamps.length - 1] + 1;
    final long start = timestamps[0];
    return ( long )( ( end - start ) * this.screenModel.getZoomFactor() );
  }

  /**
   * Calculates the drop point for the cursor under the given coordinate.
   * 
   * @param aCoordinate
   *          the coordinate to return the channel drop point for, cannot be
   *          <code>null</code>.
   * @return a drop point, never <code>null</code>.
   */
  private Point getCursorDropPoint( final Point aCoordinate )
  {
    Point dropPoint = new Point( aCoordinate );

    if ( isSnapModeEnabled() )
    {
      final SignalHoverInfo signalHover = getSignalHover( aCoordinate );
      if ( ( signalHover != null ) && !signalHover.isEmpty() )
      {
        dropPoint.x = signalHover.getMidSamplePos().intValue();
      }
    }
    dropPoint.y = 0;

    return dropPoint;
  }

  /**
   * @return
   */
  private int getMaxWidth()
  {
    return Integer.MAX_VALUE;
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
  private double getMaxZoomLevel()
  {
    final long[] timestamps = this.dataModel.getTimestamps();
    final double end = timestamps[timestamps.length - 1] + 1;
    final double start = timestamps[0];
    return Math.floor( getMaxWidth() / ( end - start ) );
  }

  /**
   * Returns the actual signal view component.
   * 
   * @return a signal view component, never <code>null</code>.
   */
  private SignalView getSignalView()
  {
    return this.signalDiagram.getSignalView();
  }

  /**
   * Returns the dimensions of the visible view, taking care of viewports (such
   * as used in {@link JScrollPane}).
   * 
   * @return a visible view size, as {@link Dimension}, never <code>null</code>.
   */
  private Dimension getVisibleViewSize()
  {
    final JComponent component = getSignalView();

    final JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, component );

    final Rectangle rect;
    if ( scrollPane != null )
    {
      rect = scrollPane.getViewport().getVisibleRect();
    }
    else
    {
      rect = this.signalDiagram.getVisibleRect();
    }

    return rect.getSize();
  }

  /**
   * @param aPoint
   * @return
   */
  private int locationToSampleIndex( final Point aPoint )
  {
    return this.signalDiagram.getModel().locationToSampleIndex( aPoint );
  }

  /**
   * @param aPoint
   * @return
   */
  private long locationToTimestamp( final Point aPoint )
  {
    return this.signalDiagram.getModel().locationToTimestamp( aPoint );
  }

  /**
   * @param aFactor
   */
  private void zoomAbsolute( final double aFactor )
  {
    try
    {
      this.screenModel.setZoomFactor( aFactor );
    }
    finally
    {
      this.screenModel.setZoomAll( false );
    }

    LOG.log( Level.INFO, "Zoom factor set to " + this.screenModel.getZoomFactor() );
  }

  /**
   * @param aFactor
   */
  private void zoomRelative( final double aFactor )
  {
    final double maxFactor = getMaxZoomLevel();
    final double newFactor = Math.min( maxFactor, aFactor * this.screenModel.getZoomFactor() );
    zoomAbsolute( newFactor );
  }
}
