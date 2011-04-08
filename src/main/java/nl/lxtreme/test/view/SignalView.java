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
import nl.lxtreme.test.dnd.DragAndDropTargetController.DragAndDropHandler;
import nl.lxtreme.test.model.*;


/**
 * Provides a view for the signal data as individual channels.
 */
final class SignalView extends JPanel
{
  // INNER TYPES

  /**
   * Provides the D&D drop handler for accepting dropped channels.
   */
  static class DropHandler implements DragAndDropHandler
  {
    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptDrop( final SignalDiagramController aController, final DropTargetDropEvent aEvent )
    {
      try
      {
        final Transferable transferable = aEvent.getTransferable();

        Integer realRowValue = ( Integer )transferable.getTransferData( ChannelRowTransferable.FLAVOR );
        if ( realRowValue != null )
        {
          final int oldRealRow = realRowValue.intValue();
          final int newRealRow = aController.getSignalRow( aEvent.getLocation() );

          if ( ( oldRealRow >= 0 ) && ( newRealRow >= 0 ) )
          {
            aEvent.acceptDrop( DnDConstants.ACTION_MOVE );

            // Move the channel rows...
            aController.moveChannelRows( oldRealRow, newRealRow );

            return true;
          }
        }
      }
      catch ( Exception exception )
      {
        LOG.log( Level.WARNING, "Getting transfer data failed!", exception );
      }

      return false;
    }

    /**
     * {@inheritDoc}
     */
    public DataFlavor getFlavor()
    {
      return ChannelRowTransferable.FLAVOR;
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger( SignalView.class.getName() );

  // VARIABLES

  private final SignalDiagramController controller;
  private final DropHandler dropHandler;

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
    this.dropHandler = new DropHandler();

    setBackground( Utils.parseColor( "#1E2126" ) );

    // setDebugGraphicsOptions( DebugGraphics.LOG_OPTION );
    // DebugGraphics.setLogStream( System.err );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    super.addNotify();

    final SignalDiagramComponent parent = ( SignalDiagramComponent )getParent();
    parent.getDndTargetController().addHandler( this.dropHandler );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    final SignalDiagramComponent parent = ( SignalDiagramComponent )getParent();
    parent.getDndTargetController().removeHandler( this.dropHandler );

    super.removeNotify();
  }

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

      final int dataWidth = dataModel.getWidth();

      // Determine which bits of the actual signal should be drawn...
      int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      int endBit = ( int )Math.min( dataWidth, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

      for ( int b = 0; b < dataWidth; b++ )
      {
        final int virtualRow = screenModel.toVirtualRow( b );
        if ( ( virtualRow < startBit ) || ( virtualRow > endBit ) )
        {
          // Trivial reject: we don't have to paint this row, as it is not asked
          // from us (due to clip boundaries)!
          continue;
        }

        canvas.setColor( screenModel.getChannelColor( b ) );

        final int mask = ( 1 << b );
        // determine where we really should draw the signal...
        final int dy = signalOffset + ( channelHeight * virtualRow );

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
