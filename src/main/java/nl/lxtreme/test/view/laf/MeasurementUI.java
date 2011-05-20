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
package nl.lxtreme.test.view.laf;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * 
 */
public class MeasurementUI extends ComponentUI
{
  // INNER TYPES

  /**
   * Provides a mouse listener for this measurement view component.
   */
  static final class MyMouseListener extends MouseAdapter
  {
    // CONSTANTS

    private static final Cursor DEFAULT = Cursor.getDefaultCursor();
    private static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );

    // VARIABLES

    private final SignalDiagramController controller;

    private volatile boolean showing = false;

    // CONSTRUCTORS

    /**
     * @param aController
     */
    public MyMouseListener( final SignalDiagramController aController )
    {
      this.controller = aController;
    }

    // METHODS

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

          aEvent.consume();
        }
      }
      else
      {
        if ( !this.controller.isMeasurementMode() )
        {
          this.showing = this.controller.hideHover();
          setMouseCursor( aEvent, DEFAULT );

          aEvent.consume();
        }
        else
        {
          setMouseCursor( aEvent, CURSOR_HOVER );
          this.controller.moveHover( point );

          aEvent.consume();
        }
      }
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

  // CONSTANTS

  public static final String LABEL_FONT = "measurement.label.font";

  // VARIABLES

  private final Renderer signalInfoRenderer = new MeasurementInfoRenderer();
  private final Renderer arrowRenderer = new ArrowRenderer();

  private Font textFont;

  private volatile SignalHoverInfo signalHover;

  private Rectangle textRectangle;
  private Rectangle arrowRectangle;

  private MyMouseListener mouseListener;

  // METHODS

  /**
   * Hides the hover from screen.
   */
  public void hideHover( final JComponent aComponent )
  {
    repaintPartially( aComponent );
    this.signalHover = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    final MeasurementView view = ( MeasurementView )aComponent;

    final IUserInterfaceSettingsProvider settingsProvider = view.getSettingsProvider();

    final Font baseFont = ( Font )UIManager.get( "Label.font" );

    this.textFont = settingsProvider.getFont( LABEL_FONT );
    if ( this.textFont == null )
    {
      this.textFont = baseFont.deriveFont( baseFont.getSize2D() * 0.9f );
    }

    // Lazy init...
    if ( this.mouseListener == null )
    {
      this.mouseListener = new MyMouseListener( view.getController() );
    }

    view.addMouseMotionListener( this.mouseListener );
  }

  /**
   * Moves the hover on screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void moveHover( final SignalHoverInfo aSignalHover, final JComponent aComponent )
  {
    repaintPartially( aComponent );
    this.signalHover = ( aSignalHover == null ) ? null : aSignalHover.clone();
    repaintPartially( aComponent );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    if ( this.signalHover == null )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      Rectangle signalHoverRect = this.signalHover.getRectangle();

      int x = signalHoverRect.x;
      int y = ( int )signalHoverRect.getCenterY();
      int w = signalHoverRect.width;
      int middlePos = this.signalHover.getMiddleXpos() - x;

      // Tell Swing how we would like to render ourselves...
      g2d.setRenderingHints( createRenderingHints() );

      g2d.setColor( Color.YELLOW );

      this.arrowRenderer.setContext( Integer.valueOf( w ), Integer.valueOf( middlePos ) );

      this.arrowRectangle = this.arrowRenderer.render( g2d, x, y );

      // Render the tool tip...
      final String text = this.signalHover.toString();
      final int textXpos = ( int )( this.arrowRectangle.getCenterX() + 8 );
      final int textYpos = this.arrowRectangle.y + 8;

      g2d.setFont( this.textFont );

      this.signalInfoRenderer.setContext( text );

      this.textRectangle = this.signalInfoRenderer.render( g2d, textXpos, textYpos );
    }
    finally
    {
      g2d.dispose();
    }
  }

  /**
   * Shows the hover from screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void showHover( final SignalHoverInfo aSignalHover, final JComponent aComponent )
  {
    if ( aSignalHover != null )
    {
      this.signalHover = aSignalHover.clone();
      repaintPartially( aComponent );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void uninstallUI( final JComponent aComponent )
  {
    final MeasurementView view = ( MeasurementView )aComponent;

    view.removeMouseListener( this.mouseListener );
    view.removeMouseMotionListener( this.mouseListener );

    this.mouseListener = null;
  }

  /**
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
    hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
    return hints;
  }

  /**
   * Repaints the areas that were affected by the last paint() call.
   */
  private void repaintPartially( final JComponent aComponent )
  {
    if ( ( this.arrowRectangle != null ) && !this.arrowRectangle.isEmpty() )
    {
      this.arrowRectangle.grow( 2, 2 );
      aComponent.repaint( this.arrowRectangle );
    }

    if ( ( this.textRectangle != null ) && !this.textRectangle.isEmpty() )
    {
      // NOTE: grow with a larger value to ensure the text rectangle "floats"
      // along with the mouse cursor...
      this.textRectangle.grow( 5, 5 );
      aComponent.repaint( this.textRectangle );
    }

    if ( ( this.arrowRectangle == null ) && ( this.textRectangle == null ) )
    {
      aComponent.repaint();
    }
  }
}
