package cs1396.escaperoom.engine.control.bindings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;

import cs1396.escaperoom.engine.control.ControlsManager.InputMethod;

public class MouseBinding extends InputBinding {
  public MouseBinding(int bind) {
    this(bind, new InputModifier[]{});
  }

  public MouseBinding(int bind, InputModifier ...modifiers) {
    key = bind;
    this.modifiers = modifiers;
    method = InputMethod.MOUSE;
  }

  public boolean wasPressed() {
    if (!Gdx.input.isButtonJustPressed(key)) return false;

    for (InputModifier modifier : modifiers) {
      if (!modifier.isDown()) return false;
    }
    
    return true;
  }

  public boolean isDown() {
    if (!Gdx.input.isButtonPressed(key)) return false;

    for (InputModifier modifier : modifiers) {
      if (!modifier.isDown()) return false;
    }

    return true;
  }
  @Override
  public String toString() {
    String inputString = "";
    switch (key) {
      case Buttons.RIGHT: 
        inputString = "RightClick";
        break;
      case Buttons.LEFT: 
        inputString = "LeftClick";
        break;
      case Buttons.MIDDLE: 
        inputString = "MiddleMouse";
        break;
    }
    return modifierString() + inputString;
  }
  
}
