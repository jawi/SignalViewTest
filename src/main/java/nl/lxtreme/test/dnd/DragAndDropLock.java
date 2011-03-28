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
package nl.lxtreme.test.dnd;


import java.util.concurrent.atomic.*;


/**
 * Provides a convenience lock mechanism for keeping the correct administration
 * in the various drag-and-drop routines.
 */
public final class DragAndDropLock
{
  // CONSTANTS

  private static AtomicBoolean locked = new AtomicBoolean( false );
  private static AtomicBoolean startedDnD = new AtomicBoolean( false );

  // METHODS

  /**
   * @return
   */
  public static boolean isDragAndDropStarted()
  {
    return startedDnD.get();
  }

  /**
   * @return
   */
  public static boolean isLocked()
  {
    return locked.get();
  }

  /**
   * @param isLocked
   */
  public static void setDragAndDropStarted( final boolean isLocked )
  {
    startedDnD.set( isLocked );
  }

  /**
   * @param isLocked
   */
  public static void setLocked( final boolean isLocked )
  {
    locked.set( isLocked );
  }

}
