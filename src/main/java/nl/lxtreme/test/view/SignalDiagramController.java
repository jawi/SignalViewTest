/**
 * 
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.model.*;


/**
 * @author jajans
 */
public final class SignalDiagramController
{
  // INNER TYPES

  /**
   * @author Quintor
   */
  static class SignalHoverInfo implements Cloneable
  {
    public final Rectangle rectangle;
    public final int firstSample;
    public final int lastSample;
    public final int referenceSample;
    public final int channelIdx;

    public SignalHoverInfo( final Rectangle aRectangle, final int aFirstSample, final int aLastSample,
        final int aReferenceSample, final int aChannelIdx )
    {
      this.rectangle = aRectangle;
      this.firstSample = aFirstSample;
      this.lastSample = aLastSample;
      this.referenceSample = aReferenceSample;
      this.channelIdx = aChannelIdx;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public SignalHoverInfo clone()
    {
      try
      {
        return ( SignalHoverInfo )super.clone();
      }
      catch ( CloneNotSupportedException exception )
      {
        throw new RuntimeException( exception );
      }
    }
  }

  // CONSTANTS

  /**
   * Defines the area around each cursor in which the mouse cursor should be in
   * before the cursor can be moved.
   */
  private static final int CURSOR_SENSITIVITY_AREA = 4;

  // VARIABLES

  private final DataModel dataModel;
  private final ScreenModel screenModel;

  private SignalView signalView;
  private CursorView cursorView;
  private ArrowView arrowView;

  // CONSTRUCTORS

  /**
   * @param aModel
   */
  public SignalDiagramController( final DataModel aModel )
  {
    this.dataModel = aModel;
    this.screenModel = new ScreenModel( aModel.getWidth() );
  }

  // METHODS

  /**
   * Disables the cursor "snap" mode.
   * 
   * @see #enableSnapMode()
   */
  public void disableSnapMode()
  {
    this.cursorView.setSnapMode( false );
  }

  /**
   * Drags a cursor with a given index to a given point, possibly snapping to a
   * signal edge.
   * 
   * @param aCursorIdx
   *          the cursor index to move, should be &gt;= 0 && &lt; 10;
   * @param aPoint
   *          the new point of the cursor, in case of snapping, it will use this
   *          point to find the nearest signal edge, cannot be <code>null</code>
   *          ;
   * @param aSnap
   *          <code>true</code> if the cursor should be snapped to the nearest
   *          signal edge, <code>false</code> otherwise.
   */
  public void dragCursor( final int aCursorIdx, final Point aPoint, final boolean aSnap )
  {
    final Point point = convertToPointOf( this.cursorView, aPoint );

    if ( aSnap )
    {
      final SignalHoverInfo signalHover = getSignalHover( aPoint );
      if ( signalHover != null )
      {
        point.x = signalHover.rectangle.x;
        point.y = ( int )signalHover.rectangle.getCenterY();
      }
    }

    this.cursorView.moveCursor( aCursorIdx, point );
  }

  /**
   * Enables the cursor "snap" mode.
   * 
   * @see #disableSnapMode()
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
    final Point point = convertToPointOf( this.cursorView, aPoint );
    final int refIdx = toUnscaledScreenCoordinate( point );

    final double snapArea = CURSOR_SENSITIVITY_AREA / this.screenModel.getZoomFactor();

    final int[] cursors = this.dataModel.getCursors();
    for ( int i = 0; i < cursors.length; i++ )
    {
      final double min = cursors[i] - snapArea;
      final double max = cursors[i] + snapArea;

      if ( ( refIdx >= min ) && ( refIdx <= max ) )
      {
        return i;
      }
    }

    return -1;
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
   * Determines the signal row beneath the given coordinate.
   * <p>
   * This method returns the <em>virtual</em> signal row, not the actual signal
   * row.
   * </p>
   * 
   * @param aCoordinate
   *          the coordinate to return the signal row for, cannot be
   *          <code>null</code>.
   * @return a signal row, or -1 if the point is nowhere near a signal row.
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

    return this.screenModel.toVirtualRow( row );
  }

  /**
   * Returns the actual time interval of the samples denoted by the given start
   * and end index, or <tt>T[end index] - T[start index]</tt>.
   * 
   * @param aStartIdx
   *          the start sample index of the time interval, >= 0;
   * @param aEndIdx
   *          the end sample index of the time interval, >= 0.
   * @return a time interval value, in seconds.
   */
  public double getTimeInterval( final int aStartIdx, final int aEndIdx )
  {
    final long[] timestamps = this.dataModel.getTimestamps();
    final long relTime = timestamps[aEndIdx] - timestamps[aStartIdx];
    final double absTime = relTime / ( double )this.dataModel.getSampleRate();
    return absTime;
  }

