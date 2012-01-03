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
package nl.lxtreme.test;


import java.awt.*;
import java.lang.reflect.*;
import java.util.*;


/**
 * Provides some utilities for fiddling with colors.
 */
public final class Utils
{
  // CONSTANTS

  private static final double ZERO_TIME_THRESHOLD = 1.0e-16;

  // CONSTRUCTORS

  /**
   * Creates a new ColorUtils instance. Never used.
   */
  private Utils()
  {
    super();
  }

  // METHODS

  /**
   * Converts a given frequency (in Hertz, Hz) to something more readable for
   * the user, like "10.0 kHz".
   * 
   * @param aFrequency
   *          the frequency (in Hz) to convert to a display value.
   * @return the display representation of the given frequency, never
   *         <code>null</code>.
   */
  public static String displayFrequency( final double aFrequency )
  {
    final String[] unitStrs = { "Hz", "kHz", "MHz", "GHz", "THz" };
    final double[] unitVals = { 1.0, 1.0e3, 1.0e6, 1.0e9, 1.0e12 };

    int i = unitVals.length - 1;
    for ( ; i >= 0; i-- )
    {
      if ( aFrequency >= unitVals[i] )
      {
        break;
      }
    }
    i = Math.max( i, 0 );

    return String.format( "%.3f %s", Double.valueOf( aFrequency / unitVals[i] ), unitStrs[i] );
  }

  /**
   * Converts a given size (in bytes) to something more readable for the user,
   * like "10K". The unit conversion is <em>always</em> done in binary (units of
   * 1024).
   * 
   * @param aSize
   *          the size (in bytes) to convert to a display value.
   * @return the display representation of the given size, never
   *         <code>null</code>.
   */
  public static String displaySize( final double aSize )
  {
    final String[] unitStrs = { "", "k", "M", "G", "T" };
    final double[] unitVals = { 1.0, 1024.0, 1048576.0, 1073741824.0, 1099511627776.0 };

    int i = unitVals.length - 1;
    for ( ; i >= 0; i-- )
    {
      if ( aSize >= unitVals[i] )
      {
        break;
      }
    }
    i = Math.max( i, 0 );

    return String.format( "%d%s", Integer.valueOf( ( int )( aSize / unitVals[i] ) ), unitStrs[i] );
  }

  /**
   * Converts a given time (in seconds) to something more readable for the user,
   * like "1.000 ms" (always a precision of three).
   * 
   * @param aTime
   *          the time (in seconds) to convert to a given display value.
   * @return the display representation of the given time, never
   *         <code>null</code>.
   */
  public static String displayTime( final double aTime )
  {
    return displayTime( aTime, 3, " ", true /* aIncludeUnit */);
  }

  /**
   * Converts a given time (in seconds) to something more readable for the user,
   * like "1.000 ms".
   * 
   * @param aTime
   *          the time (in seconds) to convert to a given display value;
   * @param aPrecision
   *          the precision of the returned string (decimals after the
   *          decimal-separator), should be >= 0 && <= 6.
   * @return the display representation of the given time, never
   *         <code>null</code>.
   */
  public static String displayTime( final double aTime, final int aPrecision, final String aSeparator,
      final boolean aIncludeUnit )
  {
    if ( ( aPrecision < 0 ) || ( aPrecision > 6 ) )
    {
      throw new IllegalArgumentException( "Precision cannot be less than zero or greater than six." );
    }
    if ( aSeparator == null )
    {
      throw new IllegalArgumentException( "Separator cannot be null!" );
    }

    // \u03BC == Greek mu character
    final String[] unitStrs = { "s", "ms", "\u03BCs", "ns", "ps" };
    final double[] unitVals = { 1.0, 1.0e-3, 1.0e-6, 1.0e-9, 1.0e-12 };

    double absTime = Math.abs( aTime );

    int i = 0;
    if ( absTime > ZERO_TIME_THRESHOLD )
    {
      for ( ; i < unitVals.length; i++ )
      {
        if ( absTime >= unitVals[i] )
        {
          break;
        }
      }
      i = Math.min( i, unitVals.length - 1 );
    }

    String format = "%." + aPrecision + "f";
    if ( aIncludeUnit )
    {
      format = format.concat( aSeparator + "%s" );
    }
    return String.format( format, Double.valueOf( aTime / unitVals[i] ), unitStrs[i] );
  }

