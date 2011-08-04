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
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.test.view.action;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.test.view.*;
import nl.lxtreme.test.view.model.*;


/**
 * Provides an action to edit a channel label.
 */
public class EditChannelLabelAction extends AbstractAction
{
  // INNER TYPES

  /**
   * Provides a Swing dialog for editing a channel label.
   */
  static final class EditChannelDialog extends JDialog
  {
    // CONSTANTS

    private static final long serialVersionUID = 1L;

    // VARIABLES

    private JTextField labelEditor;

    boolean dialogResult = false;

    // CONSTRUCTORS

    /**
     * Creates a new EditChannelLabelAction.EditChannelDialog instance.
     */
    public EditChannelDialog( final int aChannelIdx, final String aLabel )
    {
      super( null /* owner */, ModalityType.DOCUMENT_MODAL );

      setTitle( String.format( "Edit label channel %d", Integer.valueOf( aChannelIdx ) ) );

      initDialog( aChannelIdx, aLabel );
    }

    /**
     * Returns the new channel label.
     * 
     * @return the channel label, can be <code>null</code>.
     */
    public String getLabel()
    {
      return this.labelEditor.getText();
    }

    /**
     * Makes this dialog visible on screen and waits until it is dismissed.
     * 
     * @return <code>true</code> if the dialog is acknowledged by the user,
     *         <code>false</code> if it is cancelled by the user.
     */
    public boolean showDialog()
    {
      this.dialogResult = false;
      setVisible( true );
      return this.dialogResult;
    }

    /**
     * Initializes this dialog.
     */
    private void initDialog( final int aCursorIdx, final String aLabel )
    {
      JLabel label = new JLabel( String.format( "Channel label %d", Integer.valueOf( aCursorIdx ) ) );
      this.labelEditor = new JTextField( aLabel, 10 );

      final JButton okButton = new JButton( "Ok" );
      okButton.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( final ActionEvent aEvent )
        {
          EditChannelDialog.this.dialogResult = true;
          setVisible( false );
        }
      } );

      final JButton cancelButton = new JButton( "Cancel" );
      cancelButton.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( final ActionEvent aEvent )
        {
          EditChannelDialog.this.dialogResult = false;
          setVisible( false );
        }
      } );

      // XXX use SpringLayout instead...
      JPanel editorPane = new JPanel( new GridLayout( 1, 2 ) );
      editorPane.add( label );
      editorPane.add( this.labelEditor );

      // XXX use button factory instead...
      JPanel buttonPane = new JPanel( new GridLayout( 1, 3 ) );
      buttonPane.add( new JLabel( " " ) );
      buttonPane.add( okButton );
      buttonPane.add( cancelButton );

      JPanel contentPane = new JPanel( new BorderLayout( 4, 4 ) );
      contentPane.add( editorPane, BorderLayout.CENTER );
      contentPane.add( buttonPane, BorderLayout.PAGE_END );

      setContentPane( contentPane );

      pack();
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final SignalDiagramController controller;
  private final int channelIdx;

  // CONSTRUCTORS

  /**
   * Creates a new EditCursorLabelAction instance.
   * 
   * @param aController
   *          the {@link SignalDiagramController} to use;
   * @param aChannelIdx
   *          the index of the channel to edit the label for.
   */
  public EditChannelLabelAction( final SignalDiagramController aController, final int aChannelIdx )
  {
    super( "Edit label" );
    this.controller = aController;
    this.channelIdx = aChannelIdx;
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed( final ActionEvent aEvent )
  {
    final SignalDiagramModel model = this.controller.getSignalDiagramModel();

    final EditChannelDialog dialog = new EditChannelDialog( this.channelIdx, model.getChannelLabel( this.channelIdx ) );
    if ( dialog.showDialog() )
    {
      model.setChannelLabel( this.channelIdx, dialog.getLabel() );
    }
  }
}
