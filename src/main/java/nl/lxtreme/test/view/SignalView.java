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
  final class DnDTargetController extends DropTargetAdapter
  {
    // VARIABLES

    private final SignalDiagramController ctlr = SignalView.this.controller;

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

      boolean result = false;
      for ( int i = flavors.length - 1; !result && ( i >= 0 ); i-- )
      {
        if ( ChannelRowTransferable.FLAVOR.equals( flavors[i] ) )
        {
          result = dropChannelRow( aEvent );
        }
        else if ( CursorTransferable.FLAVOR.equals( flavors[i] ) )
        {
          result = dropCursor( aEvent );
        }
      }

      if ( result )
      {
        // Update our administration...
        final GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( SignalView.this ).getGlassPane();
        glassPane.clearDropPoint();
        glassPane.setVisible( false );

        DragAndDropLock.setLocked( false );

        // Acknowledge that we've successfully dropped the item...
        aEvent.dropComplete( true );
      }
    }

    /**
     * @param aEvent
     */
    private boolean dropChannelRow( final DropTargetDropEvent aEvent )
    {
      aEvent.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );

      final Transferable transferable = aEvent.getTransferable();

      Integer realRowValue = null;
      try
      {
        realRowValue = ( Integer )transferable.getTransferData( ChannelRowTransferable.FLAVOR );
        if ( realRowValue == null )
        {
          return false;
        }
      }
      catch ( final Exception exception )
      {
        // NO-op
      }

      final SampleDataModel dataModel = this.ctlr.getDataModel();
      final int oldRealRow = realRowValue.intValue();
      if ( ( oldRealRow < 0 ) || ( oldRealRow >= dataModel.getWidth() ) )
      {
        return false;
      }

      final Point coordinate = ( Point )aEvent.getLocation().clone();
      final int newRealRow = this.ctlr.getSignalRow( coordinate );

      // Move the channel rows...
      this.ctlr.moveChannelRows( oldRealRow, newRealRow );

      return true;
    }

    /**
     * @param aEvent
     */
    private boolean dropCursor( final DropTargetDropEvent aEvent )
    {
      aEvent.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );

      final Transferable transferable = aEvent.getTransferable();

      Integer cursorValue = null;
      try
      {
        cursorValue = ( Integer )transferable.getTransferData( CursorTransferable.FLAVOR );
        if ( cursorValue == null )
        {
          return false;
        }
      }
      catch ( final Exception exception )
      {
        // NO-op
      }

      final SampleDataModel dataModel = this.ctlr.getDataModel();
      final int cursorIdx = cursorValue.intValue();
      if ( ( cursorIdx < 0 ) || ( cursorIdx >= dataModel.getCursors().length ) )
      {
        return false;
      }

      final Point coordinate = ( Point )aEvent.getLocation().clone();

      // Move the cursor position...
      this.ctlr.dragCursor( cursorIdx, coordinate, true /* aSnap */);

      return true;
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;

  // CONSTRUCTORS

  /**
   * Creates a new ModelView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public SignalView( final SignalDiagramController aController )
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

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      final SampleDataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();
      final int[] values = dataModel.getValues();

      final int startIdx = getStartIndex( clip );
      final int endIdx = getEndIndex( clip, values.length );
      final int size = Math.min( values.length - 1, ( endIdx - startIdx ) );

      final long[] timestamps = dataModel.getTimestamps();

      final int[] x = new int[size];
      final int[] y = new int[size];

      final int signalHeight = screenModel.getSignalHeight();
      final int channelHeight = screenModel.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = screenModel.getSignalOffset();
      final double zoomFactor = screenModel.getZoomFactor();

      final int width = dataModel.getWidth();

      // Determine which bits of the actual signal should be drawn...
      final int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      final int endBit = ( int )Math.min( width, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

      for ( int b = startBit; b < endBit; b++ )
      {
        final int mask = ( 1 << b );
        // determine where we really should draw the signal...
        final int dy = signalOffset + ( channelHeight * screenModel.toVirtualRow( b ) );

        long prevTimestamp = startIdx;
        int prevSampleValue = ( ( values[startIdx] & mask ) == 0 ) ? 1 : 0;

        for ( int i = 0; i < size; i++ )
        {
          final int sampleIdx = ( i + startIdx );

          int sampleValue = ( ( values[sampleIdx] & mask ) == 0 ) ? 1 : 0;
          long timestamp = ( sampleValue == prevSampleValue ) ? timestamps[sampleIdx] : prevTimestamp;

          int x1 = ( int )( zoomFactor * timestamp );
          int y1 = dy + ( signalHeight * sampleValue );

          x[i] = x1;
          y[i] = y1;

          prevTimestamp = timestamp;
          prevSampleValue = sampleValue;
        }

        canvas.setColor( screenModel.getColor( b ) );
        canvas.drawPolyline( x, y, size );
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;
    }
  }

  /**
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    return new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
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
}
