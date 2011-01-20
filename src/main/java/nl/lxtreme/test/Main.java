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
  /**
   * @author jajans
   */
  static final class MyComponentListener extends ComponentAdapter
  {
    // VARIABLES

    private final ScreenController controller;

    // CONSTRUCTORS

    public MyComponentListener( final ScreenController aController )
    {
      this.controller = aController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentResized( final ComponentEvent aEvent )
    {
      this.controller.recalculateDimensions();
    }
  }

  /**
   * @author jajans
   */
  static final class MyKeyListener extends KeyAdapter
  {
    // VARIABLES

    private final ScreenController controller;

    // CONSTRUCTORS

    public MyKeyListener( final ScreenController aController )
    {
      this.controller = aController;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyPressed( final KeyEvent aEvent )
    {
      if ( aEvent.isAltDown() && !this.controller.isSnapModeEnabled() )
      {
        this.controller.enableSnapMode();
        aEvent.consume();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyReleased( final KeyEvent aEvent )
    {
      if ( this.controller.isSnapModeEnabled() )
      {
        this.controller.disableSnapMode();
        aEvent.consume();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyTyped( final KeyEvent aEvent )
    {
      if ( aEvent.getID() != KeyEvent.KEY_TYPED )
      {
        return;
      }

      if ( ( '+' == aEvent.getKeyChar() ) || ( '=' == aEvent.getKeyChar() ) )
      {
        this.controller.zoomIn();
      }
      else if ( ( '-' == aEvent.getKeyChar() ) || ( '_' == aEvent.getKeyChar() ) )
      {
        this.controller.zoomOut();
      }
    }
  }

  /**
   * @author jajans
   */
  static final class MyMouseListener extends MouseAdapter
  {
    // VARIABLES

    private int lastCursor = -1;
    private boolean showing = false;
    private final ScreenController controller;

    // CONSTRUCTORS

    public MyMouseListener( final ScreenController aController )
    {
      this.controller = aController;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged( final MouseEvent aEvent )
    {
      if ( this.lastCursor < 0 )
      {
        return;
      }

      setCursor( aEvent, CURSOR_MOVE_CURSOR );
      this.controller.dragCursor( this.lastCursor, aEvent.getPoint(), aEvent.isAltDown() );
      aEvent.consume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved( final MouseEvent aEvent )
    {
      final Point point = aEvent.getPoint();

      if ( !this.showing )
      {
        if ( aEvent.isControlDown() )
        {
          this.showing = this.controller.showHover( point );
          setCursor( aEvent, CURSOR_HOVER );
          aEvent.consume();
        }
        else
        {
          if ( this.controller.findCursor( aEvent.getPoint() ) >= 0 )
          {
            setCursor( aEvent, CURSOR_MOVE_CURSOR );
          }
          else
          {
            setCursor( aEvent, DEFAULT );
          }
        }
      }
      else
      {
        if ( !aEvent.isControlDown() )
        {
          this.showing = this.controller.hideHover();
          setCursor( aEvent, DEFAULT );
          aEvent.consume();
        }
        else
        {
          setCursor( aEvent, CURSOR_HOVER );
          this.controller.moveHover( point );
          aEvent.consume();
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed( final MouseEvent aEvent )
    {
      this.lastCursor = this.controller.findCursor( aEvent.getPoint() );
      aEvent.consume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased( final MouseEvent aEvent )
    {
      setCursor( aEvent, DEFAULT );
      if ( this.lastCursor < 0 )
      {
        return;
      }
      this.lastCursor = -1;
      aEvent.consume();
    }

    /**
     * @param aEvent
     * @param aCursor
     */
    private void setCursor( final MouseEvent aEvent, final Cursor aCursor )
    {
      if ( aEvent.getSource() instanceof JComponent )
      {
        ( ( JComponent )aEvent.getSource() ).setCursor( aCursor );
      }
    }
  }

  // CONSTANTS

  private static final Cursor DEFAULT = Cursor.getDefaultCursor();
  private static final Cursor CURSOR_HOVER = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
  private static final Cursor CURSOR_MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );

  // VARIABLES

  private ScreenController controller;

  private ModelView modelView;
  private CursorView cursorView;
  private ArrowView arrowView;

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
   * 
   */
  private void build()
  {
    final JPanel layeredPane = new JPanel()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void paint( final Graphics aG )
      {
        final long startTime = System.nanoTime();
        try
        {
          super.paint( aG );
        }
        finally
        {
          final long endTime = System.nanoTime();
          final long renderTime = endTime - startTime;
          System.out.print( "Rendering time = " );
          if ( renderTime >= 1000000.0 )
          {
            System.out.print( String.format( "%.3f", renderTime / 1000000.0 ) );
            System.out.print( "m" );
          }
          else if ( renderTime >= 1000.0 )
          {
            System.out.print( String.format( "%.3f", renderTime / 1000.0 ) );
            System.out.print( "\u00B5" );
          }
          else
          {
            System.out.print( String.format( "%.3f", renderTime / 1000.0 ) );
            System.out.print( "n" );
          }
          System.out.println( "s." );
        }
      }
    };
    layeredPane.setLayout( new StackLayout() );

    layeredPane.add( this.modelView, StackLayout.TOP );
    layeredPane.add( this.cursorView, StackLayout.TOP );
    layeredPane.add( this.arrowView, StackLayout.TOP );

    final MyMouseListener listener = new MyMouseListener( this.controller );
    layeredPane.addMouseListener( listener );
    layeredPane.addMouseMotionListener( listener );

    this.mainFrame.addKeyListener( new MyKeyListener( this.controller ) );
    this.mainFrame.addComponentListener( new MyComponentListener( this.controller ) );

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

    final DataModel model = new DataModel( 1024 * 64 * 1024 );
    this.controller = new ScreenController( model );

    this.modelView = new ModelView( this.controller );
    this.cursorView = new CursorView( this.controller );
    this.arrowView = new ArrowView( this.controller );
  }

  /**
   * 
   */
  private void run()
  {
    this.mainFrame.setVisible( true );
  }
}
