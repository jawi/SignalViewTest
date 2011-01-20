/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;

import javax.swing.*;


/**
 * @author jajans
 */
public class ModelView extends JPanel implements Scrollable
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final ScreenController controller;

  // CONSTRUCTORS

  /**
   * Creates a new ModelView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public ModelView( final ScreenController aController )
  {
    this.controller = aController;

    setOpaque( true );
    setBackground( Color.BLACK );

    setDebugGraphicsOptions( DebugGraphics.LOG_OPTION );
    DebugGraphics.setLogStream( System.err );

    this.controller.setModelView( this );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getScrollableBlockIncrement( final Rectangle aVisibleRect, final int aOrientation, final int aDirection )
  {
    if ( aOrientation == SwingConstants.HORIZONTAL )
    {
      return aVisibleRect.width - 50;
    }
    else
    {
      return aVisibleRect.height - this.controller.getScreenModel().getChannelHeight();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean getScrollableTracksViewportWidth()
  {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getScrollableUnitIncrement( final Rectangle aVisibleRect, final int aOrientation, final int aDirection )
  {
    int currentPosition = 0;
    final int maxUnitIncrement;
    if ( aOrientation == SwingConstants.HORIZONTAL )
    {
      currentPosition = aVisibleRect.x;
      maxUnitIncrement = 50;
    }
    else
    {
      currentPosition = aVisibleRect.y;
      maxUnitIncrement = this.controller.getScreenModel().getChannelHeight();
    }

    // Return the number of pixels between currentPosition
    // and the nearest tick mark in the indicated direction.
    if ( aDirection < 0 )
    {
      final int newPosition = currentPosition - ( currentPosition / maxUnitIncrement ) * maxUnitIncrement;
      return ( newPosition == 0 ) ? maxUnitIncrement : newPosition;
    }
    else
    {
      return ( ( currentPosition / maxUnitIncrement ) + 1 ) * maxUnitIncrement - currentPosition;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    super.paintComponent( aGraphics );

    Graphics canvas = aGraphics.create();

    try
    {
      final Rectangle clip = aGraphics.getClipBounds();

      final DataModel dataModel = this.controller.getDataModel();

      final int[] values = dataModel.getValues();

      final int startIdx = Math.max( 0, this.controller.toTimestampIndex( clip.getLocation() ) - 1 );
      final int endIdx = Math.min( this.controller.toTimestampIndex( new Point( clip.x + clip.width, 0 ) ) + 1,
          values.length - 1 );

      final int size = ( endIdx - startIdx );
      if ( size > 1000000 )
      {
        // Too many samples on one screen?!?
        paintLargeDataSet( canvas, size, startIdx, clip );
        // paintNormalDataSet( canvas, size, startIdx );
      }
      else
      {
        // This data set might reasonably well fit on screen...
        paintNormalDataSet( canvas, size, startIdx );
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;
    }
  }

  /**
   * @param aValues
   * @param aMask
   * @param aStartIdx
   * @param aEndIndx
   * @return
   */
  private int getMean( final int[] aValues, final int aMask, final int aStartIdx, final int aEndIndx )
  {
    int result = 0;
    for ( int i = aStartIdx; i < aEndIndx; i++ )
    {
      result += ( aValues[i] & aMask );
    }
    double count = ( aEndIndx - aStartIdx );
    double retval = result / count;
    return ( int )( retval );
  }

  /**
   * @param aCanvas
   * @param aSize
   * @param aStartSampleIdx
   * @param aClip
   */
  private void paintLargeDataSet( final Graphics aCanvas, final int aSize, final int aStartSampleIdx,
      final Rectangle aClip )
  {
    final DataModel dataModel = this.controller.getDataModel();

    final int[] values = dataModel.getValues();
    final int[] timestamps = dataModel.getTimestamps();

    final double scaleFactor = 1.0 / this.controller.getScreenModel().getZoomFactor();
    final int newSize = ( int )Math.ceil( aSize / scaleFactor ) + 1;

    final ScreenModel screenModel = this.controller.getScreenModel();
    final int signalHeight = screenModel.getSignalHeight();

    int dy = signalHeight;

    aCanvas.setColor( Color.GREEN.darker().darker() );

    final int width = dataModel.getWidth();
    for ( int b = 0; b < width; b++ )
    {
      final int mask = ( 1 << b );

      int oldX = aClip.x;
      for ( int i = 0; i < aSize; i += scaleFactor )
      {
        final int sampleIdx = Math.min( i + aStartSampleIdx, values.length - 1 );

        final int value = getMean( values, mask, sampleIdx, sampleIdx + newSize ) == 0 ? 0 : signalHeight;
        final int timestamp = timestamps[sampleIdx];

        int newX = this.controller.toScaledScreenCoordinate( timestamp ).x;
        aCanvas.drawRect( oldX, dy, newX - oldX, value );

        oldX = newX;
      }

      dy += screenModel.getChannelHeight();
    }
  }

  /**
   * @param aCanvas
   * @param aSize
   * @param aStartSampleIdx
   */
  private void paintNormalDataSet( final Graphics aCanvas, final int aSize, final int aStartSampleIdx )
  {
    final DataModel dataModel = this.controller.getDataModel();

    final int[] values = dataModel.getValues();
    final int[] timestamps = dataModel.getTimestamps();

    final int[] x = new int[aSize];
    final int[] y = new int[aSize];

    aCanvas.setColor( Color.GREEN.darker().darker() );

    final ScreenModel screenModel = this.controller.getScreenModel();
    int dy = screenModel.getSignalHeight();

    final int width = dataModel.getWidth();
    for ( int b = 0; b < width; b++ )
    {
      final int mask = ( 1 << b );

      for ( int i = 0; i < aSize; i++ )
      {
        final int sampleIdx = i + aStartSampleIdx;
        final int value = ( values[sampleIdx] & mask ) == 0 ? 0 : screenModel.getSignalHeight();
        final int timestamp = timestamps[sampleIdx];

        x[i] = this.controller.toScaledScreenCoordinate( timestamp ).x;
        y[i] = dy + value;
      }

      aCanvas.drawPolyline( x, y, aSize );

      dy += screenModel.getChannelHeight();
    }
  }
}
