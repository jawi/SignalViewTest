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

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * Manages all channel groups.
 */
public final class ChannelGroupManager implements IDataModelChangeListener
{
  // CONSTANTS

  public static final int MAX_CHANNEL_GROUPS = Channel.MAX_CHANNELS;

  // VARIABLES

  private Channel[] channels;

  private final SignalDiagramModel model;
  private final List<ChannelGroup> channelGroups;

  // CONSTRUCTORS

  /**
   * Creates a new {@link ChannelGroupManager} instance.
   * 
   * @param aModel
   *          the signal diagram model to use, cannot be <code>null</code>.
   */
  public ChannelGroupManager( final SignalDiagramModel aModel )
  {
    this.channels = new Channel[0];

    this.model = aModel;
    this.channelGroups = new ArrayList<ChannelGroup>();
  }

  // METHODS

  /**
   * Adds a given channel to the given channel group.
   * <p>
   * If the given channel group already contains the given channel, then this
   * method is effectively a no-op.
   * </p>
   * 
   * @param aChannelGroup
   *          the channel group to add the channel to, cannot be
   *          <code>null</code>;
   * @param aChannel
   *          the channel to add to the channel group, cannot be
   *          <code>null</code>.
   * @throws IllegalArgumentException
   *           in case one of the given parameters was <code>null</code>.
   */
  public void addChannel( final ChannelGroup aChannelGroup, final Channel aChannel )
  {
    if ( aChannelGroup == null )
    {
      throw new IllegalArgumentException( "ChannelGroup cannot be null!" );
    }
    if ( aChannel == null )
    {
      throw new IllegalArgumentException( "Channel cannot be null!" );
    }

    if ( aChannelGroup.hasChannel( aChannel ) )
    {
      // Nothing to do; we're done...
      return;
    }

    ChannelGroup oldCG = getChannelGroup( aChannel );
    if ( oldCG != null )
    {
      oldCG.removeChannel( aChannel );

      // When there are no more channels left in this channel group, remove
      // it...
      if ( !oldCG.hasChannels() )
      {
        this.channelGroups.remove( oldCG );
      }
    }

    aChannelGroup.addChannel( aChannel );
  }

  /**
   * Adds a new channel group to this manager.
   * 
   * @param aName
   *          the name of the new channel group, cannot be <code>null</code> or
   *          empty.
   * @return the newly added channel group, never <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given name was <code>null</code> or empty;
   * @throws IllegalStateException
   *           in case no channels are available for the new channel group.
   */
  public ChannelGroup addChannelGroup( final String aName )
  {
    final Channel firstAvailableChannel = getFirstUnassignedChannel();
    if ( firstAvailableChannel == null )
    {
      throw new IllegalStateException( "No channels left!" );
    }

    ChannelGroup result = new ChannelGroup( aName );
    // For convenience, add the first available channel to this group...
    result.addChannel( firstAvailableChannel );

    this.channelGroups.add( result );

    return result;
  }

