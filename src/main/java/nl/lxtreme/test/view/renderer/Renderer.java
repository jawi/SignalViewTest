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
 * Provides a renderer of a specific UI-part.
 * <p>
 * A renderer implementation should <em>not</em> be considered thread-safe, as
 * it can have mutable state (see {@link #setContext(Object...)}).
 * </p>
 */
public interface Renderer
{
  // METHODS

  /**
   * Sets the context in which this renderer should function.
   * 
   * @param aController
   *          the controller to use, never <code>null</code>;
   * @param aClipBounds
   *          the clip boundaries to use, cannot be <code>null</code>.
   */
  void initialize( final SignalDiagramController aController, final Rectangle aClipBounds );

  /**
   * Renders the UI-part on the given canvas. The renderer itself is responsible
   * for determining the absolute coordinates where it should render the
   * UI-part.
   * <p>
   * This method is a convenience for
   * <code>{@link #render(Graphics2D, int, int, Object...)}</code> with the
   * coordinates (0, 0).
   * </p>
   * 
   * @param aCanvas
   *          the canvas to use to render, never <code>null</code>.
   * @return the rectangle with the coordinates of the affected area on the
   *         given canvas, or <code>null</code> if the entire canvas is
   *         affected.
   */
  Rectangle render( final Graphics2D aCanvas );

  /**
   * Renders the UI-part on the given canvas.
   * 
   * @param aCanvas
   *          the canvas to use to render, never <code>null</code>;
   * @param aXpos
   *          the X-position, >= 0;
   * @param aYpos
   *          the Y-position, >= 0.
   * @return the rectangle with the coordinates of the affected area on the
   *         given canvas, or <code>null</code> if the entire canvas is
   *         affected.
   */
  Rectangle render( final Graphics2D aCanvas, final int aXpos, final int aYpos );

  /**
   * Sets the rendering context, allowing this renderer to be parameterized.
   * 
   * @param aParameters
   *          the additional (renderer-specific) parameters.
   */
  void setContext( final Object... aParameters );
}
