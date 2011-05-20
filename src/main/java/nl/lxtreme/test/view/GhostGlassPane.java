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
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides a glass pane for use while dragging channels around. This glass pane
 * will show a marker where the drop location of the channel will be.
 */
public final class GhostGlassPane extends JPanel implements AWTEventListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final float ALPHA = 0.7f;

  // VARIABLES

  private volatile Rectangle affectedArea;
  private volatile Point mousePoint;
  private volatile Point drawPoint;

  private final JComponent component;
  private final SignalDiagramController controller;

  private final Renderer signalInfoRenderer = new MeasurementInfoRenderer();
  private final Renderer arrowRenderer = new ArrowRenderer();
  private volatile Renderer customRenderer;

  // CONSTRUCTORS

  /**
   * Creates a new {@link GhostGlassPane} instance.
   */
  public GhostGlassPane( final JComponent aComponent, final SignalDiagramController aController )
  {
    this.component = aComponent;
    this.controller = aController;
    setOpaque( false );
  }

  // METHODS

  /**
   * Clears the location where the channel/cursor might be dropped. This
   * location is used to draw a marker indicating the drop point.
   */
  public void clearDropPoint()
  {
    this.drawPoint = null;
  }

  /**
   * If someone adds a mouseListener to the GlassPane or set a new cursor we
   * expect that he knows what he is doing and return the super.contains(x, y)
   * otherwise we return false to respect the cursors for the underneath
   * components
   */
  @Override
  public boolean contains( final int aX, final int aY )
  {
    if ( ( getMouseListeners().length == 0 ) && ( getMouseMotionListeners().length == 0 )
        && ( getMouseWheelListeners().length == 0 )
    /* && ( getCursor() == Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) ) */)
    {
      return false;
    }
    return super.contains( aX, aY );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void eventDispatched( final AWTEvent aEvent )
  {
    if ( aEvent instanceof MouseEvent )
    {
      final MouseEvent me = ( MouseEvent )aEvent;
      final JComponent source = ( JComponent )me.getComponent();

      if ( !SwingUtilities.isDescendingFrom( source, this.component ) )
      {
        return;
      }
      if ( ( me.getID() == MouseEvent.MOUSE_EXITED ) && ( source == this.component ) )
      {
        this.drawPoint = this.mousePoint = null;
      }
      else
      {
        System.out.println( "BEFORE = " + me.getPoint() );
        MouseEvent converted = SwingUtilities.convertMouseEvent( source, me, source.getRootPane() );
        System.out.println( "AFTER  = " + converted.getPoint() );
        this.mousePoint = me.getPoint();
        this.drawPoint = converted.getPoint();
      }
      repaint();
    }
  }

  /**
   * Repaints only the affected areas of this glass pane.
   * 
   * @see #setDropPoint(Point)
   */
  public void repaintPartially()
  {
    if ( this.affectedArea == null )
    {
      repaint();
    }
    else
    {
      final Rectangle repaintRect = new Rectangle( this.affectedArea );
      // take a slighter larger area in order to ensure we've repainted
      // everything correctly...
      repaintRect.grow( 2, 2 );

      repaint( repaintRect );
    }
  }

  /**
   * Sets the location where the channel/cursor might be dropped. This location
   * is used to draw a marker indicating the drop point.
   * 
   * @param aLocation
   *          the location where the channel/cursor might be dropped, cannot be
   *          <code>null</code>;
   * @param aContext
   *          the drag and drop context, cannot be <code>null</code>.
   */
  public void setDropPoint( final Point aLocation )
  {
    // setDropPoint( aLocation, this.renderer );
  }

  /**
   * Sets the location where the channel/cursor might be dropped. This location
   * is used to draw a marker indicating the drop point.
   * 
   * @param aLocation
   *          the location where the channel/cursor might be dropped, cannot be
   *          <code>null</code>;
   * @param aContext
   *          the drag and drop context, cannot be <code>null</code>.
   */
  public void setDropPoint( final Point aLocation, final nl.lxtreme.test.view.renderer.Renderer aRenderer )
  {
    // this.dropPoint = aLocation;
    // this.renderer = aRenderer;
  }

  /**
   * Sets the rendering context for the renderer that should be painted.
   * 
   * @param aParameters
   *          the rendering context parameters, cannot be <code>null</code>.
   */
  public void setRenderContext( final Object... aParameters )
  {
    if ( this.customRenderer != null )
    {
      this.customRenderer.setContext( aParameters );
    }
  }

  /**
   * Sets renderer to the given value.
   * 
   * @param aRenderer
   *          the renderer to set.
   */
  public void setRenderer( final nl.lxtreme.test.view.renderer.Renderer aRenderer )
  {
    this.customRenderer = aRenderer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( ( this.drawPoint == null ) || !isVisible() )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      g2d.setRenderingHints( createRenderingHints() );

      g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, GhostGlassPane.ALPHA ) );

      if ( this.controller.isMeasurementMode() )
      {
        final SignalHoverInfo signalHover = this.controller.getSignalHover( this.mousePoint );
        if ( signalHover != null )
        {
          Rectangle signalHoverRect = signalHover.getRectangle();

          int x = signalHoverRect.x;
          int y = ( int )signalHoverRect.getCenterY();
          int w = signalHoverRect.width;
          int middlePos = signalHover.getMiddleXpos() - x;

          // Tell Swing how we would like to render ourselves...
          g2d.setRenderingHints( createRenderingHints() );

          g2d.setColor( Color.YELLOW );

          this.arrowRenderer.setContext( Integer.valueOf( w ), Integer.valueOf( middlePos ) );

          Rectangle rect1 = this.arrowRenderer.render( g2d, this.drawPoint.x, this.drawPoint.y );

          final int textXpos = ( int )( rect1.getCenterX() + 8 );
          final int textYpos = rect1.y + 8;

          this.signalInfoRenderer.setContext( signalHover );

          Rectangle rect2 = this.signalInfoRenderer.render( g2d, textXpos, textYpos );

          this.affectedArea = rect1.intersection( rect2 );
        }
      }
      else
      {
        g2d.setColor( Color.YELLOW );

        int x = this.drawPoint.x;
        int y = this.drawPoint.y;

        this.affectedArea = this.customRenderer.render( g2d, x, y );
      }
    }
    finally
    {
      g2d.dispose();
    }
  }

  /**
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
  }
}
