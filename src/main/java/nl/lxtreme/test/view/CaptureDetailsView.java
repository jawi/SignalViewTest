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
package nl.lxtreme.test.view;


import static nl.lxtreme.test.Utils.*;

import java.awt.*;
import java.text.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * 
 */
public class CaptureDetailsView extends AbstractViewLayer implements IDataModelChangeListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final JLabel captureInfoField;

  // CONSTRUCTORS

  /**
   * Creates a new SignalDetailsView instance.
   * 
   * @param aController
   *          the diagram controller to use, cannot be <code>null</code>.
   */
  private CaptureDetailsView( final SignalDiagramController aController )
  {
    super( aController );

    this.captureInfoField = new JLabel( asText( null ) );
  }

  // METHODS

  /**
   * Factory method to create a new {@link CaptureDetailsView} instance.
   * 
   * @param aController
   *          the controller to use for the SignalDetailsView instance, cannot
   *          be <code>null</code>.
   * @return a new {@link CaptureDetailsView} instance, never <code>null</code>.
   */
  public static CaptureDetailsView create( final SignalDiagramController aController )
  {
    final CaptureDetailsView result = new CaptureDetailsView( aController );
    result.initComponent();

    aController.addDataModelChangeListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dataModelChanged( final SampleDataModel aDataModel )
  {
    this.captureInfoField.setText( asText( aDataModel ) );
    repaint( 50L );
  }

  /**
   * @param aEvent
   * @return
   */
  private String asText( final SampleDataModel aModel )
  {
    String sampleRate = "-", sampleCount = "-", totalWidth = "-";

    if ( aModel != null )
    {
      final long[] timestamps = aModel.getTimestamps();
      final double end = ( timestamps[timestamps.length - 1] + 1 ) - timestamps[0];

      sampleRate = displayFrequency( aModel.getSampleRate() );
      sampleCount = new DecimalFormat().format( aModel.getSize() );
      totalWidth = Utils.displayTime( end / aModel.getSampleRate() );
    }

    final StringBuilder sb = new StringBuilder( "<html><table>" );
    sb.append( "<tr><th align='right'>Sample rate:</th><td>" ).append( sampleRate ).append( "</td>" );
    sb.append( "<tr><th align='right'>Sample count:</th><td>" ).append( sampleCount ).append( "</td>" );
    sb.append( "<tr><th align='right'>Sample time:</th><td>" ).append( totalWidth ).append( "</td>" );
    sb.append( "</table></html>" );

    return sb.toString();
  }

  /**
   * Initializes this component.
   */
  private void initComponent()
  {
    setOpaque( false );

    setLayout( new BorderLayout() );

    add( this.captureInfoField, BorderLayout.NORTH );
  }
}
