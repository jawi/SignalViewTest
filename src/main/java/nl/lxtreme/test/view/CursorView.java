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
import java.util.logging.*;

import javax.swing.*;

import nl.lxtreme.test.dnd.*;
import nl.lxtreme.test.dnd.DragAndDropTargetController.DragAndDropHandler;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides a component for displaying cursors + their timing information.
 */
final class CursorView extends JComponent
{
  // INNER TYPES

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

  private final SignalDiagramController controller;
  private final DropHandler dropHandler;
  private final Renderer cursorRenderer;

  // CONSTRUCTORS

  /**
   * Creates a new CursorView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public CursorView( final SignalDiagramController aController )
  {
    this.controller = aController;
    this.dropHandler = new DropHandler();

    this.cursorRenderer = new CursorFlagRenderer();

    setOpaque( false );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    super.addNotify();

    final SignalDiagramComponent parent = ( SignalDiagramComponent )getParent();
    parent.getDndTargetController().addHandler( this.dropHandler );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    final SignalDiagramComponent parent = ( SignalDiagramComponent )getParent();
    parent.getDndTargetController().removeHandler( this.dropHandler );

    super.removeNotify();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( !this.controller.isCursorMode() )
    {
      return;
    }

    final Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      final int y = getYposition();

      final ScreenModel screenModel = this.controller.getScreenModel();

      for ( int i = 0; i < SampleDataModel.MAX_CURSORS; i++ )
      {
        final int x = this.controller.getCursorScreenCoordinate( i );

        if ( ( x < 0 ) || !clip.contains( x, 0 ) )
        {
          // Trivial reject: don't paint undefined cursors, or cursors outside
          // the clip boundaries...
          continue;
        }

        canvas.setColor( screenModel.getCursorColor( i ) );

        this.cursorRenderer.setContext( this.controller.getCursorFlagText( i ) );

        this.cursorRenderer.render( canvas, x, y );
      }
    }
    finally
    {
      canvas.dispose();
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

  /**
   * Returns the Y-position where the cursor (+ flag) should be drawn.
   * 
   * @return a Y-position, in the coordinate space of this component.
   */
  private int getYposition()
  {
    int result = 0;
    if ( SwingUtilities.getAncestorOfClass( JViewport.class, this ) != null )
    {
      // negative in order to ensure the flag itself is hidden
      result = -40;
    }
    return result;
  }
}
