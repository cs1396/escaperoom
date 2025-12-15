package cs1396.escaperoom.game.entities.player;

import java.util.Optional;

import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.ui.widgets.G24Dialog;

public interface PlayerAction {

  public class ActionResult {
    Optional<G24Dialog> display = Optional.empty();
    boolean completesInteraction = false;

    public static final ActionResult DEFAULT = new ActionResult();

    public ActionResult showsDialog(G24Dialog contentToDisplay){
      if (this == DEFAULT) return this; 

      display = Optional.of(contentToDisplay);
      return this;
    }

    public boolean completesInteraction(){
      return completesInteraction;
    }

    /**
     * Indicates that if this action is taken, the window should close
     */
    public ActionResult setCompletesInteraction(boolean shouldCompleteInteraction){
      if (this == DEFAULT) return this; 

      completesInteraction = shouldCompleteInteraction;

      return this;
    }

    public ActionResult(){}

    /**
     * Get the supplementary dialog from this action, if there is one
     */
    public Optional<G24Dialog> getDialog(){
      return display;
    }
  }

  public String getActionName();

  /**
   *
   * If the return is present, an additional dialog containing the actor is displayed
   * with a single "Continue" button, which dismisses the dialog after exit.
   *
   * Any state changes that this action enacts need to occur before this function returns.
   *
   * Before the menu is displayed, {@link PlayerAction#isValid} is called to potentially disable the button;
   */
  public ActionResult act(GameContext ctx);

  public boolean isValid(GameContext ctx);
}
