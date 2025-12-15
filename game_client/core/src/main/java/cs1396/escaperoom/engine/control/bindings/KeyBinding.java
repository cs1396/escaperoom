package cs1396.escaperoom.engine.control.bindings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import cs1396.escaperoom.engine.control.ControlsManager.InputMethod;

public class KeyBinding extends InputBinding {
  public KeyBinding(int bind) {
    this(bind, new InputModifier[]{});
  }

  public KeyBinding(int bind, InputModifier ...modifiers) {
    key = bind;
    this.modifiers = modifiers;
    method = InputMethod.KEYBOARD;
  }

  public boolean wasPressed() {
    if (!Gdx.input.isKeyJustPressed(key)) return false;

    for (InputModifier modifier : modifiers) {
      if (!modifier.isDown()) return false;
    }

    return true;
  }

  public boolean isDown() {
    if (!Gdx.input.isKeyPressed(key)) return false;

    for (InputModifier modifier : modifiers) {
      if (!modifier.isDown()) return false;
    }

    return true;
  }
  
  @Override
  public String toString() {
    return modifierString() + Keys.toString(key);
  }
}

