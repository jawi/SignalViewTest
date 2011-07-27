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
package nl.lxtreme.test.model;


import java.awt.*;

import nl.lxtreme.test.*;


/**
 * @author jajans
 */
public class ScreenModel
{
  // INNER TYPES

  public static enum HelpTextDisplay
  {
    INVISIBLE, TOOLTIP, LABEL;
  }

  /**
   * @author jawi
   */
  public static enum SignalAlignment
  {
    TOP, BOTTOM, CENTER;
  }

  // CONSTANTS

  private static final int MAX_CURSORS = 10;

  /** The tick increment (in pixels). */
  private static final int TIMELINE_INCREMENT = 5;

  private static final Color[] SALEAE_COLORS = { //
  Utils.parseColor( "000000" ), //
      Utils.parseColor( "8B4513" ), //
      Utils.parseColor( "FF0000" ), //
      Utils.parseColor( "FFA500" ), //
      Utils.parseColor( "FFFF00" ), //
      Utils.parseColor( "00FF00" ), //
      Utils.parseColor( "0000FF" ), //
      Utils.parseColor( "A020F0" ), //
      Utils.parseColor( "CDC9C9" ) //
  };
  private static final Color[] OLS_COLORS = { //
  Utils.parseColor( "000000" ), //
      Utils.parseColor( "FFFFFF" ), //
      Utils.parseColor( "00FF00" ), //
      Utils.parseColor( "FF0000" ), //
      Utils.parseColor( "0000FF" ), //
      Utils.parseColor( "00FF00" ), //
      Utils.parseColor( "FFFF00" ), //
      Utils.parseColor( "0000FF" ), //
      Utils.parseColor( "FF0000" ) //
  };
  private static final Color[] DARK_COLORS = { //
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

  private double zoomFactor;
  private boolean zoomAll;
  private int signalHeight;
  private int channelHeight;
  private int visibleMask;
  private boolean measurementMode;
  private boolean cursorMode;
  private boolean snapCursor;
  private final int[] virtualRowMapping;
  private final Color[] colors;
  private final String[] channelLabels;
  private final String[] cursorLabels;
  private SignalAlignment signalAlignment;
  private HelpTextDisplay helpTextDisplay;
  private Rectangle visibleRect;

  // CONSTRUCTORS

  /**
   * 
   */
  public ScreenModel( final int aDataWidth )
  {
    this.signalHeight = 20;
    this.channelHeight = 40;
    this.zoomFactor = 0.01;

    this.signalAlignment = SignalAlignment.CENTER;
    this.helpTextDisplay = HelpTextDisplay.TOOLTIP;

    this.snapCursor = false;

    this.virtualRowMapping = new int[aDataWidth];
    for ( int i = 0; i < aDataWidth; i++ )
    {
      this.virtualRowMapping[i] = i;
    }

    this.colors = new Color[aDataWidth];
    int value = 2;
    if ( value == 0 )
    {
      for ( int i = 0; i < aDataWidth; i++ )
      {
        int idx = ( i % ( OLS_COLORS.length - 1 ) ) + 1;
        this.colors[i] = OLS_COLORS[idx];
      }
    }
    else if ( value == 1 )
    {
      for ( int i = 0; i < aDataWidth; i++ )
      {
        int idx = ( i % ( SALEAE_COLORS.length - 1 ) ) + 1;
        this.colors[i] = SALEAE_COLORS[idx];
      }
    }
    else if ( value == 2 )
    {
      for ( int i = 0; i < aDataWidth; i++ )
      {
        int idx = ( i % ( DARK_COLORS.length - 1 ) ) + 1;
        this.colors[i] = DARK_COLORS[idx];
      }
    }

    this.channelLabels = new String[aDataWidth];
    for ( int i = 0; i < this.channelLabels.length; i++ )
    {
      this.channelLabels[i] = String.format( "Channel %c", Integer.valueOf( i + 'A' ) );
    }

    this.cursorLabels = new String[MAX_CURSORS];
    for ( int i = 0; i < this.cursorLabels.length; i++ )
    {
      this.cursorLabels[i] = String.format( "T%c", Integer.valueOf( i + 'a' ) );
    }

    this.visibleMask = 0x555;
  }

  // METHODS

  /**
   * Moves an element from a "old" position to a "new" position, shifting all
   * other elements.
   * <p>
   * NOTE: the given array's contents will be mutated!
   * </p>
   * 
   * @param aInput
   *          the input array to move the elements from, cannot be
   *          <code>null</code>;
   * @param aOldIdx
   *          the index of the element to move;
   * @param aNewIdx
   *          the index to move the element to.
   */
  static final void shiftElements( final int[] aInput, final int aOldIdx, final int aNewIdx )
  {
    final int length = aInput.length;

    final int moved = aInput[aOldIdx];
    // Delete element from array...
    System.arraycopy( aInput, aOldIdx + 1, aInput, aOldIdx, length - 1 - aOldIdx );
    // Make space for new element...
    System.arraycopy( aInput, aNewIdx, aInput, aNewIdx + 1, length - 1 - aNewIdx );
    // Set actual (inserted) element...
    aInput[aNewIdx] = moved;
  }

  /**
   * @return the colors
   */
  public Color getChannelColor( final int aChannelIdx )
  {
    return this.colors[aChannelIdx];
  }

  /**
   * @return
   */
  public int getChannelHeight()
  {
    return this.channelHeight;
  }

  /**
   * @param aChannelIdx
   * @return
   */
  public String getChannelLabel( final int aChannelIdx )
  {
    return this.channelLabels[aChannelIdx];
  }

  /**
   * @return the colors
   */
  public Color getCursorColor( final int aCursorIdx )
  {
    return this.colors[aCursorIdx % this.colors.length].darker().darker();
  }

  /**
   * @param aChannelIdx
   * @return
   */
  public String getCursorLabel( final int aCursorIdx )
  {
    return this.cursorLabels[aCursorIdx];
  }

  /**
   * Returns how the time line help text is to be displayed.
   * 
   * @return a {@link HelpTextDisplay} value, never <code>null</code>.
   */
  public HelpTextDisplay getHelpTextDisplayMode()
  {
    return this.helpTextDisplay;
  }

  /**
   * @return
   */
  public SignalAlignment getSignalAlignment()
  {
    return this.signalAlignment;
  }

  /**
   * @return
   */
  public int getSignalHeight()
  {
    return this.signalHeight;
  }

  /**
   * Returns the signal offset.
   * 
   * @return a signal offset, >= 0.
   * @see #getSignalAlignment()
   */
  public int getSignalOffset()
  {
    final int signalOffset;
    if ( SignalAlignment.BOTTOM.equals( getSignalAlignment() ) )
    {
      signalOffset = ( this.channelHeight - this.signalHeight ) - 2;
    }
    else if ( SignalAlignment.CENTER.equals( getSignalAlignment() ) )
    {
      signalOffset = ( int )( ( this.channelHeight - this.signalHeight ) / 2.0 );
    }
    else
    {
      signalOffset = 1;
    }
    return signalOffset;
  }

  /**
   * Returns the increment of pixels per timeline tick.
   * 
   * @return a tick increment, >= 1.0.
   * @see #getTimebase()
   */
  public double getTickIncrement()
  {
    return Math.max( 1.0, getTimebase() / TIMELINE_INCREMENT );
  }

  /**
   * Determines the time base for the given absolute time (= total time
   * displayed).
   * 
   * @return a time base, as power of 10.
   */
  public double getTimebase()
  {
    if ( this.visibleRect == null )
    {
      return 0.0;
    }
    final double absoluteTime = this.visibleRect.width / getZoomFactor();
    return Math.pow( 10, Math.round( Math.log10( absoluteTime ) ) );
  }

  /**
   * Returns the increment of pixels per unit of time.
   * 
   * @return a time increment, >= 0.1.
   * @see #getTimebase()
   */
  public double getTimeIncrement()
  {
    return Math.max( 0.1, getTimebase() / ( 10.0 * TIMELINE_INCREMENT ) );
  }

  /**
   * Returns the dimensions of the visible screen.
   * 
   * @return the visible dimensions, as {@link Rectangle}, never
   *         <code>null</code>.
   */
  public Rectangle getVisibleRect()
  {
    return this.visibleRect;
  }

  /**
   * @return
   */
  public double getZoomFactor()
  {
    return this.zoomFactor;
  }

  /**
   * @param aChannelIdx
   * @return
   */
  public boolean isChannelVisible( final int aChannelIdx )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= this.virtualRowMapping.length ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }

