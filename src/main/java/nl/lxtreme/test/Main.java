/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;


/**
 * @author jajans
 */
public class Main
{
  // VARIABLES

  private SignalDiagramController controller;
  private JFrame mainFrame;
  private JMenuBar menuBar;

  // METHODS

  /**
   * @param args
   */
  public static void main( final String[] aArgs ) throws Exception
  {
    final Runnable runner = new Runnable()
    {
      public void run()
      {
        final Main main = new Main();

        main.init();
        main.build();
        main.run();
      };
    };
    SwingUtilities.invokeLater( runner );
  }

  /**
   * 
   */
  private void build()
  {
    final SignalDiagramComponent layeredPane = new SignalDiagramComponent( this.controller );

    final JScrollPane contentPane = new JScrollPane( layeredPane );
    contentPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
    contentPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );

    this.mainFrame.setContentPane( contentPane );
    this.mainFrame.setJMenuBar( this.menuBar );

    this.mainFrame.pack();
  }

  /**
   * 
   */
  private void init()
  {
    try
    {
      UIManager.setLookAndFeel( "com.jgoodies.looks.plastic.Plastic3DLookAndFeel" );
    }
    catch ( Exception exception )
    {
      System.err.println( "L&F setting failed! Message = " + exception.getMessage() );
    }

    final Dimension dims = new Dimension( 800, 600 );

    this.mainFrame = new JFrame( "JLayeredPane test" );
    this.mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    this.mainFrame.setPreferredSize( dims );
    this.mainFrame.setSize( dims );
    this.mainFrame.setGlassPane( new GhostGlassPane() );

    this.menuBar = new JMenuBar();

    final JMenu fileMenu = new JMenu( "File" );
    this.menuBar.add( fileMenu );

    final JMenuItem fileExitItem = new JMenuItem( new AbstractAction( "Exit" )
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed( final ActionEvent aEvent )
      {
        Main.this.mainFrame.setVisible( false );
        Main.this.mainFrame.dispose();
      }
    } );
    fileMenu.add( fileExitItem );

    final JMenu diagramMenu = new JMenu( "Diagram" );
    this.menuBar.add( diagramMenu );

    final JMenuItem diagramEnableCursorsItem = new JCheckBoxMenuItem( new AbstractAction( "Cursor mode" )
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed( final ActionEvent aEvent )
      {
        AbstractButton button = ( AbstractButton )aEvent.getSource();
        Main.this.controller.setCursorsVisible( button.getModel().isSelected() );
      }
    } );
    diagramMenu.add( diagramEnableCursorsItem );

    final JMenuItem diagramEnableMeasureModeItem = new JCheckBoxMenuItem( new AbstractAction( "Measurement mode" )
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed( final ActionEvent aEvent )
      {
        AbstractButton button = ( AbstractButton )aEvent.getSource();
        Main.this.controller.setMeasurementMode( button.getModel().isSelected() );
      }
    } );
    diagramMenu.add( diagramEnableMeasureModeItem );

    final SampleDataModel model = new SampleDataModel( 1024 * 64 /* * 1024 */);
    this.controller = new SignalDiagramController( model );
  }

  /**
   */
  private void run()
  {
    this.mainFrame.setVisible( true );
  }
}
