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
package nl.lxtreme.test.view.model;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.model.*;
import nl.lxtreme.test.model.ScreenModel.HelpTextDisplay;
import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.laf.*;


/**
 * Provides a custom model specific for the {@link TimeLineView} component.
 */
public class TimeLineViewModel extends AbstractViewModel
{
  // CONSTANTS

  public static final String COMPONENT_BACKGROUND_COLOR = "timeline.color.background";
  public static final String COMPONENT_HEIGHT = "timeline.height";
  public static final String COMPONENT_VERTICAL_PADDING = "timeline.vertical.padding";

  public static final String CURSOR_FLAG_FONT = "timeline.cursor.font";
  public static final String TEXT_COLOR = "timeline.text.color";
  public static final String TICK_HEIGHT = "timeline.tick.height";
  public static final String TICK_COLOR = "timeline.tick.color";

  public static final String MAJOR_TICK_HEIGHT = "timeline.majortick.height";
  public static final String MAJOR_TICK_LABEL_FONT = "timeline.majortick.label.font";
  public static final String MAJOR_TICK_COLOR = "timeline.majortick.color";

  public static final String MINOR_TICK_HEIGHT = "timeline.minortick.height";
  public static final String MINOR_TICK_LABEL_FONT = "timeline.minortick.label.font";
  public static final String MINOR_TICK_COLOR = "timeline.minortick.color";

  /** The tick increment (in pixels). */
  private static final int TIMELINE_INCREMENT = 5;

  // CONSTRUCTORS

  /**
   * Creates a new TimeLineModel instance.
   * 
   * @param aController
   *          the diagram controller to use, cannot be <code>null</code>.
   */
  public TimeLineViewModel( final SignalDiagramController aController )
  {
    super( aController );
  }

  // METHODS

  /**
   * Returns the background color for the timeline.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getBackgroundColor()
  {
    Color color = UIManager.getColor( COMPONENT_BACKGROUND_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_BACKGROUND_COLOR;
    }
    return color;
  }

  /**
   * Returns the font for the cursor flags.
   * 
   * @return a font, never <code>null</code>.
   */
  public Font getCursorFlagFont()
  {
    Font font = UIManager.getFont( CURSOR_FLAG_FONT );
    if ( font == null )
    {
      font = LafDefaults.DEFAULT_CURSOR_FLAG_FONT;
    }
    return font;
  }

  /**
   * Determines the ending time stamp until which the time line should be drawn
   * given the clip boundaries of this component.
   * 
   * @param aClip
   *          the clip boundaries of the timeline component, cannot be
   *          <code>null</code>.
   * @return the ending time stamp, as long value.
   */
  public long getEndTimestamp( final Rectangle aClip )
  {
    final Point location = new Point( aClip.x + aClip.width, 0 );
    final int idx = locationToSampleIndex( location );
    if ( idx < 0 )
    {
      return 0L;
    }

    final long[] timestamps = getDataModel().getTimestamps();

    return timestamps[idx] + 1;
  }

  /**
   * Returns how the help text is to be displayed.
   * 
   * @return a {@link HelpTextDisplay} mode, never <code>null</code>.
   */
  public HelpTextDisplay getHelpTextDisplayMode()
  {
    return getScreenModel().getHelpTextDisplayMode();
  }

  /**
   * Returns the color in which the major ticks should be painted.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getMajorTickColor()
  {
    Color color = UIManager.getColor( MAJOR_TICK_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_MAJOR_TICK_COLOR;
    }
    return color;
  }

  /**
   * Returns the major tick height.
   * 
   * @return a height, in pixels.
   */
  public int getMajorTickHeight()
  {
    int value = UIManager.getInt( MAJOR_TICK_HEIGHT );
    if ( value <= 0 )
    {
      return LafDefaults.DEFAULT_MAJOR_TICK_HEIGHT;
    }
    return value;
  }

  /**
   * Returns the font for the major tick labels.
   * 
   * @return a font, never <code>null</code>.
   */
  public Font getMajorTickLabelFont()
  {
    Font font = UIManager.getFont( MAJOR_TICK_LABEL_FONT );
    if ( font == null )
    {
      font = LafDefaults.DEFAULT_MAJOR_TICK_FONT;
    }
    return font;
  }

