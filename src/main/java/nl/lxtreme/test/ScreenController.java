/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;

import javax.swing.*;


/**
 * @author jajans
 */
public final class ScreenController
{
  // CONSTANTS

  private static final int CURSOR_SENSITIVITY_AREA = 5;

  // VARIABLES

  private final DataModel dataModel;
  private final ScreenModel screenModel;

  private ModelView modelView;
  private CursorView cursorView;
  private ArrowView arrowView;

  // CONSTRUCTORS

  /**
   * @param aModel
   */
  public ScreenController( final DataModel aModel )
  {
    this.dataModel = aModel;
    this.screenModel = new ScreenModel( aModel.getWidth() );
  }

  // METHODS

  /**
	 * 
	 */
  public void disableSnapMode()
  {
    this.cursorView.setSnapMode( false );
  }

  /**
   * @param aCursorIdx
   * @param aPoint
   */
  public void dragCursor( final int aCursorIdx, final Point aPoint, final boolean aSnap )
  {
    final Point point = convertToPointOf( this.cursorView, aPoint );

    if ( aSnap )
    {
      final Rectangle signalHover = getSignalHover( aPoint );
      if ( signalHover != null )
      {
        point.x = signalHover.x;
        point.y = ( int )signalHover.getCenterY();
      }
    }

    this.cursorView.moveCursor( aCursorIdx, point );
  }

  /**
	 * 
	 */
  public void enableSnapMode()
  {
    this.cursorView.setSnapMode( true );
  }

  /**
   * Finds the cursor under the given point.
   * 
   * @param aPoint
   *          the coordinate of the potential cursor, cannot be
   *          <code>null</code>.
   * @return the cursor index, or -1 if not found.
   */
  public int findCursor( final Point aPoint )
  {
    final int[] cursors = this.dataModel.getCursors();

    final Point point = convertToPointOf( this.cursorView, aPoint );
    final int refIdx = toUnscaledScreenCoordinate( point );

    for ( int i = 0; i < cursors.length; i++ )
    {
      if ( ( refIdx > ( cursors[i] - CURSOR_SENSITIVITY_AREA ) )
          && ( refIdx < ( cursors[i] + CURSOR_SENSITIVITY_AREA ) ) )
      {
        return i;
      }
    }

    return -1;
  }

  /**
   * @return
   */
  public long getAbsoluteLength()
  {
    final long[] timestamps = this.dataModel.getTimestamps();
    return ( long )( ( timestamps[timestamps.length - 1] + 1 ) * this.screenModel.getZoomFactor() );
  }

  /**
   * @return
   */
  public DataModel getDataModel()
  {
    return this.dataModel;
  }

  /**
   * @return
   */
  public ScreenModel getScreenModel()
  {
    return this.screenModel;
  }

  /**
   * Returns the hover area of the signal under the given coordinate (= mouse
   * position).
   * 
   * @param aPoint
   *          the mouse coordinate to determine the signal rectangle for, cannot
   *          be <code>null</code>.
   * @return the rectangle of the signal the given coordinate contains,
   *         <code>null</code> if not found.
   */
  public Rectangle getSignalHover( final Point aPoint )
  {
    final int signalHeight = this.screenModel.getSignalHeight();
    final int channelHeight = this.screenModel.getChannelHeight();

    final int row = getSignalRow( aPoint );
    if ( row < 0 )
    {
      return null;
    }

    final int virtualRow = this.screenModel.toVirtualRow( row );

    final Rectangle rect = new Rectangle();
    rect.x = rect.width = 0;
    rect.y = ( virtualRow * channelHeight ) + signalHeight;
    rect.height = signalHeight;

    // find the reference time value; which is the "timestamp" under the
    // cursor...
    final int refIdx = toTimestampIndex( aPoint );

    final int[] values = this.dataModel.getValues();
    if ( ( refIdx >= 0 ) && ( refIdx < values.length ) )
    {
      final long[] timestamps = this.dataModel.getTimestamps();

      final int mask = ( 1 << row );
      final int refValue = ( values[refIdx] & mask );

      int idx = refIdx;
      do
      {
        idx--;
      }
      while ( ( idx >= 0 ) && ( ( values[idx] & mask ) == refValue ) );
      // convert the found index back to "screen" values...
      rect.x = toScaledScreenCoordinate( timestamps[Math.max( 0, idx )] ).x;

      idx = refIdx;
      do
      {
        idx++;
      }
      while ( ( idx < values.length ) && ( ( values[idx] & mask ) == refValue ) );
      // convert the found index back to "screen" values...
      rect.width = toScaledScreenCoordinate( timestamps[Math.min( idx, timestamps.length - 1 )] ).x - rect.x;
    }

    return rect;
  }

  /**
   * @param aCoordinate
   * @return
   */
  public int getSignalRow( final Point aCoordinate )
  {
    final int signalWidth = this.dataModel.getWidth();
    final int signalHeight = this.screenModel.getSignalHeight();
    final int channelHeight = this.screenModel.getChannelHeight();

    final int row = ( aCoordinate.y - signalHeight ) / channelHeight;
    if ( ( row < 0 ) || ( row > ( signalWidth - 1 ) ) )
    {
      return -1;
    }

    final int realRow = this.screenModel.toRealRow( row );
    System.out.println( "signal row, calculated = " + row + ", real = " + realRow );
    return realRow;
  }

  /**
   * @param aPoint
   * @return
   */
  public boolean hideHover()
  {
    this.arrowView.hideHover();
    return false;
  }

  /**
   * @return
   */
  public boolean isSnapModeEnabled()
  {
    return this.cursorView.isSnapModeEnabled();
  }

