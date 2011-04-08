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

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides a view for the channel labels.
 */
class ChannelLabelsView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;
  private final Renderer renderer;
  private final Font labelFont;

  // CONSTRUCTORS

  /**
   * Creates a new {@link ChannelLabelsView} instance.
   */
  public ChannelLabelsView( final SignalDiagramController aController )
  {
    this.controller = aController;

    setBackground( Utils.parseColor( "#1E2126" ) );

    Font font = ( Font )UIManager.get( "Label.font" );
    this.labelFont = font.deriveFont( Font.BOLD );

    this.renderer = new ChannelLabelRenderer();
  }

  // METHODS

  /**
   * Tries the resize this component to such a width that all labels will
   * properly fit.
   */
  public int getMinimalWidth()
  {
    final SampleDataModel dataModel = this.controller.getDataModel();
    final ScreenModel screenModel = this.controller.getScreenModel();

    int minWidth = -1;

    final FontMetrics fm = getFontMetrics( this.labelFont );
    for ( int i = 0; i < dataModel.getWidth(); i++ )
    {
      String label = screenModel.getChannelLabel( i );
      if ( ( label == null ) || label.trim().isEmpty() )
      {
        label = "W88";
      }
      minWidth = Math.max( minWidth, fm.stringWidth( label ) );
    }

    // Ensure there's room for some padding...
    minWidth += 12;

    // And always ensure we've got at least a minimal width...
    if ( minWidth < 40 )
    {
      minWidth = 40;
    }

    return minWidth;
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setColor( getBackground() );
      canvas.fillRect( clip.x, clip.y, clip.width, clip.height );

      final SampleDataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();

      final int channelHeight = screenModel.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = screenModel.getSignalOffset();

      final int compWidth = getWidth();
      final int dataWidth = dataModel.getWidth();

      // Determine which bits of the actual signal should be drawn...
      int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      int endBit = ( int )Math.min( dataWidth, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

      final Color labelBackground = Utils.parseColor( "#2E323B" );
      for ( int b = 0; b < dataWidth; b++ )
      {
        final int virtualRow = screenModel.toVirtualRow( b );
        if ( ( virtualRow < startBit ) || ( virtualRow > endBit ) )
        {
          // Trivial reject: we don't have to paint this row, as it is not asked
          // from us (due to clip boundaries)!
          continue;
        }

        final int yOffset = channelHeight * virtualRow;
        final int textYoffset = signalOffset + yOffset;

        final String label = screenModel.getChannelLabel( b );

        canvas.setFont( this.labelFont );
        canvas.setColor( labelBackground );

        canvas.fillRoundRect( clip.x - 10, yOffset + 2, clip.width + 8, channelHeight - 2, 12, 12 );

        this.renderer.setContext( Integer.valueOf( b ), Integer.valueOf( compWidth ), label );

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
   * Creates the rendering hints for this view.
   */
  private RenderingHints createRenderingHints()
  {
    RenderingHints hints = new RenderingHints( RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
    hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
    return hints;
  }
}
