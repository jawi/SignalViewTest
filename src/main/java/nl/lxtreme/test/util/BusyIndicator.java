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
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010-2012 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.test.util;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import nl.lxtreme.test.*;


/**
 * Provides a circular busy indicator that keeps rotating as long as this
 * component is visible.
 */
public class BusyIndicator extends JComponent
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private volatile int frameCounter;
  private final Timer timer;

  private final int points;
  private final float barWidth;
  private final float barLength;
  private final float centerDistance;

  private final Color baseColor;
  private final Color highlightColor;
  private final int trailLength;

  // CONSTRUCTORS

  /**
   * Creates a new {@link BusyIndicator} instance.
   */
  public BusyIndicator()
  {
    this.points = 12;
    this.barWidth = 1.3f;
    this.barLength = 4.5f;
    this.centerDistance = 3.5f;

    this.baseColor = Color.LIGHT_GRAY;
    this.highlightColor = Color.BLACK;
    this.trailLength = this.points / 2;

    this.timer = new Timer( 75, new ActionListener()
    {
      @Override
      public void actionPerformed( final ActionEvent aEvent )
      {
        increaseFrameCounter();
        repaint();
      }
    } );
    // Set up the timer itself...
    this.timer.setInitialDelay( 0 );
    this.timer.setRepeats( true );
    this.timer.setCoalesce( true );

    setOpaque( false );

    Dimension dim = new Dimension( 24, 24 );
    setPreferredSize( dim );
    setMinimumSize( dim );
    setMaximumSize( dim );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotify()
  {
    super.addNotify();
    if ( isVisible() )
    {
      startAnimation();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotify()
  {
    stopAnimation();
    super.removeNotify();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVisible( final boolean aFlag )
  {
    super.setVisible( aFlag );
    if ( aFlag )
    {
      startAnimation();
    }
    else if ( !aFlag )
    {
      stopAnimation();
      repaint();
    }
  }

  /**
   * Increases the frame counter to the next frame number.
   */
  final void increaseFrameCounter()
  {
    this.frameCounter = ( this.frameCounter + 1 ) % this.points;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    Graphics2D canvas = ( Graphics2D )aGraphics.create();
    try
    {
      if ( this.timer.isRunning() )
      {
        final Shape rect = new RoundRectangle2D.Float( this.centerDistance, -this.barWidth / 2, this.barLength,
            this.barWidth, this.barWidth, this.barWidth );

        canvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        canvas.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );

        canvas.setColor( Color.GRAY );

        canvas.translate( getWidth() / 2, getHeight() / 2 );
        for ( int i = 0; i < this.points; i++ )
        {
          canvas.setColor( calcFrameColor( i ) );
          canvas.fill( rect );
          canvas.rotate( ( 2.0 * Math.PI ) / this.points );
        }
      }
      else
      {
        canvas.setColor( getBackground() );
        canvas.fillRect( 0, 0, getWidth(), getHeight() );
      }
    }
    finally
    {
      canvas.dispose();
      canvas = null;
    }
  }

  /**
   * @param i
   * @return
   */
  private Color calcFrameColor( final int i )
  {
    for ( int t = 0; t < this.trailLength; t++ )
    {
      if ( i == ( ( ( this.frameCounter - t ) + this.points ) % this.points ) )
      {
        float terp = 1 - ( ( ( float )( this.trailLength - t ) ) / ( float )this.trailLength );
        return Utils.interpolate( this.baseColor, this.highlightColor, terp );
      }
    }

    return this.baseColor;
  }

  /**
   * Starts the animation.
   */
  private void startAnimation()
  {
    if ( !this.timer.isRunning() )
    {
      this.timer.start();
    }
  }

  /**
   * Stops the animation.
   */
  private void stopAnimation()
  {
    if ( this.timer.isRunning() )
    {
      this.timer.stop();
    }
  }
}
