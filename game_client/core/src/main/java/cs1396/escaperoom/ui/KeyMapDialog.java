package cs1396.escaperoom.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;

import cs1396.escaperoom.ui.widgets.G24Dialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class KeyMapDialog extends G24Dialog {
  private static boolean open;

  protected class CloseButton extends G24TextButton {
    protected CloseButton(){
      super("Close");
    }
  }

  @Override
  public Dialog show(Stage stage) {
    if (open) return null;
    open = true;
    return super.show(stage);
  }

  @Override
  public void hide() {
    open = false;
    super.hide();
  }

  public KeyMapDialog() {
    super("Key Maps");
    getContentTable().add(new KeyMapTable()).left().growX().expandX().pad(10).row();
    CloseButton close = new CloseButton();
    button(close, close);
  }
}
