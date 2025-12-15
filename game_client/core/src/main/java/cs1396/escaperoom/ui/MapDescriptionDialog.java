package cs1396.escaperoom.ui;

import java.util.function.Function;

import com.badlogic.gdx.utils.Null;

import cs1396.escaperoom.ui.widgets.G24Dialog;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;

public class MapDescriptionDialog extends G24Dialog {
  G24TextInput userInput;
  Function<String, Void> onSubmit;


  public MapDescriptionDialog(Function<String, Void> ifAdded) {
    this("Description for your new map:", ifAdded);
  }
  public MapDescriptionDialog(String title, Function<String, Void> onSubmit) {
    super(title);
    this.onSubmit = onSubmit;
    userInput = new G24TextInput();
    userInput.setMultiline(true);
    userInput.setPrefRows(3);
    userInput.setMaxLength(200);
    getContentTable().add(userInput).pad(20);

    G24TextButton btn = new G24TextButton("Submit");
    button(btn, true);
  }

  @Override
  protected void result(@Null Object object){
    if (object != null){
      onSubmit.apply(userInput.getText());
    }

    super.result(object);
  }
}
