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


import java.awt.font.*;
import java.text.*;
import java.util.*;


/**
 * Provides a line measurer that returns {@link TextLayout}s for complete lines
 * (that is: a series of characters ending with a carriage return and/or
 * newline).
 * <p>
 * This class provides a simple iterator interface, for easy iterating through
 * the various lines.
 * </p>
 */
public final class SimpleLineMeasurer implements Iterator<TextLayout>
{
  // VARIABLES

  private int pos;
  private final int limit;
  private final TextMeasurer measurer;
  private final AttributedCharacterIterator text;

  // CONSTRUCTORS

  /**
   * Creates a new SimpleLineMeasurer instance.
   * 
   * @param aText
   *          the text for which this class produces <code>TextLayout</code>
   *          objects; the text must contain at least one character; if the text
   *          available through <code>iter</code> changes, further calls to this
   *          SimpleLineMeasurer instance are undefined;
   * @param aRenderContext
   *          contains information about a graphics device which is needed to
   *          measure the text correctly; text measurements can vary slightly
   *          depending on the device resolution, and attributes such as
   *          antialiasing; this parameter does not specify a translation
   *          between the SimpleLineMeasurer and user space.
   */
  public SimpleLineMeasurer( final String aText, final FontRenderContext aRenderContext )
  {
    final AttributedString attributedString = new AttributedString( aText );
    attributedString.addAttribute( TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON );

    this.text = attributedString.getIterator();
    this.pos = this.text.getBeginIndex();
    this.limit = this.text.getEndIndex();
    this.measurer = new TextMeasurer( this.text, aRenderContext );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext()
  {
    return ( this.pos < this.limit );
  }

  /**
   * Returns the next layout, and updates the current position.
   * 
   * @return a <code>TextLayout</code>, beginning at the current position, which
   *         represents the next line fitting within <code>wrappingWidth</code>
   */
  @Override
  public TextLayout next()
  {
    if ( !hasNext() )
    {
      throw new NoSuchElementException();
    }

    int layoutLimit = nextOffset();
    if ( layoutLimit == this.pos )
    {
      return null;
    }

    TextLayout result = this.measurer.getLayout( this.pos, layoutLimit );
    this.pos = layoutLimit;

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove()
  {
    throw new UnsupportedOperationException( "Remove is not supported!" );
  }

  /**
   * Determines whether the given character is a carriage return or newline
   * character, denoting a line break.
   * 
   * @param aCharacter
   *          the character to test.
   * @return <code>true</code> if the given character is a carriage return or
   *         newline character, <code>false</code> otherwise.
   */
  private boolean isLineBreak( final int aCharacter )
  {
    return ( aCharacter == '\r' ) || ( aCharacter == '\n' );
  }

  /**
   * Returns the offset of the (next) line.
   * 
   * @return the position of the first character of the next line, >= 0.
   */
  private int nextOffset()
  {
    int nextOffset = this.pos;

    if ( this.pos < this.limit )
    {
      int ch;
      do
      {
        ch = this.text.next();
      }
      while ( Character.isDefined( ch ) && !isLineBreak( ch ) );

      if ( ch != CharacterIterator.DONE )
      {
        nextOffset = this.text.getIndex();
      }
      else
      {
        nextOffset = this.limit;
      }
    }

    return nextOffset;
  }
}
