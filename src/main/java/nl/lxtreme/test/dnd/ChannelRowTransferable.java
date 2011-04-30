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
package nl.lxtreme.test.dnd;


import java.awt.datatransfer.*;
import java.io.*;


/**
 * @author jajans
 */
public class ChannelRowTransferable implements Transferable
{
  // CONSTANTS

  public static final DataFlavor FLAVOR;

  static
  {
    try
    {
      FLAVOR = new DataFlavor( "application/vnd.ols.x-channel-row;class="
          + ChannelRowTransferable.class.getCanonicalName() + ";humanPresentableName=OLS%20Channel%20Row" );
    }
    catch ( final ClassNotFoundException exception )
    {
      throw new RuntimeException( exception );
    }
  }

  // VARIABLES

  private final Integer row;

  // CONSTRUCTORS

  /**
   * @param aRow
   */
  public ChannelRowTransferable( final int aRow )
  {
    this.row = Integer.valueOf( aRow );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getTransferData( final DataFlavor aFlavor ) throws UnsupportedFlavorException, IOException
  {
    if ( aFlavor.equals( FLAVOR ) )
    {
      return this.row;
    }
    else
    {
      return new UnsupportedFlavorException( aFlavor );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[] { FLAVOR };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDataFlavorSupported( final DataFlavor aFlavor )
  {
    return aFlavor.isMimeTypeEqual( FLAVOR.getMimeType() );
  }
}
