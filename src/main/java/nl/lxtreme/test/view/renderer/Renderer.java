/**
 * 
 */
package nl.lxtreme.test.view.renderer;


import java.awt.*;

import nl.lxtreme.test.view.*;


/**
 * Provides a renderer of a specific UI-part.
 */
public interface Renderer
{
  // METHODS

  /**
   * Renders the UI-part on the given canvas.
   * 
   * @param aCanvas
   *          the canvas to use to render;
   * @param aXpos
   *          the X-position;
   * @param aYpos
   *          the Y-position.
   */
  void render( final Graphics2D aCanvas, final int aXpos, final int aYpos );

  /**
   * Sets the context in which this renderer should function.
   * 
   * @param aController
   *          the controller to use;
   * @param aClipBounds
   *          the clip boundaries to use;
   * @param aUserParams
   *          the additional (renderer-specific) parameters.
   */
  void setContext( final SignalDiagramController aController, final Rectangle aClipBounds, final Object... aUserParams );
}
