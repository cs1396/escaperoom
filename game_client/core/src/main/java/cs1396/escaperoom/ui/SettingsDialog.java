package cs1396.escaperoom.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.ui.widgets.G24Dialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class SettingsDialog extends G24Dialog {
  protected class CloseButton extends G24TextButton {
    protected CloseButton(){
      super("Close");
    }
  }

  public SettingsDialog() {
    super("Settings");
    G24TextButton keyMapBtn = new G24TextButton("Key Maps");
    keyMapBtn.addListener(new ChangeListener(){
      @Override
      public void changed(ChangeEvent event, Actor actor) {
          new KeyMapDialog().show(getStage());
      }
    });
    CloseButton close = new CloseButton();
    button(close, close);
    button(keyMapBtn);
  }
}
