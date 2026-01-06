package cs1396.escaperoom.game.entities.properties.locks;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry.MenuInputOptions;
import cs1396.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.game.entities.properties.base.LockingMethod;
import cs1396.escaperoom.game.entities.properties.values.StringItemPropertyValue;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.ui.GameDialog;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;

public class PassphraseLock extends LockingMethod implements StringItemPropertyValue {

  private boolean caseSensitive = false;

  protected class LockAction extends AbstractLockAction {
    @Override
    public ActionResult act(GameContext ctx) {
      updateLocked(ctx, true);
      return ActionResult.DEFAULT;
    }
  }

  protected class UnlockAction extends AbstractUnlockAction {
    @Override
    public String getActionName() {
      return "Enter passphrase";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      if (ctx.player == null) {
        return ActionResult.DEFAULT;
      }

      GameDialog dialog = new GameDialog.Builder("Enter Passphrase", ctx.player).build();

      Table table = new Table();
      G24TextInput[] chars = new G24TextInput[passphrase.length()];
      for (int i = 0; i < passphrase.length(); i++) {
        G24TextInput input = new G24TextInput();
        input.setMessageText("?");
        input.enableAutoFocusTraversal();
        input.setMaxLength(1);
        chars[i] = input;
      }

      G24TextButton submitButton = new G24TextButton("Try Unlock");

      submitButton.setProgrammaticChangeEvents(false);
      submitButton.addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (submitButton.isPressed()) {
            String result = new String();
            for (G24TextInput input : chars) {
              result += input.getText();
            }

            String key = passphrase;

            if (!caseSensitive) {
              result = result.toLowerCase();
              key = passphrase.toLowerCase();
            }


            if (result.equals(key)) {
              submitButton.setDisabled(true);
              dialog.hide();
              updateLocked(ctx, false, "", owner.get().getItemName() + " clicked open...");

              if (actor.getStage().getKeyboardFocus() instanceof G24TextInput){
                BackManager.goBack();
              }
              BackManager.goBack();
            } else {
              updateLocked(ctx, true, owner.get().getItemName() + " won't budge...", "");
            }
          }
          submitButton.setChecked(false);
        }
      });
      table.defaults().pad(0).center();
      for (G24TextInput input : chars) {
        table.add(input).maxWidth(20).minWidth(0);
      }
      table.row();
      table.add(submitButton).align(Align.center).colspan(chars.length);
      table.row();

      dialog.getContentTable().add(table).center();
      return new ActionResult().showsDialog(dialog);
    }
  };

  public String passphrase = "a1b2";

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public PassphraseLock() {}

  public PassphraseLock(String val) {
    passphrase = val;
  }

  @Override
  public String getValue() {
    return passphrase;
  }

  @Override
  public void setValue(String value) {
    this.passphrase = value;
  }

  @Override
  public boolean isLocked() {
    return isLocked;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return new MenuEntryBuilder(parent, getName())
      .spawns((e) -> {
        Menu menu =  new Menu(e, "Passphrase Configuration", parent.getScreen());

        G24TextInput input = new G24TextInput(this.passphrase);
        input.setMaxLength(10);
        input.setAlphanumeric();

        input.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            passphrase = input.getText();
          }
        });

        menu.add(new G24Label("Passphrase:", G24LabelStyle.Underline)).row();
        menu.add(input).row();

        menu.add(MenuEntry.divider()).row();

        menu.add(
          MenuEntry.toggle(
            parent, 
            "Case sensitive",
            new MenuInputOptions<Boolean>(
              caseSensitive,
              (val) -> caseSensitive = val)
          )
        ).left();
        menu.pack();
        return menu;
    }).build();
  }

  @Override
  public String getName() {
    return "Passphrase";
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("passphrase", this.passphrase);
    json.writeValue("case_sensitive", this.caseSensitive);
  }

  @Override
  public void read(Json json, JsonValue data) {
    super.read(json, data);
    this.passphrase = data.getString("passphrase", "a12b");
    this.caseSensitive = data.getBoolean("case_sensitive", false);
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.PassphraseLock;
  }

  @Override
  protected LockingMethod getEmptyMethod() {
    return new PassphraseLock();
  }

  @Override
  protected AbstractLockAction getLockAction() {
    return new LockAction();
  }

  @Override
  protected AbstractUnlockAction getUnlockAction() {
    return new UnlockAction();
  }
}