  /**
   * Returns whether or not a new channel group can be added.
   * 
   * @return <code>true</code> if a new channel group can be added,
   *         <code>false</code> otherwise.
   */
  public boolean canAddChannelGroup()
  {
    return !getUnassignedChannels().isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dataModelChanged( final SampleDataModel aDataModel )
  {
    this.channels = new Channel[aDataModel.getWidth()];
    for ( int i = 0; i < this.channels.length; i++ )
    {
      this.channels[i] = new Channel( i );
    }

    // Reset channel groups so they align with the given data model...
    final int maxI = 8;
    final int maxJ = Channel.MAX_CHANNELS / maxI;
    for ( int i = 0; i < maxI; i++ )
    {
      ChannelGroup channelGroup = addChannelGroup( "Group " + ( i + 1 ) );
      channelGroup.setVisible( ( i % 2 ) == 0 );

      for ( int j = 0; j < maxJ; j++ )
      {
        channelGroup.addChannel( this.channels[( i * maxJ ) + j] );
      }
    }
  }

  /**
   * Returns all channels used in this signal diagram model.
   * 
   * @return an array of channels, never <code>null</code>.
   */
  public Channel[] getAllChannels()
  {
    return this.channels;
  }

  /**
   * Returns a sorted set of all assigned (not available) channels.
   * 
   * @return a sorted set of all assigned channels, never <code>null</code>.
   */
  public SortedSet<Channel> getAssignedChannels()
  {
    SortedSet<Channel> channelIndexes = new TreeSet<Channel>();

    for ( ChannelGroup cg : this.channelGroups )
    {
      channelIndexes.addAll( Arrays.asList( cg.getChannels() ) );
    }

    return channelIndexes;
  }

  /**
   * Returns the channel with a given virtual index.
   * 
   * @param aVirtualIndex
   *          the virtual index of the channel to return.
   * @return the channel with the given virtual index, or <code>null</code> if
   *         no such channel was found.
   */
  public Channel getChannelByVirtualIndex( final int aVirtualIndex )
  {
    Channel result = null;
    Iterator<ChannelGroup> channelGroupIter = this.channelGroups.iterator();

    while ( channelGroupIter.hasNext() && ( result == null ) )
    {
      ChannelGroup cg = channelGroupIter.next();
      result = cg.getChannelByVirtualIndex( aVirtualIndex );
    }

    return result;
  }

  /**
   * Returns the channel group which contains the given channel.
   * 
   * @param aChannel
   *          the channel to return the channel group for, cannot be
   *          <code>null</code>.
   * @return the channel group that contains the given channel, or
   *         <code>null</code> if the channel is not contained by any channel
   *         group.
   * @throws IllegalArgumentException
   *           if the given channel was <code>null</code>.
   */
  public ChannelGroup getChannelGroup( final Channel aChannel )
  {
    if ( aChannel == null )
    {
      throw new IllegalArgumentException( "Channel cannot be null!" );
    }

    for ( ChannelGroup cg : this.channelGroups )
    {
      if ( cg.hasChannel( aChannel ) )
      {
        return cg;
      }
    }
    return null;
  }

  /**
   * Returns the channel group with a given name.
   * 
   * @param aName
   *          the name of the channel group to return, cannot be
   *          <code>null</code> or empty.
   * @return the channel group with the given name, or <code>null</code> if no
   *         such channel group exists.
   * @throws IllegalArgumentException
   *           in case the given name was <code>null</code> or empty.
   */
  public ChannelGroup getChannelGroupByName( final String aName )
  {
    if ( ( aName == null ) || aName.trim().isEmpty() )
    {
      throw new IllegalArgumentException( "Name cannot be null or empty!" );
    }

    for ( ChannelGroup cg : this.channelGroups )
    {
      if ( aName.equals( cg.getName() ) )
      {
        return cg;
      }
    }
    return null;
  }

  /**
   * Returns all current channel groups.
   * 
   * @return an array of channel groups, never <code>null</code>.
   */
  public ChannelGroup[] getChannelGroups()
  {
    final int size = this.channelGroups.size();
    return this.channelGroups.toArray( new ChannelGroup[size] );
  }

  /**
   * Returns all channels the given range of all visible channel groups.
   * 
   * @param aStartIndex
   *          the start channel index to return (0..31);
   * @param aEndIndex
   *          the end channel index to return (0..31).
   * @return an array of channels, never <code>null</code>.
   */
  public Channel[] getChannels( final int aStartIndex, final int aEndIndex )
  {
    final List<Channel> channelIndexes = new ArrayList<Channel>();

    final int channelHeight = this.model.getChannelHeight();

    // Calculate the start & end Y position...
    int startYpos = aStartIndex * channelHeight;
    int endYpos = aEndIndex * channelHeight;

    int yPos = 0;
    for ( ChannelGroup cg : this.channelGroups )
    {
      if ( cg.isVisible() )
      {
        final List<Channel> channels = Arrays.asList( cg.getChannels() );
        for ( Channel channel : channels )
        {
          if ( ( yPos >= startYpos ) && ( yPos <= endYpos ) )
          {
            channelIndexes.add( channel );
          }
          yPos += channelHeight;
        }
      }
    }

    return channelIndexes.toArray( new Channel[channelIndexes.size()] );
  }

  /**
   * Returns a sorted set of all unassigned (= available) channels.
   * 
   * @return a sorted set of unassigned channels, never <code>null</code>.
   */
  public SortedSet<Channel> getUnassignedChannels()
  {
    SortedSet<Channel> channelIndexes = new TreeSet<Channel>();
    channelIndexes.addAll( Arrays.asList( this.channels ) );

    for ( ChannelGroup cg : this.channelGroups )
    {
      channelIndexes.removeAll( Arrays.asList( cg.getChannels() ) );
    }

    return channelIndexes;
  }

  /**
   * Returns the number of visible channels.
   * 
   * @return a channel count, >= 0.
   */
  public int getVisibleChannelCount()
  {
    int count = 0;
    for ( ChannelGroup cg : this.channelGroups )
    {
      if ( cg.isVisible() )
      {
        count += cg.getChannels().length;
      }
    }

    return count;
  }

  /**
   * Returns whether or not the given channel is assigned to any channel group.
   * 
   * @param aChannel
   *          the channel to test, cannot be <code>null</code>.
   * @return <code>true</code> if the given channel is contained by any channel
   *         group, <code>false</code> otherwise.
   * @throws IllegalArgumentException
   *           in case the given channel was <code>null</code>.
   */
  public boolean isAssigned( final Channel aChannel )
  {
    return getChannelGroup( aChannel ) != null;
  }

  /**
   * Removes a channel from a given channel group.
   * 
   * @param aChannelGroup
   *          the channel group to remove the channel from, cannot be
   *          <code>null</code>;
   * @param aChannel
   *          the channel to remove, cannot be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case one of the given parameters was <code>null</code>.
   */
  public void removeChannel( final ChannelGroup aChannelGroup, final Channel aChannel )
  {
    if ( aChannelGroup == null )
    {
      throw new IllegalArgumentException( "ChannelGroup cannot be null!" );
    }
    if ( aChannel == null )
    {
      throw new IllegalArgumentException( "Channel cannot be null!" );
    }

    aChannelGroup.removeChannel( aChannel );
  }

  /**
   * Removes the channel group with the given name.
   * 
   * @param aName
   *          the name of the channel group to remove, cannot be
   *          <code>null</code> or empty.
   * @throws IllegalArgumentException
   *           in case the given name was <code>null</code> or empty.
   */
  public void removeChannelGroup( final String aName )
  {
    if ( ( aName == null ) || aName.trim().isEmpty() )
    {
      throw new IllegalArgumentException( "Name cannot be null or empty!" );
    }

    ChannelGroup cg = getChannelGroupByName( aName );
    if ( cg != null )
    {
      this.channelGroups.remove( cg );
    }
  }

  /**
   * Returns the first available channel for a (new) channel group.
   * 
   * @return a channel, or <code>null</code> if no channels are available.
   */
  private Channel getFirstUnassignedChannel()
  {
    SortedSet<Channel> channels = getUnassignedChannels();

    // Any channels left?
    if ( ( channels == null ) || channels.isEmpty() )
    {
      return null;
    }

    return channels.first();
  }
}
