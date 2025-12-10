package group24.escaperoom.game.entities.properties.locks;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.editor.ui.ConfigurationMenu;
import group24.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import group24.escaperoom.editor.ui.Menu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;
import group24.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import group24.escaperoom.engine.BackManager;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.LockingMethod;
import group24.escaperoom.game.entities.properties.values.StringItemPropertyValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.state.GameEvent;
import group24.escaperoom.game.state.GameEvent.EventType;
import group24.escaperoom.game.state.GameEventBus;
import group24.escaperoom.game.ui.GameDialog;
import group24.escaperoom.ui.widgets.G24NumberInput;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.ui.widgets.G24TextInput;

public class CombinationLock extends LockingMethod implements StringItemPropertyValue {
  protected class TryUnlock implements PlayerAction {
    @Override
    public String getActionName() {
      return "Enter combination";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      if (ctx.player == null) {
        return ActionResult.DEFAULT;
      }

      GameDialog dialog = new GameDialog.Builder("Enter Combination", ctx.player).build();

      Table table = new Table();
      G24NumberInput[] digits = new G24NumberInput[combination.length()];
      for (int i = 0; i < combination.length(); i++) {
        G24NumberInput input = new G24NumberInput();
        input.enableAutoFocusTraversal();
        input.setMessageText("0");
        input.setMaxLength(1);
        digits[i] = input;
      }

      G24TextButton submitButton = new G24TextButton("Try Unlock");

      submitButton.setProgrammaticChangeEvents(false);
      submitButton.addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (submitButton.isPressed()) {
            String result = new String();
            for (G24NumberInput input : digits) {
              result += input.getText();
            }
            if (result.equals(combination)) {
              isLocked = false;
              if (isBarrier) {
                owner.ifPresent((i) -> {
                  i.setBlocksPlayer(false);
                  i.setAlpha(0.5f);
                });
              }
              submitButton.setDisabled(true);
              dialog.hide();
              owner.ifPresent((i) -> {
                GameEventBus.get().post(
                  new GameEvent.Builder(EventType.ItemStateChange, ctx)
                    .message(i.getItemName() + " clicked open...")
                    .build()
                );
              });
              if (actor.getStage().getKeyboardFocus() instanceof G24TextInput){
                BackManager.goBack();
              }
              BackManager.goBack();
            } else {
              owner.ifPresent((i) -> {
                GameEventBus.get().post(
                  new GameEvent.Builder(EventType.ItemStateChange, ctx)
                    .message(i.getItemName() + " won't budge...")
                    .build()
                );
              });
            }
          }
          submitButton.setChecked(false);
        }
      });
      table.defaults().pad(0).center();
      for (G24NumberInput input : digits) {
        table.add(input).maxWidth(20).minWidth(0);
      }
      table.row();
      table.add(submitButton).align(Align.center).colspan(digits.length);
      table.row();

      dialog.getContentTable().add(table).center();
      return new ActionResult().showsDialog(dialog);
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return isLocked();
    }
  };

  public String combination = "1234";

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public CombinationLock() {}

  public CombinationLock(String val) {
    combination = val;
  }

  @Override
  public String getValue() {
    return combination;
  }

  @Override
  public void setValue(String value) {
    this.combination = value;
  }

  @Override
  public boolean isLocked() {
    return isLocked;
  }


  @Override
  public MenuEntry getDisplay(Menu parent) {
    return new MenuEntryBuilder(parent, getName())
      .spawns((e) -> {
        return new ConfigurationMenu<NumberInput>(e,configurationDisplay(), "Combination", parent.getScreen());
      })
      .build();
  }
  private class NumberInput extends G24NumberInput implements HandlesMenuClose {
    NumberInput(String s){
      super(s);
    }
    @Override
      public void handle() {}
  }

  public NumberInput configurationDisplay() {
    NumberInput input = new NumberInput(this.combination);
    input.setMaxLength(8);
    input.setWidth(100);

    input.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        combination = input.getText();
      }
    });
    return input;
  }

  @Override
  public String getName() {
    return "Combination Lock";
  }

  @Override
  public Array<PlayerAction> getActions() {
    return Array.with(new TryUnlock());
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("combo", this.combination);
  }

  @Override
  public void read(Json json, JsonValue data) {
    super.read(json, data);
    this.combination = data.getString("combo", "1234");
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.CombinationLock;
  }

  @Override
  protected LockingMethod getEmptyMethod() {
    return new CombinationLock();
  }

  @Override
  protected PlayerAction maybeGetLockAction() {
    return null;
  }

  @Override
  protected PlayerAction maybeGetUnlockAction() {
    return new TryUnlock();
  }
}
