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
import java.awt.event.*;
import java.awt.image.*;
import java.util.logging.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.dnd.DragAndDropTargetController.DragAndDropHandler;
import nl.lxtreme.test.view.laf.*;
import nl.lxtreme.test.view.model.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides a view for the signal data as individual channels.
 */
public class SignalView extends AbstractViewLayer implements IMeasurementListener, ICursorChangeListener
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
        DragAndDropLock.releaseLock( this );
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

      // Use this version of the startDrag method as to avoid a potential error
      // on MacOS: without the explicit image, it will try to create one from
      // the component itself (= this SignalView), which can be as wide as
      // Integer.MAX_VALUE pixels. This can cause a numeric overflow in the
      // image routines causing a NegativeArraySizeException. By giving it
      // explicitly an image, it will use that one instead. This is not a
      // problem for this component, as the dragged cursor will be drawn on the
      // glasspane, not by the DnD routines of Java...
      final BufferedImage stubImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
      aEvent.startDrag( DragSource.DefaultMoveDrop, stubImage, new Point( 0, 0 ), new CursorTransferable( cursorIdx ),
          null /* dsl */);
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

      final int cursorIdx = ( ( CursorTransferable )dragSourceContext.getTransferable() ).getCursorIdx();
      final long timestamp = this.controller.locationToTimestamp( coordinate );
      final String cursorFlag = this.controller.getCursorFlagText( cursorIdx, timestamp );

      final Point dropPoint = createCursorDropPoint( coordinate, sourceComponent, glassPane );

      if ( this.controller.isSnapModeEnabled() )
      {
        Point cursorSnapPoint = createCursorSnapPoint( coordinate, sourceComponent, glassPane );

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
      this.controller.setSnapModeEnabled( isSnapModeKeyEvent( aEvent ) );
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
      return SwingUtilities.convertPoint( aSourceComponent, dropPoint, aTargetComponent );
    }

    /**
     * @param aPoint
     * @param aSourceComponent
     * @param aTargetComponent
     * @return
     */
    private Point createCursorSnapPoint( final Point aPoint, final Component aSourceComponent,
        final Component aTargetComponent )
    {
      Point cursorSnapPoint = this.controller.getCursorSnapPoint( aPoint );
      return SwingUtilities.convertPoint( aSourceComponent, cursorSnapPoint, aTargetComponent );
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

  private static final Logger LOG = Logger.getLogger( SignalView.class.getName() );

  // VARIABLES

  private final DropHandler dropHandler;
  private final SignalViewModel model;
  private final DragAndDropListener dndListener;
  private final DragGestureRecognizer dragGestureRecognizer;

  // CONSTRUCTORS

  /**
   * Creates a new {@link SignalView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  private SignalView( final SignalDiagramController aController )
  {
    super( aController );

    this.model = new SignalViewModel( aController );

    this.dropHandler = new DropHandler();
    this.dndListener = new DragAndDropListener( aController );

    final DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.addDragSourceMotionListener( this.dndListener );
    dragSource.addDragSourceListener( this.dndListener );

    this.dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE,
        this.dndListener );

    updateUI();
  }

  // METHODS

  /**
   * Creates a new {@link SignalView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   * @return a {@link SignalView} instance, never <code>null</code>.
   */
  public static SignalView create( final SignalDiagramController aController )
  {
    SignalView result = new SignalView( aController );

    aController.addCursorChangeListener( result );
    aController.addMeasurementListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    final DragAndDropTargetController dndTargetController = getDnDTargetController();
    dndTargetController.addHandler( this.dropHandler );

    setDropTarget( new DropTarget( this, dndTargetController ) );

    updateUI();

    super.addNotify();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorAdded( final int aCursorIdx, final long aCursorTimestamp )
  {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorChanged( final int aCursorIdx, final long aCursorTimestamp )
  {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorRemoved( final int aCursorIdx )
  {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsInvisible()
  {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsVisible()
  {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disableMeasurementMode()
  {
    final SignalUI signalUI = ( SignalUI )this.ui;

    final Rectangle oldRect = signalUI.getMeasurementRect();
    if ( oldRect != null )
    {
      repaint( oldRect );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enableMeasurementMode()
  {
    // Nothing special to do for this event...
  }

  /**
   * Returns the current value of model.
   * 
   * @return the model
   */
  public SignalViewModel getModel()
  {
    return this.model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMeasureEvent( final SignalHoverInfo aEvent )
  {
    final SignalUI signalUI = ( SignalUI )this.ui;

    final Rectangle oldRect = signalUI.getMeasurementRect();

    signalUI.handleMeasureEvent( aEvent );

    final Rectangle newRect = signalUI.getMeasurementRect();

    if ( aEvent != null )
    {
      setToolTipText( aEvent.toHtmlString() );
    }
    else
    {
      setToolTipText( null );
    }

    if ( oldRect != null )
    {
      repaint( oldRect );
    }
    if ( newRect != null )
    {
      repaint( newRect );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isListening()
  {
    return ( ( SignalUI )this.ui ).isListening();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    final DragAndDropTargetController dndTargetController = getDnDTargetController();

    dndTargetController.removeHandler( this.dropHandler );

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
    setUI( new SignalUI() );
  }
}
