/**
 * 
 */
package nl.lxtreme.test.view.renderer;


import java.awt.*;

import nl.lxtreme.test.*;
import nl.lxtreme.test.model.*;
import nl.lxtreme.test.view.*;


/**
 * Renders the "flag" of a cursor, containing the cursor's label and time
 * information.
 */
public class CursorFlagRenderer extends BaseRenderer
{
  // VARIABLES

  private int cursorIdx;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void render( final Graphics2D aCanvas, final int aXpos, final int aYpos )
  {
    final SignalDiagramController controller = getController();
    final Rectangle clip = getClipBounds();

    final SampleDataModel dataModel = controller.getDataModel();

    final Long cursorTimestamp = dataModel.getCursors()[this.cursorIdx];
    if ( cursorTimestamp == null )
    {
      return;
    }

    final int sampleRate = dataModel.getSampleRate();

    final ScreenModel screenModel = controller.getScreenModel();

    final String timeStr = String.format( "%s: %s", screenModel.getCursorLabel( this.cursorIdx ),
        Utils.displayTime( cursorTimestamp.doubleValue() / sampleRate ) );

    final FontMetrics fm = aCanvas.getFontMetrics();

    final int w = fm.stringWidth( timeStr ) + 6;
    final int h = fm.getHeight() + 4;

    final int x1 = controller.toScaledScreenCoordinate( cursorTimestamp.longValue() ).x;
    final int y1 = clip.height - h;

    if ( clip.contains( x1, y1 ) || clip.contains( x1 + w, y1 + h ) )
    {
      aCanvas.setColor( screenModel.getCursorColor( this.cursorIdx ) );

      aCanvas.drawRect( x1, y1, w, h - 1 );

      final int textXpos = x1 + 3;
      final int textYpos = y1 + fm.getLeading() + fm.getAscent() + 2;

      aCanvas.drawString( timeStr, textXpos, textYpos );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUserParams( final Object... aUserParams )
  {
    if ( ( aUserParams == null ) || ( aUserParams.length < 1 ) )
    {
      throw new IllegalArgumentException();
    }
    this.cursorIdx = ( ( Integer )aUserParams[0] ).intValue();
  }
}
