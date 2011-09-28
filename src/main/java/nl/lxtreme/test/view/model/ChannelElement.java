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


import nl.lxtreme.test.model.*;
import nl.lxtreme.test.model.ChannelGroup.ChannelElementType;


/**
 * 
 */
public class ChannelElement
{
  // VARIABLES

  private final ChannelElementType type;
  private final int yPosition;
  private final int height;
  private final int mask;

  private Channel channel;
  private ChannelGroup channelGroup;

  // TODO add label, color & font...

  // CONSTRUCTORS

  /**
   * Creates a new ChannelElement instance.
   * 
   * @param aType
   *          the type of this channel element, cannot be <code>null</code>;
   * @param aMask
   *          the mask of channel elements;
   * @param aYposition
   *          the Y-position on screen, >= 0;
   * @param aHeight
   *          the height of this channel element, in pixels, >= 0.
   */
  private ChannelElement( final ChannelElementType aType, final int aMask, final int aYposition, final int aHeight )
  {
    this.type = aType;
    this.mask = aMask;
    this.yPosition = aYposition;
    this.height = aHeight;
  }

  // METHODS

  /**
   * Factory method for creating a {@link ChannelElement} instance representing
   * an analog scope for a channel group.
   * 
   * @param aChannelGroup
   *          the channel group to create a channel element for, cannot be
   *          <code>null</code>.
   * @return a new {@link ChannelElement} instance, never <code>null</code>.
   */
  public static ChannelElement createAnalogScopeElement( final ChannelGroup aChannelGroup, final int aYposition,
      final int aHeight )
  {
    final ChannelElement channelElement = new ChannelElement( ChannelElementType.ANALOG_SIGNAL,
        aChannelGroup.getMask(), aYposition, aHeight );
    channelElement.channelGroup = aChannelGroup;
    return channelElement;
  }

  /**
   * Factory method for creating a {@link ChannelElement} instance representing
   * a data values row for a channel group.
   * 
   * @param aChannelGroup
   *          the channel group to create a channel element for, cannot be
   *          <code>null</code>.
   * @return a new {@link ChannelElement} instance, never <code>null</code>.
   */
  public static ChannelElement createDataValueElement( final ChannelGroup aChannelGroup, final int aYposition,
      final int aHeight )
  {
    final ChannelElement channelElement = new ChannelElement( ChannelElementType.DATA_VALUES, aChannelGroup.getMask(),
        aYposition, aHeight );
    channelElement.channelGroup = aChannelGroup;
    return channelElement;
  }

  /**
   * Factory method for creating a {@link ChannelElement} instance representing
   * a digital signal.
   * 
   * @param aChannel
   *          the channel to create a channel element for, cannot be
   *          <code>null</code>.
   * @return a new {@link ChannelElement} instance, never <code>null</code>.
   */
  public static ChannelElement createDigitalSignalElement( final Channel aChannel, final int aYposition,
      final int aHeight )
  {
    final ChannelElement channelElement = new ChannelElement( ChannelElementType.DIGITAL_SIGNALS, aChannel.getMask(),
        aYposition, aHeight );
    channelElement.channel = aChannel;
    return channelElement;
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
    if ( ( aObject == null ) || !( aObject instanceof ChannelElement ) )
    {
      return false;
    }

    final ChannelElement other = ( ChannelElement )aObject;
    if ( this.height != other.height )
    {
      return false;
    }
    if ( this.yPosition != other.yPosition )
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
   * @return the channel
   */
  public Channel getChannel()
  {
    if ( this.channel == null )
    {
      throw new NullPointerException();
    }
    return this.channel;
  }

  /**
   * @return the channelGroup
   */
  public ChannelGroup getChannelGroup()
  {
    if ( this.channelGroup == null )
    {
      throw new NullPointerException();
    }
    return this.channelGroup;
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
   * Returns the current value of mask.
   * 
   * @return the mask
   */
  public int getMask()
  {
    return this.mask;
  }

  /**
   * Returns the Y-position of this channel element on screen.
   * 
   * @return the Y-position, >= 0.
   */
  public int getYposition()
  {
    return this.yPosition;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( "ChannelElement [type=" );
    builder.append( this.type );
    builder.append( ", yPosition=" );
    builder.append( this.yPosition );
    builder.append( ", height=" );
    builder.append( this.height );
    if ( this.channel != null )
    {
      builder.append( ", channel=" );
      builder.append( this.channel );
    }
    if ( this.channelGroup != null )
    {
      builder.append( ", channelGroup=" );
      builder.append( this.channelGroup );
    }
    builder.append( "]" );
    return builder.toString();
  }
}
