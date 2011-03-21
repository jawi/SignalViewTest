/**
 * 
 */
package nl.lxtreme.test.dnd;


import java.awt.datatransfer.*;
import java.io.*;


/**
 * Provides a transferable for cursors.
 */
public class CursorTransferable implements Transferable
{
  // CONSTANTS

  public static final DataFlavor FLAVOR;

  static
  {
    try
    {
      FLAVOR = new DataFlavor( "application/vnd.ols.x-cursor-idx;class=" + CursorTransferable.class.getCanonicalName()
          + ";humanPresentableName=OLS%20Cursor" );
    }
    catch ( final ClassNotFoundException exception )
    {
      throw new RuntimeException( exception );
    }
  }

  // VARIABLES

  private final Integer cursorIdx;

  // CONSTRUCTORS

  /**
   * Creates a new CursorTransferable instance.
   * 
   * @param aCursorIdx
   *          the index of the cursor used in this transferable, >= 0.
   */
  public CursorTransferable( final int aCursorIdx )
  {
    this.cursorIdx = Integer.valueOf( aCursorIdx );
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
      return this.cursorIdx;
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
