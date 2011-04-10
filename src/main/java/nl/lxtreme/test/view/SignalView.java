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


import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.logging.*;

import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.dnd.DragAndDropTargetController.DragAndDropHandler;
import nl.lxtreme.test.view.laf.*;


/**
 * Provides a view for the signal data as individual channels.
 */
public class SignalView extends AbstractViewLayer
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
          final int newRealRow = aController.getChannelRow( aEvent.getLocation() );

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
    super( aController );

    this.dropHandler = new DropHandler();

    updateUI();
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
   * Overridden in order to set a custom UI, which not only paints this diagram,
   * but also can be used to manage the various settings, such as colors,
   * height, and so on.
   * 
   * @see javax.swing.JComponent#updateUI()
   */
  @Override
  public final void updateUI()
  {
    setUI( new SignalUI() );
  }
}
