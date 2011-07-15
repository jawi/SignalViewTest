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
   * Returns the background color for the channel labels.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getBackgroundColor()
  {
    Color color = getSettingsProvider().getColor( COMPONENT_BACKGROUND_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_BACKGROUND_COLOR;
    }
    return color;
  }

  /**
   * Returns the label for the channel with the given index.
   * 
   * @param aChannelIndex
   *          the channel index of the channel to get the label for.
   * @return a channel label, never <code>null</code>.
   */
  public String getChannelLabel( final int aChannelIndex )
  {
    return this.controller.getScreenModel().getChannelLabel( aChannelIndex );
  }

  /**
   * Returns the background color for the labels themselves.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getLabelBackgroundColor()
  {
    Color color = getSettingsProvider().getColor( LABEL_BACKGROUND_COLOR );
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
    Font font = getSettingsProvider().getFont( LABEL_FONT );
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
    Color color = getSettingsProvider().getColor( LABEL_FOREGROUND_COLOR );
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
    Integer minWidth = getSettingsProvider().getInteger( COMPONENT_MINIMAL_WIDTH );
    if ( minWidth == null )
    {
      return LafDefaults.DEFAULT_MINIMAL_CHANNEL_WIDTH;
    }
    return minWidth.intValue();
  }
}