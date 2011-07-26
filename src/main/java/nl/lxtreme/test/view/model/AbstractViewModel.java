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
import nl.lxtreme.test.model.*;
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
    return getScreenModel().getChannelColor( aChannelIndex );
  }

  /**
   * @return
   */
  public int getChannelHeight()
  {
    return getScreenModel().getChannelHeight();
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
    return getScreenModel().getCursorColor( aCursorIndex );
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
    return this.controller.getCursorFlagText( aCursorIndex );
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
    Long cursorTimestamp = getDataModel().getCursor( aCursorIndex );
    if ( cursorTimestamp == null )
    {
      return -1;
    }
    return this.controller.timestampToCoordinate( cursorTimestamp.longValue() );
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
  public int getDataWidth()
  {
    return getDataModel().getWidth();
  }

  /**
   * @return
   */
  public int getSignalHeight()
  {
    return getScreenModel().getSignalHeight();
  }

  /**
   * Returns the signal offset.
   * 
   * @return a signal offset, >= 0.
   */
  public int getSignalOffset()
  {
    return getScreenModel().getSignalOffset();
  }

  /**
   * Returns the current zoom factor that is used to display the signals with.
   * 
   * @return a zoom factor, >= 0.0.
   */
  public double getZoomFactor()
  {
    return getScreenModel().getZoomFactor();
  }

  /**
   * @param aB
   * @return
   */
  public boolean isChannelVisible( final int aChannelIndex )
  {
    return getScreenModel().isChannelVisible( aChannelIndex );
  }

  /**
   * @return
   */
  public boolean isCursorMode()
  {
    return this.controller.isCursorMode();
  }

  /**
   * @return
   */
  public boolean isMeasurementMode()
  {
    return this.controller.isMeasurementMode();
  }

  /**
   * @param aB
   * @return
   */
  public int toVirtualRow( final int aChannelIndex )
  {
    return getScreenModel().toVirtualRow( aChannelIndex );
  }

  /**
   * @return
   */
  protected final SampleDataModel getDataModel()
  {
    return this.controller.getDataModel();
  }

  /**
   * @return
   */
  protected final ScreenModel getScreenModel()
  {
    return this.controller.getScreenModel();
  }
}
