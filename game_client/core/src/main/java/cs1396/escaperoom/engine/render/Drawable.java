package cs1396.escaperoom.engine.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public interface Drawable {
  /**
   * @return the renderPriority of this Drawable 
   */
  public int renderPriority();

  /**
   * Draw this Drawable on the {@link Batch}
   * @param batch to draw on
   */
  public void draw(Batch batch);

  /**
   * @return coordinates of the lower left corner of this Drawable
   */
  public Vector2 position();


  /**
   * @return the tile depth of this Drawable
   */
  public int getTileDepth();
}
