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

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.model.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides the actual UI implementation for the channel labels.
 */
public class ChannelLabelsUI extends ComponentUI
{
  // CONSTANTS

  private static final String MINIMAL_LABEL = "W88";

  // VARIABLES

  private final Renderer renderer = new ChannelLabelRenderer();

  // METHODS

  /**
   * Creates the rendering hints for this view.
   */
  private static RenderingHints createRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
    hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getMaximumSize( final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;
    final ChannelLabelsViewModel model = view.getModel();

    return determineSize( view, model );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getMinimumSize( final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;
    final ChannelLabelsViewModel model = view.getModel();

    return determineSize( view, model );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;
    final ChannelLabelsViewModel model = view.getModel();

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setBackground( model.getBackgroundColor() );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final int channelHeight = model.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = model.getSignalOffset();

      final int compWidth = view.getWidth();
      final int dataWidth = model.getSampleWidth();

      // Determine which bits of the actual signal should be drawn...
      int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      int endBit = ( int )Math.min( dataWidth, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

      for ( int b = 0; b < dataWidth; b++ )
      {
        final int virtualRow = model.toVirtualRow( b );
        if ( ( virtualRow < startBit ) || ( virtualRow > endBit ) )
        {
          // Trivial reject: we don't have to paint this row, as it is not asked
          // from us (due to clip boundaries)!
          continue;
        }

        final int yOffset = channelHeight * virtualRow;
        final int textYoffset = signalOffset + yOffset;

        final String label = model.getChannelLabel( b );

        canvas.setFont( model.getLabelFont() );
        canvas.setColor( model.getLabelBackgroundColor() );

        canvas.fillRoundRect( clip.x - 10, yOffset + 2, clip.width + 8, channelHeight - 2, 12, 12 );

        this.renderer.setContext( Integer.valueOf( b ), Integer.valueOf( compWidth ), label );

        canvas.setColor( model.getLabelForegroundColor() );

        this.renderer.render( canvas, 0, textYoffset );
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;
    }
  }

  /**
   * Determines the size of the view.
   * 
   * @param aView
   *          the view to determine the size for;
   * @param aModel
   *          the model of the view to determine the size for.
   * @return a size, never <code>null</code>.
   */
  private Dimension determineSize( final ChannelLabelsView aView, final ChannelLabelsViewModel aModel )
  {
    Dimension result = super.getPreferredSize( aView );
    if ( result == null )
    {
      result = new Dimension();
    }

    ChannelLabelsViewModel model = aView.getModel();

    int minWidth = -1;

    final FontMetrics fm = aView.getFontMetrics( model.getLabelFont() );
    for ( int i = 0; i < aModel.getSampleWidth(); i++ )
    {
      String label = aModel.getChannelLabel( i );
      if ( ( label == null ) || label.trim().isEmpty() )
      {
        label = MINIMAL_LABEL;
      }
      minWidth = Math.max( minWidth, fm.stringWidth( label ) );
    }

    // And always ensure we've got at least a minimal width...
    minWidth = Math.max( minWidth + 12, model.getMinimalWidth() );

    // Overwrite the preferred width with the one calculated...
    result.width = minWidth;

    return result;
  }
}
