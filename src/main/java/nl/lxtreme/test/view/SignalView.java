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
 * 
 * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
 */
package nl.lxtreme.test.view;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.logging.*;

import javax.swing.*;

import nl.lxtreme.test.*;
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
      if ( ( flavors == null ) || ( flavors.length == 0 ) )
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
        SwingUtilities.getRootPane( SignalView.this ).getGlassPane().setVisible( false );

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
      try
      {
        aEvent.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );

        final Transferable transferable = aEvent.getTransferable();

        Integer realRowValue = ( Integer )transferable.getTransferData( ChannelRowTransferable.FLAVOR );
        if ( realRowValue != null )
        {
          final int oldRealRow = realRowValue.intValue();
          final int newRealRow = this.ctlr.getSignalRow( aEvent.getLocation() );

          // Move the channel rows...
          this.ctlr.moveChannelRows( oldRealRow, newRealRow );

          return true;
        }
      }
      catch ( Exception exception )
      {
        LOG.log( Level.WARNING, "Getting transfer data failed!", exception );
      }

      return false;
    }

    /**
     * @param aEvent
     */
    private boolean dropCursor( final DropTargetDropEvent aEvent )
    {
      try
      {
        aEvent.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );

        final Transferable transferable = aEvent.getTransferable();

        Integer cursorValue = ( Integer )transferable.getTransferData( CursorTransferable.FLAVOR );
        if ( cursorValue != null )
        {
          final int cursorIdx = cursorValue.intValue();
          final Point newLocation = aEvent.getLocation();

          // Move the cursor position...
          this.ctlr.moveCursor( cursorIdx, newLocation, false /* aSnap */);

          return true;
        }
      }
      catch ( Exception exception )
      {
        LOG.log( Level.WARNING, "Getting transfer data failed!", exception );
      }

      return false;
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger( SignalView.class.getName() );

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

    setBackground( Utils.parseColor( "#1E2126" ) );

    // setDebugGraphicsOptions( DebugGraphics.LOG_OPTION );
    // DebugGraphics.setLogStream( System.err );

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
      final long[] timestamps = dataModel.getTimestamps();

      final int startIdx = getStartIndex( clip );
      final int endIdx = getEndIndex( clip, values.length );
      final int size = Math.min( values.length - 1, ( endIdx - startIdx ) + 1 );

      final int[] x = new int[2 * size];
      final int[] y = new int[2 * size];

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
        canvas.setColor( screenModel.getChannelColor( b ) );

        final int mask = ( 1 << b );
        // determine where we really should draw the signal...
        final int dy = signalOffset + ( channelHeight * screenModel.toVirtualRow( b ) );

        long timestamp = timestamps[startIdx];
        int prevSampleValue = ( ( values[startIdx] & mask ) == 0 ) ? 1 : 0;

        x[0] = ( int )( zoomFactor * timestamp );
        y[0] = dy + ( signalHeight * prevSampleValue );
        int p = 1;

        for ( int i = 1; i < size; i++ )
        {
          final int sampleIdx = ( i + startIdx );

          int sampleValue = ( ( values[sampleIdx] & mask ) == 0 ) ? 1 : 0;
          timestamp = timestamps[sampleIdx];

          if ( prevSampleValue != sampleValue )
          {
            x[p] = ( int )( zoomFactor * timestamp );
            y[p] = dy + ( signalHeight * prevSampleValue );
            p++;
          }

          x[p] = ( int )( zoomFactor * timestamp );
          y[p] = dy + ( signalHeight * sampleValue );
          p++;

          prevSampleValue = sampleValue;
        }

        canvas.drawPolyline( x, y, p );
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
