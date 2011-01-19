/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;

import javax.swing.JComponent;


/**
 * @author jajans
 */
public class ArrowView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final int LEFT_FACING = 1;
  private static final int RIGHT_FACING = -1;

  // VARIABLES

  private volatile Rectangle rectangle;

  // CONSTRUCTORS

  /**
   * Creates a new ArrowView instance.
   */
  public ArrowView()
  {
    setOpaque( false );
  }

  // METHODS

  /**
   * Hides the hover from screen.
   */
  public void hideHover()
  {
    repaintPartially();
    this.rectangle = null;
  }

  /**
   * Moves the hover on screen.
   * 
   * @param aRectangle
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void moveHover( final Rectangle aRectangle )
  {
    repaintPartially();
    this.rectangle = aRectangle;
    repaintPartially();
  }

  /**
   * Shows the hover from screen.
   * 
   * @param aRectangle
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void showHover( final Rectangle aRectangle )
  {
    this.rectangle = aRectangle;
    repaintPartially();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    if ( this.rectangle == null )
    {
      return;
    }

    final Graphics2D g2d = ( Graphics2D )aGraphics;
    final Rectangle clip = g2d.getClipBounds();

    final int x1 = this.rectangle.x + 2;
    final int x2 = this.rectangle.x + this.rectangle.width - 2;
    final double yOffset = this.rectangle.getCenterY();
    final int y = ( int )( yOffset );

    if ( clip.contains( x1, y ) || clip.contains( x2, y ) )
    {
      g2d.setColor( Color.YELLOW );
      drawDoubleHeadedArrow( g2d, x1, y, x2 );
    }
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
   * Repaints the areas that were affected by the last paint() call.
   */
  private void repaintPartially()
  {
    if ( this.rectangle != null )
    {
      final int x = this.rectangle.x - 1;
      final int y = this.rectangle.y - 1;
      final int w = this.rectangle.width + 2;
      final int h = this.rectangle.height + 2;

      repaint( x, y, w, h );
    }
  }
}
