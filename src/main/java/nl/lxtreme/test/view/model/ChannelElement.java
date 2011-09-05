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


import nl.lxtreme.test.model.ChannelGroup.*;


/**
 * 
 */
public class ChannelElement
{
  // VARIABLES

  private final ChannelElementType type;
  private final int height;
  private final int index;
  private final int mask;

  // TODO add label, color & font...

  // CONSTRUCTORS

  /**
   * Creates a new ChannelElement instance.
   * 
   * @param aType
   *          the type of this channel element, cannot be <code>null</code>;
   * @param aMask
   *          the mask of channel elements;
   * @param aHeight
   *          the height of this channel element, in pixels, >= 0.
   */
  public ChannelElement( final ChannelElementType aType, final int aMask, final int aHeight )
  {
    this( aType, aMask, -1, aHeight );
  }

  /**
   * Creates a new ChannelElement instance.
   * 
   * @param aType
   *          the type of this channel element, cannot be <code>null</code>;
   * @param aMask
   *          the mask of channel elements;
   * @param aIndex
   *          the channel index;
   * @param aHeight
   *          the height of this channel element, in pixels, >= 0.
   */
  public ChannelElement( final ChannelElementType aType, final int aMask, final int aIndex, final int aHeight )
  {
    this.type = aType;
    this.height = aHeight;
    this.mask = aMask;
    this.index = aIndex;
  }

  // METHODS

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
    if ( ( aObject == null ) || !( aObject instanceof ChannelElement ) )
    {
      return false;
    }

    final ChannelElement other = ( ChannelElement )aObject;
    if ( this.height != other.height )
    {
      return false;
    }
    if ( this.type != other.type )
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the current value of height.
   * 
   * @return the height, in pixels.
   */
  public int getHeight()
  {
    return this.height;
  }

  /**
   * Returns the index of the channel.
   * 
   * @return the channel index, >= 0.
   */
  public int getIndex()
  {
    return this.index;
  }

  /**
   * Returns the current value of mask.
   * 
   * @return the mask
   */
  public int getMask()
  {
    return this.mask;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + this.height;
    result = ( prime * result ) + ( ( this.type == null ) ? 0 : this.type.hashCode() );
    return result;
  }

  /**
   * Returns whether we should show the analog signal for this group.
   * 
   * @return <code>true</code> if the analog signal is to be shown,
   *         <code>false</code> to hide it.
   */
  public boolean isAnalogSignal()
  {
    return ( this.type == ChannelElementType.ANALOG_SIGNAL );
  }

  /**
   * Returns whether we should show data values in this group.
   * 
   * @return <code>true</code> if the data values are to be shown,
   *         <code>false</code> to hide them.
   */
  public boolean isDataValues()
  {
    return ( this.type == ChannelElementType.DATA_VALUES );
  }

  /**
   * Returns whether we should show digital signals in this group.
   * 
   * @return <code>true</code> if the individual digital signals are to be
   *         shown, <code>false</code> to hide them.
   */
  public boolean isDigitalChannel()
  {
    return ( this.type == ChannelElementType.DIGITAL_SIGNALS );
  }
}
