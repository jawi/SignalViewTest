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
package nl.lxtreme.test.view.renderer;


import java.awt.*;

import nl.lxtreme.test.view.*;


/**
 * Provides an abstract base class for a renderer.
 */
abstract class BaseRenderer implements Renderer
{
  // VARIABLES

  private SignalDiagramController controller;
  private Rectangle clipBounds;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize( final SignalDiagramController aController, final Rectangle aClipBounds )
  {
    this.controller = aController;
    this.clipBounds = aClipBounds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Rectangle render( final Graphics2D aCanvas, final int aXpos, final int aYpos )
  {
    // Move the canvas to the requested position...
    aCanvas.translate( aXpos, aYpos );

    try
    {
      final Rectangle result = render( aCanvas );
      result.translate( -aXpos, -aYpos );
      return result;
    }
    finally
    {
      // Move the canvas to the requested position...
      aCanvas.translate( -aXpos, -aYpos );
    }
  }

  /**
   * @return the clipBounds
   */
  protected final Rectangle getClipBounds()
  {
    return this.clipBounds;
  }

  /**
   * @return the controller
   */
  protected final SignalDiagramController getController()
  {
    return this.controller;
  }
}
