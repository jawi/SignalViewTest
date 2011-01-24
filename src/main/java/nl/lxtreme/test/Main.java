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

  private static final Cursor DEFAULT = Cursor.getDefaultCursor();
  private static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
  private static final Cursor CURSOR_MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );

  // VARIABLES

  private ScreenController controller;

  private JFrame mainFrame;

  // METHODS

  /**
   * @param args
   */
  public static void main( final String[] aArgs )
  {
    final Main main = new Main();
    main.init();
    main.build();
    main.run();
  }

  /**
   * @param <T>
   * @param aComponent
   * @param aListener
   */
  private static <T extends MouseListener & MouseMotionListener> void installMouseListener(
      final JComponent aComponent, final T aListener )
  {
    aComponent.addMouseListener( aListener );
    aComponent.addMouseMotionListener( aListener );
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

    this.controller.zoomAll( this.mainFrame.getPreferredSize() );

    this.mainFrame.pack();
  }

  /**
   * 
   */
  private void init()
  {
    final Dimension dims = new Dimension( 1240, 600 );

    this.mainFrame = new JFrame( "JLayeredPane test" );
    this.mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    this.mainFrame.setPreferredSize( dims );
    this.mainFrame.setSize( dims );

    final DataModel model = new DataModel( 1024 * 4 /* * 1024 */);
    this.controller = new ScreenController( model );
  }

  /**
   * 
   */
  private void run()
  {
    this.mainFrame.setVisible( true );
  }
}
