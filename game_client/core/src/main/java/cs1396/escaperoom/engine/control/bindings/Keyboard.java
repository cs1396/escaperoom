package cs1396.escaperoom.engine.control.bindings;

import cs1396.escaperoom.engine.control.ControlsManager.InputPair;
import cs1396.escaperoom.engine.control.input.Input;

public class Keyboard {
  public static InputPair isHeld(Input input) {
    return oneOfHeld(input.getBinds());
  }
  public static InputPair isHeld(InputBinding input) {
    return oneOfHeld(input);
  }

  public static InputPair isPressed(Input input) {
    return oneOfPressed(input.getBinds());
  }
  public static InputPair isPressed(InputBinding input) {
    return oneOfPressed(input);
  }

  private static InputPair oneOfHeld(InputBinding ...keys) {
    for (InputBinding key : keys) {
      if (key.isDown()) {
        return key.getPair();
      }
    }
    return null;
  }

  private static InputPair oneOfPressed(InputBinding ...keys) {
    for (InputBinding key : keys) {
      if (key.wasPressed()) {
        return key.getPair();
      }
    }
    return null;
  }
}
