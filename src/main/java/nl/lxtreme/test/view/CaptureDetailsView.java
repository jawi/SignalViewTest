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
import java.beans.*;
import java.text.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.model.*;


/**
 * 
 */
public class CaptureDetailsView extends AbstractViewLayer implements IDataModelChangeListener, PropertyChangeListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private volatile String sampleRate = "-";
  private volatile String sampleCount = "-";
  private volatile String totalWidth = "-";
  private volatile String tickInterval = "-";
  private volatile String displayedTime = "-";

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

    this.captureInfoField = new JLabel( asText() );
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
    aController.addPropertyChangeListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dataModelChanged( final SampleDataModel aDataModel )
  {
    this.sampleRate = "-";
    this.sampleCount = "-";
    this.totalWidth = "-";

    if ( aDataModel != null )
    {
      this.sampleRate = displayFrequency( aDataModel.getSampleRate() );
      this.sampleCount = new DecimalFormat().format( aDataModel.getSize() );
      this.totalWidth = displayTime( aDataModel.getCaptureLength() );
    }

    repaint( 50L );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void propertyChange( final PropertyChangeEvent aEvent )
  {
    final String name = aEvent.getPropertyName();
    if ( "zoomFactor".equals( name ) || "visibleRect".equals( name ) )
    {
      final SignalDiagramModel model = getController().getSignalDiagramModel();

      this.tickInterval = displayTime( model.getTimeInterval() );
      this.displayedTime = displayTime( model.getDisplayedTimeInterval() );

      repaint( 50L );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void paintComponent( final Graphics aGraphics )
  {
    this.captureInfoField.setText( asText() );

    super.paintComponent( aGraphics );
  }

  /**
   * @param aEvent
   * @return
   */
  private String asText()
  {
    final StringBuilder sb = new StringBuilder( "<html><table>" );
    sb.append( "<tr><th align='right'>Sample rate:</th><td>" ).append( this.sampleRate ).append( "</td>" );
    sb.append( "<tr><th align='right'>Sample count:</th><td>" ).append( this.sampleCount ).append( "</td>" );
    sb.append( "<tr><th align='right'>Sample time:</th><td>" ).append( this.totalWidth ).append( "</td>" );
    sb.append( "<tr><th align='right'>Tick interval:</th><td>" ).append( this.tickInterval ).append( "</td>" );
    sb.append( "<tr><th align='right'>Displayed time:</th><td>" ).append( this.displayedTime ).append( "</td>" );
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
