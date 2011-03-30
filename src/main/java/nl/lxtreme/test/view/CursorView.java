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
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * @author jajans
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
  private Point lastPoint;

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

    setOpaque( false );
  }

  // METHODS

  /**
   * @param aPoint
   * @param aX
   * @return
   */
  private static boolean inArea( final Point aPoint, final int aX )
  {
    if ( aPoint == null )
    {
      return false;
    }
    return ( aX >= aPoint.x - 5 ) && ( aX <= aPoint.x + 5 );
  }

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
   * Moves the cursor with the given index to the given coordinate.
   * 
   * @param aCursorIdx
   *          the index of the cursor to move;
   * @param aPoint
   *          the coordinate to move the cursor to, cannot be <code>null</code>;
   * @param aSnap
   *          if <code>true</code> snaps to the signal value, <code>false</code>
   *          otherwise.
   */
  public void moveCursor( final int aCursorIdx, final Point aPoint )
  {
    final Long[] cursors = getCursors();

    repaintPartially( cursors, aCursorIdx );

    final long newCursorTimestamp = this.controller.toUnscaledScreenCoordinate( aPoint );
    this.controller.getDataModel().setCursor( aCursorIdx, Long.valueOf( newCursorTimestamp ) );

    if ( this.controller.isSnapModeEnabled() )
    {
      this.lastPoint = aPoint;
    }
    else
    {
      this.lastPoint = null;
    }

    repaintPartially( cursors, aCursorIdx );
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

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = aGraphics.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      g2d.setRenderingHints( createRenderingHints() );

      // negative in order to ensure the flag itself is hidden
      final int y = -40;

      final Renderer renderer = new CursorFlagRenderer();

      final Long[] cursors = getCursors();
      for ( int i = 0; i < cursors.length; i++ )
      {
        final Long cursorTimestamp = cursors[i];
        if ( cursorTimestamp == null )
        {
          continue;
        }

        final int x = this.controller.toScaledScreenCoordinate( cursorTimestamp.longValue() ).x;

        renderer.setContext( this.controller, Integer.valueOf( i ), cursorTimestamp );

        renderer.render( g2d, clip, x, y );

        if ( this.controller.isSnapModeEnabled() && inArea( this.lastPoint, x ) )
        {
          g2d.drawOval( x - 4, this.lastPoint.y - 4, 8, 8 );
        }
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

  /**
   * @return
   */
  private Long[] getCursors()
  {
    return this.controller.getDataModel().getCursors();
  }

  /**
   * Repaints the areas that were affected by the last paint() call.
   * 
   * @param aCursorIdx
   *          the cursor index of the cursor to repaint.
   */
  private void repaintPartially( final Long[] aCursors, final int aCursorIdx )
  {
    int x, y, w, h;

    final Long cursorTimestamp = aCursors[aCursorIdx];
    if ( cursorTimestamp != null )
    {
      x = this.controller.toScaledScreenCoordinate( cursorTimestamp.longValue() ).x - 1;
      y = 0;
      w = 2;
      h = getHeight();

      repaint( x, y, w, h );
    }

    if ( this.lastPoint != null )
    {
      x = this.lastPoint.x - 5;
      y = this.lastPoint.y - 5;
      w = 10;
      h = 10;

      repaint( x, y, w, h );
    }
  }
}
