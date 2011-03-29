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

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.model.*;


/**
 * Provides a signal diagram, where signals in the form of sample data is
 * represented by channels.
 */
public class SignalDiagramComponent extends JPanel implements Scrollable
{
  // INNER TYPES

  /**
   * Listens for window events in order to set the proper dimensions for the
   * displayed views.
   */
  static final class ComponentSizeListener extends ComponentAdapter
  {
    // VARIABLES

    private final SignalDiagramController controller;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public ComponentSizeListener( final SignalDiagramController aController )
    {
      this.controller = aController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentResized( final ComponentEvent aEvent )
    {
      final Component component = aEvent.getComponent();
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            component.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

            final SignalDiagramController ctrl = ComponentSizeListener.this.controller;
            if ( ctrl.isZoomAll() )
            {
              ctrl.zoomAll();
            }
          }
          finally
          {
            component.setCursor( Cursor.getDefaultCursor() );
          }
        }
      } );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentShown( final ComponentEvent aEvent )
    {
      final Component component = aEvent.getComponent();
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            component.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

            // Ask the controller to redimension all contained components...
            ComponentSizeListener.this.controller.zoomAll();
          }
          finally
          {
            component.setCursor( Cursor.getDefaultCursor() );
          }
        }
      } );
    }
  }

  /**
   * Provides an mouse event listener to allow some of the functionality (such
   * as DnD and cursor dragging) of this component to be controlled with the
   * mouse.
   */
  static final class CursorMouseListener extends MouseAdapter
  {
    // CONSTANTS

    private static final Cursor DEFAULT = Cursor.getDefaultCursor();
    private static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    private static final Cursor CURSOR_MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );

    // VARIABLES

    private final SignalDiagramController controller;

    private volatile boolean showing = false;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public CursorMouseListener( final SignalDiagramController aController )
    {
      this.controller = aController;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked( final MouseEvent aEvent )
    {
      // TODO: this should be done through a context menu...
      if ( this.controller.isCursorMode() && ( aEvent.getClickCount() == 2 ) )
      {
        final Point point = aEvent.getPoint();
        this.controller.moveCursor( 3, point );
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved( final MouseEvent aEvent )
    {
      final Point point = aEvent.getPoint();

      if ( !this.showing )
      {
        if ( this.controller.isMeasurementMode() )
        {
          this.showing = this.controller.showHover( point );
          setMouseCursor( aEvent, CURSOR_HOVER );
        }
        else if ( this.controller.isCursorMode() )
        {
          if ( this.controller.findCursor( aEvent.getPoint() ) >= 0 )
          {
            setMouseCursor( aEvent, CURSOR_MOVE_CURSOR );
          }
          else
          {
            setMouseCursor( aEvent, DEFAULT );
          }
        }
      }
      else
      {
        if ( !this.controller.isMeasurementMode() )
        {
          this.showing = this.controller.hideHover();
          setMouseCursor( aEvent, DEFAULT );
        }
        else
        {
          setMouseCursor( aEvent, CURSOR_HOVER );
          this.controller.moveHover( point );
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased( final MouseEvent aEvent )
    {
      setMouseCursor( aEvent, DEFAULT );
    }

    /**
     * @param aEvent
     * @param aCursor
     */
    private void setMouseCursor( final MouseEvent aEvent, final Cursor aCursor )
    {
      if ( aEvent.getSource() instanceof JComponent )
      {
        ( ( JComponent )aEvent.getSource() ).setCursor( aCursor );
      }
    }
  }

  /**
   * Provides an mouse event listener to allow some of the functionality (such
   * as DnD and cursor dragging) of this component to be controlled with the
   * mouse.
   */
  static final class DragAndDropListener implements DragGestureListener, DragSourceMotionListener, DragSourceListener
  {
    // VARIABLES

    private final SignalDiagramController controller;
    private final SignalView signalView;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public DragAndDropListener( final SignalDiagramController aController, final SignalView aSignalView )
    {
      this.controller = aController;
      this.signalView = aSignalView;
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

      GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( this.signalView ).getGlassPane();

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
      if ( DragAndDropLock.isLocked() )
      {
        DragAndDropLock.setDragAndDropStarted( false );
        return;
      }

      // We're having the exclusive right to start moving a channel around...
      DragAndDropLock.setLocked( true );
      DragAndDropLock.setDragAndDropStarted( true );

      final Point coordinate = ( Point )aEvent.getDragOrigin().clone();

      int row = -1;
      int cursorIdx = this.controller.findCursor( coordinate );
      if ( !this.controller.isCursorMode() || ( cursorIdx < 0 ) )
      {
        row = this.controller.getSignalRow( coordinate );
      }

      // Check whether we've got either a row or a cursor in our dragging
      // context...
      if ( ( row < 0 ) && ( cursorIdx < 0 ) )
      {
        return;
      }

      final GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( this.signalView ).getGlassPane();

      if ( row >= 0 )
      {
        // We're starting to drag a channel row...
        glassPane.setDropPoint( createChannelDropPoint( coordinate, glassPane ), DragAndDropContext.CHANNEL_ROW );
        aEvent.startDrag( DragSource.DefaultMoveDrop, new ChannelRowTransferable( row ) );
      }
      else
      {
        // We're starting to drag a cursor...
        glassPane.setDropPoint( createCursorDropPoint( coordinate, glassPane ), DragAndDropContext.CURSOR );
        aEvent.startDrag( DragSource.DefaultCopyDrop, new CursorTransferable( cursorIdx ) );
      }

      glassPane.setVisible( true );
      glassPane.repaintPartially();
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

      this.controller.setSnapModeEnabled( isSnapModeKeyEvent( aEvent ) );

      final Point coordinate = ( Point )aEvent.getLocation().clone();
      SwingUtilities.convertPointFromScreen( coordinate, this.signalView );

      Point dropPoint;

      final GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( this.signalView ).getGlassPane();

      DragAndDropContext context = getContext( aEvent );
      if ( DragAndDropContext.CHANNEL_ROW == context )
      {
        dropPoint = createChannelDropPoint( coordinate, glassPane );
      }
      else
      {
        dropPoint = createCursorDropPoint( coordinate, glassPane );
      }

      glassPane.setDropPoint( dropPoint, context );
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
    private Point createChannelDropPoint( final Point aPoint, final JComponent aTargetComponent )
    {
      final ScreenModel screenModel = this.controller.getScreenModel();
      final SampleDataModel dataModel = this.controller.getDataModel();

      final int signalWidth = dataModel.getWidth();
      final int channelHeight = screenModel.getChannelHeight();

      final int dropRow = Math.max( 0, Math.min( signalWidth, ( int )( aPoint.y / ( double )channelHeight ) ) );

      final Point dropPoint = new Point( 0, ( dropRow + 1 ) * channelHeight );

      SwingUtilities.convertPointToScreen( dropPoint, this.signalView );
      SwingUtilities.convertPointFromScreen( dropPoint, aTargetComponent );

      return dropPoint;
    }

    /**
     * @param aPoint
     * @param aTargetComponent
     * @return
     */
    private Point createCursorDropPoint( final Point aPoint, final JComponent aTargetComponent )
    {
      final Point dropPoint = new Point( aPoint.x, 0 );

      if ( this.controller.isSnapModeEnabled() )
      {
        final SignalHoverInfo signalHover = this.controller.getSignalHover( aPoint );
        if ( signalHover != null )
        {
          dropPoint.x = signalHover.getRectangle().x;
        }
      }

      SwingUtilities.convertPointToScreen( dropPoint, this.signalView );
      SwingUtilities.convertPointFromScreen( dropPoint, aTargetComponent );

      return dropPoint;
    }

    /**
     * @param aEvent
     * @return
     */
    private DragAndDropContext getContext( final DragSourceEvent aEvent )
    {
      final Transferable transferable = aEvent.getDragSourceContext().getTransferable();
      if ( transferable instanceof CursorTransferable )
      {
        return DragAndDropContext.CURSOR;
      }
      return DragAndDropContext.CHANNEL_ROW;
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
   * Provides an keyboard event listener to allow some of the functionality
   * (such as: zooming) of this component to be controlled from the keyboard.
   */
  static final class KeyboardControlListener extends KeyAdapter
  {
    // VARIABLES

    private final SignalDiagramController controller;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public KeyboardControlListener( final SignalDiagramController aController )
    {
      this.controller = aController;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyTyped( final KeyEvent aEvent )
    {
      if ( aEvent.getID() != KeyEvent.KEY_TYPED )
      {
        return;
      }

      if ( ( '+' == aEvent.getKeyChar() ) || ( '=' == aEvent.getKeyChar() ) )
      {
        this.controller.zoomIn();
      }
      else if ( ( '-' == aEvent.getKeyChar() ) || ( '_' == aEvent.getKeyChar() ) )
      {
        this.controller.zoomOut();
      }
      else if ( '0' == aEvent.getKeyChar() )
      {
        this.controller.zoomAll();
      }
      else if ( '1' == aEvent.getKeyChar() )
      {
        this.controller.zoomOriginal();
      }
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final boolean DEBUG = false;

  // VARIABLES

  private final SignalDiagramController controller;

  private final SignalView signalView;
  private final CursorView cursorView;
  private final MeasurementView measurementView;

  private DragAndDropTargetController dndTargetController;

  private ComponentSizeListener componentSizeListener;
  private CursorMouseListener cursorMouseListener;
  private DragAndDropListener dndListener;
  private KeyboardControlListener keyboardListener;

  // CONSTRUCTORS

  /**
   * Creates a new SampleViewComponent instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public SignalDiagramComponent( final SignalDiagramController aController )
  {
    super( new StackLayout() );

    this.controller = aController;

    this.signalView = new SignalView( this.controller );
    this.cursorView = new CursorView( this.controller );
    this.measurementView = new MeasurementView( this.controller );

    add( this.signalView, StackLayout.TOP );
    add( this.cursorView, StackLayout.TOP );
    add( this.measurementView, StackLayout.TOP );

    this.controller.setSignalDiagram( this );
  }

  // METHODS

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
      final Container parent = getParent();
      final Container window = SwingUtilities.getWindowAncestor( parent );

      final JRootPane rootPane = SwingUtilities.getRootPane( parent );
      rootPane.setGlassPane( new GhostGlassPane() );

      this.componentSizeListener = new ComponentSizeListener( this.controller );
      this.keyboardListener = new KeyboardControlListener( this.controller );

      window.addComponentListener( this.componentSizeListener );
      window.addKeyListener( this.keyboardListener );

      this.cursorMouseListener = new CursorMouseListener( this.controller );

      addMouseListener( this.cursorMouseListener );
      addMouseMotionListener( this.cursorMouseListener );

      this.dndListener = new DragAndDropListener( this.controller, this.signalView );

      final DragSource dragSource = DragSource.getDefaultDragSource();
      dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_COPY_OR_MOVE, this.dndListener );
      dragSource.addDragSourceMotionListener( this.dndListener );
      dragSource.addDragSourceListener( this.dndListener );

      this.dndTargetController = new DragAndDropTargetController( this.controller );
      setDropTarget( new DropTarget( this, this.dndTargetController ) );

      configureEnclosingScrollPane();
    }
    finally
    {
      super.addNotify();
    }
  }

  /**
   * /** {@inheritDoc}
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
      return 50;
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
    return true;
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
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics aGraphics )
  {
    if ( DEBUG )
    {
      final long startTime = System.nanoTime();
      try
      {
        super.paint( aGraphics );
      }
      finally
      {
        final long endTime = System.nanoTime();
        final long renderTime = endTime - startTime;
        System.out.println( "Rendering time = " + Utils.displayTime( renderTime / 1.0e9 ) );
      }
    }
    else
    {
      super.paint( aGraphics );
    }
  }

  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  @Override
  public void removeNotify()
  {
    try
    {
      unconfigureEnclosingScrollPane();

      final Container parent = getParent();
      final Container window = SwingUtilities.getWindowAncestor( parent );

      if ( this.componentSizeListener != null )
      {
        window.removeComponentListener( this.componentSizeListener );
        this.componentSizeListener = null;
      }
      if ( this.keyboardListener != null )
      {
        window.removeKeyListener( this.keyboardListener );
        this.keyboardListener = null;
      }

      if ( this.cursorMouseListener != null )
      {
        removeMouseListener( this.cursorMouseListener );
        removeMouseMotionListener( this.cursorMouseListener );
        this.cursorMouseListener = null;
      }

      final DragSource dragSource = DragSource.getDefaultDragSource();
      if ( this.dndListener != null )
      {
        dragSource.removeDragSourceListener( this.dndListener );
        dragSource.removeDragSourceMotionListener( this.dndListener );
        this.dndListener = null;
      }
    }
    finally
    {
      super.removeNotify();
    }
  }

  /**
   * @return the cursorView
   */
  final CursorView getCursorView()
  {
    return this.cursorView;
  }

  /**
   * @return the dndTargetController
   */
  final DragAndDropTargetController getDndTargetController()
  {
    return this.dndTargetController;
  }

  /**
   * @return the arrowView
   */
  final MeasurementView getMeasurementView()
  {
    return this.measurementView;
  }

  /**
   * Returns the actual signal view component.
   * 
   * @return a signal view component, never <code>null</code>.
   */
  final SignalView getSignalView()
  {
    return this.signalView;
  }

  /**
   * If this component is the <code>viewportView</code> of an enclosing
   * <code>JScrollPane</code> (the usual situation), configure this
   * <code>ScrollPane</code> by, amongst other things, installing the diagram's
   * <code>timeline</code> as the <code>columnHeaderView</code> of the scroll
   * pane.
   * 
   * @see #addNotify
   */
  private void configureEnclosingScrollPane()
  {
    final Container p = getParent();
    if ( p instanceof JViewport )
    {
      final Container gp = p.getParent();
      if ( gp instanceof JScrollPane )
      {
        final JScrollPane scrollPane = ( JScrollPane )gp;
        // Make certain we are the viewPort's view and not, for
        // example, the rowHeaderView of the scrollPane -
        // an implementor of fixed columns might do this.
        final JViewport viewport = scrollPane.getViewport();
        if ( ( viewport == null ) || ( viewport.getView() != this ) )
        {
          return;
        }

        final TimeLineView timelineView = new TimeLineView( this.controller );
        timelineView.addMouseListener( this.cursorMouseListener );

        scrollPane.setColumnHeaderView( timelineView );
        scrollPane.setRowHeaderView( new ChannelLabelsView( this.controller ) );

        scrollPane.setCorner( ScrollPaneConstants.UPPER_LEADING_CORNER, new JPanel() );
      }
    }
  }

  /**
   * Reverses the effect of <code>configureEnclosingScrollPane</code> by
   * replacing the <code>columnHeaderView</code> of the enclosing scroll pane
   * with <code>null</code>.
   * 
   * @see #removeNotify
   * @see #configureEnclosingScrollPane
   */
  private void unconfigureEnclosingScrollPane()
  {
    final Container p = getParent();
    if ( p instanceof JViewport )
    {
      final Container gp = p.getParent();
      if ( gp instanceof JScrollPane )
      {
        final JScrollPane scrollPane = ( JScrollPane )gp;
        // Make certain we are the viewPort's view and not, for
        // example, the rowHeaderView of the scrollPane -
        // an implementor of fixed columns might do this.
        final JViewport viewport = scrollPane.getViewport();
        if ( ( viewport == null ) || ( viewport.getView() != this ) )
        {
          return;
        }

        scrollPane.getColumnHeader().removeMouseListener( this.cursorMouseListener );

        scrollPane.setColumnHeaderView( null );
        scrollPane.setRowHeaderView( null );
        scrollPane.setCorner( ScrollPaneConstants.UPPER_LEADING_CORNER, null );
      }
    }
  }
}
