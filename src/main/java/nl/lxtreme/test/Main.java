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
package nl.lxtreme.test;


import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;

import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.plaf.*;


/**
 * @author jajans
 */
public class Main
{
  // INNER TYPES

  /**
   * Provides only samples with alternating values.
   */
  static class AlternatingDataProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      aValues[0] = 0;
      aTimestamps[0] = 0;
      aValues[1] = 0xFFFFFFFF;
      aTimestamps[1] = 10;
      aValues[2] = 0;
      aTimestamps[2] = 20;

      int si = 3;
      for ( int i = si; i < aSize; i++ )
      {
        if ( i > si )
        {
          aValues[i] = aValues[i - 1] == 0xAAAAAAAA ? 0x55555555 : 0xAAAAAAAA;
        }
        else
        {
          aValues[i] = 0xAAAAAAAA;
        }
        aTimestamps[i] = ( i * 10 );
      }
      return 1000000000; // 1000MHz
    }
  }

  /**
   * Provides only samples with alternating values with large spaces in between.
   */
  static class AlternatingDataWithSpacesProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      boolean markOrSpace = false; // mark
      int interval = aSize / 10;

      int lastValue = -1;
      for ( int i = 0; i < aSize; i++ )
      {
        if ( ( i % interval ) == 0 )
        {
          markOrSpace = !markOrSpace;
        }

        if ( markOrSpace )
        {
          if ( i > 0 )
          {
            aValues[i] = ( ( lastValue == 0xAAAA ) ? 0x5555 : 0xAAAA );
          }
          else
          {
            aValues[i] = 0xAAAA;
          }
          lastValue = aValues[i];
        }
        else
        {
          aValues[i] = 0;
        }
        aTimestamps[i] = 0 + ( i * 10 );
      }
      return 1000000000; // 1GHz
    }
  }

  /**
   * Provides only samples that together form a 10-bit counter.
   */
  static class CounterDataProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      for ( int i = 0; i < aSize; i++ )
      {
        aValues[i] = i % 1024;
        aTimestamps[i] = i;
      }
      return 100000000; // 100MHz
    }
  }

  /**
   * Provides only samples with alternating values.
   */
  static class OffsetDataProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      boolean on = true;
      int chunkSize = ( aSize / 10 );

      for ( int i = 0; i < aSize; i++ )
      {
        if ( ( i % chunkSize ) == 0 )
        {
          on = !on;
        }
        if ( !on )
        {
          continue;
        }

        if ( i > 0 )
        {
          aValues[i] = aValues[i - 1] == 0xAAAA ? 0x5555 : 0xAAAA;
        }
        else
        {
          aValues[i] = 0xAAAA;
        }
        aTimestamps[i] = ( i * 10 ) + 0;
      }
      return 200000000; // 200MHz
    }
  }

  /**
   * Provides only samples with ones.
   */
  static class OneDataProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      for ( int i = 0; i < aSize; i++ )
      {
        aValues[i] = 0xFFFF;
        aTimestamps[i] = i;
      }
      return 1;
    }
  }

  /**
   * Provides only samples with random data.
   */
  static class RandomDataProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      final Random rnd = new Random();
      for ( int i = 0; i < aSize; i++ )
      {
        aValues[i] = rnd.nextInt();
        aTimestamps[i] = 4 + i;
      }
      return 25000000; // 25MHz
    }
  }

  /**
   * Provides only zero samples.
   */
  static class ZeroDataProvider implements SampleDataProvider
  {
    @Override
    public int getSampleData( final int[] aValues, final long[] aTimestamps, final int aSize )
    {
      for ( int i = 0; i < aSize; i++ )
      {
        aValues[i] = 0;
        aTimestamps[i] = i;
      }
      return 1;
    }
  }

  // VARIABLES

  private SignalDiagramComponent signalDiagram;
  private SignalDetailsView signalDetails;
  private CaptureDetailsView captureDetails;
  private CursorDetailsView cursorDetails;
  private JMenuBar menuBar;

  SignalDiagramController controller;
  JFrame mainFrame;

  // METHODS

  /**
   * @param args
   */
  public static void main( final String[] aArgs ) throws Exception
  {
    final Runnable runner = new Runnable()
    {
      @Override
      public void run()
      {
        final Main main = new Main();

        // ThreadViolationDetectionRepaintManager.install();

        main.init();
        main.build();
        main.run();
      };
    };
    SwingUtilities.invokeLater( runner );
  }

  /**
   * Returns whether the current host's operating system is Mac OS X.
   * 
   * @return <code>true</code> if running on Mac OS X, <code>false</code>
   *         otherwise.
   */
  private static final boolean isMacOS()
  {
    final String osName = System.getProperty( "os.name" );
    return ( "Mac OS X".equalsIgnoreCase( osName ) );
  }

  /**
   * @param aWindow
   */
  private static void tweakToolWindow( final ToolWindow aWindow, final int aDockLength )
  {
    RepresentativeAnchorDescriptor<?> anchorDesc = aWindow.getRepresentativeAnchorDescriptor();
    anchorDesc.setPreviewEnabled( false );

    final ToolWindowType[] types = ToolWindowType.values();
    for ( ToolWindowType type : types )
    {
      ToolWindowTypeDescriptor desc = aWindow.getTypeDescriptor( type );
      desc.setHideRepresentativeButtonOnVisible( true );
      desc.setIdVisibleOnTitleBar( false );
    }

    DockedTypeDescriptor desc = ( DockedTypeDescriptor )aWindow.getTypeDescriptor( ToolWindowType.DOCKED );
    desc.setDockLength( aDockLength );
    desc.setHideRepresentativeButtonOnVisible( true );
    desc.setPopupMenuEnabled( true );

    aWindow.setAvailable( true );
    aWindow.setHideOnZeroTabs( true );
  }

  /**
   * 
   */
  void build()
  {
    final MyDoggyToolWindowManager wm = new MyDoggyToolWindowManager();
    ToolWindowGroup group = wm.getToolWindowGroup( "Main" );
    group.setVisible( false );
    group.setImplicit( true );

    ToolWindow tw1 = wm.registerToolWindow( "Acquisition", // Id
        "Acquisition Details", // Title
        null, // Icon
        this.captureDetails, // Component
        ToolWindowAnchor.RIGHT ); // Anchor
    group.addToolWindow( tw1 );

    ToolWindow tw2 = wm.registerToolWindow( "Signal", // Id
        "Signal Details", // Title
        null, // Icon
        this.signalDetails, // Component
        ToolWindowAnchor.RIGHT ); // Anchor
    group.addToolWindow( tw2 );

    ToolWindow tw3 = wm.registerToolWindow( "Cursor", // Id
        "Cursor Details", // Title
        null, // Icon
        this.cursorDetails, // Component
        ToolWindowAnchor.RIGHT ); // Anchor
    group.addToolWindow( tw3 );

    // Given string is based on some experiments with the best "default"
    // length...
    final int dockLength = SwingUtils.getStringWidth( "XXXXXXXXXXXXXXXXXXXXXXXXXXXX" );

    tweakToolWindow( tw1, dockLength );
    tweakToolWindow( tw2, dockLength );
    tweakToolWindow( tw3, dockLength );

    final JScrollPane contentPane = new JScrollPane( this.signalDiagram );
    contentPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
    contentPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );

    wm.setMainContent( contentPane );

    this.mainFrame.setContentPane( wm );
    this.mainFrame.setJMenuBar( this.menuBar );

    this.mainFrame.pack();
  }

  /**
   * 
   */
  void init()
  {
    if ( !isMacOS() )
    {
      try
      {
        UIManager.setLookAndFeel( "com.jgoodies.looks.plastic.Plastic3DLookAndFeel" );
      }
      catch ( Exception exception )
      {
        try
        {
          UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch ( Exception exception2 )
        {
          System.err.println( "L&F setting failed!" );
        }
      }
    }
    else
    {
      try
      {
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
      }
      catch ( Exception exception2 )
      {
        System.err.println( "L&F setting failed!" );
      }
    }

    final Dimension dims = new Dimension( 800, 600 );

    this.mainFrame = new JFrame( "OLS Signal View Component - v2" );
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

    final JMenuItem diagramSnapCursorsItem = new JCheckBoxMenuItem( new AbstractAction( "Snap Cursors?" )
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed( final ActionEvent aEvent )
      {
        AbstractButton button = ( AbstractButton )aEvent.getSource();
        Main.this.controller.setSnapModeEnabled( button.getModel().isSelected() );
      }
    } );
    diagramMenu.add( diagramSnapCursorsItem );

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

    // final SampleDataModel model = new SampleDataModel( 256 * 1024, new
    // AlternatingDataWithSpacesProvider() );
    final SampleDataModel model = new SampleDataModel( 512 * 1024, new AlternatingDataProvider() );

    this.controller = new SignalDiagramController();

    this.signalDiagram = SignalDiagramComponent.create( this.controller );
    this.signalDetails = SignalDetailsView.create( this.controller );
    this.captureDetails = CaptureDetailsView.create( this.controller );
    this.cursorDetails = CursorDetailsView.create( this.controller );

    this.controller.setDataModel( model );

    this.signalDiagram.zoomOriginal();
  }

  /**
   */
  void run()
  {
    this.mainFrame.setVisible( true );
  }
}
