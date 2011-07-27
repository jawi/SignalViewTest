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
package nl.lxtreme.test.view.model;


import java.awt.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.view.*;


/**
 * Provides a common base class for the view models.
 */
abstract class AbstractViewModel
{
  // VARIABLES

  protected final SignalDiagramController controller;

  // CONSTRUCTORS

  /**
   * Creates a new AbstractViewModel instance.
   * 
   * @param aController
   *          the diagram controller to use, cannot be <code>null</code>.
   */
  protected AbstractViewModel( final SignalDiagramController aController )
  {
    this.controller = aController;
  }

  // METHODS

  /**
   * @param aChannelIndex
   * @return
   */
  public Color getChannelColor( final int aChannelIndex )
  {
    return getSignalDiagramModel().getChannelColor( aChannelIndex );
  }

  /**
   * @return
   */
  public int getChannelHeight()
  {
    return getSignalDiagramModel().getChannelHeight();
  }

  /**
   * Returns the color for a cursor with the given index.
   * 
   * @param aCursorIndex
   *          the index of the cursor to retrieve the color for.
   * @return a cursor color, never <code>null</code>.
   */
  public Color getCursorColor( final int aCursorIndex )
  {
    final nl.lxtreme.test.model.Cursor cursor = getSignalDiagramModel().getCursor( aCursorIndex );
    return cursor.getColor();
  }

  /**
   * Returns the cursor flag text for a cursor with the given index.
   * 
   * @param aCursorIndex
   *          the index of the cursor to retrieve the flag text for.
   * @return a cursor flag text, never <code>null</code>.
   */
  public String getCursorFlagText( final int aCursorIndex )
  {
    final nl.lxtreme.test.model.Cursor cursor = getSignalDiagramModel().getCursor( aCursorIndex );
    if ( !cursor.isDefined() )
    {
      return "";
    }
    return getCursorFlagText( aCursorIndex, cursor.getTimestamp() );
  }

  /**
   * Returns the cursor flag text for the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the index of the cursor, >= 0 && < 10;
   * @param aCursorTimestamp
   *          the timestamp of the cursor.
   * @return a cursor flag text, or an empty string if the cursor with the given
   *         index is undefined.
   */
  public String getCursorFlagText( final int aCursorIdx, final long aCursorTimestamp )
  {
    final SignalDiagramModel model = getSignalDiagramModel();

    final nl.lxtreme.test.model.Cursor cursor = model.getCursor( aCursorIdx );

    final double sampleRate = model.getSampleRate();
    final String cursorTime = Utils.displayTime( aCursorTimestamp / sampleRate );

    String label = cursor.getLabel();
    if ( !cursor.hasLabel() )
    {
      label = Integer.toString( aCursorIdx + 1 );
    }

    return String.format( "%s: %s", label, cursorTime );
  }

  /**
   * Returns the X-position of the cursor with the given index, for displaying
   * purposes on screen.
   * 
   * @param aCursorIdx
   *          the index of the cursor to retrieve the X-position for, >= 0.
   * @return the screen X-position of the cursor with the given index, or -1 if
   *         the cursor is not defined.
   */
  public int getCursorScreenCoordinate( final int aCursorIndex )
  {
    nl.lxtreme.test.model.Cursor cursorTimestamp = getSignalDiagramModel().getCursor( aCursorIndex );
    if ( !cursorTimestamp.isDefined() )
    {
      return -1;
    }
    return timestampToCoordinate( cursorTimestamp.getTimestamp() );
  }

  /**
   * Returns the text color for a cursor with the given index.
   * 
   * @param aCursorIndex
   *          the index of the cursor to retrieve the color for.
   * @return a cursor text color, never <code>null</code>.
   */
  public Color getCursorTextColor( final int aCursorIndex )
  {
    return Utils.getContrastColor( getCursorColor( aCursorIndex ) );
  }

  /**
   * @return
   */
  public int getSampleWidth()
  {
    return getSignalDiagramModel().getSampleWidth();
  }

  /**
   * @return
   */
  public int getSignalHeight()
  {
    return getSignalDiagramModel().getSignalHeight();
  }

  /**
   * Returns the signal offset.
   * 
   * @return a signal offset, >= 0.
   */
  public int getSignalOffset()
  {
    return getSignalDiagramModel().getSignalOffset();
  }

  /**
   * Returns the current zoom factor that is used to display the signals with.
   * 
   * @return a zoom factor, >= 0.0.
   */
  public double getZoomFactor()
  {
    return getSignalDiagramModel().getZoomFactor();
  }

  /**
   * @param aB
   * @return
   */
  public boolean isChannelVisible( final int aChannelIndex )
  {
    return getSignalDiagramModel().isChannelVisible( aChannelIndex );
  }

  /**
   * @return
   */
  public boolean isCursorMode()
  {
    return getSignalDiagramModel().isCursorMode();
  }

  /**
   * @return
   */
  public boolean isMeasurementMode()
  {
    return getSignalDiagramModel().isMeasurementMode();
  }

  /**
   * Converts a given time stamp to a screen coordinate.
   * 
   * @param aTimestamp
   *          the time stamp to convert, >= 0.
   * @return a screen coordinate, >= 0.
   */
  public int timestampToCoordinate( final long aTimestamp )
  {
    double result = getSignalDiagramModel().getZoomFactor() * aTimestamp;
    if ( result > Integer.MAX_VALUE )
    {
      return Integer.MAX_VALUE;
    }
    return ( int )result;
  }

  /**
   * @param aB
   * @return
   */
  public int toVirtualRow( final int aChannelIndex )
  {
    return getSignalDiagramModel().toVirtualRow( aChannelIndex );
  }

  /**
   * @return
   */
  protected final SignalDiagramModel getSignalDiagramModel()
  {
    return this.controller.getSignalDiagramModel();
  }

  /**
   * @param aPoint
   * @return
   */
  protected int locationToSampleIndex( final Point aPoint )
  {
    final SignalDiagramModel model = this.controller.getSignalDiagram().getModel();
    return model.locationToSampleIndex( aPoint );
  }

  /**
   * @param aPoint
   * @return
   */
  protected long locationToTimestamp( final Point aPoint )
  {
    final SignalDiagramModel model = this.controller.getSignalDiagram().getModel();
    return model.locationToTimestamp( aPoint );
  }
}
