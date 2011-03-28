/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;
import java.awt.font.*;
import java.util.*;
import java.util.List;

import javax.swing.*;


/**
 * @author jajans
 */
class MeasurementView extends JComponent
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

  private static final long serialVersionUID = 1L;

  private static final int LEFT_FACING = 1;
  private static final int RIGHT_FACING = -1;

  private static Stroke THICK = new BasicStroke( 2.0f );

  // VARIABLES

  private final Rectangle textRectangle;
  private final Rectangle arrowRectangle;
  private final Font textFont;

  private volatile SignalHoverInfo signalHover;

  // CONSTRUCTORS

  /**
   * Creates a new ArrowView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public MeasurementView( final SignalDiagramController aController )
  {
    this.arrowRectangle = new Rectangle();
    this.textRectangle = new Rectangle();

    setOpaque( false );

    final Font baseFont = ( Font )UIManager.get( "Label.font" );
    this.textFont = baseFont.deriveFont( baseFont.getSize2D() * 0.9f );
  }

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
   * Hides the hover from screen.
   */
  public void hideHover()
  {
    repaintPartially();
    this.signalHover = null;
  }

  /**
   * Moves the hover on screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void moveHover( final SignalHoverInfo aSignalHover )
  {
    repaintPartially();
    this.signalHover = ( aSignalHover == null ) ? null : aSignalHover.clone();
    repaintPartially();
  }

  /**
   * Shows the hover from screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void showHover( final SignalHoverInfo aSignalHover )
  {
    if ( aSignalHover != null )
    {
      this.signalHover = aSignalHover.clone();
      repaintPartially();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( this.signalHover == null )
    {
      return;
    }

    // Copy the boundaries of the arrow rectangle...
    this.arrowRectangle.setBounds( this.signalHover.getRectangle() );

    final Graphics2D g2d = ( Graphics2D )aGraphics.create();
    try
    {
      final Rectangle clip = g2d.getClipBounds();
      // Tell Swing how we would like to render ourselves...
      g2d.setRenderingHints( createRenderingHints() );

      final int x1 = this.arrowRectangle.x + 1;
      final int x2 = this.arrowRectangle.x + this.arrowRectangle.width - 1;
      final int x3 = this.signalHover.getMiddleXpos();

      final double yOffset = this.arrowRectangle.getCenterY();
      final int y = ( int )( yOffset );

      if ( clip.contains( x1, y ) || clip.contains( x2, y ) )
      {
        g2d.setColor( Color.YELLOW );
        drawDoubleHeadedArrow( g2d, x1, y, x2 );
        // When given, show an additional arrowhead to denote the pulse
        // itself, taking care of the "smallest" pulse we're displaying...
        int dir = LEFT_FACING;
        if ( ( x3 >= x1 ) && ( ( x2 - x3 ) > ( x3 - x1 ) ) )
        {
          dir = RIGHT_FACING;
        }
        drawArrowHead( g2d, x3, y, dir, 8, 8 );
      }

      final float textXpos = ( x1 + ( ( x2 - x1 ) / 2.0f ) ) + 8;
      final float textYpos = ( float )( yOffset ) + 8;
      final String text = this.signalHover.toString();

      drawToolTip( g2d, text, textXpos, textYpos );
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
        RenderingHints.VALUE_INTERPOLATION_BICUBIC );
    hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    hints.put( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
    hints.put( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
    hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
    return hints;
  }

  /**
   * Draws a single arrow head
   * 
   * @param aG
   *          the canvas to draw on;
   * @param aXpos
   *          the X position of the arrow head;
   * @param aYpos
   *          the (center) Y position of the arrow head;
   * @param aFactor
   *          +1 to have a left-facing arrow head, -1 to have a right-facing
   *          arrow head;
   * @param aArrowWidth
   *          the total width of the arrow head;
   * @param aArrowHeight
   *          the total height of the arrow head.
   */
  private void drawArrowHead( final Graphics2D aG, final int aXpos, final int aYpos, final int aFactor,
      final int aArrowWidth, final int aArrowHeight )
  {
    final double halfHeight = aArrowHeight / 2.0;
    final int x1 = aXpos + ( aFactor * aArrowWidth );
    final int y1 = ( int )Math.ceil( aYpos - halfHeight );
    final int y2 = ( int )Math.floor( aYpos + halfHeight );

    final Polygon arrowHead = new Polygon();
    arrowHead.addPoint( aXpos, aYpos );
    arrowHead.addPoint( x1, y1 );
    arrowHead.addPoint( x1, y2 );

    aG.fill( arrowHead );
  }

  /**
   * Draws a double headed arrow of 8x8.
   * 
   * @param aG
   *          the canvas to draw on;
   * @param aX1
   *          the starting X position of the arrow;
   * @param aY
   *          the starting Y position of the arrow;
   * @param aX2
   *          the ending X position of the arrow.
   */
  private void drawDoubleHeadedArrow( final Graphics aG, final int aX1, final int aY, final int aX2 )
  {
    drawDoubleHeadedArrow( aG, aX1, aY, aX2, aY );
  }

  /**
   * Draws a double headed arrow of 8x8.
   * 
   * @param aG
   *          the canvas to draw on;
   * @param aX1
   *          the starting X position of the arrow;
   * @param aY1
   *          the starting Y position of the arrow;
   * @param aX2
   *          the ending X position of the arrow;
   * @param aY2
   *          the ending Y position of the arrow.
   */
  private void drawDoubleHeadedArrow( final Graphics aG, final int aX1, final int aY1, final int aX2, final int aY2 )
  {
    drawDoubleHeadedArrow( aG, aX1, aY1, aX2, aY2, 8, 8 );
  }

  /**
   * Draws a double headed arrow with arrow heads of a given width and height.
   * 
   * @param aG
   *          the canvas to draw on;
   * @param aX1
   *          the starting X position of the arrow;
   * @param aY1
   *          the starting Y position of the arrow;
   * @param aX2
   *          the ending X position of the arrow;
   * @param aY2
   *          the ending Y position of the arrow;
   * @param aArrowWidth
   *          the total width of the arrow head;
   * @param aArrowHeight
   *          the total height of the arrow head.
   */
  private void drawDoubleHeadedArrow( final Graphics aG, final int aX1, final int aY1, final int aX2, final int aY2,
      final int aArrowWidth, final int aArrowHeight )
  {
    final Graphics2D g2d = ( Graphics2D )aG.create();

    final int lineWidth = Math.abs( aX2 - aX1 );
    final int threshold = ( 2 * aArrowWidth ) + 2;
    try
    {
      int x1 = aX1;
      int x2 = aX2;

      if ( lineWidth > threshold )
      {
        drawArrowHead( g2d, aX1, aY1, LEFT_FACING, aArrowWidth, aArrowHeight );
        // why x2 needs to be shifted by one pixel is beyond me...
        drawArrowHead( g2d, aX2 + 1, aY2, RIGHT_FACING, aArrowWidth, aArrowHeight );

        x1 += aArrowWidth - 1;
        x2 -= aArrowWidth + 1;
      }

      g2d.drawLine( x1, aY1, x2, aY2 );
    }
    finally
    {
      g2d.dispose();
    }
  }

  /**
   * Draws the tooltip text at the given X,Y position, using a simple line
   * wrapping mechanism.
   * 
   * @param aCanvas
   *          the canvas to draw on;
   * @param aText
   *          the tooltip text to draw;
   * @param aXpos
   *          the X-position to draw the tooltip text;
   * @param aYpos
   *          the Y-position to draw the tooltip text.
   */
  private void drawToolTip( final Graphics2D aCanvas, final String aText, final float aXpos, final float aYpos )
  {
    aCanvas.setFont( this.textFont );

    float linePos = 0;
    float width = 0;

    List<ToolTipTextPart> textParts = new ArrayList<ToolTipTextPart>();
    TextLayout layout = null;

    final SimpleLineMeasurer lineMeasurer = new SimpleLineMeasurer( aText, aCanvas.getFontRenderContext() );
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

    final Rectangle rect = new Rectangle( ( int )aXpos, ( int )aYpos, ( int )width + 8, ( int )linePos + 8 );
    // Fit as much of the tooltip on screen as possible...
    ensureRectangleWithinBounds( rect, getViewBounds() );

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

    this.textRectangle.setBounds( rect );
  }

  /**
   * Returns the view boundaries.
   * 
   * @return a view boundaries, never <code>null</code>.
   */
  private Rectangle getViewBounds()
  {
    Rectangle result = getBounds();
    JViewport viewPort = ( JViewport )SwingUtilities.getAncestorOfClass( JViewport.class, this );
    if ( viewPort != null )
    {
      result = viewPort.getViewRect();
    }

    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Insets screenInsets = toolkit.getScreenInsets( getGraphicsConfiguration() );

    // Take into account screen insets, decrease viewport
    result.x += screenInsets.left;
    result.y += screenInsets.top;
    result.width -= ( screenInsets.left + screenInsets.right );
    result.height -= ( screenInsets.top + screenInsets.bottom );
    // Make sure the width of this component is taken into account...
    result.width = Math.min( getWidth(), result.width );
    // Make sure the height of this component is taken into account...
    result.height = Math.min( getHeight(), result.height );

    return result;
  }

  /**
   * Repaints the areas that were affected by the last paint() call.
   */
  private void repaintPartially()
  {
    if ( !this.arrowRectangle.isEmpty() )
    {
      final int x = this.arrowRectangle.x - 10;
      final int y = this.arrowRectangle.y - 10;
      final int w = this.arrowRectangle.width + 20;
      final int h = this.arrowRectangle.height + 20;

      repaint( x, y, w, h );
    }
    if ( !this.textRectangle.isEmpty() )
    {
      final int x = this.textRectangle.x - 10;
      final int y = this.textRectangle.y - 10;
      final int w = this.textRectangle.width + 20;
      final int h = this.textRectangle.height + 20;

      repaint( x, y, w, h );
    }
  }
}
