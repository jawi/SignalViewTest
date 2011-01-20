/**
 * 
 */
package nl.lxtreme.test;


/**
 * @author jajans
 */
public class DataModel
{
  private final int[] values;
  private final int[] timestamps;
  private final int[] cursors;

  /**
	 * 
	 */
  public DataModel( final int aSize )
  {
    if ( aSize <= 0 )
    {
      throw new IllegalArgumentException();
    }
    this.values = new int[aSize];
    this.timestamps = new int[aSize];

    int value = 0xAA;
    for ( int i = 0; i < aSize; i++ )
    {
      if ( ( i % 4000000 ) == 0 )
      {
        value = ( value == 0xAA ) ? 0x00 : 0xAA;
      }

      this.values[i] = value;
      this.timestamps[i] = 2 * i;
    }
    // for ( int i = 0; i < aSize; i++ )
    // {
    // this.values[i] = ( i % 1024 ) + 2;
    // this.timestamps[i] = i;
    // }

    this.cursors = new int[] { 100, 200 };
  }

  /**
   * Provides a binary search for arrays of long-values.
   * <p>
   * This implementation is directly copied from the JDK
   * {@link Arrays#binarySearch(long[], long)} implementation, slightly modified
   * to only perform a single comparison-action.
   * </p>
   * 
   * @param aArray
   *          the array of long values to search in;
   * @param aFromIndex
   *          the from index to search from;
   * @param aToIndex
   *          the to index to search up and until;
   * @param aKey
   *          the value to search for.
   * @return the index of the given key, which is either the greatest index of
   *         the value less or equal to the given key.
   * @see Arrays#binarySearch(long[], long)
   */
  static final int binarySearch( final int[] aArray, final int aFromIndex, final int aToIndex, final Integer aKey )
  {
    int mid = -1;
    int low = aFromIndex;
    int high = aToIndex - 1;

    while ( low <= high )
    {
      mid = ( low + high ) >>> 1;
      final Integer midVal = aArray[mid];

      final int c = aKey.compareTo( midVal );
      if ( c > 0 )
      {
        low = mid + 1;
      }
      else if ( c < 0 )
      {
        high = mid - 1;
      }
      else
      {
        return mid; // key found
      }
    }

    if ( mid < 0 )
    {
      return low;
    }

    // Determine the insertion point, avoid crossing the array boundaries...
    if ( mid < aToIndex - 1 )
    {
      // If the searched value is greater than the value of the found index,
      // insert it after this value, otherwise before it (= the last return)...
      if ( aKey > aArray[mid] )
      {
        return mid + 1;
      }
    }

    return mid;
  }

  public int getAbsoluteLength()
  {
    int idx = this.timestamps.length - 1;
    if ( idx >= 0 )
    {
      return this.timestamps[idx] + 1;
    }
    return -1;
  }

  public int[] getCursors()
  {
    return this.cursors;
  }

  public int getSize()
  {
    return this.values.length;
  }

  public int getTimestampIndex( final int aValue )
  {
    return binarySearch( this.timestamps, 0, this.timestamps.length, aValue );
  }

  public int[] getTimestamps()
  {
    return this.timestamps;
  }

  public int[] getValues()
  {
    return this.values;
  }

  public int getWidth()
  {
    return 9;
  }
}