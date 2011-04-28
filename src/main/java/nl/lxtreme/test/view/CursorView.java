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
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;

import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.dnd.DragAndDropTargetController.DragAndDropHandler;
import nl.lxtreme.test.view.laf.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides a component for displaying cursors + their timing information.
 */
public class CursorView extends AbstractViewLayer
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

      int cursorIdx = this.controller.findCursor( coordinate );
      if ( !this.controller.isCursorMode() || ( cursorIdx < 0 ) )
      {
        return;
      }

      final Component sourceComponent = aEvent.getComponent();
      final GhostGlassPane glassPane = getGlassPane( sourceComponent );

      final Point dropPoint = createCursorDropPoint( coordinate, sourceComponent, glassPane );

      // We're starting to drag a cursor...
      final Renderer renderer = new CursorFlagRenderer();
      renderer.setContext( this.controller.getCursorFlagText( cursorIdx ) );

      glassPane.setDropPoint( dropPoint, renderer );
      glassPane.setVisible( true );
      glassPane.repaintPartially();

      aEvent.startDrag( DragSource.DefaultMoveDrop, new CursorTransferable( cursorIdx ) );
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

      final Component sourceComponent = dragSourceContext.getComponent();
      final GhostGlassPane glassPane = getGlassPane( sourceComponent );

      SwingUtilities.convertPointFromScreen( coordinate, sourceComponent );

      this.controller.setSnapModeEnabled( isSnapModeKeyEvent( aEvent ) );

      final int cursorIdx = ( ( CursorTransferable )dragSourceContext.getTransferable() ).getCursorIdx();
      final long timestamp = this.controller.locationToTimestamp( coordinate );
      final String cursorFlag = this.controller.getCursorFlagText( cursorIdx, timestamp );

      final Point dropPoint = createCursorDropPoint( coordinate, sourceComponent, glassPane );

      if ( this.controller.isSnapModeEnabled() )
      {
        final Point cursorSnapPoint = this.controller.getCursorSnapPoint( coordinate );
        SwingUtilities.convertPoint( sourceComponent, cursorSnapPoint, glassPane );

        glassPane.setRenderContext( cursorFlag, cursorSnapPoint );
      }
      else
      {
        glassPane.setRenderContext( cursorFlag );
      }

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
      System.out.println( "dropActionChanged!" );
    }

    /**
     * @param aPoint
     * @param aTargetComponent
     * @return
     */
    private Point createCursorDropPoint( final Point aPoint, final Component aSourceComponent,
        final Component aTargetComponent )
    {
      final Point dropPoint = this.controller.getCursorDropPoint( aPoint );

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
     * @param aEvent
     * @return
     */
    private boolean isSnapModeKeyEvent( final DragSourceDragEvent aEvent )
    {
      return ( aEvent.getGestureModifiersEx() & InputEvent.SHIFT_DOWN_MASK ) == InputEvent.SHIFT_DOWN_MASK;
    }
  }

  /**
   * Provides the D&D drop controller for accepting dropped cursors.
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

        Integer cursorValue = ( Integer )transferable.getTransferData( CursorTransferable.FLAVOR );
        if ( cursorValue != null )
        {
          aEvent.acceptDrop( DnDConstants.ACTION_MOVE );

          final int cursorIdx = cursorValue.intValue();
          final Point newLocation = aEvent.getLocation();

          // Move the cursor position...
          aController.moveCursor( cursorIdx, newLocation );

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
     * {@inheritDoc}
     */
    @Override
    public DataFlavor getFlavor()
    {
      return CursorTransferable.FLAVOR;
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger( CursorView.class.getName() );

  // VARIABLES

  private final DropHandler dropHandler;

  private DragAndDropListener dndListener;

  // CONSTRUCTORS

  /**
   * Creates a new CursorView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public CursorView( final SignalDiagramController aController )
  {
    super( aController );
    setOpaque( false );

    this.dropHandler = new DropHandler();
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    final DragAndDropTargetController dndTargetController = getDnDTargetController();

    this.dndListener = new DragAndDropListener( getController() );

    final DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this.dndListener );
    dragSource.addDragSourceMotionListener( this.dndListener );
    dragSource.addDragSourceListener( this.dndListener );

    dndTargetController.addHandler( this.dropHandler );

    setDropTarget( new DropTarget( this, dndTargetController ) );

    updateUI();

    super.addNotify();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    final DragAndDropTargetController dndTargetController = getDnDTargetController();

    dndTargetController.removeHandler( this.dropHandler );

    if ( this.dndListener != null )
    {
      final DragSource dragSource = DragSource.getDefaultDragSource();
      dragSource.removeDragSourceListener( this.dndListener );
      dragSource.removeDragSourceMotionListener( this.dndListener );
      this.dndListener = null;
    }

    setUI( null );

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
    setUI( new CursorUI() );
  }
}
