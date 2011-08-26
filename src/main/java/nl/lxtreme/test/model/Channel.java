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
package nl.lxtreme.test.model;


import java.awt.*;

import nl.lxtreme.test.*;


/**
 * Denotes a channel with a label and a color.
 */
public class Channel implements Comparable<Channel>
{
  // CONSTANTS

  public static final int MAX_CHANNELS = 32;

  private static final Color[] DEFAULT_COLORS = { //
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

  private final int index;
  private final int mask;
  private String label;
  private Color color;
  private boolean enabled;
  private int virtualIndex;

  // TODO annotations!

  // CONSTRUCTORS

  /**
   * Creates a new Channel instance.
   * 
   * @param aChannelIdx
   *          the index of this channel, >= 0 && < {@value #MAX_CHANNELS}.
   */
  public Channel( final int aChannelIdx )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= MAX_CHANNELS ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }

    this.enabled = true;
    this.index = aChannelIdx;
    this.mask = ( int )( 1L << aChannelIdx );
    this.virtualIndex = this.index;
    // Make sure we've got a default color set...
    this.color = DEFAULT_COLORS[aChannelIdx % DEFAULT_COLORS.length];
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo( final Channel aChannel )
  {
    return this.index - aChannel.index;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals( final Object aObject )
  {
    if ( this == aObject )
    {
      return true;
    }
    if ( ( aObject == null ) || !( aObject instanceof Channel ) )
    {
      return false;
    }

    Channel other = ( Channel )aObject;
    if ( this.index != other.index )
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the current value of color.
   * 
   * @return the color
   */
  public Color getColor()
  {
    return this.color;
  }

  /**
   * Returns the current value of index.
   * 
   * @return the index
   */
  public int getIndex()
  {
    return this.index;
  }

  /**
   * Returns the current value of name.
   * 
   * @return the name
   */
  public String getLabel()
  {
    return this.label;
  }

  /**
   * Returns bit-mask to use for this channel.
   * 
   * @return a bit-mask (= always a power of two), >= 1.
   */
  public int getMask()
  {
    return this.mask;
  }

  /**
   * Returns the current value of virtualIndex.
   * 
   * @return the virtualIndex
   */
  public int getVirtualIndex()
  {
    return this.virtualIndex;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + this.index;
    return result;
  }

  /**
   * Returns whether or not this channel has a name.
   * 
   * @return <code>true</code> if a name is given to this channel,
   *         <code>false</code> otherwise.
   */
  public boolean hasName()
  {
    return ( this.label != null ) && !this.label.trim().isEmpty();
  }

  /**
   * Returns whether or not this channel is "enabled".
   * <p>
   * When a channel is enabled, it is visible in the signal diagram. When
   * disabled, it is masked out from the signal diagram.
   * </p>
   * 
   * @return the enabled
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }

  /**
   * Sets color for this channel.
   * 
   * @param aColor
   *          the color to set.
   */
  public void setColor( final Color aColor )
  {
    if ( aColor == null )
    {
      throw new IllegalArgumentException( "Color cannot be null!" );
    }
    this.color = aColor;
  }

  /**
   * Sets enabled to the given value.
   * 
   * @param aEnabled
   *          the enabled to set.
   */
  public void setEnabled( final boolean aEnabled )
  {
    this.enabled = aEnabled;
  }

  /**
   * Sets name to the given value.
   * 
   * @param aName
   *          the name to set.
   */
  public void setLabel( final String aName )
  {
    this.label = aName;
  }

  /**
   * Sets virtualIndex to the given value.
   * 
   * @param aVirtualIndex
   *          the virtualIndex to set.
   */
  public void setVirtualIndex( final int aVirtualIndex )
  {
    this.virtualIndex = aVirtualIndex;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return this.index + ": " + getLabel();
  }
}
