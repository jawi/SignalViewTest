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
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.test;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * Some easier alternatives for SwingUtilities methods.
 */
public final class SwingUtils
{
  // METHODS

  /**
   * Convenience method for searching above <code>comp</code> in the component
   * hierarchy and returns the first object of class <code>c</code> it finds.
   * Can return {@code null}, if a class <code>c</code> cannot be found.
   */
  @SuppressWarnings( "unchecked" )
  public static <TYPE extends Container> TYPE getAncestorOfClass( final Class<TYPE> aClass, final Component aComponent )
  {
    return ( TYPE )SwingUtilities.getAncestorOfClass( aClass, aComponent );
  }

  /**
   * Returns the deepest visible descendent Component of <code>parent</code>
   * that contains the location <code>x</code>, <code>y</code>. If
   * <code>parent</code> does not contain the specified location, then
   * <code>null</code> is returned. If <code>parent</code> is not a container,
   * or none of <code>parent</code>'s visible descendents contain the specified
   * location, <code>parent</code> is returned.
   * 
   * @param aParent
   *          the root component to begin the search
   * @param aXpos
   *          the x target location
   * @param aYpos
   *          the y target location
   */
  public static JComponent getDeepestComponentAt( final Component aParent, final int aXpos, final int aYpos )
  {
    return ( JComponent )SwingUtilities.getDeepestComponentAt( aParent, aXpos, aYpos );
  }

  /**
   * Returns the deepest visible descendent Component of <code>parent</code>
   * that contains the location <code>x</code>, <code>y</code>. If
   * <code>parent</code> does not contain the specified location, then
   * <code>null</code> is returned. If <code>parent</code> is not a container,
   * or none of <code>parent</code>'s visible descendents contain the specified
   * location, <code>parent</code> is returned.
   * 
   * @param aParent
   *          the root component to begin the search
   * @param aXpos
   *          the x target location
   * @param aYpos
   *          the y target location
   */
  public static JComponent getDeepestComponentAt( final MouseEvent aEvent )
  {
    return getDeepestComponentAt( aEvent.getComponent(), aEvent.getX(), aEvent.getY() );
  }

}