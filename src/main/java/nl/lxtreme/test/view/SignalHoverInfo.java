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
package nl.lxtreme.test.view;


import static nl.lxtreme.test.Utils.*;

import java.awt.*;


/**
 * Provides a small DTO for keeping signal hover information together.
 */
public class SignalHoverInfo
{
  // CONSTANTS

  public static final double TIMESTAMP_FACTOR = 100.0;

  // VARIABLES

  private final int channelIdx;
  private final String channelLabel;
  private final double refTime;
  private final Long startTimestamp;
  private final Long endTimestamp;
  private final Rectangle rectangle;
  private final Double highTime;
  private final Double totalTime;
  private final Integer midSamplePos;

  // CONSTRUCTORS

  /**
   * Creates a new SignalHoverInfo instance.
   * 
   * @param aChannelIdx
   *          the channel index on which the hover information is based;
   * @param aChannelLabel
   *          the label of the channel;
   * @param aRefTime
   *          the time stamp of this hover, based on the mouse position.
   */
  public SignalHoverInfo( final int aChannelIdx, final String aChannelLabel, final double aRefTime )
  {
    this.channelIdx = aChannelIdx;
    this.channelLabel = aChannelLabel;
    this.rectangle = new Rectangle();
    this.startTimestamp = null;
    this.endTimestamp = null;
    this.refTime = aRefTime;
    this.totalTime = null;
    this.highTime = null;
    this.midSamplePos = null;
  }

  /**
   * Creates a new SignalHoverInfo instance.
   * 
   * @param aChannelIdx
   *          the channel index on which the hover information is based;
   * @param aChannelLabel
   *          the label of the channel;
   * @param aRectangle
   *          the UI coordinates defining the hover on screen, cannot be
   *          <code>null</code>;
   * @param aStartTimestamp
   *          the time stamp that makes up the left side of the hover;
   * @param aEndTimestamp
   *          the time stamp that makes up the right side of the hover;
   * @param aRefTime
   *          the time stamp of this hover, based on the mouse position;
   * @param aHighTime
   *          the time the signal is non-zero (high);
   * @param aTotalTime
   *          the total time of the signal (high + low);
   * @param aMidSamplePos
   *          the screen coordinate of the middle X position.
   */
  public SignalHoverInfo( final int aChannelIdx, final String aChannelLabel, final Rectangle aRectangle,
      final long aStartTimestamp, final long aEndTimestamp, final double aRefTime, final double aHighTime,
      final double aTotalTime, final int aMidSamplePos )
  {
    this.channelIdx = aChannelIdx;
    this.channelLabel = aChannelLabel;
    this.rectangle = aRectangle;
    this.startTimestamp = aStartTimestamp;
    this.endTimestamp = aEndTimestamp;
    this.refTime = aRefTime;
    this.totalTime = aTotalTime;
    this.highTime = aHighTime;
    this.midSamplePos = aMidSamplePos;
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
    if ( ( aObject == null ) || ( getClass() != aObject.getClass() ) )
    {
      return false;
    }

    final SignalHoverInfo other = ( SignalHoverInfo )aObject;
    if ( this.channelIdx != other.channelIdx )
    {
      return false;
    }

    if ( this.endTimestamp == null )
    {
      if ( other.endTimestamp != null )
      {
        return false;
      }
    }
    else if ( !this.endTimestamp.equals( other.endTimestamp ) )
    {
      return false;
    }

    if ( this.midSamplePos == null )
    {
      if ( other.midSamplePos != null )
      {
        return false;
      }
    }
    else if ( !this.midSamplePos.equals( other.midSamplePos ) )
    {
      return false;
    }

    if ( Double.doubleToLongBits( this.refTime ) != Double.doubleToLongBits( other.refTime ) )
    {
      return false;
    }

    if ( this.startTimestamp == null )
    {
      if ( other.startTimestamp != null )
      {
        return false;
      }
    }
    else if ( !this.startTimestamp.equals( other.startTimestamp ) )
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the channel index.
   * 
   * @return a channel index, >= 0, never <code>null</code>.
   */
  public int getChannelIndex()
  {
    return this.channelIdx;
  }

  /**
   * Returns the channel index.
   * 
   * @return a channel index, >= 0, never <code>null</code>.
   */
  public String getChannelIndexAsString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( this.channelIdx );
    if ( this.channelLabel != null )
    {
      sb.append( ", " ).append( this.channelLabel );
    }
    return sb.toString();
  }

