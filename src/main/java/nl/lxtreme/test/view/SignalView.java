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
package nl.lxtreme.test.view;


import java.awt.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.view.laf.*;
import nl.lxtreme.test.view.model.*;


/**
 * Provides a view for the signal data as individual channels.
 */
public class SignalView extends AbstractViewLayer implements IMeasurementListener, ICursorChangeListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalViewModel model;

  // CONSTRUCTORS

  /**
   * Creates a new {@link SignalView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  private SignalView( final SignalDiagramController aController )
  {
    super( aController );

    this.model = new SignalViewModel( aController );

    updateUI();
  }

  // METHODS

  /**
   * Creates a new {@link SignalView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   * @return a {@link SignalView} instance, never <code>null</code>.
   */
  public static SignalView create( final SignalDiagramController aController )
  {
    SignalView result = new SignalView( aController );

    aController.addCursorChangeListener( result );
    aController.addMeasurementListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    updateUI();

    super.addNotify();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorAdded( final int aCursorIdx, final long aCursorTimestamp )
  {
    final int visibleHeight = getVisibleRect().height;

    final SignalViewModel model = getModel();

    int cursorPos = model.timestampToCoordinate( aCursorTimestamp );
    repaint( new Rectangle( cursorPos - 1, 0, 2, visibleHeight ) );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorChanged( final int aCursorIdx, final long aOldCursorTimestamp, final long aNewCursorTimestamp )
  {
    final int visibleHeight = getVisibleRect().height;

    final SignalViewModel model = getModel();

    int cursorPos = model.timestampToCoordinate( aOldCursorTimestamp );
    repaint( 0, cursorPos - 1, 0, 2, visibleHeight );

    cursorPos = model.timestampToCoordinate( aNewCursorTimestamp );
    repaint( 0, cursorPos - 1, 0, 2, visibleHeight );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorRemoved( final int aCursorIdx, final long aOldCursorTimestamp )
  {
    final int visibleHeight = getVisibleRect().height;

    final SignalViewModel model = getModel();

    int cursorPos = model.timestampToCoordinate( aOldCursorTimestamp );
    repaint( 0, cursorPos - 1, 0, 2, visibleHeight );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsInvisible()
  {
    repaint( 50L );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsVisible()
  {
    repaint( 50L );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void disableMeasurementMode()
  {
    final SignalUI signalUI = ( SignalUI )this.ui;

    final Rectangle oldRect = signalUI.getMeasurementRect();
    if ( oldRect != null )
    {
      repaint( oldRect );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enableMeasurementMode()
  {
    // Nothing special to do for this event...
  }

  /**
   * Returns the current value of model.
   * 
   * @return the model
   */
  public SignalViewModel getModel()
  {
    return this.model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMeasureEvent( final SignalHoverInfo aEvent )
  {
    final SignalUI signalUI = ( SignalUI )this.ui;

    final Rectangle oldRect = signalUI.getMeasurementRect();

    signalUI.handleMeasureEvent( aEvent );

    final Rectangle newRect = signalUI.getMeasurementRect();

    if ( aEvent != null )
    {
      setToolTipText( aEvent.toHtmlString() );
    }
    else
    {
      setToolTipText( null );
    }

    if ( oldRect != null )
    {
      repaint( oldRect );
    }
    if ( newRect != null )
    {
      repaint( newRect );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isListening()
  {
    return ( ( SignalUI )this.ui ).isListening();
  }

  /**
   * Overridden in order to set a custom UI, which not only paints this diagram,
   * but also can be used to manage the various settings, such as colors,
   * height, and so on.
   * 
   * @see javax.swing.JComponent#updateUI()
   */
  @Override
  public final void updateUI()
  {
    setUI( new SignalUI() );
  }
}
