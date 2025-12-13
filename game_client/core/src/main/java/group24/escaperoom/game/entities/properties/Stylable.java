package group24.escaperoom.game.entities.properties;

import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.base.ItemProperty;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.entities.properties.values.Style;

public class Stylable extends ItemProperty<Style> {
  private Array<Style> availableStyles = new Array<>();
  private Style currentStyle;

  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(Stylable.class.getName());

  private static final PropertyDescription description = new PropertyDescription(
    "Stylable",
    "Has multiple available textures",
    "Stylable items can have multiple textures. Primarily a convienence to avoid creating multiple items that differ only in their texture.",
    null
  );

  public PropertyDescription getDescription() {
    return description;
  };

  @Override
  public void write(Json json) {
    if (currentStyle == null) buildDefaultStyle();

    json.writeArrayStart("available");
    availableStyles.forEach((s) -> {
      json.writeObjectStart();
      s.write(json);
      json.writeObjectEnd();
    });
    json.writeArrayEnd();
    json.writeValue("current", currentStyle);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue available = jsonData.get("available");
    available.forEach((s) -> {
      Style style = new Style();
      style.read(json, s);
      availableStyles.add(style);
    });
    this.currentStyle = new Style();
    JsonValue current = jsonData.get("current");
    if (current != null) {
      currentStyle.read(json, current);
    } else {
      currentStyle.name = "Default";
      currentStyle.texture = owner.getType().texture;
    }
    currentStyle.apply(owner);
  }

  @Override
  public String getDisplayName() {
    return "Stylable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Stylable;
  }

  @Override
  public void set(Style value) {
    currentStyle = value;
    currentStyle.apply(owner);
  }


  @Override
  public MenuType getInputType() {
    return MenuType.SelectOne;
  }

  private void buildDefaultStyle(){
    currentStyle = new Style("Default", owner.getType().texture);
    availableStyles.add(currentStyle);
  }

  @Override
  public Style getCurrentValue() {
    if (currentStyle == null) buildDefaultStyle();
    return currentStyle;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  @Override
  public Array<Style> getPotentialValues() {
    return availableStyles;
  }

  @Override
  public Class<Style> getValueClass() {
    return Style.class;
  }
}
