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
  final class MyMouseListener extends MouseAdapter
  {
    private volatile int lastCursor = -1;
    private volatile boolean showing = false;

    @Override
    public void mouseDragged( final MouseEvent aEvent )
    {
      if ( this.lastCursor < 0 )
      {
        return;
      }

      dragCursor( this.lastCursor, aEvent.getPoint() );
      aEvent.consume();
    }

    @Override
    public void mouseMoved( final MouseEvent aEvent )
    {
      final Point point = aEvent.getPoint();

      if ( !this.showing )
      {
        if ( aEvent.isControlDown() )
        {
          this.showing = showHover( point );
          aEvent.consume();
        }
      }
      else
      {
        if ( !aEvent.isControlDown() )
        {
          this.showing = hideHover();
          aEvent.consume();
        }
        else
        {
          moveHover( point );
          aEvent.consume();
        }
      }
    }

    @Override
    public void mousePressed( final MouseEvent aEvent )
    {
      this.lastCursor = findCursor( aEvent.getPoint() );
      aEvent.consume();
    }

    @Override
    public void mouseReleased( final MouseEvent aEvent )
    {
      if ( this.lastCursor < 0 )
      {
        return;
      }
      this.lastCursor = -1;
      aEvent.consume();
    }
  }

  private Model model;

  private ModelView modelView;
  private CursorView cursorView;
  private ArrowView arrowView;

  private JFrame mainFrame;

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
   * @return
   */
  public Model getModel()
  {
    return this.model;
  }

  /**
   * @return
   */
  public Dimension getPreferredSize()
  {
    return this.mainFrame.getPreferredSize();
  }

  /**
   * @param aCursorIdx
   * @param aPoint
   */
  final void dragCursor( final int aCursorIdx, final Point aPoint )
  {
    final Point point = convertToPoint( this.cursorView, aPoint );
    this.cursorView.moveCursor( aCursorIdx, point );
  }

  /**
   * @param aPoint
   * @return
   */
  final int findCursor( final Point aPoint )
  {
    final Point point = convertToPoint( this.cursorView, aPoint );
    return this.cursorView.findCursor( point );
  }

  /**
   * @param aPoint
   * @return
   */
  final boolean hideHover()
  {
    this.arrowView.hideHover();
    return false;
  }

  /**
   * @param aPoint
   */
  final void moveHover( final Point aPoint )
  {
    final Rectangle signalHover = this.modelView.getSignalHover( convertToPoint( this.modelView, aPoint ) );
    this.arrowView.moveHover( signalHover );
  }

  /**
   * @param aPoint
   * @return
   */
  final boolean showHover( final Point aPoint )
  {
    final Rectangle signalHover = this.modelView.getSignalHover( convertToPoint( this.modelView, aPoint ) );
    this.arrowView.showHover( signalHover );

    return true;
  }

  /**
   * 
   */
  private void build()
  {
    final JPanel layeredPane = new JPanel();
    layeredPane.setLayout( new StackLayout() );

    layeredPane.add( this.modelView, StackLayout.TOP );
    layeredPane.add( this.cursorView, StackLayout.TOP );
    layeredPane.add( this.arrowView, StackLayout.TOP );

    final MyMouseListener listener = new MyMouseListener();
    layeredPane.addMouseListener( listener );
    layeredPane.addMouseMotionListener( listener );

    this.mainFrame.setContentPane( layeredPane );
    this.mainFrame.pack();
  }

  /**
   * @param aDestination
   * @param aOriginal
   * @return
   */
  private Point convertToPoint( final Component aDestination, final Point aOriginal )
  {
    return SwingUtilities.convertPoint( this.mainFrame.getContentPane(), aOriginal, aDestination );
  }

  /**
   * 
   */
  private void init()
  {
    this.model = new Model( 1024 );

    final Dimension dims = new Dimension( 640, 480 );

    this.mainFrame = new JFrame( "JLayeredPane test" );
    this.mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    this.mainFrame.setPreferredSize( dims );
    this.mainFrame.setSize( dims );

    this.modelView = new ModelView( this );
    this.cursorView = new CursorView( this );
    this.arrowView = new ArrowView();
  }

  /**
   * 
   */
  private void run()
  {
    this.mainFrame.setVisible( true );
  }
}
