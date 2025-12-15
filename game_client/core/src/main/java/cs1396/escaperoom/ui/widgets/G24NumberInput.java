package cs1396.escaperoom.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.ui.notifications.Notifier;

public class G24NumberInput extends G24TextInput {
  public G24NumberInput() {
    this("");
  }

  public G24NumberInput(String text) {
    super(text);

    this.setFilter(c -> {
      String newText = this.getText() + c;

      try {
        Integer.parseInt(newText);
      } catch (NumberFormatException e) {
        Notifier.warn("Textbox must contain a digit!", G24NumberInput.this);
        return false;
      }

      return true;
    });
  }

  public static class IntInput extends G24NumberInput {
    public interface OnIntChange{
      public void onChange(int newVal);
    }

    public IntInput(int initialValue, OnIntChange onChange){
      super(Integer.toString(initialValue));

      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          String newValueStr = getText();
          if (newValueStr.isBlank() || newValueStr.isEmpty()) return; 

          onChange.onChange(Integer.parseInt(newValueStr));
        }
      });
    }
  }

  public static class FloatInput extends G24TextInput {
    public interface OnFloatChange{
      public void onChange(float newVal);
    }

    public FloatInput(float initialValue, OnFloatChange onChange){
      super(Float.toString(initialValue));

      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          String newValueStr = getText();
          if (newValueStr.isBlank() || newValueStr.isEmpty()) return; 

          onChange.onChange(Float.parseFloat(newValueStr));
        }
      });

      setFilter(c -> {
        String newText = this.getText() + c;

        try {
          Float.parseFloat(newText);
        } catch (NumberFormatException e) {
          return false;
        }

        return true;
      });
    }
  }
}
