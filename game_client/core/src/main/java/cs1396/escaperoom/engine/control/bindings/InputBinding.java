package cs1396.escaperoom.engine.control.bindings;

import cs1396.escaperoom.engine.control.ControlsManager.InputMethod;
import cs1396.escaperoom.engine.control.ControlsManager.InputPair;

public abstract class InputBinding {
  protected int key;
  protected InputModifier[] modifiers;
  protected InputMethod method;


  public abstract boolean wasPressed();
  public abstract boolean isDown();

  public InputPair getPair() {
    return new InputPair(key, method);
  }

  public InputModifier[] getModifiers() {
    return modifiers;
  }

  public int numModifiers() {
    return modifiers.length;
  }

  public String modifierString(){
    String ret = "";
    for (InputModifier mod : modifiers){
      ret += mod.toString() + "-";
    }
    return ret;
  }
}
