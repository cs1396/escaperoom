package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.screens.AbstractScreen;

public class G24TextButton extends TextButton{
	public G24TextButton(String text) {
		super(text, AbstractScreen.skin);
    getLabel().setFontScale(0.75f);
	}

  /**
   * Helper to disable programmatic {@code ChangeEvents}
   * and reset check status when checked.
   */
  public void autoResetCheck(){
    setProgrammaticChangeEvents(false);
    addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (isChecked()){
          setChecked(false);
        } 
      }
    });
  }

}
