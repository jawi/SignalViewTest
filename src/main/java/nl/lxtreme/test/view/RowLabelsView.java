/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.model.*;


/**
 * @author jawi
 */
class RowLabelsView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final ScreenController controller;
  private final Font labelFont;
  private final Font indexFont;

  // CONSTRUCTORS

  /**
   * Creates a new {@link RowLabelsView} instance.
   */
  public RowLabelsView( final ScreenController aController )
  {
    this.controller = aController;

    setOpaque( true );
    setBackground( Color.BLACK );

    Font font = ( Font )UIManager.get( "Label.font" );
    this.labelFont = font.deriveFont( Font.BOLD );
    this.indexFont = font.deriveFont( font.getSize() * 0.75f );
  }

  // METHODS

  @Override
  public Dimension getPreferredSize()
  {
    return new Dimension( getMinimalWidth(), 240 );
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    Graphics canvas = aGraphics.create();

    try
    {
      final Rectangle clip = canvas.getClipBounds();

      canvas.setColor( getBackground() );
      canvas.fillRect( clip.x, clip.y, clip.width, clip.height );

      final FontMetrics fm = canvas.getFontMetrics( this.labelFont );
      final FontMetrics indexFm = canvas.getFontMetrics( this.indexFont );

      final DataModel dataModel = this.controller.getDataModel();
      final ScreenModel screenModel = this.controller.getScreenModel();

      final int channelHeight = screenModel.getChannelHeight();
      // Where is the signal to be drawn?
      final int signalOffset = screenModel.getSignalOffset();

      final int width = dataModel.getWidth();
      for ( int b = 0; b < width; b++ )
      {
        final int yOffset = channelHeight * screenModel.toVirtualRow( b );

        canvas.setColor( Color.GRAY );
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
        final int labelXpos = ( clip.width - fm.stringWidth( label ) - 6 );

        canvas.setColor( screenModel.getColor( b ) );
        canvas.drawString( label, labelXpos, labelYpos );

        // paint the channel number below the label
        canvas.setFont( this.indexFont );
        final int indexYpos = textYoffset + ( int )( indexFm.getHeight() + ( 2.0 * fm.getHeight() / 3.0 ) );
        final int indexXpos = ( clip.width - indexFm.stringWidth( indexStr ) - 6 );

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
   * Tries the resize this component to such a width that all labels will
   * properly fit.
   */
  private int getMinimalWidth()
  {
    final DataModel dataModel = this.controller.getDataModel();
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

}
