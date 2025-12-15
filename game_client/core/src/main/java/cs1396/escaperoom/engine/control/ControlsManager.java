package cs1396.escaperoom.engine.control;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.control.bindings.InputBinding;
import cs1396.escaperoom.engine.control.bindings.InputModifier;
import cs1396.escaperoom.engine.control.bindings.Keyboard;
import cs1396.escaperoom.engine.control.bindings.MapGroup;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.control.input.InputOverride;

public class ControlsManager {
  public static enum InputType {
    HELD,
    PRESSED,
  }
  public interface InputAction {
    void perform();
  }
  public static enum InputMethod {
    KEYBOARD,
    MOUSE,
  }
  public static class InputPair {
    final int key;
    final InputMethod method;

    public InputPair(int key, InputMethod method) {
      this.key = key;
      this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof InputPair)) return false;
      InputPair other = (InputPair) obj;
      return this.key == other.key && this.method.equals(other.method);
    }
  }

  private static class BoundInput {
    Input input;
    InputBinding binding;
    BoundInput(Input input, InputBinding binding){
      this.input = input;
      this.binding = binding;
    }
  }

  private static class BoundInputComparator implements Comparator<BoundInput> {
    @Override
    public int compare(BoundInput left, BoundInput right) {
      InputPair l = left.binding.getPair();
      InputPair r = right.binding.getPair();

      if (l.key != r.key) return r.key - l.key;

      InputModifier[] rmods = right.binding.getModifiers();
      InputModifier[] lmods = left.binding.getModifiers();
      if (rmods.length != 0 && rmods.length == lmods.length){
        int mi = 0;
        while (mi < rmods.length && mi < lmods.length){
          if (rmods[mi] != lmods[mi]) return rmods[mi].ordinal() - lmods[mi].ordinal(); 
          mi++;
        }
        return 0;
      } else {
        return rmods.length - lmods.length;
      }
    }
  }

  public static final class InputGroupMap extends HashMap<MapGroup, Array<Input>> {};
  public static final class BoundInputs extends TreeMap<BoundInput, HashMap<InputType, Array<InputAction>>> {
    BoundInputs(){ super(new BoundInputComparator()); }
  };
  private static InputGroupMap permMappings = new InputGroupMap();
  private static InputGroupMap activeMappings = new InputGroupMap();
  private static BoundInputs register = new BoundInputs(); 
  private static BoundInputs pregister = new BoundInputs();
  private static Stack<InputOverride> overrides = new Stack<>();
  private static Array<InputPair> processedInputs = new Array<>();
  private static boolean keyboardEnabled = true;

  /**
   * Disable handling non-permanent controls. Typically used when in a text-box
   */
  public static void setKeyboardEnabled(boolean keyboardEnabled) {
    ControlsManager.keyboardEnabled = keyboardEnabled;
  }

  private static void registerNewMapping(InputGroupMap map, Input input){
    if (map.get(input.getGroup()) == null){
      map.put(input.getGroup(), new Array<>());
    }
    map.get(input.getGroup()).add(input);
  }

  /**
   * Register a permanent mapping
   *
   * @see ControlsManager#registerPermanentInput(Input, InputType, InputAction)
   */
  private static void registerPermanentMapping(Input input){
    registerNewMapping(permMappings, input);
  }

  /**
   * Register a mapping
   *
   * @see ControlsManager#registerInput(Input, InputType, InputAction)
   */
  private static void registerMapping(Input input){
    registerNewMapping(activeMappings, input);
  }

  /**
   * Permanently register a new input.
   *
   * These inputs can never be removed or cleared.
   * These inputs always resolve before normal inputs.
   * Any inputs registered for the same input resolve in order of registration.
   */
  public static void registerPermanentInput(Input input, InputType type, InputAction action) {
    registerPermanentMapping(input);
    for (InputBinding bind : input.getBinds()) {
      registerNewInput(new BoundInput(input, bind), type, action, pregister);
    }
  }

  /**
   * Register a new input.
   *
   * These inputs always resolve after permanent inputs.
   * Any inputs registered for the same input resolve in order of registration.
   */
  public static void registerInput(Input input, InputType type, InputAction action) {
    registerMapping(input);
    for (InputBinding bind : input.getBinds()) {
      registerNewInput(new BoundInput(input, bind), type, action, register);
    }
  }

  /**
   * Clear any registered non-permanent inputs and overrides.
   */
  public static void clearRegisteredInputs() {
    register.clear();
    activeMappings.clear();
    overrides.clear();
  }

  public static void pushOverride(InputOverride override){
    overrides.push(override);
  }

  public static void popOverride(InputOverride override){
    if (overrides.isEmpty()) return;

    if (overrides.peek() == override){
      overrides.pop();
    }
  }

  private static void registerNewInput(BoundInput key, InputType type, InputAction action, BoundInputs store) {
    if (store.containsKey(key)) {

      store.get(key).get(type).add(action);

    } else {

      HashMap<InputType, Array<InputAction>> typeMap = new HashMap<>();
      for (InputType t : InputType.values()) {
        typeMap.put(t, new Array<>());
      }

      typeMap.get(type).add(action);

      store.put(key, typeMap);
    }
  }

  public static void processInputs() {
    // clear processed tracker
    processedInputs.clear();

    // permanent binds are always handled. standard binds are only registered when active
    pregister.forEach((input, actions) -> {
      handleInput(input, actions);
    });
    register.forEach((input, actions) -> {
      if (!keyboardEnabled && input.binding.getPair().method == InputMethod.KEYBOARD) return;

      if (processOverride(input)) return;

      handleInput(input, actions);
    });
  }

  private static boolean processOverride(BoundInput bindingKey){
    if (overrides.isEmpty()) return false;

    if (processedInputs.contains(bindingKey.binding.getPair(), false)) return true;

    if (!overrides.peek().getOverriddenInputs().contains(bindingKey.input)){
      return false;
    }

    InputPair pair = null;
    if ((pair = Keyboard.isHeld(bindingKey.binding)) != null){
      if (overrides.peek().handleInput(bindingKey.input, InputType.HELD)){ 
        processedInputs.add(pair);
        return true;
      }
    }

    if ((pair = Keyboard.isPressed(bindingKey.binding)) != null){
      if (overrides.peek().handleInput(bindingKey.input, InputType.PRESSED)){ 
        processedInputs.add(pair);
        return true;
      }
    }

    return false;
  }


  private static void handleInput(BoundInput bindingKey, HashMap<InputType, Array<InputAction>> actions) {
    InputBinding input = bindingKey.binding;
    // if this key, input method pair has been handled already, don't process it anymore this cycle
    if (processedInputs.contains(input.getPair(), false)) return;

    actions.forEach((type, actionList) -> {
      InputPair pair = null;

      switch(type) {
        case HELD:
          pair = Keyboard.isHeld(input);
          break;
        case PRESSED:
          pair = Keyboard.isPressed(input);
          break;
      }

      if (pair != null) {
        actionList.forEach(InputAction::perform);
        processedInputs.add(pair);
      }
    });
  }

  /**
   * Get all currently registered key binds
   */
  public static InputGroupMap registedMappings() {
    InputGroupMap ret = new InputGroupMap();
    ret.putAll(permMappings);
    ret.putAll(activeMappings);

    return ret;
  }

  /**
   * Check if a keycode, input method, and list of input modifiers corresponds
   * to a known Input.
   *
   * Used with listeners rather than input registration, though is *much* slower
   * and is thus recommended against using when possible.
   *
   * Ignores whether keyboard is enabled or not, along with if inputs for this
   * key have been processed this input cycle.
   */
  public static Input matchesInput(int code, InputMethod method, InputModifier ...modifiers) {
    Set<InputModifier> givenMods = Set.of(modifiers);

    for (Input i : Input.values()) {
      for (InputBinding bind : i.getBinds()) {
        InputPair pair = bind.getPair();
        if (pair.key != code || pair.method != method) continue;

        Set<InputModifier> expctMods = Set.of(bind.getModifiers());
        if (givenMods.equals(expctMods)) return i;
      }
    }
    return null;
  }
}
