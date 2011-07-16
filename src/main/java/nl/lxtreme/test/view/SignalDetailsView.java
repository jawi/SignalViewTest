/*
 *
 */
package nl.lxtreme.test.view;


import static nl.lxtreme.test.Utils.*;
import java.awt.*;
import java.beans.*;

import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * 
 */
public class SignalDetailsView extends AbstractViewLayer implements IMeasurementListener, PropertyChangeListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final JLabel measureInfoField;
  private final JLabel captureInfoField;

  // CONSTRUCTORS

  /**
   * Creates a new SignalDetailsView instance.
   * 
   * @param aController
   *          the diagram controller to use, cannot be <code>null</code>.
   */
  private SignalDetailsView( final SignalDiagramController aController )
  {
    super( aController );

    this.measureInfoField = new JLabel( asText( ( SignalHoverInfo )null ) );
    this.captureInfoField = new JLabel( asText( ( PropertyChangeEvent )null ) );

    aController.addPropertyChangeListener( this );
  }

  // METHODS

  /**
   * Factory method to create a new {@link SignalDetailsView} instance.
   * 
   * @param aController
   *          the controller to use for the SignalDetailsView instance, cannot
   *          be <code>null</code>.
   * @return a new {@link SignalDetailsView} instance, never <code>null</code>.
   */
  public static SignalDetailsView create( final SignalDiagramController aController )
  {
    final SignalDetailsView result = new SignalDetailsView( aController );
    result.initComponent();

    aController.addMeasurementListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMeasureEvent( final SignalHoverInfo aEvent )
  {
    this.measureInfoField.setText( asText( aEvent ) );
    repaint( 50L );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isListening()
  {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void propertyChange( final PropertyChangeEvent aEvent )
  {
    final String propertyName = aEvent.getPropertyName();
    if ( "dataModel".equals( propertyName ) )
    {
      this.captureInfoField.setText( asText( aEvent ) );
      repaint( 50L );
    }
  }

  /**
   * @param aEvent
   * @return
   */
  private String asText( final PropertyChangeEvent aEvent )
  {
    String sampleRate = "-", sampleCount = "-", totalWidth = "-";

    if ( aEvent != null )
    {
      final SampleDataModel dataModel = ( SampleDataModel )aEvent.getNewValue();
      final long[] timestamps = dataModel.getTimestamps();
      final double end = ( timestamps[timestamps.length - 1] + 1 ) - timestamps[0];

      sampleRate = displayFrequency( dataModel.getSampleRate() );
      sampleCount = Integer.toString( dataModel.getSize() );
      totalWidth = Utils.displayTime( end / dataModel.getSampleRate() );
    }

    final StringBuilder sb = new StringBuilder( "<html><table>" );
    sb.append( "<tr><th align='right'>Sample rate:</th><td>" ).append( sampleRate ).append( "</td>" );
    sb.append( "<tr><th align='right'>Sample count:</th><td>" ).append( sampleCount ).append( "</td>" );
    sb.append( "<tr><th align='right'>Sample time:</th><td>" ).append( totalWidth ).append( "</td>" );
    sb.append( "</table></html>" );

    return sb.toString();
  }

  /**
   * @param aEvent
   * @return
   */
  private String asText( final SignalHoverInfo aEvent )
  {
    String channelIndex = "-", timeValue = "-", totalWidth = "-", pwHigh = "-", pwLow = "-", dc = "-";

    if ( aEvent != null )
    {
      channelIndex = aEvent.getChannelIndexAsString();
      timeValue = aEvent.getReferenceTimeAsString();
      totalWidth = aEvent.getTotalTimeAsString();
      pwHigh = aEvent.getHighTimeAsString();
      pwLow = aEvent.getLowTimeAsString();
      dc = aEvent.getDutyCycleAsString();
    }

    final StringBuilder sb = new StringBuilder( "<html><table>" );
    sb.append( "<tr><th align='right'>Channel:</th><td>" ).append( channelIndex ).append( "</td>" );
    sb.append( "<tr><th align='right'>Time:</th><td>" ).append( timeValue ).append( "</td>" );
    sb.append( "<tr><th align='right'>Period:</th><td>" ).append( totalWidth ).append( "</td>" );
    sb.append( "<tr><th align='right'>Width (H):</th><td>" ).append( pwHigh ).append( "</td>" );
    sb.append( "<tr><th align='right'>Width (L):</th><td>" ).append( pwLow ).append( "</td>" );
    sb.append( "<tr><th align='right'>Duty cycle:</th><td>" ).append( dc ).append( "</td>" );
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

    add( this.measureInfoField, BorderLayout.NORTH );
    add( this.captureInfoField, BorderLayout.SOUTH );
  }
}