  /**
   * Creates a contrasting color, based on the "perceived luminance" of the
   * given color.
   * <p>
   * See also:
   * <tt>http://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color</tt>
   * , and
   * <tt>http://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color</tt>
   * .
   * </p>
   * 
   * @param aColor
   *          the color to create a contrasting color for, cannot be
   *          <code>null</code>.
   * @return a contrasting color, never <code>null</code>.
   */
  public static Color getContrastColor( final Color aColor )
  {
    // Counting the perceptive luminance - human eye favors green color...
    double pl = 1.0 - getPerceivedLuminance( aColor );
    if ( pl < 0.5 )
    {
      // bright colors -> use black
      return Color.BLACK;
    }

    // dark colors -> use white
    return Color.WHITE;
  }

  /**
   * Returns the Digital CCIR601 luminance value of the given color.
   * 
   * @param aColor
   *          the color to return the luminance value for, cannot be
   *          <code>null</code>.
   * @return a luminance value, 0.0..1.0.
   */
  public static double getPerceivedLuminance( final Color aColor )
  {
    final float[] rgb = aColor.getRGBComponents( null );
    // (0.299*R + 0.587*G + 0.114*B)
    return ( ( 0.299 * rgb[0] ) + ( 0.587 * rgb[1] ) + ( 0.114 * rgb[2] ) );
  }

  /**
   * Returns the Photometric/digital ITU-R luminance value of the given color.
   * 
   * @param aColor
   *          the color to return the luminance value for, cannot be
   *          <code>null</code>.
   * @return a luminance value, 0.0..1.0.
   */
  public static double getStandardLuminance( final Color aColor )
  {
    final float[] rgb = aColor.getRGBComponents( null );
    // (0.2126*R) + (0.7152*G) + (0.0722*B)
    return ( ( 0.2126 * rgb[0] ) + ( 0.7152 * rgb[1] ) + ( 0.0722 * rgb[2] ) );
  }

  /**
   * Interpolates a gray-scale color between two given colors.
   * 
   * @param aBaseColor
   * @param aSecondaryColor
   * @param aDelta
   * @return
   */
  public static Color interpolate( final Color aBaseColor, final Color aSecondaryColor, final float aDelta )
  {
    float[] acomp = aSecondaryColor.getRGBComponents( null );
    float[] bcomp = aBaseColor.getRGBComponents( null );
    float[] ccomp = new float[4];

    for ( int i = 0; i < 4; i++ )
    {
      ccomp[i] = acomp[i] + ( ( bcomp[i] - acomp[i] ) * aDelta );
    }

    return new Color( ccomp[0], ccomp[1], ccomp[2], ccomp[3] );
  }

  /**
   * Returns whether the current host's operating system is Mac OS X.
   * 
   * @return <code>true</code> if running on Mac OS X, <code>false</code>
   *         otherwise.
   */
  public static boolean isMacOS()
  {
    final String osName = System.getProperty( "os.name" );
    return ( "Mac OS X".equalsIgnoreCase( osName ) || "Darwin".equalsIgnoreCase( osName ) );
  }

  /**
   * @param aI
   * @param aFreq1
   * @param aFreq2
   * @param aFreq3
   * @param aPhase1
   * @param aPhase2
   * @param aPhase3
   * @return
   */
  public static Color makeColorGradient( final int aI, final double aFreq1, final double aFreq2, final double aFreq3,
      final double aPhase1, final double aPhase2, final double aPhase3 )
  {
    final int width = 127;
    final int center = 128;
    final int red = ( int )( ( Math.sin( ( aFreq1 * aI ) + aPhase1 ) * width ) + center );
    final int grn = ( int )( ( Math.sin( ( aFreq2 * aI ) + aPhase2 ) * width ) + center );
    final int blu = ( int )( ( Math.sin( ( aFreq3 * aI ) + aPhase3 ) * width ) + center );
    return new Color( red, grn, blu );
  }

