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


/**
 * 
 */
public class SimpleAnnotation implements Annotation<String>
{
  // VARIABLES

  private final String annotation;
  private final int channel;
  private final long startTimestamp;
  private final long endTimestamp;

  // CONSTRUCTORS

  /**
   * Creates a new SimpleAnnotation instance.
   */
  public SimpleAnnotation( final int aChannel, final String aAnnotation, final int aStartTime, final int aEndTime )
  {
    this.channel = aChannel;
    this.annotation = aAnnotation;
    this.startTimestamp = aStartTime;
    this.endTimestamp = aEndTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals( final Object obj )
  {
    if ( this == obj )
    {
      return true;
    }
    if ( ( obj == null ) || !( obj instanceof SimpleAnnotation ) )
    {
      return false;
    }

    final SimpleAnnotation other = ( SimpleAnnotation )obj;
    if ( this.channel != other.channel )
    {
      return false;
    }
    if ( this.startTimestamp != other.startTimestamp )
    {
      return false;
    }
    if ( this.endTimestamp != other.endTimestamp )
    {
      return false;
    }
    if ( this.annotation == null )
    {
      if ( other.annotation != null )
      {
        return false;
      }
    }
    else if ( !this.annotation.equals( other.annotation ) )
    {
      return false;
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAnnotation()
  {
    return this.annotation;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getChannel()
  {
    return this.channel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getEndTime()
  {
    return this.endTimestamp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getStartTime()
  {
    return this.startTimestamp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + ( ( this.annotation == null ) ? 0 : this.annotation.hashCode() );
    result = ( prime * result ) + this.channel;
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return getAnnotation();
  }
}
