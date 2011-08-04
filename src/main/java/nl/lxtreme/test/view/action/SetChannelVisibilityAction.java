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
package nl.lxtreme.test.view.action;


import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.model.*;


/**
 * Provides an action to set a channel either as visible or invisible.
 */
public class SetChannelVisibilityAction extends AbstractAction
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;
  private final int channelIdx;

  // CONSTRUCTORS

  /**
   * Creates a new SetChannelVisibilityAction instance.
   */
  public SetChannelVisibilityAction( final SignalDiagramController aController, final int aChannelIdx )
  {
    super();

    this.controller = aController;
    this.channelIdx = aChannelIdx;

    final SignalDiagramModel signalDiagramModel = aController.getSignalDiagramModel();

    putValue( Action.NAME, getLabel( signalDiagramModel, Integer.valueOf( aChannelIdx ) ) );
    putValue( Action.SELECTED_KEY, Boolean.valueOf( signalDiagramModel.isChannelVisible( aChannelIdx ) ) );
  }

  // METHODS

  /**
   * @param aSignalDiagramModel
   * @param aChannelIdx
   * @return
   */
  private static String getLabel( final SignalDiagramModel aSignalDiagramModel, final Integer aChannelIdx )
  {
    if ( aSignalDiagramModel.isChannelVisible( aChannelIdx.intValue() ) )
    {
      return String.format( "Hide channel %d", aChannelIdx );
    }

    return String.format( "Show channel %d", aChannelIdx );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed( final ActionEvent aEvent )
  {
    final JCheckBoxMenuItem menuitem = ( JCheckBoxMenuItem )aEvent.getSource();
    final SignalDiagramModel model = this.controller.getSignalDiagramModel();

    model.setChannelVisible( this.channelIdx, menuitem.getState() );
  }
}