    int mask = ( 1 << aChannelIdx );
    return ( this.visibleMask & mask ) != 0;
  }

  /**
   * @return the cursorMode
   */
  public boolean isCursorMode()
  {
    return this.cursorMode;
  }

  public boolean isMeasurementMode()
  {
    return this.measurementMode;
  }

  /**
   * @return the snapCursor
   */
  public boolean isSnapCursor()
  {
    return this.snapCursor;
  }

  /**
   * Returns whether or not the time line help text is to be displayed.
   * 
   * @return <code>true</code> if the time line help text is to be displayed,
   *         <code>false</code> otherwise.
   */
  public boolean isTimeLineHelpTextDisplayed()
  {
    return HelpTextDisplay.INVISIBLE != this.helpTextDisplay;
  }

  /**
   * @return the zoomAll
   */
  public boolean isZoomAll()
  {
    return this.zoomAll;
  }

  /**
   * Moves the given "old" row index to the new row index position.
   * 
   * @param aOldRowIdx
   *          the old (virtual) row to move;
   * @param aNewRowIdx
   *          the new (virtual) row to insert the "old" row to.
   */
  public void moveRows( final int aOldRowIdx, final int aNewRowIdx )
  {
    final int dataWidth = this.virtualRowMapping.length;
    if ( ( aOldRowIdx < 0 ) || ( aOldRowIdx >= dataWidth ) )
    {
      throw new IllegalArgumentException( "Moved row invalid!" );
    }
    if ( ( aNewRowIdx < 0 ) || ( aNewRowIdx >= dataWidth ) )
    {
      throw new IllegalArgumentException( "Insert row invalid!" );
    }

    if ( aOldRowIdx == aNewRowIdx )
    {
      return;
    }

    final int row = toRealRow( aOldRowIdx );
    final int newRow = toRealRow( aNewRowIdx );

    shiftElements( this.virtualRowMapping, row, newRow );
  }

  /**
   * @param aChannelHeight
   */
  public void setChannelHeight( final int aChannelHeight )
  {
    this.channelHeight = aChannelHeight;
  }

  /**
   * @param aChannelIdx
   * @param aVisible
   */
  public void setChannelVisible( final int aChannelIdx, final boolean aVisible )
  {
    if ( ( aChannelIdx < 0 ) || ( aChannelIdx >= this.virtualRowMapping.length ) )
    {
      throw new IllegalArgumentException( "Invalid channel index!" );
    }

    int mask = ( 1 << aChannelIdx );
    if ( aVisible )
    {
      this.visibleMask |= mask;
    }
    else
    {
      this.visibleMask &= ~mask;
    }
  }

  /**
   * Enables or disables the cursors.
   * 
   * @param aSelected
   *          <code>true</code> to enable the cursors, <code>false</code> to
   *          disable the cursors.
   */
  public void setCursorMode( final boolean aCursorMode )
  {
    this.cursorMode = aCursorMode;
  }

  /**
   * Sets how the time line help text is to be displayed.
   * 
   * @param aHelpTextDisplay
   *          the {@link HelpTextDisplay} to set, cannot be <code>null</code>.
   */
  public void setHelpTextDisplayMode( final HelpTextDisplay aHelpTextDisplay )
  {
    this.helpTextDisplay = aHelpTextDisplay;
  }

  /**
   * @param aEnabled
   */
  public void setMeasurementMode( final boolean aEnabled )
  {
    this.measurementMode = aEnabled;
  }

  /**
   * @param aSignalAlignment
   */
  public void setSignalAlignment( final SignalAlignment aSignalAlignment )
  {
    this.signalAlignment = aSignalAlignment;
  }

  /**
   * @param aSignalHeight
   */
  public void setSignalHeight( final int aSignalHeight )
  {
    this.signalHeight = aSignalHeight;
  }

  /**
   * @param aSnapCursor
   *          the snapCursor to set
   */
  public void setSnapCursor( final boolean aSnapCursor )
  {
    this.snapCursor = aSnapCursor;
  }

  /**
   * Sets the visible dimensions of the signal view.
   * 
   * @param aVisibleRect
   *          the visible dimensions to set, cannot be <code>null</code>.
   */
  public void setVisibleRect( final Rectangle aVisibleRect )
  {
    if ( aVisibleRect == null )
    {
      throw new IllegalArgumentException( "Parameter VisibleRect cannot be null!" );
    }
    // Take a copy to avoid external modifications...
    this.visibleRect = new Rectangle( aVisibleRect );
  }

  /**
   * @param aZoomAll
   *          the zoomAll to set
   */
  public void setZoomAll( final boolean aZoomAll )
  {
    this.zoomAll = aZoomAll;
  }

  /**
   * @param aZoomFactor
   */
  public void setZoomFactor( final double aZoomFactor )
  {
    this.zoomFactor = aZoomFactor;
  }

  /**
   * @param aRowIdx
   * @return
   */
  public int toRealRow( final int aRowIdx )
  {
    return this.virtualRowMapping[aRowIdx];
  }

  /**
   * @param aRowIdx
   * @return
   */
  public int toVirtualRow( final int aRowIdx )
  {
    for ( int i = 0; i < this.virtualRowMapping.length; i++ )
    {
      if ( this.virtualRowMapping[i] == aRowIdx )
      {
        return i;
      }
    }
    return -1;
  }
}
