/**
 * 
 */
package nl.lxtreme.test;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * @author jajans
 */
public class Main
{
  // CONSTANTS

  private static final double ZERO_TIME_THRESHOLD = 1.0e-16;

  // VARIABLES

  private ScreenController controller;
  private JFrame mainFrame;
  private JMenuBar menuBar;

  /**
   * Converts a given frequency (in Hertz, Hz) to something more readable for
   * the user, like "10.0 kHz".
   * 
   * @param aFrequency
   *          the frequency (in Hz) to convert to a display value.
   * @return the display representation of the given frequency, never
   *         <code>null</code>.
   */
  public static String displayFrequency( final double aFrequency )
  {
    final String[] unitStrs = { "Hz", "kHz", "MHz", "GHz", "THz" };
    final double[] unitVals = { 1.0, 1.0e3, 1.0e6, 1.0e9, 1.0e12 };

    int i = unitVals.length - 1;
    for ( ; i >= 0; i-- )
    {
      if ( aFrequency >= unitVals[i] )
      {
        break;
      }
    }
    i = Math.max( i, 0 );

    return String.format( "%.3f %s", Double.valueOf( aFrequency / unitVals[i] ), unitStrs[i] );
  }

  /**
   * Converts a given size (in bytes) to something more readable for the user,
   * like "10K". The unit conversion is <em>always</em> done in binary (units of
   * 1024).
   * 
   * @param aSize
   *          the size (in bytes) to convert to a display value.
   * @return the display representation of the given size, never
   *         <code>null</code>.
   */
  public static String displaySize( final double aSize )
  {
    final String[] unitStrs = { "", "k", "M", "G", "T" };
    final double[] unitVals = { 1.0, 1024.0, 1048576.0, 1073741824.0, 1099511627776.0 };

    int i = unitVals.length - 1;
    for ( ; i >= 0; i-- )
    {
      if ( aSize >= unitVals[i] )
      {
        break;
      }
    }
    i = Math.max( i, 0 );

    return String.format( "%d%s", Integer.valueOf( ( int )( aSize / unitVals[i] ) ), unitStrs[i] );
  }

  // METHODS
  /**
   * Converts a given time (in seconds) to something more readable for the user,
   * like "1.000 ms" (always a precision of three).
   * 
   * @param aTime
   *          the time (in seconds) to convert to a given display value.
   * @return the display representation of the given time, never
   *         <code>null</code>.
   */
  public static String displayTime( final double aTime )
  {
    return displayTime( aTime, 3, " " );
  }

  /**
   * Converts a given time (in seconds) to something more readable for the user,
   * like "1.000 ms".
   * 
   * @param aTime
   *          the time (in seconds) to convert to a given display value;
   * @param aPrecision
   *          the precision of the returned string (decimals after the
   *          decimal-separator), should be >= 0 && <= 6.
   * @return the display representation of the given time, never
   *         <code>null</code>.
   */
  public static String displayTime( final double aTime, final int aPrecision, final String aSeparator )
  {
    if ( ( aPrecision < 0 ) || ( aPrecision > 6 ) )
    {
      throw new IllegalArgumentException( "Precision cannot be less than zero or greater than six." );
    }
    if ( aSeparator == null )
    {
      throw new IllegalArgumentException( "Separator cannot be null!" );
    }

    // \u03BC == Greek mu character
    final String[] unitStrs = { "s", "ms", "\u03BCs", "ns", "ps" };
    final double[] unitVals = { 1.0, 1.0e-3, 1.0e-6, 1.0e-9, 1.0e-12 };

    double absTime = Math.abs( aTime );

    int i = 0;
    if ( absTime > ZERO_TIME_THRESHOLD )
    {
      for ( ; i < unitVals.length; i++ )
      {
        if ( absTime >= unitVals[i] )
        {
          break;
        }
      }
      i = Math.min( i, unitVals.length - 1 );
    }

    final String format = "%." + aPrecision + "f" + aSeparator + "%s";
    return String.format( format, Double.valueOf( aTime / unitVals[i] ), unitStrs[i] );
  }

  /**
   * @param args
   */
  public static void main( final String[] aArgs ) throws Exception
  {
    try
    {
      UIManager.setLookAndFeel( "com.jgoodies.looks.plastic.Plastic3DLookAndFeel" );
    }
    catch ( Exception exception )
    {
      System.err.println( "L&F setting failed!" );
      exception.printStackTrace();
    }

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
    final SampleViewComponent layeredPane = new SampleViewComponent( this.controller );

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
    final Dimension dims = new Dimension( 800, 600 );

    this.mainFrame = new JFrame( "JLayeredPane test" );
    this.mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    this.mainFrame.setPreferredSize( dims );
    this.mainFrame.setSize( dims );

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
        Main.this.controller.setCursorMode( button.getModel().isSelected() );
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

    final DataModel model = new DataModel( 1024 * 64 /* * 1024 */);
    this.controller = new ScreenController( model );
  }

  /**
   */
  private void run()
  {
    this.mainFrame.setVisible( true );
  }
}
