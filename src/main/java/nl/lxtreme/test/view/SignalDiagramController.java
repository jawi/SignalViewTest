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

import nl.lxtreme.test.*;
import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.model.*;


/**
 * Provides the main component controller for the signal diagram component.
 */
public final class SignalDiagramController
{
  // CONSTANTS

  private static final Logger LOG = Logger.getLogger( SignalDiagramController.class.getName() );

  // VARIABLES

  private final DragAndDropTargetController dndTargetController;

  private SignalDiagramComponent signalDiagram;

  // CONSTRUCTORS

  /**
   * @param aModel
   */
  public SignalDiagramController()
  {
    this.dndTargetController = new DragAndDropTargetController( this );
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
    getSignalDiagramModel().addCursorChangeListener( aListener );
  }

  /**
   * Adds a data model change listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addDataModelChangeListener( final IDataModelChangeListener aListener )
  {
    getSignalDiagramModel().addDataModelChangeListener( aListener );
  }

  /**
   * Adds a measurement listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addMeasurementListener( final IMeasurementListener aListener )
  {
    getSignalDiagramModel().addMeasurementListener( aListener );
  }

  /**
   * Adds a property change listener.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addPropertyChangeListener( final PropertyChangeListener aListener )
  {
    getSignalDiagramModel().addPropertyChangeListener( aListener );
  }

  /**
   * @return the dndTargetController
   */
  public final DragAndDropTargetController getDndTargetController()
  {
    return this.dndTargetController;
  }

  /**
   * @return the signalDiagram
   */
  public final SignalDiagramComponent getSignalDiagram()
  {
    return this.signalDiagram;
  }

  /**
   * @return
   */
  public SignalDiagramModel getSignalDiagramModel()
  {
    if ( this.signalDiagram == null )
    {
      return null;
    }
    return this.signalDiagram.getModel();
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
    return getSignalDiagramModel().isCursorDefined( aCursorIdx );
  }

  /**
   * Drags a cursor with a given index to a given point, possibly snapping to a
   * signal edge.
   * 
   * @param aCursorIdx
   *          the cursor index to move, should be &gt;= 0 && &lt; 10;
   * @param aPoint
   *          the new point of the cursor. In case of snapping, this point
   *          should match a signal edge, cannot be <code>null</code>.
   */
  public void moveCursor( final int aCursorIdx, final Point aPoint )
  {
    final long newCursorTimestamp = locationToTimestamp( aPoint );

    getSignalDiagramModel().setCursor( aCursorIdx, newCursorTimestamp );
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
    final SignalDiagramModel model = getSignalDiagramModel();

    int width = model.getAbsoluteScreenWidth();
    if ( width < viewPortSize.width )
    {
      width = viewPortSize.width;
    }

    int height = model.getAbsoluteScreenHeight();
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
    getSignalDiagramModel().removeCursor( aCursorIdx );
  }

  /**
   * Removes a cursor change listener.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeCursorChangeListener( final ICursorChangeListener aListener )
  {
    getSignalDiagramModel().removeCursorChangeListener( aListener );
  }

  /**
   * Removes a data model change listener.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeDataModelChangeListener( final IDataModelChangeListener aListener )
  {
    getSignalDiagramModel().removeDataModelChangeListener( aListener );
  }

  /**
   * Removes the given measurement listener from the list of listeners.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeMeasurementListener( final IMeasurementListener aListener )
  {
    getSignalDiagramModel().removeMeasurementListener( aListener );
  }

  /**
   * Removes a property change listener.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removePropertyChangeListener( final PropertyChangeListener aListener )
  {
    getSignalDiagramModel().removePropertyChangeListener( aListener );
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
    getSignalDiagramModel().setCursorMode( aVisible );
  }

  /**
   * Sets the data model for this controller.
   * 
   * @param aDataModel
   *          the dataModel to set, cannot be <code>null</code>.
   */
  public void setDataModel( final SampleDataModel aDataModel )
  {
    getSignalDiagramModel().setDataModel( aDataModel );

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
    getSignalDiagramModel().setMeasurementMode( aEnabled );
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
    getSignalDiagramModel().setSnapCursor( aSnapMode );
  }

  /**
   * Zooms to make all data visible in one screen.
   */
  public void zoomAll()
  {
    final SignalDiagramModel model = getSignalDiagramModel();

    try
    {
      Dimension viewSize = getVisibleViewSize();
      model.setZoomFactor( viewSize.getWidth() / model.getAbsoluteLength() );
    }
    finally
    {
      model.setZoomAll( true );
    }

    LOG.log( Level.INFO, "Zoom factor set to " + model.getZoomFactor() );

    recalculateDimensions();
  }

  /**
   * Zooms in with a factor 1.5
   */
  public void zoomIn()
  {
    zoomRelative( 2.0 );

    recalculateDimensions();
  }

  /**
   * Zooms to a factor of 1.0.
   */
  public void zoomOriginal()
  {
    zoomAbsolute( 1.0 );

    recalculateDimensions();
  }

  /**
   * Zooms out with a factor 1.5
   */
  public void zoomOut()
  {
    zoomRelative( 0.5 );

    recalculateDimensions();
  }

  /**
   * @param aComponent
   */
  final void setSignalDiagram( final SignalDiagramComponent aComponent )
  {
    this.signalDiagram = aComponent;
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
      getSignalDiagramModel().setZoomFactor( aFactor );
    }
    finally
    {
      getSignalDiagramModel().setZoomAll( false );
    }

    LOG.log( Level.INFO, "Zoom factor set to " + getSignalDiagramModel().getZoomFactor() );
  }

  /**
   * @param aFactor
   */
  private void zoomRelative( final double aFactor )
  {
    final SignalDiagramModel model = getSignalDiagramModel();
    final double newFactor = Math.min( model.getMaxZoomLevel(), aFactor * model.getZoomFactor() );

    zoomAbsolute( newFactor );
  }
}