  /**
   * Returns the color in which the minor ticks should be painted.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getMinorTickColor()
  {
    Color color = UIManager.getColor( MINOR_TICK_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_MINOR_TICK_COLOR;
    }
    return color;
  }

  /**
   * Returns the minor tick height.
   * 
   * @return a height, in pixels.
   */
  public int getMinorTickHeight()
  {
    int value = UIManager.getInt( MINOR_TICK_HEIGHT );
    if ( value <= 0 )
    {
      return LafDefaults.DEFAULT_MINOR_TICK_HEIGHT;
    }
    return value;
  }

  /**
   * Returns the font for the minor tick labels.
   * 
   * @return a font, never <code>null</code>.
   */
  public Font getMinorTickLabelFont()
  {
    Font font = UIManager.getFont( MINOR_TICK_LABEL_FONT );
    if ( font == null )
    {
      font = LafDefaults.DEFAULT_MINOR_TICK_FONT;
    }
    return font;
  }

  /**
   * Returns the sample rate of the sampled.
   * 
   * @return a sample rate, in Hertz.
   */
  public int getSampleRate()
  {
    return this.controller.getDataModel().getSampleRate();
  }

  /**
   * Determines the starting time stamp from which the time line should be drawn
   * given the clip boundaries of this component.
   * 
   * @param aClip
   *          the clip boundaries of the timeline component, cannot be
   *          <code>null</code>.
   * @return the starting time stamp, as long value.
   */
  public long getStartTimestamp( final Rectangle aClip )
  {
    final Point location = aClip.getLocation();
    final int idx = locationToSampleIndex( location ) - 1;
    if ( idx < 0 )
    {
      return 0L;
    }

    final SampleDataModel dataModel = this.controller.getDataModel();
    final long[] timestamps = dataModel.getTimestamps();

    // Make sure that if we're at the beginning of the timeline, we're always
    // start at 0...
    return ( idx == 0 ) ? 0 : timestamps[idx];
  }

  /**
   * Returns the color in which the texts should be painted.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getTextColor()
  {
    Color color = UIManager.getColor( TEXT_COLOR );
    if ( color == null )
    {
      color = LafDefaults.DEFAULT_TEXT_COLOR;
    }
    return color;
  }

  /**
   * Returns the color of the individual ticks.
   * 
   * @return a color, never <code>null</code>.
   */
  public Color getTickColor()
  {
    Color value = UIManager.getColor( TICK_COLOR );
    if ( value == null )
    {
      value = LafDefaults.DEFAULT_TICK_COLOR;
    }
    return value;
  }

  /**
   * Returns the height of the individual ticks.
   * 
   * @return a height, in pixels.
   */
  public int getTickHeight()
  {
    int value = UIManager.getInt( TICK_HEIGHT );
    if ( value <= 0 )
    {
      return LafDefaults.DEFAULT_TICK_HEIGHT;
    }
    return value;
  }

  /**
   * Returns the increment of pixels per timeline tick.
   * 
   * @param aTimebase
   *          a time base.
   * @return a tick increment, >= 1.0.
   * @see #getTimebase(Rectangle)
   */
  public double getTickIncrement( final double aTimebase )
  {
    return Math.max( 1.0, aTimebase / TIMELINE_INCREMENT );
  }

  /**
   * Determines the time base for the given absolute time (= total time
   * displayed).
   * 
   * @param aViewRect
   *          the view rectangle to base the time base on.
   * @return a time base, as power of 10.
   */
  public double getTimebase( final Rectangle aViewRect )
  {
    final double absoluteTime = aViewRect.width / getZoomFactor();
    return Math.pow( 10, Math.round( Math.log10( absoluteTime ) ) );
  }

  /**
   * Returns the increment of pixels per unit of time.
   * 
   * @param aTimebase
   *          a time base.
   * @return a time increment, >= 0.1.
   * @see #getTimebase(Rectangle)
   */
  public double getTimeIncrement( final double aTimebase )
  {
    return Math.max( 0.1, aTimebase / ( 10.0 * TIMELINE_INCREMENT ) );
  }

  /**
   * Returns the height of the time line component.
   * 
   * @return a height, in pixels, > 0.
   */
  public int getTimeLineHeight()
  {
    int value = UIManager.getInt( COMPONENT_HEIGHT );
    if ( value <= 0 )
    {
      return LafDefaults.DEFAULT_TIMELINE_HEIGHT;
    }
    return value;
  }

  /**
   * Returns whether or not a help text is to be displayed for the timeline
   * component.
   * 
   * @return <code>true</code> if a help text is to be displayed,
   *         <code>false</code> otherwise.
   */
  public boolean isRenderHelpText()
  {
    return getScreenModel().isTimeLineHelpTextDisplayed();
  }
}