  /**
   * @return
   */
  public static void makeColorPalette( final Color[] aResult, final int aSteps )
  {
    final double freq = ( 2 * Math.PI ) / aSteps;
    for ( int i = 0; i < aResult.length; i++ )
    {
      // aResult[i] = makeColorGradient( i, freq, freq, freq, 2.7, 2.4, 4.6 );
      // aResult[i] = makeColorGradient( i, freq, freq, freq, 2.7, 7.4, 3.4 );
      aResult[i] = makeColorGradient( i, freq, freq, freq, 2.0, 4.0, 6.0 );
    }
  }

  /**
   * @return
   */
  public static void makeMonochromaticColorPalette( final Color[] aResult, final Color aColor )
  {
    Arrays.fill( aResult, aColor );
  }

  /**
   * Parses the given color-string into a valid Color instance.
   * <p>
   * A color-string has the following form: <tt>[#]rrggbb</tt> where <tt>rr</tt>, <tt>gg</tt> and <tt>bb</tt> are the hexadecimal color values for red,
   * green and blue. The string may optionally start with a hashpound sign.
   * </p>
   * 
   * @param aColor
   *          the color string to parse as color, cannot be <code>null</code>.
   * @return the Color-instance matching the given color, never
   *         <code>null</code>.
   */
  public static final Color parseColor( final String aColor )
  {
    if ( aColor == null )
    {
      throw new IllegalArgumentException( "Color cannot be null!" );
    }

    String color = aColor.trim();
    if ( color.startsWith( "#" ) )
    {
      color = color.substring( 1 );
    }

    try
    {
      final int colorValue = Integer.parseInt( color, 16 );
      return new Color( ( colorValue >> 16 ) & 0xFF, ( colorValue >> 8 ) & 0xFF, colorValue & 0xFF );
    }
    catch ( NumberFormatException exception )
    {
      throw new IllegalArgumentException( "Given string does NOT represent a valid color!" );
    }
  }

  /**
   * @param aCollection
   * @return
   */
  @SuppressWarnings( "unchecked" )
  public static <T> T[] toArray( final Class<T> aType, final Collection<T> aCollection )
  {
    final T[] result = ( T[] )Array.newInstance( aType, aCollection.size() );
    return aCollection.toArray( result );
  }

  /**
   * Converts a given font to a font-clause that can be used in a CSS-file.
   * 
   * @param aFont
   *          the font convert to CSS, cannot be <code>null</code>.
   * @return a CSS clause for the given font, never <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given font was <code>null</code>.
   */
  public static String toCssString( final Font aFont )
  {
    if ( aFont == null )
    {
      throw new IllegalArgumentException( "Parameter Font cannot be null!" );
    }

    final StringBuilder sb = new StringBuilder( "font: " );
    if ( aFont.isItalic() )
    {
      sb.append( "italic " );
    }
    if ( aFont.isBold() )
    {
      sb.append( "bold " );
    }
    sb.append( aFont.getSize() ).append( "pt " );
    sb.append( '"' ).append( aFont.getFontName() ).append( "\", " );
    sb.append( '"' ).append( aFont.getPSName() ).append( "\";" );
    return sb.toString();
  }

  /**
   * Returns the given color instance as a string in the form of
   * <tt>RR GG BB</tt> in which <tt>RR</tt>, <tt>GG</tt>, <tt>BB</tt> are the
   * hexadecimal representations of red, green and blue.
   * 
   * @param aColor
   *          the color to return as a string value, cannot be <code>null</code>
   *          .
   * @return the string representing the given color.
   * @see #parseColor(String)
   */
  public static String toHexString( final Color aColor )
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( String.format( "%02x", Integer.valueOf( aColor.getRed() ) ) );
    sb.append( String.format( "%02x", Integer.valueOf( aColor.getGreen() ) ) );
    sb.append( String.format( "%02x", Integer.valueOf( aColor.getBlue() ) ) );
    return sb.toString();
  }

}
