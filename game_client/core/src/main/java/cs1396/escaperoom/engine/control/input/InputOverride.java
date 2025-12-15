package cs1396.escaperoom.engine.control.input;

import java.util.Set;

import cs1396.escaperoom.engine.control.ControlsManager.InputType;

public interface InputOverride {

  /**
   * Attempt to handle the given input.
   *
   * @return whether or not the input should be consumed
   */
  boolean handleInput(Input input, InputType type);

  /**
   *
   * {@link Input}s that this override cares about
   */
  Set<Input> getOverriddenInputs();
}
