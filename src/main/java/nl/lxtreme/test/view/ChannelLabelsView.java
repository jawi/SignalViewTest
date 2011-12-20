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
 * Copyright (C) 2010-2011 - J.W. Janssen, <http://www.lxtreme.nl>
 */
package nl.lxtreme.test.view;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.util.logging.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.dnd.*;
import nl.lxtreme.test.view.dnd.DragAndDropTargetController.*;
import nl.lxtreme.test.view.laf.*;
import nl.lxtreme.test.view.model.*;
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

    // See #dragGestureRecognized for details about why this image exists...
    private final BufferedImage stubImage;

    // CONSTRUCTORS

    /**
     * Creates a new DragAndDropListener instance.
     */
    public DragAndDropListener()
    {
      this.stubImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
    }

    // METHODS

    /**
     * @param aDropRow
     * @return
     */
    private static Point createChannelDropPoint( final Point aPoint, final ChannelLabelsView aView,
        final Component aTargetComponent )
    {
      final ChannelLabelsViewModel model = aView.getModel();

      final int offset = model.findChannelVirtualOffset( aPoint );
      final int channelHeight = model.getChannelHeight();

      final Point dropPoint = new Point( 0, offset + channelHeight );

      SwingUtilities.convertPointToScreen( dropPoint, aView );
      SwingUtilities.convertPointFromScreen( dropPoint, aTargetComponent );

      return dropPoint;
    }

    /**
     * @param aComponent
     * @return
     */
    private static GhostGlassPane getGlassPane( final Component aComponent )
    {
      return ( GhostGlassPane )SwingUtilities.getRootPane( aComponent ).getGlassPane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragDropEnd( final DragSourceDropEvent aEvent )
    {
      if ( !DragAndDropLock.releaseLock( this ) )
      {
        return;
      }

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragGestureRecognized( final DragGestureEvent aEvent )
    {
      if ( DragAndDropLock.isLocked( this ) || !DragAndDropLock.obtainLock( this ) )
      {
        return;
      }

      final Point coordinate = ( Point )aEvent.getDragOrigin().clone();

      final ChannelLabelsView sourceComponent = ( ChannelLabelsView )aEvent.getComponent();
      final ChannelLabelsViewModel model = sourceComponent.getModel();

      final Channel channel = model.findChannel( coordinate );
      if ( channel == null )
      {
        DragAndDropLock.releaseLock( this );
        return;
      }

      final GhostGlassPane glassPane = getGlassPane( sourceComponent );

      final Point dropPoint = createChannelDropPoint( coordinate, sourceComponent, glassPane );

      final ChannelInsertionPointRenderer renderer = new ChannelInsertionPointRenderer();
      renderer.setContext( channel.getLabel() );

      glassPane.setDropPoint( dropPoint, renderer );
      glassPane.setVisible( true );
      glassPane.repaintPartially();

      // Use this version of the startDrag method as to avoid a potential error
      // on MacOS: without the explicit image, it will try to create one from
      // the component itself (= this SignalView), which can be as wide as
      // Integer.MAX_VALUE pixels. This can cause a numeric overflow in the
      // image routines causing a NegativeArraySizeException. By giving it
      // explicitly an image, it will use that one instead. This is not a
      // problem for this component, as the dragged cursor will be drawn on the
      // glasspane, not by the DnD routines of Java...
      aEvent.startDrag( DragSource.DefaultMoveDrop, this.stubImage, new Point( 0, 0 ), new ChannelTransferable(
          channel, new Point( 0, model.findChannelVirtualOffset( coordinate ) ) ), null /* dsl */);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragMouseMoved( final DragSourceDragEvent aEvent )
    {
      final Point coordinate = aEvent.getLocation();
      if ( !DragAndDropLock.isLocked( this ) || ( coordinate == null ) )
      {
        return;
      }

      final DragSourceContext dragSourceContext = aEvent.getDragSourceContext();

      final ChannelLabelsView sourceComponent = ( ChannelLabelsView )dragSourceContext.getComponent();
      final GhostGlassPane glassPane = getGlassPane( sourceComponent );

      SwingUtilities.convertPointFromScreen( coordinate, sourceComponent );

      final Point dropPoint = createChannelDropPoint( coordinate, sourceComponent, glassPane );

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
  }

  /**
   * Provides the D&D drop handler for accepting dropped channels.
   */
  final class DropHandler implements DragAndDropHandler
  {
    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptDrop( final SignalDiagramController aController, final DropTargetDropEvent aEvent )
    {
      boolean accepted = false;

      try
      {
        final Transferable transferable = aEvent.getTransferable();

        final Channel movedChannel = ( Channel )transferable.getTransferData( ChannelTransferable.CHANNEL_FLAVOR );
        final Point origLocation = ( Point )transferable.getTransferData( ChannelTransferable.LOCATION_FLAVOR );

        if ( movedChannel != null )
        {
          final ChannelLabelsViewModel model = getModel();

          final Channel insertChannel = model.findChannel( aEvent.getLocation() );

          if ( accepted = model.acceptChannel( movedChannel, insertChannel ) )
          {
            // Move the channel rows...
            model.moveChannelRows( movedChannel, insertChannel );

            // Update the screen accordingly...
            repaintAffectedAreas( aController, aEvent.getLocation(), origLocation );
          }
        }
      }
      catch ( Exception exception )
      {
        LOG.log( Level.WARNING, "Getting transfer data failed!", exception );
      }

      // Update the administration of the event whether or not we've accepted
      // or rejected the drop...
      if ( accepted )
      {
        aEvent.acceptDrop( DnDConstants.ACTION_MOVE );
      }
      else
      {
        aEvent.rejectDrop();
      }

      return accepted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataFlavor getFlavor()
    {
      return ChannelTransferable.CHANNEL_FLAVOR;
    }

    /**
     * Repaints the areas affected by the movement of the channels.
     * 
     * @param aController
     *          the {@link SignalDiagramController} to use;
     * @param aMovedChannel
     *          the channel that is moved;
     * @param aInsertChannel
     *          the new position of the moved channel.
     */
    private void repaintAffectedAreas( final SignalDiagramController aController, final Point aNewLocation,
        final Point aOldLocation )
    {
      final JScrollPane scrollPane = SwingUtils.getAncestorOfClass( JScrollPane.class, aController.getSignalDiagram() );
      if ( scrollPane != null )
      {
        final int channelHeight = getModel().getChannelHeight();

        final int oldRowY = aOldLocation.y - 3;
        final int newRowY = aNewLocation.y - 3;
        final int rowHeight = channelHeight + 6;

        // Update the signal display's view port; only the affected
        // regions... XXX this doesn't feel like the right approach!
        final JViewport viewport = scrollPane.getViewport();

        Rectangle rect = viewport.getVisibleRect();
        // ...old region...
        rect.y = oldRowY;
        rect.height = rowHeight;
        viewport.repaint( rect );
        // ...new region...
        rect.y = newRowY;
        viewport.repaint( rect );

        final JViewport channelLabelsView = scrollPane.getRowHeader();

        rect = channelLabelsView.getVisibleRect();
        // ...old region...
        rect.y = oldRowY;
        rect.height = rowHeight;
        channelLabelsView.repaint( rect );
        // ...new region...
        rect.y = newRowY;
        channelLabelsView.repaint( rect );
      }
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  static final Logger LOG = Logger.getLogger( ChannelLabelsView.class.getName() );

  // VARIABLES

  private final DropHandler dropHandler;
  private final ChannelLabelsViewModel model;
  private final DragAndDropListener dndListener;
  private final DragGestureRecognizer dragGestureRecognizer;

  // CONSTRUCTORS

  /**
   * Creates a new {@link ChannelLabelsView} instance.
   */
  private ChannelLabelsView( final SignalDiagramController aController )
  {
    super( aController );

    this.model = new ChannelLabelsViewModel( aController );

    this.dropHandler = new DropHandler();
    this.dndListener = new DragAndDropListener();

    final DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.addDragSourceMotionListener( this.dndListener );
    dragSource.addDragSourceListener( this.dndListener );

    this.dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE,
        this.dndListener );

    updateUI();
  }

  // METHODS

  /**
   * Creates a new {@link ChannelLabelsView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   * @return a new {@link ChannelLabelsView} instance, never <code>null</code>.
   */
  public static ChannelLabelsView create( final SignalDiagramController aController )
  {
    ChannelLabelsView result = new ChannelLabelsView( aController );
    return result;
  }

  /**
   * Installs all listeners and the support for DnD.
   * 
   * @see javax.swing.JComponent#addNotify()
   */
  @Override
  public void addNotify()
  {
    final DragAndDropTargetController dndTargetController = getDnDTargetController();
    dndTargetController.addHandler( this.dropHandler );

    setDropTarget( new DropTarget( this, dndTargetController ) );

    super.addNotify();
  }

  /**
   * Returns the current value of model.
   * 
   * @return the model
   */
  public ChannelLabelsViewModel getModel()
  {
    return this.model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    getDnDTargetController().removeHandler( this.dropHandler );

    DragSource dragSource = this.dragGestureRecognizer.getDragSource();
    if ( dragSource != null )
    {
      dragSource.removeDragSourceListener( this.dndListener );
      dragSource.removeDragSourceMotionListener( this.dndListener );
    }

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
    setUI( new ChannelLabelsUI() );
  }
}
