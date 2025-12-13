package group24.escaperoom.game.entities.properties;


import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.PhantomProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.ui.ActionDialog;

public class InteractableProperty extends PhantomProperty {
  private Logger log = Logger.getLogger(InteractableProperty.class.getName());

  private static final PropertyDescription description = new PropertyDescription(
    "Interactable",
    "Can be interacted with",
    "Interactable items provide actions to the player. These actions are determined by other item properties",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  @Override
  public String getDisplayName() {
    return "Interactable Property";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Interactable;
  }

  public void interact(GameContext ctx) {

    Array<PlayerAction> actions = owner.getPlayerActions(ctx);
    Array<PlayerAction> validActions = new Array<>();

    for (PlayerAction action : actions){
      log.fine("Checking if action: " + action.getActionName() + " is valid");
      if (action.isValid(ctx)) {
        log.fine(action.getActionName() + " is valid!");
        validActions.add(action);
      } else {
        log.fine(action.getActionName() + " is invalid ");
      }
    }

    if (validActions.isEmpty()){
      log.fine("No available actions -> returning");
      return;
    } else if (validActions.size == 1){
      log.fine("One available action -> acting on it");
      ctx.player.stats.actionsPerformed += 1;
      validActions.first().act(ctx).getDialog().ifPresent((dialog) ->{
        dialog.show(ctx.map.getUIStage());
      });
    } else {
      log.fine("Many actions -> showing dialog");
      new ActionDialog(owner, ctx.player).show(ctx.map.getUIStage());
    }
  }
}
