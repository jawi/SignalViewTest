/**
 * 
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

  /**
   * @author jawi
   */
  public static enum SignalAlignment
  {
    TOP, BOTTOM, CENTER;
  }

  // CONSTANTS

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
  private boolean measurementMode;
  private boolean cursorMode;
  private final int[] virtualRowMapping;
  private final Color[] colors;
  private final String[] channelLabels;
  private final String[] cursorLabels;
  private SignalAlignment signalAlignment;

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

    this.cursorLabels = new String[10]; // XXX
    for ( int i = 0; i < this.cursorLabels.length; i++ )
    {
      this.cursorLabels[i] = String.format( "T%c", Integer.valueOf( i + 'a' ) );
    }
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
    return this.colors[aCursorIdx].darker().darker();
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
   * @return
   */
  public double getZoomFactor()
  {
    return this.zoomFactor;
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
    shiftElements( this.virtualRowMapping, aOldRowIdx, aNewRowIdx );
  }

  /**
   * @param aChannelHeight
   */
  public void setChannelHeight( final int aChannelHeight )
  {
    this.channelHeight = aChannelHeight;
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
