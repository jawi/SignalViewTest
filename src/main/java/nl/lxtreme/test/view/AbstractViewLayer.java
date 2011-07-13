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


import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.dnd.*;


/**
 * Provides a common base class for view components.
 */
abstract class AbstractViewLayer extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;

  // CONSTRUCTORS

  /**
   * Creates a new AbstractViewLayer instance.
   */
  public AbstractViewLayer(final SignalDiagramController aController)
  {
    this.controller = aController;
  }

  // METHODS

  /**
   * Adds the given measurement listener to the list of listeners.
   * 
   * @param aListener
   *          the listener to add, cannot be <code>null</code>.
   */
  public void addMeasurementListener(final IMeasurementListener aListener)
  {
    this.controller.addMeasurementListener(aListener);
  }

  /**
   * Returns the drag-and-drop target controller of this component.
   * 
   * @return a DnD target controller, never <code>null</code>.
   */
  public final DragAndDropTargetController getDnDTargetController()
  {
    return this.controller.getDndTargetController();
  }

  /**
   * Removes the given measurement listener from the list of listeners.
   * 
   * @param aListener
   *          the listener to remove, cannot be <code>null</code>.
   */
  public void removeMeasurementListener(final IMeasurementListener aListener)
  {
    this.controller.removeMeasurementListener(aListener);
  }
}
