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
package nl.lxtreme.test.view;


import static nl.lxtreme.test.Utils.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.model.Cursor;
import nl.lxtreme.test.view.model.*;


/**
 * Provides a simple measurement component for measuring some common aspects of
 * signals, such as frequency, # of pulses and so on.
 */
public class MeasurementView extends AbstractViewLayer implements IChannelChangeListener, ICursorChangeListener
{
  // INNER TYPES

  /**
   * {@link ActionListener} implementation for the channel comboboxes.
   */
  final class ChannelActionListener implements ActionListener
  {
    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed( final ActionEvent aEvent )
    {
      final Collection<Channel> channelList = getChannelGroupManager().getAssignedChannels();
      updateChannelModel( Utils.toArray( Channel.class, channelList ) );
    }
  }

  /**
   * {@link ActionListener} implementation for the cursor comboboxes.
   */
  final class CursorActionListener implements ActionListener
  {
    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed( final ActionEvent aEvent )
    {
      updateCursorModels();
    }
  }

  /**
   * Provides a {@link SwingWorker} to measure the frequency, dutycycle and such
   * asynchronously from the UI.
   */
  final class SignalMeasurer extends SwingWorker<String, Boolean>
  {
    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground() throws Exception
    {
      MeasurementView.this.progressBar.setVisible( true );

      final int mask = ( ( Channel )MeasurementView.this.channel.getSelectedItem() ).getMask();
      final long startTimestamp = ( ( Cursor )MeasurementView.this.cursorA.getSelectedItem() ).getTimestamp();
      final long endTimestamp = ( ( Cursor )MeasurementView.this.cursorB.getSelectedItem() ).getTimestamp();

      final SignalDiagramModel model = getSignalDiagramModel();

      final int startIdx = model.getTimestampIndex( startTimestamp );
      final int endIdx = model.getTimestampIndex( endTimestamp );
      final int[] values = model.getValues();

      final double measureTime = ( double )Math.abs( endTimestamp - startTimestamp ) / model.getSampleRate();

      int i = Math.max( 0, startIdx - 1 );
      int lastBitValue = values[++i] & mask;

      int highCount = 0;
      int lowCount = 0;

      for ( ; !isCancelled() && ( i <= endIdx ); i++ )
      {
        final int bitValue = values[i] & mask;

        if ( lastBitValue != bitValue )
        {
          if ( lastBitValue > bitValue )
          {
            // Low to high transition: previously seen a low-state...
            lowCount++;
          }
          else if ( lastBitValue < bitValue )
          {
            // High to low transition: previously seen a high-state...
            highCount++;
          }
        }

        lastBitValue = bitValue;
      }

      int pulseCount = ( lowCount + highCount ) / 2;

      String timeText = displayTime( measureTime );
      String frequencyText = displayFrequency( pulseCount / measureTime );
      String dutyCycleText = String
          .format( "%.3f%%", Double.valueOf( ( 100.0 * highCount ) / ( lowCount + highCount ) ) );
      String pulseCountText = Integer.toString( pulseCount );

      final StringBuilder sb = new StringBuilder( "<html><table>" );
      sb.append( "<tr><th align='right'>Time:</th><td>" ).append( timeText ).append( "</td>" );
      sb.append( "<tr><th align='right'># of pulses:</th><td>" ).append( pulseCountText ).append( "</td>" );
      sb.append( "<tr><th align='right'>Frequency:</th><td>" ).append( frequencyText ).append( "</td>" );
      sb.append( "<tr><th align='right'>Duty cycle:</th><td>" ).append( dutyCycleText ).append( "</td>" );
      sb.append( "</table></html>" );

      return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void done()
    {
      try
      {
        MeasurementView.this.progressBar.setVisible( false );
        MeasurementView.this.measurementInfo.setText( get() );
      }
      catch ( Exception exception )
      {
        exception.printStackTrace();
      }
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private JComboBox channel;
  private JComboBox cursorA;
  private JComboBox cursorB;
  private JProgressBar progressBar;

  private JLabel measurementInfo;

  private volatile boolean listening;

  // CONSTRUCTORS

  /**
   * Creates a new MeasurementView instance.
   * 
   * @param aController
   */
  public MeasurementView( final SignalDiagramController aController )
  {
    super( aController );

    initComponent();

    this.listening = true;
  }

  // METHODS

  /**
   * Factory method to create a new {@link MeasurementView} instance.
   * 
   * @param aController
   *          the controller to use for the SignalDetailsView instance, cannot
   *          be <code>null</code>.
   * @return a new {@link MeasurementView} instance, never <code>null</code>.
   */
  public static MeasurementView create( final SignalDiagramController aController )
  {
    final MeasurementView result = new MeasurementView( aController );

    aController.addChannelChangeListener( result );
    aController.addCursorChangeListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void channelChanged( final ChannelChangeEvent aEvent )
  {
    if ( this.listening && ChannelChangeEvent.PROPERTY_ENABLED.equals( aEvent.getPropertyName() ) )
    {
      final Collection<Channel> channelList = getChannelGroupManager().getAssignedChannels();
      updateChannelModel( Utils.toArray( Channel.class, channelList ) );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void channelGroupStructureChanged( final Collection<Channel> aChannelList )
  {
    if ( this.listening )
    {
      updateChannelModel( Utils.toArray( Channel.class, aChannelList ) );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void channelMoved( final ChannelMoveEvent aEvent )
  {
    // NO-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorAdded( final Cursor aCursor )
  {
    if ( this.listening )
    {
      updateCursorModels();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorChanged( final String aPropertyName, final Cursor aOldCursor, final Cursor aNewCursor )
  {
    if ( this.listening && !ICursorChangeListener.PROPERTY_COLOR.equals( aPropertyName ) )
    {
      updateCursorModels();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorRemoved( final Cursor aOldCursor )
  {
    if ( this.listening )
    {
      updateCursorModels();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsInvisible()
  {
    if ( this.listening )
    {
      updateCursorModels();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsVisible()
  {
    if ( this.listening )
    {
      updateCursorModels();
    }
  }

  /**
   * Updates the channel model to the current list of channels.
   */
  protected void updateChannelModel( final Channel... aChannels )
  {
    updateChannelComboBoxModel( this.channel, aChannels );

    updateMeasurementInfo();

    repaint( 50L );
  }

  /**
   * Updates both combobox models to the current list of available cursors.
   */
  protected void updateCursorModels()
  {
    updateCursorComboBoxModel( this.cursorA );
    updateCursorComboBoxModel( this.cursorB );

    updateMeasurementInfo();

    repaint( 50L );
  }

  /**
   * @return
   */
  private boolean canPerformMeasurement()
  {
    final SignalDiagramModel model = getSignalDiagramModel();

    boolean cursorMode = model.isCursorMode();
    Cursor selectedCursor = ( Cursor )this.cursorA.getSelectedItem();
    boolean cursorA_defined = ( selectedCursor != null ) && selectedCursor.isDefined();
    selectedCursor = ( Cursor )this.cursorB.getSelectedItem();
    boolean cursorB_defined = ( selectedCursor != null ) && selectedCursor.isDefined();

    return cursorMode && cursorA_defined && cursorB_defined;
  }

  /**
   * Returns the channel group manager.
   * 
   * @return a channel group manager, never <code>null</code>.
   */
  private ChannelGroupManager getChannelGroupManager()
  {
    return getSignalDiagramModel().getChannelGroupManager();
  }

  /**
   * @return
   */
  private SignalDiagramModel getSignalDiagramModel()
  {
    return getController().getSignalDiagramModel();
  }

  /**
   * Initializes this component.
   */
  private void initComponent()
  {
    this.channel = updateChannelComboBoxModel( new JComboBox() );
    this.channel.addActionListener( new CursorActionListener() );
    // Make the component a bit smaller and a pop-down on OSX...
    this.channel.putClientProperty( "JComponent.sizeVariant", "small" );
    this.channel.putClientProperty( "JComboBox.isPopDown", Boolean.TRUE );

    this.cursorA = updateCursorComboBoxModel( new JComboBox() );
    this.cursorA.addActionListener( new ChannelActionListener() );
    // Make the component a bit smaller and a pop-down on OSX...
    this.cursorA.putClientProperty( "JComponent.sizeVariant", "small" );
    this.cursorA.putClientProperty( "JComboBox.isPopDown", Boolean.TRUE );

    this.cursorB = updateCursorComboBoxModel( new JComboBox() );
    this.cursorB.addActionListener( new ChannelActionListener() );
    // Make the component a bit smaller and a pop-down on OSX...
    this.cursorB.putClientProperty( "JComponent.sizeVariant", "small" );
    this.cursorB.putClientProperty( "JComboBox.isPopDown", Boolean.TRUE );

    this.progressBar = new JProgressBar();
    this.progressBar.setIndeterminate( true );
    // On OSX this will display as a circular progress bar...
    this.progressBar.putClientProperty( "JProgressBar.style", "circular" );
    this.progressBar.setVisible( false );

    this.measurementInfo = new JLabel();

    setOpaque( false );
    setLayout( new BorderLayout() );

    JPanel cursorPanel = new JPanel( new GridLayout( 4, 2 ) );
    cursorPanel.add( new JLabel( "Channel" ) );
    cursorPanel.add( this.channel );
    cursorPanel.add( new JLabel( "Cursor A" ) );
    cursorPanel.add( this.cursorA );
    cursorPanel.add( new JLabel( "Cursor B" ) );
    cursorPanel.add( this.cursorB );
    cursorPanel.add( new JLabel( "" ) );
    cursorPanel.add( this.progressBar );

    add( cursorPanel, BorderLayout.NORTH );
    add( this.measurementInfo, BorderLayout.CENTER );
  }

  /**
   * Updates a given combobox' model to contain the current list of defined
   * cursors.
   * 
   * @return the given combobox.
   */
  private JComboBox updateChannelComboBoxModel( final JComboBox aComboBox, final Channel... aChannels )
  {
    ComboBoxModel model = new DefaultComboBoxModel( aChannels );

    final Object oldSelectedItem = aComboBox.getSelectedItem();
    aComboBox.setModel( model );
    aComboBox.setSelectedItem( oldSelectedItem );

    return aComboBox;
  }

  /**
   * Updates a given combobox' model to contain the current list of defined
   * cursors.
   * 
   * @return the given combobox.
   */
  private JComboBox updateCursorComboBoxModel( final JComboBox aComboBox )
  {
    final Cursor[] cursors = getSignalDiagramModel().getDefinedCursors();
    ComboBoxModel model = new DefaultComboBoxModel( cursors );

    final Object oldSelectedItem = aComboBox.getSelectedItem();
    aComboBox.setModel( model );
    aComboBox.setSelectedItem( oldSelectedItem );

    return aComboBox;
  }

  /**
   * Updates the actual measurement information.
   */
  private void updateMeasurementInfo()
  {
    this.listening = false;

    try
    {
      if ( canPerformMeasurement() )
      {
        ( new SignalMeasurer() ).execute();
      }
    }
    finally
    {
      this.listening = true;
    }
  }
}
