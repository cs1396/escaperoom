package group24.escaperoom.game.entities.properties;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.editor.ui.ConfigurationMenu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.ItemProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.entities.properties.values.ReadableContents;
import group24.escaperoom.game.entities.properties.values.StringItemPropertyValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.ui.GameDialog;
import group24.escaperoom.screens.LevelEditor;
import group24.escaperoom.ui.SimpleUI;
import group24.escaperoom.ui.widgets.G24Label;
import group24.escaperoom.ui.widgets.G24TextInput;

public class ReadableProperty extends ItemProperty<ReadableContents> {

  private static final PropertyDescription description = new PropertyDescription(
    "Readable",
    "Can be read",
    "Readable items provide the player with the Read action. The value of this property can be customized",
    null
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  ReadableContents contents = new ReadableContents();
  String title;
  String actionName = "Inspect";

  PlayerAction read = new PlayerAction() {
    public boolean isValid(GameContext ctx) {
      return true;
    }

    public ActionResult act(GameContext ctx) {
      return new ActionResult().showsDialog(
          new GameDialog.Builder(title, ctx.player)
            .withContent(new G24Label(contents.getValue()))
            .build()
        );
    }

    public String getActionName() {
      return actionName;
    }
  };

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public ReadableProperty() {
    this("Hidden Info");
  }
  public ReadableProperty(String title) {
    this.title = title;
  }

  @Override
  public Array<ReadableContents> getPotentialValues() {
    return Array.with(contents);
  }

  @Override
  public String getDisplayName() {
    return "Readable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ReadableProperty;
  }

  @Override
  public void set(ReadableContents value) {
    this.contents =  value;
  }

  @Override
  public MenuType getInputType() {
    return MenuType.PopOut;
  }

  @Override
  public ConfigurationMenu<SimpleUI> getPopOut(MenuEntry parent) {
    LevelEditor screen = (LevelEditor) parent.getScreen();

    StringItemPropertyValue contents = StringItemPropertyValue.class.cast(getCurrentValue());
    G24TextInput contentField = new G24TextInput(contents.getValue());
    contentField.setMaxLength(200);
    contentField.setMultiline(true);
    contentField.setPrefRows(2);

    contentField.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        contents.setValue(contentField.getText());
      }
    });
    contentField.pack();

    G24TextInput actionField = new G24TextInput(actionName);
    actionField.setMaxLength(200);
    actionField.setMultiline(false);
    actionField.setPrefRows(1);

    actionField.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        actionName = actionField.getText();
      }
    });
    actionField.pack();

    VerticalGroup col = new VerticalGroup();
    col.space(10);
    col.align(Align.center);

    col.addActor(new G24Label("Action Name:", "underline", 0.65f));
    col.addActor(actionField);

    col.addActor(new G24Label("Contents:", "underline", 0.65f));
    col.addActor(contentField);

    return new ConfigurationMenu<>(parent, new SimpleUI(col), getDisplayName(), screen);
  }

  @Override
  public ReadableContents getCurrentValue() {
    return contents;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(read);
  }

  @Override
  public void write(Json json) {
    json.writeValue("title", title);
    json.writeValue("contents", contents.getValue());
    json.writeValue("action_name", actionName);
  }
  @Override
  public void read(Json json, JsonValue jsonData) {
    title = jsonData.getString("title", "Hidden Info");
    contents =  new ReadableContents(jsonData.getString("contents", "<nothing here>"));
    actionName = jsonData.getString("action_name", "Inspect");
  }
  @Override
  public Class<ReadableContents> getValueClass() {
    return ReadableContents.class;
  }
}
