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

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.renderer.*;
import nl.lxtreme.test.view.renderer.Renderer;


/**
 * Provides the actual UI implementation for the channel labels.
 */
public class ChannelLabelsUI extends ComponentUI
{
  // CONSTANTS

  public static final String COMPONENT_MINIMAL_WIDTH = "channellabels.width.minimal";
  public static final String COMPONENT_BACKGROUND_COLOR = "channellabels.color.background";
  public static final String LABEL_BACKGROUND_COLOR = "channellabels.label.color.background";
  public static final String LABEL_FONT = "channellabels.label.font";

  // VARIABLES

  private final Renderer renderer = new ChannelLabelRenderer();

  private Color backgroundColor;
  private Color labelBackgroundColor;
  private Font labelFont;
  private int minimalWidth;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getMinimumSize( final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;

    final SignalDiagramController controller = view.getController();

    final Dimension result = super.getMinimumSize( aComponent );

    final SampleDataModel dataModel = controller.getDataModel();
    final ScreenModel screenModel = controller.getScreenModel();

    int minWidth = -1;

    final FontMetrics fm = view.getFontMetrics( this.labelFont );
    for ( int i = 0; i < dataModel.getWidth(); i++ )
    {
      String label = screenModel.getChannelLabel( i );
      if ( ( label == null ) || label.trim().isEmpty() )
      {
        label = "W88";
      }
      minWidth = Math.max( minWidth, fm.stringWidth( label ) );
    }

    // And always ensure we've got at least a minimal width...
    minWidth = Math.min( minWidth + 12, this.minimalWidth );

    // Overwrite the preferred width with the one calculated...
    result.width = minWidth;

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;

    final IUserInterfaceSettingsProvider settingsProvider = view.getSettingsProvider();

    this.backgroundColor = settingsProvider.getColor( COMPONENT_BACKGROUND_COLOR );
    if ( this.backgroundColor == null )
    {
      this.backgroundColor = Utils.parseColor( "#1E2126" );
    }

    this.labelBackgroundColor = settingsProvider.getColor( LABEL_BACKGROUND_COLOR );
    if ( this.labelBackgroundColor == null )
    {
      this.labelBackgroundColor = Utils.parseColor( "#2E323B" );
    }

    this.labelFont = settingsProvider.getFont( LABEL_FONT );
    if ( this.labelFont == null )
    {
      this.labelFont = ( ( Font )UIManager.get( "Label.font" ) ).deriveFont( Font.BOLD );
    }

    this.minimalWidth = settingsProvider.getInteger( COMPONENT_MINIMAL_WIDTH );
    if ( this.minimalWidth < 1 )
    {
      this.minimalWidth = 40;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    final ChannelLabelsView view = ( ChannelLabelsView )aComponent;

    final SignalDiagramController controller = view.getController();

    Graphics2D canvas = ( Graphics2D )aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      canvas.setRenderingHints( createRenderingHints() );

      canvas.setBackground( this.backgroundColor );
      canvas.clearRect( clip.x, clip.y, clip.width, clip.height );

      final SampleDataModel dataModel = controller.getDataModel();
      final ScreenModel screenModel = controller.getScreenModel();

      final int channelHeight = screenModel.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = screenModel.getSignalOffset();

      final int compWidth = view.getWidth();
      final int dataWidth = dataModel.getWidth();

      // Determine which bits of the actual signal should be drawn...
      int startBit = ( int )Math.max( 0, Math.floor( clip.y / ( double )channelHeight ) );
      int endBit = ( int )Math.min( dataWidth, Math.ceil( ( clip.y + clip.height ) / ( double )channelHeight ) );

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
        canvas.setColor( this.labelBackgroundColor );

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