  /**
   * @param aPoint
   */
  public void moveHover( final Point aPoint )
  {
    final Rectangle signalHover = getSignalHover( convertToPointOf( this.modelView, aPoint ) );
    this.arrowView.moveHover( signalHover );
  }

  /**
   * Moves a given sample row to another position.
   * 
   * @param aMovedRow
   *          the real row that is to be moved;
   * @param aInsertRow
   *          the real row that the moved row is moved to.
   */
  public void moveSampleRows( final int aMovedRow, final int aInsertRow )
  {
    if ( aMovedRow == aInsertRow )
    {
      return;
    }

    final int row = this.screenModel.toVirtualRow( aMovedRow );
    final int newRow = this.screenModel.toVirtualRow( aInsertRow );

    System.out.println( "Dropped row from " + row + " to " + newRow );

    this.screenModel.swapVirtualRows( row, newRow );

    final JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class, this.arrowView );
    if ( scrollPane != null )
    {
      final int signalHeight = this.screenModel.getSignalHeight();
      final int channelHeight = this.screenModel.getChannelHeight();

      final Rectangle rect = scrollPane.getVisibleRect();
      rect.y = ( row * channelHeight ) + signalHeight - 1;
      rect.height = signalHeight + 2;
      scrollPane.repaint( rect );

      rect.y = ( newRow * channelHeight ) + signalHeight - 1;
      rect.height = signalHeight + 2;
      scrollPane.repaint( rect );
    }
  }

  /**
	 * 
	 */
  public void recalculateDimensions()
  {
    final JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class, this.arrowView );
    if ( scrollPane != null )
    {
      final int width = ( int )Math.min( Integer.MAX_VALUE, getAbsoluteLength() );

      final int height = this.screenModel.getChannelHeight() * this.dataModel.getWidth()
          + this.screenModel.getSignalHeight();

      final Dimension newSize = new Dimension( width, height );

      final JComponent view = ( JComponent )scrollPane.getViewport().getView();
      view.setPreferredSize( newSize );
      view.revalidate();

      scrollPane.repaint();
    }
  }

  /**
   * @param aPoint
   * @return
   */
  public boolean showHover( final Point aPoint )
  {
    final Rectangle signalHover = getSignalHover( convertToPointOf( this.modelView, aPoint ) );
    this.arrowView.showHover( signalHover );

    return true;
  }

  /**
   * @param aTimestamp
   * @return
   */
  public Point toScaledScreenCoordinate( final long aTimestamp )
  {
    final int xPos = ( int )( this.screenModel.getZoomFactor() * aTimestamp );
    final int yPos = 0;
    return new Point( xPos, yPos );
  }

  /**
   * Finds the timestamp that corresponds to the X-position of the given
   * coordinate.
   * 
   * @param aPoint
   *          the screen coordinate to find a corresponding timestamp for,
   *          cannot be <code>null</code>.
   * @return a timestamp corresponding to the given coordinate, or -1 if no such
   *         timestamp could be found.
   */
  public int toTimestampIndex( final Point aPoint )
  {
    return this.dataModel.getTimestampIndex( toUnscaledScreenCoordinate( aPoint ) );
  }

  /**
   * @param aXpos
   * @return
   */
  public int toUnscaledScreenCoordinate( final Point aPoint )
  {
    return ( int )Math.abs( aPoint.getX() / this.screenModel.getZoomFactor() );
  }

  /**
	 * 
	 */
  public void zoomAll( final Dimension aViewSize )
  {
    this.screenModel.setZoomFactor( aViewSize.getWidth() / this.dataModel.getAbsoluteLength() );

    recalculateDimensions();
  }

  /**
   * Zooms in with a factor 1.5
   */
  public void zoomIn()
  {
    zoomRelative( 1.5 );

    recalculateDimensions();
  }

  /**
   * 
   */
  public void zoomOriginal()
  {
    zoomAbsolute( 1.0 );
    recalculateDimensions();
  }

  /**
   * Zooms out with a factor 1.5
   */
  public void zoomOut()
  {
    zoomRelative( 1.0 / 1.5 );

    recalculateDimensions();
  }

  /**
   * @param aArrowView
   */
  final void setArrowView( final ArrowView aArrowView )
  {
    this.arrowView = aArrowView;
  }

  /**
   * @param aCursorView
   */
  final void setCursorView( final CursorView aCursorView )
  {
    this.cursorView = aCursorView;
  }

  /**
   * @param aModelView
   */
  final void setModelView( final ModelView aModelView )
  {
    this.modelView = aModelView;
  }

  /**
   * @param aDestination
   * @param aOriginal
   * @return
   */
  private Point convertToPointOf( final Component aDestination, final Point aOriginal )
  {
    Component view = SwingUtilities.getAncestorOfClass( JScrollPane.class, aDestination );
    if ( view instanceof JScrollPane )
    {
      view = ( ( JScrollPane )view ).getViewport().getView();
    }
    else
    {
      view = SwingUtilities.getRootPane( aDestination );
    }
    return SwingUtilities.convertPoint( view, aOriginal, aDestination );
  }

  /**
   * @param aFactor
   */
  private void zoomAbsolute( final double aFactor )
  {
    this.screenModel.setZoomFactor( aFactor );
    System.out.println( "Zoom factor = " + this.screenModel.getZoomFactor() );
  }

  /**
   * @param aFactor
   */
  private void zoomRelative( final double aFactor )
  {
    final double factor = this.screenModel.getZoomFactor();
    zoomAbsolute( aFactor * factor );
  }
}
