/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * @author jawi
 */
class ChannelLabelsView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;
  private final Font labelFont;
  private final Font indexFont;

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
    this.indexFont = font.deriveFont( font.getSize() * 0.75f );
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

      final FontMetrics fm = canvas.getFontMetrics( this.labelFont );
      final FontMetrics indexFm = canvas.getFontMetrics( this.indexFont );

      final SampleDataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();

      final int channelHeight = screenModel.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = screenModel.getSignalOffset();

      final int width = getWidth();

      final Color labelBackground = Utils.parseColor( "#2E323B" );

      final int dataWidth = dataModel.getWidth();
      for ( int b = 0; b < dataWidth; b++ )
      {
        final int yOffset = channelHeight * screenModel.toVirtualRow( b );

        canvas.setColor( labelBackground );
        canvas.fillRoundRect( clip.x - 10, yOffset + 2, clip.width + 8, channelHeight - 2, 12, 12 );

        final int textYoffset = signalOffset + yOffset;

        String indexStr = Integer.toString( b );
        String label = screenModel.getChannelLabel( b );
        if ( ( label == null ) || label.trim().isEmpty() )
        {
          label = indexStr;
        }

        canvas.setFont( this.labelFont );
        final int labelYpos = textYoffset + ( int )( fm.getHeight() - ( 1.0 * fm.getHeight() / 3.0 ) );
        final int labelXpos = ( width - fm.stringWidth( label ) - 6 );

        canvas.setColor( Utils.getContrastColor( labelBackground ) );
        canvas.drawString( label, labelXpos, labelYpos );

        // paint the channel number below the label
        canvas.setFont( this.indexFont );
        final int indexYpos = textYoffset + ( int )( indexFm.getHeight() + ( 2.0 * fm.getHeight() / 3.0 ) );
        final int indexXpos = ( width - indexFm.stringWidth( indexStr ) - 6 );

        canvas.drawString( indexStr, indexXpos, indexYpos );
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
