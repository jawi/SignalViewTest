/**
 * 
 */
package nl.lxtreme.test.view;


import static nl.lxtreme.test.Utils.*;

import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.view.SignalDiagramController.SignalHoverInfo;


/**
 * @author jajans
 */
class ArrowView extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final int LEFT_FACING = 1;
  private static final int RIGHT_FACING = -1;

  // VARIABLES

  private final Rectangle textRectangle;
  private final Rectangle arrowRectangle;
  private final SignalDiagramController controller;

  private volatile SignalHoverInfo signalHover;

  // CONSTRUCTORS

  /**
   * Creates a new ArrowView instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public ArrowView( final SignalDiagramController aController )
  {
    this.controller = aController;

    this.arrowRectangle = new Rectangle();
    this.textRectangle = new Rectangle();

    aController.setArrowView( this );

    setOpaque( false );
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
    this.signalHover = aSignalHover.clone();
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
    this.signalHover = aSignalHover.clone();
    repaintPartially();
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

    final Graphics2D g2d = ( Graphics2D )aGraphics;
    final Rectangle clip = g2d.getClipBounds();

    //
    this.arrowRectangle.setBounds( this.signalHover.rectangle );

    final int x1 = this.arrowRectangle.x + 2;
    final int x2 = this.arrowRectangle.x + this.arrowRectangle.width - 2;
    final double yOffset = this.arrowRectangle.getCenterY();
    final int y = ( int )( yOffset );

    if ( clip.contains( x1, y ) || clip.contains( x2, y ) )
    {
      g2d.setColor( Color.YELLOW );
      drawDoubleHeadedArrow( g2d, x1, y, x2 );
    }

    final FontMetrics fm = g2d.getFontMetrics();

    final int startIdx = this.signalHover.firstSample;
    final int endIdx = this.signalHover.lastSample;
    final int timestampIdx = this.signalHover.referenceSample;

    final String pulseTime = "Width: " + displayTime( this.controller.getTimeInterval( startIdx, endIdx ) );
    final String sampleTime = "Time: " + displayTime( this.controller.getTimeValue( timestampIdx ) );
    final String channel = "Channel: " + String.format( "Channel: %d", Integer.valueOf( this.signalHover.channelIdx ) );

    this.textRectangle.x = ( int )( x1 + ( ( x2 - x1 ) / 2.0f ) ) + 8;
    this.textRectangle.y = ( int )( yOffset + 8 );
    this.textRectangle.width = Math.max( fm.stringWidth( channel ),
        Math.max( fm.stringWidth( pulseTime ), fm.stringWidth( sampleTime ) ) ) + 4;
    this.textRectangle.height = ( 3 * fm.getHeight() ) + 2;

    // Fit as much of the tooltip on screen as possible...
    ensureRectangleWithinBounds( this.textRectangle, getViewBounds() );

    g2d.setColor( Color.DARK_GRAY );
    g2d.fillRect( this.textRectangle.x, this.textRectangle.y, this.textRectangle.width, this.textRectangle.height );
    g2d.setColor( Color.LIGHT_GRAY );
    g2d.drawRect( this.textRectangle.x, this.textRectangle.y, this.textRectangle.width, this.textRectangle.height );

    g2d.setColor( Color.YELLOW );
    g2d.drawString( pulseTime, this.textRectangle.x + 2, ( int )( this.textRectangle.getCenterY()
        + ( fm.getHeight() / 2.0 ) - 20 ) );
    g2d.drawString( sampleTime, this.textRectangle.x + 2,
        ( int )( this.textRectangle.getCenterY() + fm.getHeight() - 10 ) );
    g2d.drawString( channel, this.textRectangle.x + 2,
        ( int )( this.textRectangle.getCenterY() + 2 * fm.getHeight() - 8 ) );
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
   * Returns the view boundaries.
   * 
   * @return a view boundaries, never <code>null</code>.
   */
  private Rectangle getViewBounds()
  {
    Component comp = SwingUtilities.getAncestorOfClass( JViewport.class, this );
    if ( comp == null )
    {
      comp = this;
    }

    final Rectangle result = comp.getBounds();
    final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets( comp.getGraphicsConfiguration() );

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
    if ( this.arrowRectangle != null )
    {
      final int x = this.arrowRectangle.x - 1;
      final int y = this.arrowRectangle.y - 1;
      final int w = this.arrowRectangle.width + 2;
      final int h = this.arrowRectangle.height + 2;

      repaint( x, y, w, h );
    }
    if ( this.textRectangle != null )
    {
      final int x = this.textRectangle.x - 1;
      final int y = this.textRectangle.y - 1;
      final int w = this.textRectangle.width + 2;
      final int h = this.textRectangle.height + 2;

      repaint( x, y, w, h );
    }
  }
}
