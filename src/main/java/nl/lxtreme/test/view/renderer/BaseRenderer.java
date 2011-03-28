/**
 * 
 */
package nl.lxtreme.test.view.renderer;


import java.awt.*;

import nl.lxtreme.test.view.*;


/**
 * Provides an abstract base class for renderers.
 */
abstract class BaseRenderer implements Renderer
{
  // VARIABLES

  private SignalDiagramController controller;
  private Rectangle clipBounds;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void setContext( final SignalDiagramController aController, final Rectangle aClipBounds,
      final Object... aUserParams )
  {
    this.controller = aController;
    this.clipBounds = aClipBounds;
    setUserParams( aUserParams );
  }

  /**
   * @return the clipBounds
   */
  protected final Rectangle getClipBounds()
  {
    return this.clipBounds;
  }

  /**
   * @return the controller
   */
  protected final SignalDiagramController getController()
  {
    return this.controller;
  }

  /**
   * @param aUserParams
   */
  protected abstract void setUserParams( final Object... aUserParams );
}
