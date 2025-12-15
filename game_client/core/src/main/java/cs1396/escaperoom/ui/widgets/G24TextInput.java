package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.ui.notifications.Notifier;

public class G24TextInput extends TextArea {

  private G24InputListener inputListener = new G24InputListener();
  private G24FocusListener focusListener = new G24FocusListener();

  public interface Filter {
    public boolean isValid(char c);
  }
  public interface OnEnter {
    public void perform();
  }

  private class G24InputListener extends TextAreaListener {
    @Override
    public boolean keyTyped(InputEvent event, char character) {
      // dont do anything on newlines if `allowMultiLine == false`
      if (!allowMultiLine && (character == '\r' || character == '\n')) {
        onEnter.perform();
        return true;
      }

      return super.keyTyped(event, character);
    }
  }

  /**
   * Automatically traverse to the next text area
   * when the maximum number characters are typed into 
   * the text area.
   */
  public void enableAutoFocusTraversal(){
      removeListener(inputListener);
      TextAreaListener traversingListener = new TextAreaListener(){
        @Override
        public boolean keyTyped(InputEvent event, char character) {
          boolean result = inputListener.keyTyped(event, character);
          if (getText().trim().length() + 1 >= getMaxLength()){
            next(false);
          } 
          return result;
        }
      };
      addListener(traversingListener);
  }

  private class G24FocusListener extends FocusListener {
    @Override
    public boolean handle(Event event) {
      if (!(event instanceof FocusEvent)) return false;

      FocusEvent e = (FocusEvent) event;
      if (e.isFocused()) {
        ControlsManager.setKeyboardEnabled(false);
        BackManager.addBack(() -> {
          if (G24TextInput.this.getStage() == null) return false;

          boolean isFocused = G24TextInput.this.getStage().getKeyboardFocus() == G24TextInput.this;
          if (!isFocused) return false;

          G24TextInput.this.getStage().setKeyboardFocus(G24TextInput.this.getParent());
          return true;
        });
      } else {
        ControlsManager.setKeyboardEnabled(true);
      }

      return true;
    }
  }


  boolean allowMultiLine;
  OnEnter onEnter;
  Filter filter;
  String filterInvalidMsg;


  public G24TextInput() {
    this("", AbstractScreen.skin);
  }
  public G24TextInput(String text) {
    this(text, AbstractScreen.skin);
  }
  public G24TextInput(String text, Skin skin) {
    super(text, skin);

    getStyle().font.getData().setScale(0.65f);
    setMultiline(false);
    setOnEnter(() -> {});
    setFilter(c -> true);

    setTextFieldFilter(new TextFieldFilter() {
      @Override
      public boolean acceptChar(TextField textField, char c) {
        if (!filter.isValid(c)) {
          if (filterInvalidMsg != null) Notifier.info(filterInvalidMsg);

          return false;
        }

        return true;
      }
    });

    // remove default listeners
    for (EventListener l : getListeners()) {
      removeListener(l);
    }

    // add custom input and focus listeners
    addListener(inputListener);
    addListener(focusListener);
  }


  public void setMultiline(boolean multiline) {
    allowMultiLine = multiline;
    if (!multiline) {
      setPrefRows(1);
    }
  }

  public void setOnEnter(OnEnter action) {
    onEnter = action;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
    this.filterInvalidMsg = null;
  }

  public void setFilter(Filter filter, String invalidMsg) {
    this.filter = filter;
    this.filterInvalidMsg = invalidMsg;
  }

  public void setAlphanumeric() {
    setFilter(c -> Character.isAlphabetic(c) || Character.isDigit(c));
  }

  public void setAlphanumericWithWhitespace() {
    setFilter(c -> Character.isAlphabetic(c) || Character.isDigit(c) || Character.isWhitespace(c));
  }
}
