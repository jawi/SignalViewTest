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

import javax.swing.*;

import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.laf.*;


/**
 * 
 */
public class ChannelLabelsViewModel extends AbstractViewModel
{
  // CONSTANTS

  public static final String COMPONENT_MINIMAL_WIDTH = "channellabels.width.minimal";
  public static final String COMPONENT_BACKGROUND_COLOR = "channellabels.color.background";

  public static final String LABEL_FOREGROUND_COLOR = "channellabels.label.color.foreground";
  public static final String LABEL_BACKGROUND_COLOR = "channellabels.label.color.background";
  public static final String LABEL_FONT = "channellabels.label.font";

  // CONSTRUCTORS

  /**
   * Creates a new ChannelLabelsViewModel instance.
   * 
   * @param aController
   *          the diagram controller to use, cannot be <code>null</code>.
   */
  public ChannelLabelsViewModel( final SignalDiagramController aController )
  {
    super( aController );
  }

  // METHODS

  /**
   * Determines whether or not the move given channel index can be accepted.
   * <p>
   * Channels can only be moved within a single group.
   * </p>
   * 
   * @param aMovedChannel
   *          the channel that is moved;
   * @param aInsertRow
   *          the row to which the channel is about to be moved.
   * @return <code>true</code> if the move is accepted, <code>false</code> if
   *         the move is declined.
   */
  public boolean acceptChannel( final Channel aMovedChannel, final Point aInsertPoint )
  {
    boolean result = false;

    Channel insertChannel = findChannel( aInsertPoint );
    if ( ( aMovedChannel != null ) && ( insertChannel != null ) )
    {
      // result = insertChannel.getChannelGroup() ==
      // aMovedChannel.getChannelGroup();
      result = true;
    }

    System.out.println( "acceptChannel result == " + result + "; for " + aMovedChannel.getVirtualIndex() + " => "
        + insertChannel.getVirtualIndex() );

    return result;
  }

  /**
   * Finds the channel that lies underneat the given coordinate.
   * 
   * @param aCoordinate
   *          the X,Y-coordinate to find the channel for, cannot be
   *          <code>null</code>.
   * @return the channel underneat the given X,Y-coordinate, or
   *         <code>null</code> if no channel is found.
   */
  public Channel findChannel( final Point aCoordinate )
  {
    SignalElement signalElement = getSignalDiagramModel().findSignalElement( aCoordinate );
    if ( ( signalElement != null ) && signalElement.isDigitalSignal() )
    {
      return signalElement.getChannel();
    }
    return null;
  }

  /**
   * Determines the virtual channel row corresponding to the given
   * X,Y-coordinate.
   * 
   * @param aCoordinate
   *          the coordinate to return the channel row for, cannot be
   *          <code>null</code>.
   * @return a channel row index (>= 0), or -1 if the point is nowhere near a
   *         channel row.
   */
  public int findChannelVirtualOffset( final Point aCoordinate )
  {
    SignalElement signalElement = getSignalDiagramModel().findSignalElement( aCoordinate );
    if ( signalElement != null )
    {
      return signalElement.getYposition();
    }
    return -1;
  }

  /**
   * Returns all available channels.
   * 
   * @return an array of channels, never <code>null</code>.
   */
  public final Channel[] getAllChannels()
  {
    return getChannelGroupManager().getAllChannels();
  }

  /**
   * Returns the background color for the channel labels.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getBackgroundColor()
  {
    Color color = UIManager.getColor( COMPONENT_BACKGROUND_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_BACKGROUND_COLOR;
    }
    return color;
  }

  /**
   * Returns the background color for the labels themselves.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getLabelBackgroundColor()
  {
    Color color = UIManager.getColor( LABEL_BACKGROUND_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_CHANNEL_BACKGROUND_COLOR;
    }
    return color;
  }

  /**
   * Returns the font for the labels.
   * 
   * @return a font, never <code>null</code>.
   */
  public Font getLabelFont()
  {
    Font font = UIManager.getFont( LABEL_FONT );
    if ( font == null )
    {
      font = LafDefaults.DEFAULT_CHANNEL_LABEL_FONT;
    }
    return font;
  }

  /**
   * Returns the foreground color for the labels themselves.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getLabelForegroundColor()
  {
    Color color = UIManager.getColor( LABEL_FOREGROUND_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_CHANNEL_LABEL_COLOR;
    }
    return color;
  }

  /**
   * Returns the minimal width of the channel labels.
   * 
   * @return a minimal width, in pixels.
   */
  public int getMinimalWidth()
  {
    int minWidth = UIManager.getInt( COMPONENT_MINIMAL_WIDTH );
    if ( minWidth <= 0 )
    {
      return LafDefaults.DEFAULT_MINIMAL_CHANNEL_WIDTH;
    }
    return minWidth;
  }

  /**
   * Moves a given channel row to another position.
   * 
   * @param aMovedChannel
   *          the channel that is to be moved;
   * @param aNewLocation
   *          the new location the moved row is moved to.
   */
  public void moveChannelRows( final Channel aMovedChannel, final Point aNewLocation )
  {
    final ChannelGroupManager channelGroupManager = getChannelGroupManager();

    final Channel insertLocation = findChannel( aNewLocation );
    if ( insertLocation != null )
    {
      channelGroupManager.moveChannel( aMovedChannel, insertLocation );
    }
  }
}
