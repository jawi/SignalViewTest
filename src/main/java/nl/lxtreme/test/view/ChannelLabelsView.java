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

import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.dnd.DragAndDropTargetController.*;
import nl.lxtreme.test.view.laf.*;
import nl.lxtreme.test.view.renderer.*;


/**
 * Provides a view for the channel labels.
 */
public class ChannelLabelsView extends AbstractViewLayer
{
  // INNER TYPES

  /**
   * Provides an mouse event listener to allow some of the functionality (such
   * as DnD and cursor dragging) of this component to be controlled with the
   * mouse.
   */
  static final class DragAndDropListener implements DragGestureListener, DragSourceMotionListener, DragSourceListener
  {
    // VARIABLES

    private final SignalDiagramController controller;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public DragAndDropListener( final SignalDiagramController aController )
    {
      this.controller = aController;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragDropEnd( final DragSourceDropEvent aEvent )
    {
      if ( !DragAndDropLock.isDragAndDropStarted() )
      {
        return;
      }

      DragAndDropLock.setDragAndDropStarted( false );
      DragAndDropLock.setLocked( false );

      final GhostGlassPane glassPane = getGlassPane( aEvent.getDragSourceContext().getComponent() );

      glassPane.clearDropPoint();
      glassPane.repaint();

      glassPane.setVisible( false );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragEnter( final DragSourceDragEvent aEvent )
    {
      // NO-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragExit( final DragSourceEvent aEvent )
    {
      // NO-op
      DragAndDropLock.setLocked( false );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragGestureRecognized( final DragGestureEvent aEvent )
    {
      if ( DragAndDropLock.isLocked() )
      {
        return;
      }

      final Point coordinate = ( Point )aEvent.getDragOrigin().clone();

      int channelRow = this.controller.getChannelRow( coordinate );

      // Check whether we've got either a row or a cursor in our dragging
      // context...
      if ( channelRow < 0 )
      {
        return;
      }

      final Component sourceComponent = getSignalView( aEvent.getComponent() );
      if ( sourceComponent != null )
      {
        final GhostGlassPane glassPane = getGlassPane( aEvent.getComponent() );

        final Point dropPoint = createChannelDropPoint( coordinate, sourceComponent, glassPane );

        glassPane.setDropPoint( dropPoint, new ChannelInsertionPointRenderer() );
        glassPane.setVisible( true );
        glassPane.repaintPartially();

        // Ensure we're having the exclusive right to start moving a channel
        // around...
        DragAndDropLock.setLocked( true );
        DragAndDropLock.setDragAndDropStarted( true );

        aEvent.startDrag( DragSource.DefaultMoveDrop, new ChannelRowTransferable( channelRow ) );
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragMouseMoved( final DragSourceDragEvent aEvent )
    {
      if ( !DragAndDropLock.isDragAndDropStarted() )
      {
        return;
      }

      final DragSourceContext dragSourceContext = aEvent.getDragSourceContext();
      final Point coordinate = aEvent.getLocation();

      if ( ( coordinate == null ) || !( dragSourceContext.getTransferable() instanceof ChannelRowTransferable ) )
      {
        return;
      }

      final GhostGlassPane glassPane = getGlassPane( dragSourceContext.getComponent() );
      final Component signalView = getSignalView( dragSourceContext.getComponent() );

      SwingUtilities.convertPointFromScreen( coordinate, signalView );

      final Point dropPoint = createChannelDropPoint( coordinate, signalView, glassPane );

      glassPane.setDropPoint( dropPoint );
      glassPane.repaintPartially();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragOver( final DragSourceDragEvent aEvent )
    {
      // NO-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropActionChanged( final DragSourceDragEvent aEvent )
    {
      // NO-op
    }

    /**
     * @param aDropRow
     * @return
     */
    private Point createChannelDropPoint( final Point aPoint, final Component aSourceComponent,
        final Component aTargetComponent )
    {
      final Point dropPoint = this.controller.getChannelDropPoint( aPoint );

      SwingUtilities.convertPointToScreen( dropPoint, aSourceComponent );
      SwingUtilities.convertPointFromScreen( dropPoint, aTargetComponent );

      return dropPoint;
    }

    /**
     * @param aComponent
     * @return
     */
    private GhostGlassPane getGlassPane( final Component aComponent )
    {
      return ( GhostGlassPane )SwingUtilities.getRootPane( aComponent ).getGlassPane();
    }

    /**
     * @param aComponent
     * @return
     */
    private SignalView getSignalView( final Component aComponent )
    {
      SignalDiagramComponent signalDiagram = getSignalDiagramComponent( aComponent );
      if ( signalDiagram == null )
      {
        return null;
      }

      return signalDiagram.getSignalView();
    }
  }

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

  private static final Logger LOG = Logger.getLogger( ChannelLabelsView.class.getName() );

  // VARIABLES

  private DragAndDropListener dndListener;
  private final DropHandler dropHandler;

  // CONSTRUCTORS

  /**
   * Creates a new {@link ChannelLabelsView} instance.
   */
  public ChannelLabelsView( final SignalDiagramController aController )
  {
    super( aController );

    this.dropHandler = new DropHandler();

    updateUI();
  }

  // METHODS

  /**
   * @param aComponent
   * @return
   */
  static SignalDiagramComponent getSignalDiagramComponent( final Component aComponent )
  {
    JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class, aComponent );
    if ( scrollPane == null )
    {
      return null;
    }

    JComponent mainViewComponent = ( JComponent )scrollPane.getViewport().getView();
    if ( mainViewComponent instanceof SignalDiagramComponent )
    {
      return ( SignalDiagramComponent )mainViewComponent;
    }
    return null;
  }

  /**
   * Installs all listeners and the support for DnD.
   * 
   * @see javax.swing.JComponent#addNotify()
   */
  @Override
  public void addNotify()
  {
    try
    {
      this.dndListener = new DragAndDropListener( getController() );

      final DragSource dragSource = DragSource.getDefaultDragSource();
      dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this.dndListener );
      dragSource.addDragSourceMotionListener( this.dndListener );
      dragSource.addDragSourceListener( this.dndListener );

      final SignalDiagramComponent parent = getSignalDiagramComponent( getParent() );
      if ( parent != null )
      {
        final DragAndDropTargetController dndTargetController = parent.getDndTargetController();

        dndTargetController.addHandler( this.dropHandler );

        setDropTarget( new DropTarget( this, dndTargetController ) );
      }
    }
    finally
    {
      super.addNotify();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    try
    {
      final SignalDiagramComponent parent = getSignalDiagramComponent( getParent() );
      if ( parent != null )
      {
        parent.getDndTargetController().removeHandler( this.dropHandler );
      }
    }
    finally
    {
      super.removeNotify();
    }
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
    setUI( new ChannelLabelsUI() );
  }
}
