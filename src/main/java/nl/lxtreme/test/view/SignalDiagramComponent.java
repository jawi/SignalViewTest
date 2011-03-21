/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;
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
    // VARIABLES

    private int lastCursor = -1;
    private boolean showing = false;
    private final SignalDiagramController controller;

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
    public void mouseDragged( final MouseEvent aEvent )
    {
      if ( this.controller.isCursorMode() )
      {
        if ( this.lastCursor < 0 )
        {
          return;
        }

        setCursor( aEvent, CURSOR_MOVE_CURSOR );
        this.controller.dragCursor( this.lastCursor, aEvent.getPoint(), aEvent.isAltDown() );
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
          setCursor( aEvent, CURSOR_HOVER );
        }
        else if ( this.controller.isCursorMode() )
        {
          if ( this.controller.findCursor( aEvent.getPoint() ) >= 0 )
          {
            setCursor( aEvent, CURSOR_MOVE_CURSOR );
          }
          else
          {
            setCursor( aEvent, DEFAULT );
          }
        }
      }
      else
      {
        if ( !this.controller.isMeasurementMode() )
        {
          this.showing = this.controller.hideHover();
          setCursor( aEvent, DEFAULT );
        }
        else
        {
          setCursor( aEvent, CURSOR_HOVER );
          this.controller.moveHover( point );
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed( final MouseEvent aEvent )
    {
      this.lastCursor = this.controller.findCursor( aEvent.getPoint() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased( final MouseEvent aEvent )
    {
      setCursor( aEvent, DEFAULT );
      if ( this.lastCursor < 0 )
      {
        return;
      }
      this.lastCursor = -1;
    }

    /**
     * @param aEvent
     * @param aCursor
     */
    private void setCursor( final MouseEvent aEvent, final Cursor aCursor )
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
    public DragAndDropListener( final SignalDiagramController aController )
    {
      this.controller = aController;
      this.signalView = aController.getSignalView();
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

      GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( this.signalView ).getGlassPane();

      final Point startPt = ( Point )aEvent.getLocation().clone();
      SwingUtilities.convertPointFromScreen( startPt, this.signalView );

      glassPane.setDropChannelPoint( startPt );
      glassPane.repaintPartially();
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
      final int row = this.controller.getSignalRow( coordinate );

      aEvent.startDrag( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ), new ChannelRowTransferable( row ) );

      final GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( this.signalView ).getGlassPane();
      glassPane.setDropChannelPoint( createChannelDropPoint( coordinate, glassPane ) );
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

      final Point cursor = ( Point )aEvent.getLocation().clone();
      SwingUtilities.convertPointFromScreen( cursor, this.signalView );

      final GhostGlassPane glassPane = ( GhostGlassPane )SwingUtilities.getRootPane( this.signalView ).getGlassPane();
      glassPane.setDropChannelPoint( createChannelDropPoint( cursor, glassPane ) );
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
    public void keyPressed( final KeyEvent aEvent )
    {
      if ( isSnapModeKeyEvent( aEvent ) )
      {
        this.controller.enableSnapMode();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyReleased( final KeyEvent aEvent )
    {
      if ( this.controller.isSnapModeEnabled() )
      {
        this.controller.disableSnapMode();
      }
    }

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

    /**
     * @param aEvent
     * @return
     */
    private boolean isSnapModeKeyEvent( final KeyEvent aEvent )
    {
      if ( this.controller.isSnapModeEnabled() )
      {
        return false;
      }

      return ( aEvent.isAltDown() || aEvent.isAltGraphDown() ) && aEvent.isControlDown();
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final boolean DEBUG = false;

  private static final Cursor DEFAULT = Cursor.getDefaultCursor();
  private static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
  private static final Cursor CURSOR_MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );

  // VARIABLES

  private final SignalDiagramController controller;

  private final SignalView signalView;
  private final CursorView cursorView;
  private final ArrowView arrowView;

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
    this.arrowView = new ArrowView( this.controller );

    add( this.signalView, StackLayout.TOP );
    add( this.cursorView, StackLayout.TOP );
    add( this.arrowView, StackLayout.TOP );
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

      this.componentSizeListener = new ComponentSizeListener( this.controller );
      this.keyboardListener = new KeyboardControlListener( this.controller );

      window.addComponentListener( this.componentSizeListener );
      window.addKeyListener( this.keyboardListener );

      this.cursorMouseListener = new CursorMouseListener( this.controller );

      addMouseListener( this.cursorMouseListener );
      addMouseMotionListener( this.cursorMouseListener );

      this.dndListener = new DragAndDropListener( this.controller );

      final DragSource dragSource = DragSource.getDefaultDragSource();
      dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this.dndListener );
      dragSource.addDragSourceMotionListener( this.dndListener );
      dragSource.addDragSourceListener( this.dndListener );

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
    return false;
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

        scrollPane.setColumnHeaderView( new TimeLineView( this.controller ) );
        scrollPane.setRowHeaderView( new ChannelLabelsView( this.controller ) );
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
        scrollPane.setColumnHeaderView( null );
        scrollPane.setRowHeaderView( null );
        scrollPane.setCorner( ScrollPaneConstants.UPPER_LEADING_CORNER, null );
      }
    }
  }
}
