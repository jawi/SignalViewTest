/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.test.dnd.*;


/**
 * @author jawi
 */
public class SampleViewComponent extends JPanel implements Scrollable
{
  // INNER TYPES

  /**
   * @author jajans
   */
  static final class MyComponentListener extends ComponentAdapter
  {
    // VARIABLES

    private final ScreenController controller;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public MyComponentListener( final ScreenController aController )
    {
      this.controller = aController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentResized( final ComponentEvent aEvent )
    {
      this.controller.recalculateDimensions();
      System.out.println( "resize!" );
    }
  }

  /**
   * @author jajans
   */
  static final class MyKeyListener extends KeyAdapter
  {
    // VARIABLES

    private final ScreenController controller;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public MyKeyListener( final ScreenController aController )
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
      if ( aEvent.isAltDown() && !this.controller.isSnapModeEnabled() )
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
        final Component comp = SwingUtilities.getRootPane( aEvent.getComponent() );
        this.controller.zoomAll( comp.getSize() );
      }
      else if ( '1' == aEvent.getKeyChar() )
      {
        this.controller.zoomOriginal();
      }
    }
  }

  /**
   * @author jajans
   */
  static final class MyMouseListener extends MouseAdapter implements DragGestureListener
  {
    // VARIABLES

    private int lastCursor = -1;
    private boolean showing = false;
    private final ScreenController controller;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public MyMouseListener( final ScreenController aController )
    {
      this.controller = aController;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void dragGestureRecognized( final DragGestureEvent aEvent )
    {
      if ( this.showing || ( this.lastCursor >= 0 ) || aEvent.getTriggerEvent().isAltDown()
          || aEvent.getTriggerEvent().isControlDown() )
      {
        return;
      }

      final Point coordinate = ( Point )aEvent.getDragOrigin().clone();
      final int row = this.controller.getSignalRow( coordinate );

      aEvent.startDrag( Cursor.getDefaultCursor(), new SampleRowTransferable( row ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged( final MouseEvent aEvent )
    {
      if ( this.lastCursor < 0 )
      {
        return;
      }

      setCursor( aEvent, CURSOR_MOVE_CURSOR );
      this.controller.dragCursor( this.lastCursor, aEvent.getPoint(), aEvent.isAltDown() );
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
        if ( aEvent.isControlDown() )
        {
          this.showing = this.controller.showHover( point );
          setCursor( aEvent, CURSOR_HOVER );
        }
        else
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
        if ( !aEvent.isControlDown() )
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

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final Cursor DEFAULT = Cursor.getDefaultCursor();
  private static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
  private static final Cursor CURSOR_MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );

  // VARIABLES

  private final ScreenController controller;

  private final ModelView modelView;
  private final CursorView cursorView;
  private final ArrowView arrowView;

  // CONSTRUCTORS

  /**
   * Creates a new SampleViewComponent instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public SampleViewComponent( final ScreenController aController )
  {
    super( new StackLayout() );

    this.controller = aController;

    this.modelView = new ModelView( this.controller );
    this.cursorView = new CursorView( this.controller );
    this.arrowView = new ArrowView( this.controller );

    add( this.modelView, StackLayout.TOP );
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
    final Container parent = getParent();

    final Container window = SwingUtilities.getWindowAncestor( parent );
    window.addComponentListener( new MyComponentListener( this.controller ) );
    window.addKeyListener( new MyKeyListener( this.controller ) );

    final MyMouseListener listener = new MyMouseListener( this.controller );
    addMouseListener( listener );
    addMouseMotionListener( listener );

    final DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_COPY, listener );

    super.addNotify();
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
    final long startTime = System.nanoTime();
    try
    {
      super.paint( aGraphics );
    }
    finally
    {
      final long endTime = System.nanoTime();
      final long renderTime = endTime - startTime;
      System.out.print( "Rendering time = " );
      if ( renderTime >= 1000000.0 )
      {
        System.out.print( renderTime / 1000000.0 );
        System.out.print( "m" );
      }
      else if ( renderTime >= 1000.0 )
      {
        System.out.print( renderTime / 1000.0 );
        System.out.print( "\u00B5" );
      }
      else
      {
        System.out.print( renderTime / 1000.0 );
        System.out.print( "n" );
      }
      System.out.println( "s." );
    }
  }
}
