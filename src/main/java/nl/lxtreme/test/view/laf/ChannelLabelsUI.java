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

import nl.lxtreme.test.model.*;
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

  private static final int ARC_WIDTH = 12;
  private static final int PADDING_Y = 2;
  private static final int PADDING_X = 4;

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

    return determineSize( view );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getMinimumSize( final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;

    return determineSize( view );
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

      final ChannelElement[] channelElements = model.getChannelElements( clip.y, clip.height );
      if ( channelElements.length == 0 )
      {
        return; // XXX
      }

      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setBackground( model.getBackgroundColor() );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final int channelHeight = model.getChannelHeight();
      final int dataValueRowHeight = model.getDataValuesRowHeight();
      final int scopeHeight = model.getScopeHeight();

      // Where is the text to be drawn?
      final int textOffsetChannel = ( int )( ( channelHeight - model.getSignalHeight() ) / 2.0 );
      final int textOffsetScope = ( int )( scopeHeight / 2.0 );

      final int compWidth = view.getWidth();

      // Start drawing at the correct position in the clipped region...
      canvas.translate( 0, channelElements[0].getYposition() );

      for ( ChannelElement channelElement : channelElements )
      {
        if ( channelElement.isDigitalChannel() )
        {
          final Channel channel = channelElement.getChannel();

          canvas.setFont( model.getLabelFont() );
          canvas.setColor( model.getLabelBackgroundColor() );

          canvas.fillRoundRect( clip.x - ARC_WIDTH, PADDING_Y, clip.width + ( ARC_WIDTH - PADDING_X ), channelHeight
              - PADDING_Y, ARC_WIDTH, ARC_WIDTH );

          this.renderer.setContext( Integer.valueOf( channel.getIndex() ), Integer.valueOf( compWidth ),
              channel.getLabel() );

          canvas.setColor( model.getLabelForegroundColor() );

          this.renderer.render( canvas, 0, textOffsetChannel );

          // Advance to the next channel...
          canvas.translate( 0, channelHeight );
        }

        canvas.setFont( model.getLabelFont() );
        canvas.setColor( model.getLabelBackgroundColor() );

        // Before drawing the new channel, we should "finish" up the old
        // channel group...
        if ( channelElement.isDataValues() )
        {
          canvas.fillRoundRect( clip.x - ARC_WIDTH, PADDING_Y, clip.width + ( ARC_WIDTH - PADDING_X ),
              dataValueRowHeight - PADDING_Y, ARC_WIDTH, ARC_WIDTH );

          // TODO label...

          canvas.translate( 0, dataValueRowHeight );
        }

        if ( channelElement.isAnalogSignal() )
        {
          canvas.fillRoundRect( clip.x - ARC_WIDTH, PADDING_Y, clip.width + ( ARC_WIDTH - PADDING_X ), scopeHeight
              - PADDING_Y, ARC_WIDTH, ARC_WIDTH );

          this.renderer.setContext( Integer.valueOf( 0 ), Integer.valueOf( compWidth ), "SCOPE" ); // XXX

          canvas.setColor( model.getLabelForegroundColor() );

          this.renderer.render( canvas, 0, textOffsetScope );

          canvas.translate( 0, scopeHeight );
        }
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
  private Dimension determineSize( final ChannelLabelsView aView )
  {
    Dimension result = super.getPreferredSize( aView );
    if ( result == null )
    {
      result = new Dimension();
    }

    ChannelLabelsViewModel model = aView.getModel();

    int minWidth = -1;

    final FontMetrics fm = aView.getFontMetrics( model.getLabelFont() );
    for ( Channel channel : model.getAllChannels() )
    {
      String label = channel.getLabel();
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
