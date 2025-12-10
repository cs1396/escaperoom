package group24.escaperoom.game.ui;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.Player;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.player.PlayerAction.ActionResult;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.screens.AbstractScreen;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.ui.widgets.G24Label;


/**
 * Appears when the {@link Player} interacts with an {@link Item} AND
 * that {@link Item} provides more than one {@link PlayerAction}
 * 
 */
public class ActionDialog extends GameDialog {
  private Array<PlayerAction> actions;
  HashMap<PlayerAction, ActionButton> buttons = new HashMap<>();
  Item item;

  /**
   * A specialized button which executes a {@link PlayerAction} when clicked
   */
  public class ActionButton extends G24TextButton {


    /**
     * @param label The label to be displayed on this button
     * @param action The action to perform when clicked
     * @param item The item on which the action is being performed
     * @param player a reference to the {@link Player} who would perform that action
     */
    public ActionButton(String label, PlayerAction action, Item item, Player player) {
      super(label);
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {

          GameContext ctx = new GameContext(player.getGameScreen(), player);

          if (isChecked() && action.isValid(ctx)) {
            player.stats.actionsPerformed += 1;
            ActionResult result = action.act(ctx);

            if (result.completesInteraction()){
              hide();
            }

            result.getDialog().ifPresent(dialog -> {
              dialog.disableSpawner(buttons.get(action));
              dialog.show(ActionButton.this.getStage());
              setChecked(false);
              if (!action.isValid(ctx)){
                setDisabled(true);
              }
            });
          }

          setChecked(false);
        }
      });
    }
  }


  private class ActionButtonGroup extends Table {
    public ActionButtonGroup() {
      super(AbstractScreen.skin);
      if (actions.isEmpty()){
        add(new G24Label("No available actions for " + item.getItemName()));
      } else {
        for (PlayerAction action : actions) {
          if (action.isValid(new GameContext(player.getGameScreen()))) {
            ActionButton b = new ActionButton(action.getActionName(), action, item, player);
            buttons.put(action, b);
            add(b);
            row();
          }
        }
      }
    }
  }

  /**
   * @param item the item which is being interacted with
   * @param player the player which is doing the interacting
   */
  public ActionDialog(Item item, Player player) {
    super(new GameDialog.Builder("Actions for " + item.getItemName() + "...", player));
    this.item = item;
    setModal(false);
    GameContext ctx = new GameContext(player.getGameScreen(), player);
    actions = item.getPlayerActions(ctx);
    getContentTable().add(new ActionButtonGroup());
  }

  @Override
  public void act(float delta){
    super.act(delta);
    buttons.forEach((PlayerAction a, ActionButton b) -> {
      if (!a.isValid(new GameContext(player.getGameScreen()))){
        b.setDisabled(true);
      }
    });
  }
}
