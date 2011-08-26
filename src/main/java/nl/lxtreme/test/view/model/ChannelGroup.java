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


import java.util.*;

import nl.lxtreme.test.model.*;


/**
 * Indicates a number of grouped channels, with their own set of labels and
 * indexes of channels.
 */
public class ChannelGroup
{
  // VARIABLES

  private final Collection<Channel> channels;

  private String name;
  private boolean visible;

  // CONSTRUCTORS

  /**
   * Creates a new ChannelGroup instance.
   * 
   * @param aName
   *          the name of this channel group, cannot be <code>null</code> or
   *          empty.
   * @throws IllegalArgumentException
   *           in case the given name was <code>null</code> or empty.
   */
  ChannelGroup( final String aName )
  {
    if ( ( aName == null ) || aName.trim().isEmpty() )
    {
      throw new IllegalArgumentException( "Name cannot be null or empty!" );
    }

    this.name = aName;
    // By default visible...
    this.visible = true;

    this.channels = new ArrayList<Channel>();
  }

  // METHODS

  /**
   * Adds a given channel to this channel group.
   * <p>
   * If the given channel is already contained by this channel group, this
   * method is effectively a no-op.
   * </p>
   * 
   * @param aChannel
   *          the channel to add, cannot be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given channel was <code>null</code>.
   */
  public void addChannel( final Channel aChannel )
  {
    if ( !hasChannel( aChannel ) )
    {
      this.channels.add( aChannel );

      if ( !aChannel.hasName() )
      {
        aChannel.setLabel( getChannelName( aChannel ) );
      }
      // Make other defaults...
      aChannel.setVirtualIndex( aChannel.getIndex() );
    }
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
    if ( ( aObject == null ) || !( aObject instanceof ChannelGroup ) )
    {
      return false;
    }

    ChannelGroup other = ( ChannelGroup )aObject;
    if ( this.name == null )
    {
      if ( other.name != null )
      {
        return false;
      }
    }
    else if ( !this.name.equals( other.name ) )
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the channel with the given index.
   * 
   * @param aIndex
   *          the channel index to return the channel for.
   * @return a channel with the given index, or <code>null</code> if no such
   *         channel exists.
   */
  public Channel getChannel( final int aIndex )
  {
    for ( Channel channel : this.channels )
    {
      if ( channel.getIndex() == aIndex )
      {
        return channel;
      }
    }
    return null;
  }

  /**
   * Returns the channel with the given virtual index.
   * 
   * @param aIndex
   *          the virtual channel index to return the channel for.
   * @return a channel with the given virtual index, or <code>null</code> if no
   *         such channel exists.
   */
  public Channel getChannelByVirtualIndex( final int aIndex )
  {
    for ( Channel channel : this.channels )
    {
      if ( channel.getVirtualIndex() == aIndex )
      {
        return channel;
      }
    }
    return null;
  }

  /**
   * Returns all channels assigned to this channel group.
   * 
   * @return an array of channels, never <code>null</code>.
   */
  public Channel[] getChannels()
  {
    final int size = this.channels.size();
    return this.channels.toArray( new Channel[size] );
  }

  /**
   * Returns the name of this channel group.
   * 
   * @return a name, never <code>null</code> or empty.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Returns whether or not a channel is
   * 
   * @param aChannel
   *          the channel to test, cannot be <code>null</code>.
   * @return <code>true</code> if the given channel is contained by this channel
   *         group, <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           in case the given channel was <code>null</code>.
   */
  public boolean hasChannel( final Channel aChannel )
  {
    if ( aChannel == null )
    {
      throw new IllegalArgumentException( "Channel cannot be null!" );
    }

    return this.channels.contains( aChannel );
  }

  /**
   * Returns whether or not this channel group has any channels.
   * 
   * @return <code>true</code> if this channel group contains at least one
   *         channel, <code>false</code> otherwise.
   */
  public boolean hasChannels()
  {
    return !this.channels.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + ( ( this.name == null ) ? 0 : this.name.hashCode() );
    return result;
  }

  /**
   * Returns whether or not this entire channel group is visible.
   * 
   * @return <code>true</code> if this channel group is visible,
   *         <code>false</code> otherwise.
   */
  public boolean isVisible()
  {
    return this.visible;
  }

  /**
   * Removes a given channel from this channel group.
   * <p>
   * If the given channel is <em>not</em> contained by this channel group, this
   * method is effectively a no-op.
   * </p>
   * 
   * @param aChannel
   *          the channel to remove, cannot be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given channel was <code>null</code>.
   */
  public void removeChannel( final Channel aChannel )
  {
    if ( hasChannel( aChannel ) )
    {
      this.channels.remove( aChannel );
    }
  }

  /**
   * Sets name to the given value.
   * 
   * @param aName
   *          the name to set.
   * @throws IllegalArgumentException
   *           in case the given name is <code>null</code> or empty.
   */
  public void setName( final String aName )
  {
    if ( ( aName == null ) || aName.trim().isEmpty() )
    {
      throw new IllegalArgumentException( "Name cannot be null or empty!" );
    }
    this.name = aName;
  }

  /**
   * Sets visible to the given value.
   * 
   * @param aVisible
   *          the visible to set.
   */
  public void setVisible( final boolean aVisible )
  {
    this.visible = aVisible;
  }

  /**
   * Crafts a proposed channel name for use when a channel is added to this
   * channel group.
   * 
   * @param aChannel
   *          the channel to add, cannot be <code>null</code>.
   * @return a proposed channel name, never <code>null</code>.
   */
  private String getChannelName( final Channel aChannel )
  {
    return String.format( "%s-%d", getName(), Integer.valueOf( aChannel.getIndex() + 1 ) );
  }
}
