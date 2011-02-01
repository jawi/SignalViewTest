/**
 * 
 */
package nl.lxtreme.test;


/**
 * @author jajans
 */
public class ScreenModel
{
  // VARIABLES

  private double zoomFactor;
  private int signalHeight;
  private int channelHeight;

  private final int[] virtualRowMapping;

  // CONSTRUCTORS

  /**
	 * 
	 */
  public ScreenModel( final int aDataWidth )
  {
    this.signalHeight = 20;
    this.channelHeight = 40;
    this.zoomFactor = 0.01;

    this.virtualRowMapping = new int[aDataWidth];
    for ( int i = 0; i < aDataWidth; i++ )
    {
      // this.virtualRowMapping[i] = i;
      this.virtualRowMapping[i] = ( aDataWidth - 1 ) - i;
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
   * @return
   */
  public int getChannelHeight()
  {
    return this.channelHeight;
  }

  /**
   * @return
   */
  public int getSignalHeight()
  {
    return this.signalHeight;
  }

  /**
   * @return
   */
  public double getZoomFactor()
  {
    return this.zoomFactor;
  }

  /**
   * Moves the given "old" row index to the new row index position.
   * 
   * @param aOldRowIdx
   *          the old (virtual) row to move;
   * @param aNewRowIdx
   *          the new (virtual) row to insert the "old" row to.
   */
  public void moveVirtualRows( final int aOldRowIdx, final int aNewRowIdx )
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
   * @param aSignalHeight
   */
  public void setSignalHeight( final int aSignalHeight )
  {
    this.signalHeight = aSignalHeight;
  }

  /**
   * @param aZoomFactor
   */
  public void setZoomFactor( final double aZoomFactor )
  {
    this.zoomFactor = aZoomFactor;
  }

  /**
   * @param aVirtualRowIdx
   * @return
   */
  public int toRealRow( final int aVirtualRowIdx )
  {
    for ( int i = 0; i < this.virtualRowMapping.length; i++ )
    {
      if ( this.virtualRowMapping[i] == aVirtualRowIdx )
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * @param aRealRowIdx
   * @return
   */
  public int toVirtualRow( final int aRealRowIdx )
  {
    return this.virtualRowMapping[aRealRowIdx];
  }
}
