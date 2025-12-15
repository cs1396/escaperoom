package cs1396.escaperoom.engine.types;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class Size implements Serializable {
  public int width;
  public int height;

  public Size(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public Vector2 toVector() {
    return new Vector2(width, height);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Size)) return false;

    Size that = (Size) other;
    if (this.width == that.width && this.height == that.height) return true;

    return false;
  }

  @Override
  public String toString() {
    return String.format("[w: %d, h: %d]", width, height);
  }

  public Size copy(){
    return new Size(width, height);
  }
}