  /**
   * Returns the duty cycle, or the ratio in which the signal is high to the
   * total time of the signal.
   * 
   * @return a duty cycle, as percentage.
   */
  public Double getDutyCycle()
  {
    if ( ( this.highTime == null ) || ( this.totalTime == null ) )
    {
      return null;
    }
    return ( ( 100.0 * this.highTime ) / this.totalTime );
  }

  /**
   * Returns the duty cycle as formatted String value.
   * 
   * @return a String representation, never <code>null</code>.
   */
  public String getDutyCycleAsString()
  {
    Double dc = getDutyCycle();
    if ( dc != null )
    {
      return String.format( "%.1f %%", dc.doubleValue() );
    }
    return "-";
  }

  /**
   * Returns the current value of endTimestamp.
   * 
   * @return the endTimestamp
   */
  public Long getEndTimestamp()
  {
    return this.endTimestamp;
  }

  /**
   * Returns the time the signal is in a non-zero (or high) state.
   * 
   * @return a pulse high time, in seconds.
   */
  public Double getHighTime()
  {
    return this.highTime;
  }

  /**
   * Returns the time the signal is in a non-zero (or high) state.
   * 
   * @return a pulse high time, in seconds.
   */
  public String getHighTimeAsString()
  {
    Double ht = getHighTime();
    if ( ht != null )
    {
      return displayTime( ht.doubleValue() );
    }
    return "-";
  }

  /**
   * Returns the time the signal is in a zero (or low) state.
   * 
   * @return a pulse low time, in seconds.
   */
  public Double getLowTime()
  {
    if ( ( this.totalTime == null ) || ( this.highTime == null ) )
    {
      return null;
    }
    return this.totalTime - this.highTime;
  }

  /**
   * Returns the time the signal is in a non-zero (or high) state.
   * 
   * @return a pulse high time, in seconds.
   */
  public String getLowTimeAsString()
  {
    Double lt = getLowTime();
    if ( lt != null )
    {
      return displayTime( lt.doubleValue() );
    }
    return "-";
  }

  /**
   * Returns the current value of middleXpos.
   * 
   * @return the middleXpos
   */
  public Integer getMidSamplePos()
  {
    return this.midSamplePos;
  }

  /**
   * Returns the hover rectangle.
   * 
   * @return the rectangle, never <code>null</code>.
   */
  public Rectangle getRectangle()
  {
    return this.rectangle;
  }

  /**
   * Returns the time value where the mouse cursor is.
   * 
   * @return a reference time value, in seconds.
   */
  public double getReferenceTime()
  {
    return this.refTime;
  }

  /**
   * Returns the time value where the mouse cursor is.
   * 
   * @return a reference time value, in seconds.
   */
  public String getReferenceTimeAsString()
  {
    return displayTime( getReferenceTime() );
  }

  /**
   * Returns the current value of startTimestamp.
   * 
   * @return the startTimestamp
   */
  public Long getStartTimestamp()
  {
    return this.startTimestamp;
  }

  /**
   * Returns the width of the total pulse.
   * 
   * @return a total pulse width, in seconds.
   */
  public Double getTotalTime()
  {
    return this.totalTime;
  }

  /**
   * Returns the width of the total pulse.
   * 
   * @return a total pulse width, in seconds.
   */
  public String getTotalTimeAsString()
  {
    Double tt = getTotalTime();
    if ( tt != null )
    {
      return displayTime( tt.doubleValue() );
    }
    return "-";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + this.channelIdx;
    result = ( prime * result ) + ( ( this.endTimestamp == null ) ? 0 : this.endTimestamp.hashCode() );
    result = ( prime * result ) + ( ( this.midSamplePos == null ) ? 0 : this.midSamplePos.hashCode() );
    long temp;
    temp = Double.doubleToLongBits( this.refTime );
    result = ( prime * result ) + ( int )( temp ^ ( temp >>> 32 ) );
    result = ( prime * result ) + ( ( this.startTimestamp == null ) ? 0 : this.startTimestamp.hashCode() );
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public String toHtmlString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( "<html>" );
    sb.append( "Channel: " ).append( getChannelIndexAsString() ).append( "<br>" );
    sb.append( "Time: " ).append( getReferenceTimeAsString() ).append( "<br>" );
    sb.append( "Period: " ).append( getTotalTimeAsString() ).append( "<br>" );
    sb.append( "</html>" );
    return sb.toString();
  }
}
