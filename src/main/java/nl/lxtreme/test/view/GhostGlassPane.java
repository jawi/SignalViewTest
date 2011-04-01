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


/**
 * Provides a glass pane for use while dragging channels around. This glass pane
 * will show a marker where the drop location of the channel will be.
 */
final class GhostGlassPane extends JPanel
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final float ALPHA = 0.7f;

  // VARIABLES

  private volatile Rectangle affectedArea;
  private volatile Point dropPoint;
  private volatile nl.lxtreme.test.view.renderer.Renderer renderer;

  // CONSTRUCTORS

  /**
   * Creates a new {@link GhostGlassPane} instance.
   */
  public GhostGlassPane()
  {
    setOpaque( false );
  }

  // METHODS

  /**
   * Clears the location where the channel/cursor might be dropped. This
   * location is used to draw a marker indicating the drop point.
   */
  public void clearDropPoint()
  {
    this.dropPoint = null;
  }

  /**
   * Repaints only the affected areas of this glass pane.
   * 
   * @see #setDropPoint(Point)
   */
  public void repaintPartially()
  {
    // int x, y, width, height;
    // if ( DragAndDropContext.CHANNEL_ROW == this.context )
    // {
    // width = Math.min( getWidth(), CHANNEL_ROW_MARKER_WIDTH ) + 2;
    // height = CHANNEL_ROW_MARKER_HEIGHT + 2;
    // }
    // else
    // {
    // width = CURSOR_MARKER_WIDTH + 2;
    // height = Math.min( getHeight(), CURSOR_MARKER_HEIGHT ) + 2;
    // }
    //
    // if ( this.oldDropPoint != null )
    // {
    // x = Math.max( 0, this.oldDropPoint.x - 1 );
    // y = Math.max( 0, this.oldDropPoint.y + 1 );
    // repaint( x, y, width, height );
    // }
    // if ( ( this.dropPoint != null ) && !this.dropPoint.equals(
    // this.oldDropPoint ) )
    // {
    // x = Math.max( 0, this.dropPoint.x - 1 );
    // y = Math.max( 0, this.dropPoint.y + 1 );
    // repaint( x, y, width, height );
    // }
    //
    // if ( DragAndDropContext.CHANNEL_ROW == this.context )

    if ( this.affectedArea == null )
    {
      System.out.println( "!!!" );
      repaint(); // TODO fix me!
    }
    else
    {
      repaint( this.affectedArea );
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
    setDropPoint( aLocation, this.renderer );
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
    this.dropPoint = aLocation;
    this.renderer = aRenderer;
  }

  /**
   * @param aParameters
   */
  public void setRenderContext( final Object... aParameters )
  {
    if ( this.renderer != null )
    {
      this.renderer.setContext( aParameters );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( ( this.dropPoint == null ) || !isVisible() )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, GhostGlassPane.ALPHA ) );
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

      g2d.setColor( Color.YELLOW );

      int x = this.dropPoint.x;
      int y = this.dropPoint.y;

      this.affectedArea = this.renderer.render( g2d, x, y );
    }
    finally
    {
      g2d.dispose();
    }
  }
}
