package nl.lxtreme.test.view;


import static nl.lxtreme.test.Utils.*;

import java.awt.*;


/**
 * Provides a small DTO for keeping signal hover information together.
 */
final class SignalHoverInfo implements Cloneable
{
  // CONSTANTS

  public static final double TIMESTAMP_FACTOR = 100.0;

  // VARIABLES

  private final Rectangle rectangle;
  private final double timestamp;
  private final double pulseWidth;
  private final double totalPulseWidth;
  private final Integer channelIdx;
  private final int sampleRate;
  private final int middleXpos;

  // CONSTRUCTORS

  /**
   * Creates a new SignalHoverInfo instance.
   * 
   * @param aRectangle
   *          the UI coordinates defining the hover on screen, cannot be
   *          <code>null</code>;
   * @param aStartTimestamp
   *          the time stamp that makes up the left side of the hover;
   * @param aEndTimestamp
   *          the time stamp that makes up the right side of the hover;
   * @param aReferenceSample
   *          the reference sample that is used to calculate the hover
   *          information;
   * @param aTimestamp
   *          the time stamp of this hover, based on the mouse position;
   * @param aMiddleXpos
   *          the screen coordinate of the middle X position;
   * @param aChannelIdx
   *          the channel index on which the hover information is based;
   * @param aSampleRate
   *          the sample rate on which the timing information should be based.
   */
  public SignalHoverInfo( final Rectangle aRectangle, final long aStartTimestamp, final long aEndTimestamp,
      final long aMiddleTimestamp, final long aTimestamp, final int aMiddleXpos, final int aChannelIdx,
      final int aSampleRate )
  {
    this.rectangle = aRectangle;

    this.middleXpos = aMiddleXpos;

    this.totalPulseWidth = ( aEndTimestamp - aStartTimestamp ) / ( double )aSampleRate;
    // Determine the smallest pulse width we're going to display...
    if ( ( aEndTimestamp - aMiddleTimestamp ) < ( aMiddleTimestamp - aStartTimestamp ) )
    {
      this.pulseWidth = ( aEndTimestamp - aMiddleTimestamp ) / ( double )aSampleRate;
    }
    else
    {
      this.pulseWidth = ( aMiddleTimestamp - aStartTimestamp ) / ( double )aSampleRate;
    }
    this.timestamp = aTimestamp / ( TIMESTAMP_FACTOR * aSampleRate );
    this.channelIdx = Integer.valueOf( aChannelIdx );

    this.sampleRate = aSampleRate;
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public SignalHoverInfo clone()
  {
    try
    {
      return ( SignalHoverInfo )super.clone();
    }
    catch ( CloneNotSupportedException exception )
    {
      throw new RuntimeException( exception );
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
    if ( ( aObject == null ) || !( aObject instanceof SignalHoverInfo ) )
    {
      return false;
    }

    final SignalHoverInfo other = ( SignalHoverInfo )aObject;
    if ( this.channelIdx.intValue() != other.channelIdx.intValue() )
    {
      return false;
    }
    if ( this.totalPulseWidth != other.totalPulseWidth )
    {
      return false;
    }
    if ( this.timestamp != other.timestamp )
    {
      return false;
    }
    if ( this.rectangle == null )
    {
      if ( other.rectangle != null )
      {
        return false;
      }
    }
    else if ( !this.rectangle.equals( other.rectangle ) )
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
  public Integer getChannelIndex()
  {
    return this.channelIdx;
  }

  /**
   * Returns the current value of middleXpos.
   * 
   * @return the middleXpos
   */
  public int getMiddleXpos()
  {
    return this.middleXpos;
  }

  /**
   * Returns the width of the (first half of) pulse, in seconds.
   * 
   * @return a pulse width, in seconds.
   */
  public double getPulseWidth()
  {
    return this.pulseWidth;
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
   * Returns the time value where the mouse cursor is, in seconds.
   * 
   * @return a time value, in seconds.
   */
  public double getTimeValue()
  {
    return this.timestamp;
  }

  /**
   * Returns the width of the total pulse, in seconds.
   * 
   * @return a total pulse width, in seconds.
   */
  public double getTotalPulseWidth()
  {
    return this.totalPulseWidth;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( this.channelIdx == null ) ? 0 : this.channelIdx.hashCode() );
    long temp;
    temp = Double.doubleToLongBits( this.totalPulseWidth );
    result = prime * result + ( int )( temp ^ ( temp >>> 32 ) );
    result = prime * result + ( ( this.rectangle == null ) ? 0 : this.rectangle.hashCode() );
    result = prime * result + this.sampleRate;
    temp = Double.doubleToLongBits( this.timestamp );
    result = prime * result + ( int )( temp ^ ( temp >>> 32 ) );
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( "Width: " ).append( displayTime( getPulseWidth() ) ).append( "; " );
    sb.append( "period: " ).append( displayTime( getTotalPulseWidth() ) ).append( '\n' );
    sb.append( "Time:" ).append( displayTime( getTimeValue() ) ).append( '\n' );
    sb.append( "Channel: " ).append( getChannelIndex() );
    return sb.toString();
  }
}
