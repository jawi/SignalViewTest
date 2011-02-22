/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

import javax.swing.*;

import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.model.*;


/**
 * @author jajans
 */
class SignalView extends JPanel
{
  // INNER TYPES

  /**
   * @author jajans
   */
  protected final class DnDTargetController extends DropTargetAdapter
  {
    // VARIABLES

    private final ScreenController _controller = SignalView.this.controller;

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop( final DropTargetDropEvent aEvent )
    {
      final DataFlavor[] flavors = aEvent.getCurrentDataFlavors();
      if ( flavors == null )
      {
        return;
      }

      boolean loop = true;
      for ( int i = flavors.length - 1; loop && ( i >= 0 ); i-- )
      {
        if ( SampleRowTransferable.FLAVOR.equals( flavors[i] ) )
        {
          aEvent.acceptDrop( DnDConstants.ACTION_COPY );

          final Transferable transferable = aEvent.getTransferable();

          Integer realRow = null;
          try
          {
            realRow = ( Integer )transferable.getTransferData( SampleRowTransferable.FLAVOR );
          }
          catch ( final Exception exception )
          {
            // NO-op
          }

          if ( ( realRow == null ) || ( realRow.intValue() < 0 ) )
          {
            return;
          }

          final Point coordinate = ( Point )aEvent.getLocation().clone();
          final int newRealRow = this._controller.getSignalRow( coordinate );

          this._controller.moveSampleRows( realRow.intValue(), newRealRow );

          aEvent.dropComplete( true );
          loop = false;
        }
      }
    }
  }

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
  public SignalView( final ScreenController aController )
  {
    this.controller = aController;

    setOpaque( true );
    setBackground( Color.BLACK );

    // setDebugGraphicsOptions( DebugGraphics.LOG_OPTION );
    // DebugGraphics.setLogStream( System.err );

    this.controller.setSignalView( this );

    final DnDTargetController targetController = new DnDTargetController();
    setDropTarget( new DropTarget( this, targetController ) );
  }

  // METHODS

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

      final int startIdx = getStartIndex( clip );
      final int endIdx = getEndIndex( clip, values.length );

      final int size = ( endIdx - startIdx );
      if ( size > 1000000 )
      {
        // Too many samples on one screen?!?
        System.out.println( "size = " + size + ", start = " + startIdx + ", end = " + endIdx );
        paintNormalDataSet( canvas, size, startIdx );
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
   * @param aClip
   * @return
   */
  private int getEndIndex( final Rectangle aClip, final int aLength )
  {
    final Point location = new Point( aClip.x + aClip.width, 0 );
    return Math.min( this.controller.toTimestampIndex( location ) + 1, aLength - 1 );
  }

  /**
   * @param aClip
   * @return
   */
  private int getStartIndex( final Rectangle aClip )
  {
    final Point location = aClip.getLocation();
    return Math.max( this.controller.toTimestampIndex( location ) - 1, 0 );
  }

  /**
   * @param aCanvas
   * @param aSize
   * @param aStartSampleIdx
   */
  private void paintNormalDataSet( final Graphics aCanvas, final int aSize, final int aStartSampleIdx )
  {
    final DataModel dataModel = this.controller.getDataModel();
    final ScreenModel screenModel = this.controller.getScreenModel();

    final int[] values = dataModel.getValues();
    final long[] timestamps = dataModel.getTimestamps();

    final int[] x = new int[aSize];
    final int[] y = new int[aSize];

    final int signalHeight = screenModel.getSignalHeight();
    final int channelHeight = screenModel.getChannelHeight();
    // Where is the signal to be drawn?
    final int signalOffset = screenModel.getSignalOffset();

    final int width = dataModel.getWidth();
    for ( int b = 0; b < width; b++ )
    {
      final int mask = ( 1 << b );
      // determine where we really should draw the signal...
      final int dy = signalOffset + ( channelHeight * screenModel.toVirtualRow( b ) );

      for ( int i = 0; i < aSize; i++ )
      {
        final int sampleIdx = i + aStartSampleIdx;

        final int sampleValue = ( ( values[sampleIdx] & mask ) >>> b ) & 0x01;
        final int value = ( signalHeight * ( 1 - sampleValue ) );
        final long timestamp = timestamps[sampleIdx];

        x[i] = this.controller.toScaledScreenCoordinate( timestamp ).x;
        y[i] = dy + value;
      }

      aCanvas.setColor( screenModel.getColor( b ) );
      aCanvas.drawPolyline( x, y, aSize );
    }
  }
}
