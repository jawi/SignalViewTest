/**
 * 
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
