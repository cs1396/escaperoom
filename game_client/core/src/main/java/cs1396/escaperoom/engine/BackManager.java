package cs1396.escaperoom.engine;

import java.util.Stack;

import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.screens.AbstractScreen;

/**
 * Static class for managing "go back" actions
 */
public class BackManager {
  static {
    ControlsManager.registerPermanentInput(Input.BACK, InputType.PRESSED, BackManager::goBack);
  }

  /**
   * Represent a possible back action.
   */
  public interface PossibleBackAction {
    /**
     * Called when the user wants to go back
     * @return whether or not the back action was performed
     */
    public boolean onBack();
  }

  /**
   * Represent a back action.
   */
  public interface BackAction {
    /**
     * Called when the user wants to go back
     */
    public void onBack();
  }

  static private Stack<PossibleBackAction> backActions = new Stack<>();
  static private AbstractScreen owner;
  static private BackAction onEmpty;

  /**
   * @param screen the current owner of the {@link BackManager}
   */
  public static void setOwner(AbstractScreen screen) {
    owner = screen;
  }

  /**
   * Clear all actions and reset on empty
   *
   * @see BackManager#setOnEmpty(BackAction)
   */
  public static void clearActions() {
    backActions.clear();
    onEmpty = null;
  }

  /**
   * @param action to be performed when the user "goes back" with nothing in the stack
   */
  public static void setOnEmpty(BackAction action) {
    onEmpty = action;
  }
  
  /**
   * @param action to add to the stack of back actions
   */
  public static void addBack(BackAction action) {
    addBack(() -> {
      action.onBack();
      return true;
    });
  }

  /**
   * @param action to add to the stack of back actions
   */
  public static void addBack(PossibleBackAction action) {
    backActions.add(action);
  }

  /**
   * Go back
   */
  public static void goBack() {
    if (owner == null) return;

    if (backActions.empty() && onEmpty != null) {
      onEmpty.onBack();
      return;
    }

    if (backActions.size() > 0) {
      PossibleBackAction backAction = backActions.pop();
      if (!backAction.onBack()) goBack();
    }
  }
}
