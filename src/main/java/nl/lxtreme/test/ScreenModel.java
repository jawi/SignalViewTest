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

  // CONSTRUCTORS

  /**
	 * 
	 */
  public ScreenModel()
  {
    this.signalHeight = 20;
    this.channelHeight = 30;
    this.zoomFactor = 0.01;
  }

  // METHODS

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
}
