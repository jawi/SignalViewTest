package nl.lxtreme.test.model;


import static org.junit.Assert.*;

import org.junit.*;

public class ScreenModelTest
{
  // METHODS

  /**
   * 
   */
  @Test
  public void testFiveElements_FourToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 4, 1);
    assertArrayEquals("4 -> 1", new int[] { 1, 5, 2, 3, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_FourToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 4, 3);
    assertArrayEquals("4 -> 3", new int[] { 1, 2, 3, 5, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_FourToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 4, 2);
    assertArrayEquals("4 -> 2", new int[] { 1, 2, 5, 3, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_FourToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 4, 0);
    assertArrayEquals("4 -> 0", new int[] { 5, 1, 2, 3, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_OneToFour()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 1, 4);
    assertArrayEquals("1 -> 4", new int[] { 1, 3, 4, 5, 2 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_OneToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 1, 3);
    assertArrayEquals("1 -> 3", new int[] { 1, 3, 4, 2, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_OneToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 1, 2);
    assertArrayEquals("1 -> 2", new int[] { 1, 3, 2, 4, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_OneToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 1, 0);
    assertArrayEquals("1 -> 0", new int[] { 2, 1, 3, 4, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ThreeToFour()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 3, 4);
    assertArrayEquals("3 -> 4", new int[] { 1, 2, 3, 5, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ThreeToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 3, 1);
    assertArrayEquals("3 -> 1", new int[] { 1, 4, 2, 3, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ThreeToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 3, 2);
    assertArrayEquals("3 -> 2", new int[] { 1, 2, 4, 3, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ThreeToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 3, 0);
    assertArrayEquals("3 -> 0", new int[] { 4, 1, 2, 3, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_TwoToFour()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 2, 4);
    assertArrayEquals("2 -> 4", new int[] { 1, 2, 4, 5, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_TwoToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 2, 1);
    assertArrayEquals("2 -> 1", new int[] { 1, 3, 2, 4, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_TwoToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 2, 3);
    assertArrayEquals("2 -> 3", new int[] { 1, 2, 4, 3, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_TwoToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 2, 0);
    assertArrayEquals("2 -> 0", new int[] { 3, 1, 2, 4, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ZeroToFour()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 0, 4);
    assertArrayEquals("0 -> 4", new int[] { 2, 3, 4, 5, 1 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ZeroToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 0, 1);
    assertArrayEquals("0 -> 1", new int[] { 2, 1, 3, 4, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ZeroToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 0, 3);
    assertArrayEquals("0 -> 3", new int[] { 2, 3, 4, 1, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFiveElements_ZeroToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4, 5 };
    ScreenModel.shiftElements(input, 0, 2);
    assertArrayEquals("0 -> 2", new int[] { 2, 3, 1, 4, 5 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_OneToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 1, 3);
    assertArrayEquals("1 -> 3", new int[] { 1, 3, 4, 2 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_OneToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 1, 2);
    assertArrayEquals("1 -> 2", new int[] { 1, 3, 2, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_OneToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 1, 0);
    assertArrayEquals("1 -> 0", new int[] { 2, 1, 3, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_ThreeToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 3, 1);
    assertArrayEquals("3 -> 1", new int[] { 1, 4, 2, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_ThreeToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 3, 2);
    assertArrayEquals("3 -> 2", new int[] { 1, 2, 4, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_ThreeToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 3, 0);
    assertArrayEquals("3 -> 0", new int[] { 4, 1, 2, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_TwoToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 2, 1);
    assertArrayEquals("2 -> 1", new int[] { 1, 3, 2, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_TwoToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 2, 3);
    assertArrayEquals("2 -> 3", new int[] { 1, 2, 4, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_TwoToZero()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 2, 0);
    assertArrayEquals("2 -> 0", new int[] { 3, 1, 2, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_ZeroToOne()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 0, 1);
    assertArrayEquals("0 -> 1", new int[] { 2, 1, 3, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_ZeroToThree()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 0, 3);
    assertArrayEquals("0 -> 3", new int[] { 2, 3, 4, 1 }, input);
  }

  /**
   * 
   */
  @Test
  public void testFourElements_ZeroToTwo()
  {
    final int[] input = new int[] { 1, 2, 3, 4 };
    ScreenModel.shiftElements(input, 0, 2);
    assertArrayEquals("0 -> 2", new int[] { 2, 3, 1, 4 }, input);
  }

  /**
   * 
   */
  @Test
  public void testThreeElements_OneToTwo()
  {
    final int[] input = new int[] { 1, 2, 3 };
    ScreenModel.shiftElements(input, 1, 2);
    assertArrayEquals("1 -> 2", new int[] { 1, 3, 2 }, input);
  }

  /**
   * 
   */
  @Test
  public void testThreeElements_OneToZero()
  {
    final int[] input = new int[] { 1, 2, 3 };
    ScreenModel.shiftElements(input, 1, 0);
    assertArrayEquals("1 -> 0", new int[] { 2, 1, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testThreeElements_TwoToOne()
  {
    final int[] input = new int[] { 1, 2, 3 };
    ScreenModel.shiftElements(input, 2, 1);
    assertArrayEquals("2 -> 1", new int[] { 1, 3, 2 }, input);
  }

  /**
   * 
   */
  @Test
  public void testThreeElements_TwoToZero()
  {
    final int[] input = new int[] { 1, 2, 3 };
    ScreenModel.shiftElements(input, 2, 0);
    assertArrayEquals("2 -> 0", new int[] { 3, 1, 2 }, input);
  }

  /**
   * 
   */
  @Test
  public void testThreeElements_ZeroToOne()
  {
    final int[] input = new int[] { 1, 2, 3 };
    ScreenModel.shiftElements(input, 0, 1);
    assertArrayEquals("0 -> 1", new int[] { 2, 1, 3 }, input);
  }

  /**
   * 
   */
  @Test
  public void testThreeElements_ZeroToTwo()
  {
    final int[] input = new int[] { 1, 2, 3 };
    ScreenModel.shiftElements(input, 0, 2);
    assertArrayEquals("0 -> 2", new int[] { 2, 3, 1 }, input);
  }
}
