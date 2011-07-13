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


import nl.lxtreme.test.view.laf.*;
import nl.lxtreme.test.view.model.*;


/**
 * Provides a time line view, displaying ticks at regular intervals along with
 * timing information.
 */
public class TimeLineView extends AbstractViewLayer
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private TimeLineViewModel model;

  // CONSTRUCTORS

  /**
   * Creates a new {@link TimeLineView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>.
   */
  public TimeLineView(final SignalDiagramController aController)
  {
    super(aController);

    this.model = new TimeLineViewModel(aController);

    updateUI();
  }

  // METHODS

  /**
   * @return
   */
  public TimeLineViewModel getModel()
  {
    return this.model;
  }

  /**
   * @return
   */
  public int getTimeLineHeight()
  {
    return getModel().getTimeLineHeight();
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
    setUI(new TimeLineUI());
  }
}
