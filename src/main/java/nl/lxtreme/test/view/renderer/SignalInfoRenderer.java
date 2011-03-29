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
package nl.lxtreme.test.view.renderer;


import java.awt.*;
import java.awt.font.*;
import java.util.*;
import java.util.List;

import nl.lxtreme.test.*;


/**
 * 
 */
public class SignalInfoRenderer extends BaseRenderer
{
  // INNER TYPES

  /**
   * @author jawi
   */
  static class ToolTipTextPart
  {
    public final TextLayout layout;
    public final float relYpos;

    /**
     * Creates a new MeasurementView.ToolTipPart instance.
     */
    public ToolTipTextPart( final TextLayout aLayout, final float aRelYPos )
    {
      this.layout = aLayout;
      this.relYpos = aRelYPos;
    }
  }

  // CONSTANTS

  private static Stroke THICK = new BasicStroke( 2.0f );

  // VARIABLES

  private String text;

  // METHODS

  /**
   * Ensures that the given rectangle is within the given view boundaries,
   * moving the location of the rectangle if needed. Note that the width and
   * height of the given rectangle are <em>not</em> modified.
   * 
   * @param aRectangle
   *          the rectangle to move within the given view boundaries;
   * @param aViewBounds
   *          the view boundaries to move the rectangle in.
   */
  private static void ensureRectangleWithinBounds( final Rectangle aRectangle, final Rectangle aViewBounds )
  {
    if ( aRectangle.x < aViewBounds.x )
    {
      aRectangle.x = aViewBounds.x;
    }
    else if ( aRectangle.x - aViewBounds.x + aRectangle.width > aViewBounds.width )
    {
      aRectangle.x = aViewBounds.x + Math.max( 0, aViewBounds.width - aRectangle.width - 4 );
    }
    if ( aRectangle.y < aViewBounds.y )
    {
      aRectangle.y = aViewBounds.y;
    }
    else if ( aRectangle.y - aViewBounds.y + aRectangle.height > aViewBounds.height )
    {
      aRectangle.y = aViewBounds.y + Math.max( 0, aViewBounds.height - aRectangle.height - 4 );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Rectangle render( final Graphics2D aCanvas )
  {
    float linePos = 0;
    float width = 0;

    List<ToolTipTextPart> textParts = new ArrayList<ToolTipTextPart>();
    TextLayout layout = null;

    final SimpleLineMeasurer lineMeasurer = new SimpleLineMeasurer( this.text, aCanvas.getFontRenderContext() );
    while ( lineMeasurer.hasNext() )
    {
      if ( layout != null )
      {
        // Move y-coordinate in preparation for next layout...
        linePos += layout.getDescent() + layout.getLeading();
      }

      layout = lineMeasurer.next();

      // Move y-coordinate by the ascent of the layout...
      linePos += layout.getAscent();

      textParts.add( new ToolTipTextPart( layout, linePos ) );

      // Determine the maximum line width...
      width = Math.max( width, layout.getVisibleAdvance() );
    }

    final Rectangle rect = new Rectangle( 0, 0, ( int )width + 8, ( int )linePos + 8 );
    // Fit as much of the tooltip on screen as possible...
    ensureRectangleWithinBounds( rect, getClipBounds() );

    aCanvas.setColor( Color.DARK_GRAY );
    aCanvas.fillRoundRect( rect.x, rect.y, rect.width, rect.height, 8, 8 );

    aCanvas.setColor( Color.DARK_GRAY.brighter() );
    aCanvas.setStroke( THICK );
    aCanvas.drawRoundRect( rect.x, rect.y, rect.width - 1, rect.height - 1, 8, 8 );

    aCanvas.setColor( Color.WHITE.darker() );

    for ( ToolTipTextPart textPart : textParts )
    {
      float x = ( rect.x + 4 );
      float y = ( rect.y + textPart.relYpos + 4 );
      textPart.layout.draw( aCanvas, x, y );
    }

    return rect;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final Object... aParameters )
  {
    if ( ( aParameters == null ) || ( aParameters.length < 1 ) )
    {
      throw new IllegalArgumentException( "Expected a String parameter!" );
    }
    this.text = ( String )aParameters[0];
  }
}
