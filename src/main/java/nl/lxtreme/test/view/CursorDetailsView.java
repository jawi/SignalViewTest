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

import javax.swing.*;
import javax.swing.event.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;


/**
 * 
 */
public class CursorDetailsView extends AbstractViewLayer implements ICursorChangeListener, HyperlinkListener
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private final JEditorPane cursorInfoField;

  // CONSTRUCTORS

  /**
   * Creates a new SignalDetailsView instance.
   * 
   * @param aController
   *          the diagram controller to use, cannot be <code>null</code>.
   */
  private CursorDetailsView( final SignalDiagramController aController )
  {
    super( aController );

    this.cursorInfoField = new JEditorPane( "text/html", asText() );

    initComponent();
  }

  // METHODS

  /**
   * Factory method to create a new {@link CursorDetailsView} instance.
   * 
   * @param aController
   *          the controller to use for the SignalDetailsView instance, cannot
   *          be <code>null</code>.
   * @return a new {@link CursorDetailsView} instance, never <code>null</code>.
   */
  public static CursorDetailsView create( final SignalDiagramController aController )
  {
    final CursorDetailsView result = new CursorDetailsView( aController );

    aController.addCursorChangeListener( result );

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorAdded( final int aCursorIdx, final long aCursorTimestamp )
  {
    updateViewText();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorChanged( final int aCursorIdx, final long aOldCursorTimestamp, final long aNewCursorTimestamp )
  {
    updateViewText();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorRemoved( final int aCursorIdx, final long aOldCursorTimestamp )
  {
    updateViewText();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsInvisible()
  {
    updateViewText();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cursorsVisible()
  {
    updateViewText();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void hyperlinkUpdate( final HyperlinkEvent aEvent )
  {
    if ( HyperlinkEvent.EventType.ACTIVATED.equals( aEvent.getEventType() ) )
    {
      String desc = aEvent.getDescription();
      if ( desc.startsWith( "#" ) )
      {
        desc = desc.substring( 1 );
      }

      try
      {
        long value = Long.parseLong( desc );
        getController().getSignalDiagram().scrollToTimestamp( 0, value );
      }
      catch ( NumberFormatException exception )
      {
        // Ignore...
      }
    }
  }

  /**
   * @param aEvent
   * @return
   */
  private String asText()
  {
    final SignalDiagramController ctrl = getController();

    final SampleDataModel dataModel = ctrl.getDataModel();
    if ( ( dataModel == null ) || !ctrl.isCursorMode() )
    {
      return "";
    }

    final ScreenModel screenModel = ctrl.getScreenModel();

    final Font labelFont = UIManager.getFont( SwingUtils.SWING_LABEL_FONT );

    final StringBuilder sb = new StringBuilder( "<html><head><style>td, th {" );
    sb.append( toCssString( labelFont ) ).append( "} th { font-weight: bold; }</style></head><body><table>" );
    for ( int c = 0; c < SampleDataModel.MAX_CURSORS; c++ )
    {
      Long cursor = dataModel.getCursor( c );
      if ( cursor == null )
      {
        continue;
      }

      sb.append( "<tr><th align='right'>" );
      sb.append( c + 1 ).append( ":" ).append( "</th>" );
      sb.append( "<td>" ).append( screenModel.getCursorLabel( c ) ).append( "</td>" );
      sb.append( "<td align='right'><a href='#" ).append( cursor.longValue() ).append( "'>" );
      sb.append( displayTime( cursor.doubleValue() / dataModel.getSampleRate() ) );
      sb.append( "</a></td></tr>" );
    }
    sb.append( "</table></body></html>" );
    return sb.toString();
  }

  /**
   * Initializes this component.
   */
  private void initComponent()
  {
    setOpaque( false );

    setLayout( new BorderLayout() );

    add( this.cursorInfoField, BorderLayout.NORTH );

    this.cursorInfoField.setEditable( false );
    this.cursorInfoField.setOpaque( false );
    this.cursorInfoField.addHyperlinkListener( this );
  }

  /**
   * Updates the view text and schedules this component for a repaint.
   */
  private void updateViewText()
  {
    this.cursorInfoField.setText( asText() );
    repaint( 50L );
  }
}
