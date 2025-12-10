package group24.escaperoom.game.entities.properties;

import java.io.ByteArrayOutputStream;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.editor.ui.ConfigurationMenu;
import group24.escaperoom.editor.ui.DrawingPane;
import group24.escaperoom.editor.ui.DrawingUI;
import group24.escaperoom.editor.ui.Menu.MenuEntry;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.ItemProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.entities.properties.values.ImageValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.ui.GameDialog;
import group24.escaperoom.ui.widgets.G24Dialog.Content;

public class ViewableProperty extends ItemProperty<ImageValue> {

  private static final PropertyDescription description = new PropertyDescription(
    "Viewable",
    "Contains a picture",
    "Viewable items have a configurable image diplayed to the player when they take the View action",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  ImageValue value;

  @Override
  public void write(Json json) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PixmapIO.PNG png = new PixmapIO.PNG();
    png.setFlipY(false);
    try {
      png.write(baos, value.inner);
      baos.close();
    } catch (Exception e) {
      System.out.println("failed to write pixmap");
    }
    char[] encoded = Base64Coder.encode(baos.toByteArray());
    json.writeValue("data", new String(encoded));
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    value = new ImageValue();
    String base64 = jsonData.getString("data", "");
    if (!base64.isEmpty()) {
      byte[] pngBytes = Base64Coder.decode(base64);
      value.inner = new Pixmap(pngBytes, 0, pngBytes.length);
    } else {
      value.inner = new Pixmap(200, 200, Pixmap.Format.RGBA8888);
      value.inner.setColor(DrawingPane.canvasColor);
      value.inner.fill();
    }
  }

  @Override
  public Class<ImageValue> getValueClass() {
    return ImageValue.class;
  }

  @Override
  public Array<ImageValue> getPotentialValues() {
    return new Array<>();
  }

  @Override
  public String getDisplayName() {
    return "Viewable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Viewable;
  }

  @Override
  public void set(ImageValue value) {
    this.value = value;
  }


  @Override
  public MenuType getInputType() {
    return MenuType.PopOut;
  }

  @Override
  public ConfigurationMenu<DrawingUI> getPopOut(MenuEntry parent) {
    return new ConfigurationMenu<DrawingUI>(parent, new DrawingUI(value.inner, parent.getScreen()), "Draw pane", parent.getScreen());
  }

  @Override
  public ImageValue getCurrentValue() {
    return value;
  }

  private class ViewAction implements PlayerAction {

    @Override
    public String getActionName() {
      return "View";
    }

    @Override
    public ActionResult act(GameContext ctx) {
      Texture texture = new Texture(value.inner);
      texture.draw(value.inner, 0, 0);
      return new ActionResult().showsDialog(
          new GameDialog.Builder(
            "Viewing " + owner.getItemName(),
          ctx.player
          ).withContent(new Image(texture)).build());
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return true;
    }
  }

  @Override
  protected Array<PlayerAction> getAvailableActions() {
    return Array.with(new ViewAction());
  }
}