  /**
   * Returns the time value of the sample denoted by the given index.
   * 
   * @param aSampleIdx
   *          the sample index to return the time value for, >= 0.
   * @return a time value, in seconds.
   */
  public double getTimeValue( final int aSampleIdx )
  {
    final long[] timestamps = this.dataModel.getTimestamps();
    final long relTime = timestamps[aSampleIdx];
    final double absTime = relTime / ( double )this.dataModel.getSampleRate();
    return absTime;
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
  public boolean isMeasurementMode()
  {
    return this.screenModel.isMeasurementMode();
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
    final SignalHoverInfo signalHover = getSignalHover( convertToPointOf( this.signalView, aPoint ) );

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

    final int row = this.screenModel.toRealRow( aMovedRow );
    final int newRow = this.screenModel.toRealRow( aInsertRow );

    this.screenModel.moveRows( row, newRow );

    final JScrollPane scrollPane = ( JScrollPane )SwingUtilities.getAncestorOfClass( JScrollPane.class, this.arrowView );
    if ( scrollPane != null )
    {
      final int signalHeight = this.screenModel.getSignalHeight();
      final int channelHeight = this.screenModel.getChannelHeight();

      final Rectangle rect = scrollPane.getVisibleRect();
      rect.y = ( row * channelHeight ) + signalHeight - 3;
      rect.height = channelHeight + 6;
      scrollPane.repaint( rect );

      rect.y = ( newRow * channelHeight ) + signalHeight - 3;
      rect.height = channelHeight + 6;
      scrollPane.repaint( rect );
    }
  }

  /**
   * Recalculates the dimensions of the main view.
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
   * Turns the visibility of all cursors either on or off.
   * <p>
   * This method does <em>not</em> modify any cursor, only whether they are
   * displayed or not!
   * </p>
   * 
   * @param aVisible
   *          <code>true</code> if the cursors should be made visible,
   *          <code>false</code> if the cursors should be made invisible.
   */
  public void setCursorsVisible( final boolean aVisible )
  {
    this.cursorView.setCursorMode( aVisible );
    this.cursorView.repaint( 25L );
  }

  /**
   * Enables or disables the measurement mode.
   * 
   * @param aEnabled
   *          <code>true</code> to enable the measurement mode,
   *          <code>false</code> to disable this mode.
   */
  public void setMeasurementMode( final boolean aEnabled )
  {
    this.screenModel.setMeasurementMode( aEnabled );
  }

  /**
   * Shows the hover information for the given coordinate.
   * 
   * @param aPoint
   *          the coordinate to show the hover information for, cannot be
   *          <code>null</code>.
   * @return <code>true</code> if the hover is going to be shown,
   *         <code>false</code> otherwise.
   */
  public boolean showHover( final Point aPoint )
  {
    final SignalHoverInfo signalHover = getSignalHover( convertToPointOf( this.signalView, aPoint ) );
    if ( signalHover != null )
    {
      this.arrowView.showHover( signalHover );
    }

    return ( signalHover != null );
  }

  /**
   * Converts a given time stamp value to a screen coordinate.
   * 
   * @param aTimestamp
   *          the time stamp <em>value</em> to convert, >= 0.
   * @return a coordinate whose X-position denotes the given time stamp, and a
   *         Y-position of 0, never <code>null</code>.
   */
  public Point toScaledScreenCoordinate( final long aTimestamp )
  {
    final int xPos = ( int )( this.screenModel.getZoomFactor() * aTimestamp );
    final int yPos = 0;
    return new Point( xPos, yPos );
  }

  /**
   * Finds the time stamp that corresponds to the X-position of the given
   * coordinate.
   * 
   * @param aPoint
   *          the screen coordinate to find a corresponding time stamp for,
   *          cannot be <code>null</code>.
   * @return a time stamp corresponding to the given coordinate, or -1 if no
   *         such time stamp could be found.
   */
  public int toTimestampIndex( final Point aPoint )
  {
    return this.dataModel.getTimestampIndex( toUnscaledScreenCoordinate( aPoint ) );
  }

  /**
   * Converts a given screen coordinate to an unscaled value (regardless zoom
   * factor).
   * 
   * @param aPoint
   *          the screen coordinate to convert, cannot be <code>null</code>.
   * @return an unscaled version of the given coordinate's X-position, >= 0.
   */
  public int toUnscaledScreenCoordinate( final Point aPoint )
  {
    return ( int )Math.ceil( Math.abs( aPoint.getX() / this.screenModel.getZoomFactor() ) );
  }

  /**
   * Zooms to make all data visible in one screen.
   * 
   * @param aViewSize
   *          the original view size in which all data should fit, cannot be
   *          <code>null</code>.
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
   * Zooms to a factor of 1.0.
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
   * Sets the actual arrow view instance to is to be managed by this controller.
   * 
   * @param aArrowView
   *          an arrow view instance, cannot be <code>null</code>.
   */
  final void setArrowView( final ArrowView aArrowView )
  {
    this.arrowView = aArrowView;
  }

  /**
   * Sets the actual cursor view instance to is to be managed by this
   * controller.
   * 
   * @param aCursorView
   *          a cursor view instance, cannot be <code>null</code>.
   */
  final void setCursorView( final CursorView aCursorView )
  {
    this.cursorView = aCursorView;
  }

  /**
   * Sets the actual signal view instance to is to be managed by this
   * controller.
   * 
   * @param aSignalView
   *          a signal view instance, cannot be <code>null</code>.
   */
  final void setSignalView( final SignalView aSignalView )
  {
    this.signalView = aSignalView;
  }

  /**
   * Converts a given point to the coordinate space of a given destination
   * component.
   * 
   * @param aDestination
   *          the destination component to convert the point to, cannot be
   *          <code>null</code>;
   * @param aOriginal
   *          the original point to convert, cannot be <code>null</code>.
   * @return the converted point, never <code>null</code>.
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
   * Returns the "visual length" of the timeline.
   * 
   * @return a visual length, >= 0.
   */
  private long getAbsoluteLength()
  {
    final long[] timestamps = this.dataModel.getTimestamps();
    return ( long )( ( timestamps[timestamps.length - 1] + 1 ) * this.screenModel.getZoomFactor() );
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
  private SignalHoverInfo getSignalHover( final Point aPoint )
  {
    final int signalWidth = this.dataModel.getWidth();
    final int signalHeight = this.screenModel.getSignalHeight();
    final int channelHeight = this.screenModel.getChannelHeight();

    final int virtualRow = ( aPoint.y - signalHeight ) / channelHeight;
    if ( ( virtualRow < 0 ) || ( virtualRow > ( signalWidth - 1 ) ) )
    {
      return null;
    }

    final int realRow = this.screenModel.toRealRow( virtualRow );

    final Rectangle rect = new Rectangle();
    rect.x = rect.width = 0;
    rect.y = ( virtualRow * channelHeight ) + signalHeight;
    rect.height = signalHeight;

    // find the reference time value; which is the "timestamp" under the
    // cursor...
    final int refIdx = toTimestampIndex( aPoint );

    int firstSample = -1;
    int lastSample = -1;

    final int[] values = this.dataModel.getValues();
    if ( ( refIdx >= 0 ) && ( refIdx < values.length ) )
    {
      final long[] timestamps = this.dataModel.getTimestamps();

      final int mask = ( 1 << realRow );
      final int refValue = ( values[refIdx] & mask );

      int idx = refIdx;
      do
      {
        idx--;
      }
      while ( ( idx >= 0 ) && ( ( values[idx] & mask ) == refValue ) );
      // Search for the original value again, to complete the pulse...
      do
      {
        idx--;
      }
      while ( ( idx >= 0 ) && ( ( values[idx] & mask ) != refValue ) );

      // convert the found index back to "screen" values...
      firstSample = Math.max( 0, idx + 1 );
      rect.x = toScaledScreenCoordinate( timestamps[firstSample] ).x;

      idx = refIdx;
      do
      {
        idx++;
      }
      while ( ( idx < values.length ) && ( ( values[idx] & mask ) == refValue ) );

      // convert the found index back to "screen" values...
      lastSample = Math.min( idx, timestamps.length - 1 );
      rect.width = toScaledScreenCoordinate( timestamps[lastSample] ).x - rect.x;
    }

    return new SignalHoverInfo( rect, firstSample, lastSample, refIdx, realRow );
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
