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
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.test.*;
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
        private final SignalDiagramController ctrl = ComponentSizeListener.this.controller;

        @Override
        public void run()
        {
          component.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

          try
          {
            if ( this.ctrl.isZoomAll() )
            {
              this.ctrl.zoomAll();
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
          component.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

          try
          {
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

  private ComponentSizeListener componentSizeListener;
  private KeyboardControlListener keyboardListener;
  private CursorMouseListener cursorMouseListener;

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
      this.controller.setSignalDiagram( this );

      final JRootPane rootPane = SwingUtilities.getRootPane( this );
      rootPane.setGlassPane( new GhostGlassPane() );

      this.componentSizeListener = new ComponentSizeListener( this.controller );
      this.keyboardListener = new KeyboardControlListener( this.controller );

      final Container window = SwingUtilities.getWindowAncestor( this );
      window.addComponentListener( this.componentSizeListener );
      window.addKeyListener( this.keyboardListener );

      this.cursorMouseListener = new CursorMouseListener( this.controller );

      addMouseListener( this.cursorMouseListener );
      addMouseMotionListener( this.cursorMouseListener );

      configureEnclosingScrollPane();

      this.controller.recalculateDimensions();
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
    final SampleDataModel dataModel = this.controller.getDataModel();
    final ScreenModel screenModel = this.controller.getScreenModel();

    final int channelHeight = screenModel.getChannelHeight();
    final int channelCount = dataModel.getWidth();
    final int lastSampleIdx = dataModel.getSize();

    final int inc;
    if ( aOrientation == SwingConstants.VERTICAL )
    {
      inc = getVerticalBlockIncrement( aVisibleRect, aDirection, channelHeight, channelCount );
    }
    else
    /* if ( aOrientation == SwingConstants.HORIZONTAL ) */
    {
      inc = getHorizontalBlockIncrement( aVisibleRect, aDirection, lastSampleIdx );
    }

    return inc;
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
    return getScrollableBlockIncrement( aVisibleRect, aOrientation, aDirection );
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

        scrollPane.setCorner( ScrollPaneConstants.UPPER_LEADING_CORNER, new CornerView( this.controller ) );
      }
    }
  }

  /**
   * Calculates the horizontal block increment.
   * <p>
   * The following rules are adhered for scrolling horizontally:
   * </p>
   * <ol>
   * <li>unless the first or last sample is not shown, scroll a full block;
   * otherwise</li>
   * <li>do not scroll.</li>
   * </ol>
   * 
   * @param aVisibleRect
   *          the visible rectangle of the component, never <code>null</code>;
   * @param aDirection
   *          the direction in which to scroll (&gt; 0 to scroll left, &lt; 0 to
   *          scroll right);
   * @param aLastSampleIdx
   *          the index of the last available sample in the data model.
   * @return a horizontal block increment, determined according to the rules
   *         described.
   */
  private int getHorizontalBlockIncrement( final Rectangle aVisibleRect, final int aDirection, final int aLastSampleIdx )
  {
    final int blockIncr = 50;

    final int firstVisibleSample = this.controller.locationToSampleIndex( aVisibleRect.getLocation() );
    final int lastVisibleSample = this.controller.locationToSampleIndex( new Point(
        aVisibleRect.x + aVisibleRect.width, 0 ) );

    int inc = 0;
    if ( aDirection < 0 )
    {
      // Scroll left
      if ( firstVisibleSample > 0 )
      {
        inc = blockIncr;
      }
    }
    else if ( aDirection > 0 )
    {
      // Scroll right
      if ( lastVisibleSample < aLastSampleIdx )
      {
        inc = blockIncr;
      }
    }

    return inc;
  }

  /**
   * Calculates the vertical block increment.
   * <p>
   * The following rules are adhered for scrolling vertically:
   * </p>
   * <ol>
   * <li>if the first shown channel is not completely visible, it will be made
   * fully visible; otherwise</li>
   * <li>scroll down to show the succeeding channel fully;</li>
   * <li>if the last channel is fully shown, and there is some room left at the
   * bottom, show the remaining space.</li>
   * </ol>
   * 
   * @param aVisibleRect
   *          the visible rectangle of the component, never <code>null</code>;
   * @param aDirection
   *          the direction in which to scroll (&gt; 0 to scroll down, &lt; 0 to
   *          scroll up);
   * @param aChannelHeight
   *          the height of a single channel row (> 0);
   * @param aChannelCount
   *          the number of channels shown in this component (> 0).
   * @return a vertical block increment, determined according to the rules
   *         described.
   */
  private int getVerticalBlockIncrement( final Rectangle aVisibleRect, final int aDirection, final int aChannelHeight,
      final int aChannelCount )
  {
    int inc;
    int firstVisibleRow = ( int )( aVisibleRect.y / ( double )aChannelHeight );
    int lastVisibleRow = ( int )( ( aVisibleRect.y + aVisibleRect.height ) / ( double )aChannelHeight );

    inc = 0;
    if ( aDirection < 0 )
    {
      // Scroll up...
      if ( ( firstVisibleRow > 0 ) && ( lastVisibleRow <= aChannelCount ) )
      {
        // Scroll to the first fully visible channel row...
        inc = aVisibleRect.y % aChannelHeight;
      }
      if ( inc == 0 )
      {
        // All rows are fully visible, scroll an entire row up...
        inc = aChannelHeight;
      }
      if ( ( aVisibleRect.y - inc ) < 0 )
      {
        // Make sure that we do not scroll beyond the first row...
        inc = aVisibleRect.y;
      }
    }
    else if ( aDirection > 0 )
    {
      // Scroll down...
      if ( ( firstVisibleRow >= 0 ) && ( lastVisibleRow < aChannelCount ) )
      {
        // Scroll to the first fully visible channel row...
        inc = aVisibleRect.y % aChannelHeight;
      }
      if ( inc == 0 )
      {
        // All rows are fully visible, scroll an entire row up...
        inc = aChannelHeight;
      }
      int height = getHeight();
      if ( ( aVisibleRect.y + aVisibleRect.height + inc ) > height )
      {
        // Make sure that we do not scroll beyond the last row...
        inc = height - aVisibleRect.y - aVisibleRect.height;
      }
    }
    return inc;
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
