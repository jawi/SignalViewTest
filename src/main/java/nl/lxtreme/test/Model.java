/**
 * 
 */
package nl.lxtreme.test;

/**
 * @author jajans
 * 
 */
public class Model
{
	private final int[] values;
	private final int[] timestamps;
	private final int[] cursors;

	/**
	 * 
	 */
	public Model(final int aSize)
	{
		if (aSize <= 0)
		{
			throw new IllegalArgumentException();
		}
		this.values = new int[aSize];
		this.timestamps = new int[aSize];

		for (int i = 0; i < aSize; i++)
		{
			this.values[i] = (i % 512);
			this.timestamps[i] = i;
		}

		this.cursors = new int[] { 100, 200 };
	}

	public int[] getCursors()
	{
		return this.cursors;
	}

	public int getSize()
	{
		return this.values.length;
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
