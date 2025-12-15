package cs1396.escaperoom.engine.control.bindings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public enum InputModifier {
  SHIFT(Keys.SHIFT_LEFT, Keys.SHIFT_RIGHT),
  CONTROL(Keys.CONTROL_LEFT, Keys.CONTROL_RIGHT, Keys.CAPS_LOCK),

  ;

  private int[] keys;

  private InputModifier(int ...keys) {
    this.keys = keys;
  }

  public boolean isDown() {
    for (int key : keys) {
      if (Gdx.input.isKeyPressed(key)) return true;
    }

    return false;
  }

  @Override
  public String toString() {
    switch(this) {
		case CONTROL:
      return "C";
		case SHIFT:
      return "S";
    }
    return "";
  }
}
