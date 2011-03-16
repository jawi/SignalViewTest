/**
 * 
 */
package nl.lxtreme.test.dnd;


import java.awt.datatransfer.*;
import java.io.*;


/**
 * @author jajans
 */
public class SampleRowTransferable implements Transferable
{
  // CONSTANTS

  public static final DataFlavor FLAVOR;

  static
  {
    try
    {
      FLAVOR = new DataFlavor( "image/x-sample-row;class=" + SampleRowTransferable.class.getCanonicalName() );
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
  public SampleRowTransferable( final int aRow )
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
