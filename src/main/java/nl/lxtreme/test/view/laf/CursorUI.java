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
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.test.view.laf;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides the actual UI implementation for the cursor view.
 */
public class CursorUI extends ComponentUI
{
  // INNER TYPES

  /**
   * Provides an mouse event listener to allow some of the functionality (such
   * as DnD and cursor dragging) of this component to be controlled with the
   * mouse.
   */
  static final class MyMouseListener extends MouseAdapter
  {
    // VARIABLES

    private final SignalDiagramController controller;

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
    public void mouseClicked( final MouseEvent aEvent )
    {
      // TODO: this should be done through a context menu...
      if ( this.controller.isCursorMode() && ( aEvent.getClickCount() == 2 ) )
      {
        final Point point = aEvent.getPoint();
        this.controller.moveCursor( 3, point );
      }
    }
  }

  // VARIABLES

  private final Renderer cursorRenderer = new CursorFlagRenderer();

  private MyMouseListener mouseListener;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    final CursorView view = ( CursorView )aComponent;

    // Lazy init...
    if ( this.mouseListener == null )
    {
      this.mouseListener = new MyMouseListener( view.getController() );
    }

    view.addMouseListener( this.mouseListener );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    final CursorView view = ( CursorView )aComponent;

    final SignalDiagramController controller = view.getController();
    if ( !controller.isCursorMode() )
    {
      return;
    }

    final Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      final int y = getYposition( view );

      final ScreenModel screenModel = controller.getScreenModel();

      for ( int i = 0; i < SampleDataModel.MAX_CURSORS; i++ )
      {
        final int x = controller.getCursorScreenCoordinate( i );

        if ( ( x < 0 ) || !clip.contains( x, 0 ) )
        {
          // Trivial reject: don't paint undefined cursors, or cursors outside
          // the clip boundaries...
          continue;
        }

        canvas.setColor( screenModel.getCursorColor( i ) );

        this.cursorRenderer.setContext( controller.getCursorFlagText( i ) );

        this.cursorRenderer.render( canvas, x, y );
      }
    }
    finally
    {
      canvas.dispose();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void uninstallUI( final JComponent aComponent )
  {
    final CursorView view = ( CursorView )aComponent;

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
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
  }

  /**
   * Returns the Y-position where the cursor (+ flag) should be drawn.
   * 
   * @return a Y-position, in the coordinate space of this component.
   */
  private int getYposition( final JComponent aComponent )
  {
    int result = 0;
    if ( SwingUtilities.getAncestorOfClass( JViewport.class, aComponent ) != null )
    {
      // negative in order to ensure the flag itself is hidden
      result = -40;
    }
    return result;
  }
}
