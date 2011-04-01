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

import javax.swing.*;

import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * @author jajans
 */
final class MeasurementView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final Rectangle textRectangle;
  private final Rectangle arrowRectangle;
  private final Font textFont;

  private volatile SignalHoverInfo signalHover;

  // CONSTRUCTORS

  /**
   * Creates a new ArrowView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public MeasurementView( final SignalDiagramController aController )
  {
    this.arrowRectangle = new Rectangle();
    this.textRectangle = new Rectangle();

    setOpaque( false );

    final Font baseFont = ( Font )UIManager.get( "Label.font" );
    this.textFont = baseFont.deriveFont( baseFont.getSize2D() * 0.9f );
  }

  // METHODS

  /**
   * Hides the hover from screen.
   */
  public void hideHover()
  {
    repaintPartially();
    this.signalHover = null;
  }

  /**
   * Moves the hover on screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void moveHover( final SignalHoverInfo aSignalHover )
  {
    repaintPartially();
    this.signalHover = ( aSignalHover == null ) ? null : aSignalHover.clone();
    repaintPartially();
  }

  /**
   * Shows the hover from screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void showHover( final SignalHoverInfo aSignalHover )
  {
    if ( aSignalHover != null )
    {
      this.signalHover = aSignalHover.clone();
      repaint();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( this.signalHover == null )
    {
      return;
    }

    Rectangle rect;

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      final Rectangle hoverRect = this.signalHover.getRectangle();

      int x = hoverRect.x;
      int w = hoverRect.width;
      int y = ( int )( hoverRect.getCenterY() );
      int middlePos = this.signalHover.getMiddleXpos() - x;

      // Tell Swing how we would like to render ourselves...
      g2d.setRenderingHints( createRenderingHints() );

      // Render the arrow + arrowheads...
      final Renderer arrowRenderer = new ArrowRenderer();

      arrowRenderer.setContext( Integer.valueOf( w ), Integer.valueOf( middlePos ) );

      g2d.setColor( Color.YELLOW );

      rect = arrowRenderer.render( g2d, x, y );

      this.arrowRectangle.setBounds( rect );

      // Render the tool tip...
      final String text = this.signalHover.toString();
      final int textXpos = ( int )( ( x + ( w / 2.0f ) ) + 8 );
      final int textYpos = y + 8;

      g2d.setFont( this.textFont );

      Renderer signalInfoRenderer = new SignalInfoRenderer();

      signalInfoRenderer.setContext( text );

      rect = signalInfoRenderer.render( g2d, textXpos, textYpos );

      this.textRectangle.setBounds( rect );
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
  private void repaintPartially()
  {
    Rectangle rect = this.arrowRectangle;
    if ( this.signalHover != null )
    {
      rect = rect.union( this.signalHover.getRectangle() );
    }
    if ( !rect.isEmpty() )
    {
      final int x = rect.x - 10;
      final int y = rect.y - 10;
      final int w = rect.width + 20;
      final int h = rect.height + 20;

      repaint( x, y, w, h );
    }
    if ( !this.textRectangle.isEmpty() )
    {
      final int x = this.textRectangle.x - 10;
      final int y = this.textRectangle.y - 10;
      final int w = this.textRectangle.width + 20;
      final int h = this.textRectangle.height + 20;

      repaint( x, y, w, h );
    }
  }
}
