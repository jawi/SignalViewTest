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
 * 
 * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
 */
package nl.lxtreme.test.view;


import java.util.logging.*;

import nl.lxtreme.test.view.laf.*;


/**
 * Presents a view/layer that shows the measurement information.
 */
public class MeasurementView extends AbstractViewLayer
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger( MeasurementView.class.getName() );

  // CONSTRUCTORS

  /**
   * Creates a new MeasurementView instance.
   */
  public MeasurementView( final SignalDiagramController aController )
  {
    super( aController );
    setOpaque( false );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    super.addNotify();
    updateUI();
  }

  /**
   * Hides the hover from screen.
   */
  public void hideHover()
  {
    LOG.fine( "Hiding measurement hover..." );

    ( ( MeasurementUI )this.ui ).hideHover( this );
  }

  /**
   * Moves the hover on screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void moveHover( final SignalHoverInfo aSignalHover )
  {
    LOG.fine( "Moving measurement hover..." );

    ( ( MeasurementUI )this.ui ).moveHover( aSignalHover, this );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    setUI( null );
    super.removeNotify();
  }

  /**
   * Shows the hover from screen.
   * 
   * @param aSignalHover
   *          the rectangle of the sample to draw, cannot be <code>null</code>.
   */
  public void showHover( final SignalHoverInfo aSignalHover )
  {
    LOG.fine( "Showing measurement hover..." );

    ( ( MeasurementUI )this.ui ).showHover( aSignalHover, this );
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
    setUI( new MeasurementUI() );
  }
}
