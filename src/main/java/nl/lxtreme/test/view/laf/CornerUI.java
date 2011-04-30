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
package nl.lxtreme.test.view.laf;


import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.view.*;


/**
 * 
 */
public class CornerUI extends ComponentUI
{
  // CONSTANTS

  public static final String COMPONENT_BACKGROUND_COLOR = "timeline.color.background";

  // VARIABLES

  private Color backgroundColor;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void installUI( final JComponent aComponent )
  {
    final CornerView view = ( CornerView )aComponent;

    final IUserInterfaceSettingsProvider settingsProvider = view.getSettingsProvider();

    this.backgroundColor = settingsProvider.getColor( COMPONENT_BACKGROUND_COLOR );
    if ( this.backgroundColor == null )
    {
      this.backgroundColor = Utils.parseColor( "#1E2126" );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paint( final Graphics aGraphics, final JComponent aComponent )
  {
    final Rectangle clip = aGraphics.getClipBounds();

    ( ( Graphics2D )aGraphics ).setBackground( this.backgroundColor );
    aGraphics.clearRect( clip.x, clip.y, clip.width, clip.height );
  }
}
