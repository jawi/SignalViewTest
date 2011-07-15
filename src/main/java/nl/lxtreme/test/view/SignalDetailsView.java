/*
 *
 */
package nl.lxtreme.test.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.test.*;


/**
 * 
 */
public class SignalDetailsView extends AbstractViewLayer implements IMeasurementListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final JLabel textField;

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

    this.textField = new JLabel( asText( null ) );
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
    this.textField.setText( asText( aEvent ) );
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
    add( this.textField, BorderLayout.NORTH );
  }
}
