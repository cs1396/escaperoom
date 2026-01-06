package cs1396.escaperoom.engine.assets.items;

import java.io.Serializable;
import java.util.HashMap;

import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.engine.types.Size;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;

public class ItemTypeData implements Serializable, Json.Serializable {

  public String name, category;
  public Size size;
  public String texture;
  public HashMap<PropertyType, JsonValue> propertyParameters = new HashMap<>();
  public int renderPriority;

  public ItemTypeData(String name, String category, Size size, String texture, int renderPriority,
      HashMap<PropertyType, JsonValue> propertyParameters) {
    init(name, category, size, texture, renderPriority, propertyParameters);
  }

  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public ItemTypeData(){}

  private void init(String name, String category, Size size, String texture, int renderPriority,
      HashMap<PropertyType, JsonValue> propertyParameters){
    this.name = name;
    this.category = category;
    this.size = size;
    this.renderPriority = renderPriority;
    this.texture = texture;
    if (propertyParameters != null) this.propertyParameters.putAll(propertyParameters);
  }

  public ItemTypeData copy(){
    return new ItemTypeData(
      new String(name),
      new String(category),
      size.copy(),
      new String(texture),
      renderPriority,
      propertyParameters == null ? new HashMap<>() : new HashMap<>(propertyParameters)
    );
  }

  @Override
  public String toString() {
      return this.name;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ItemTypeData)) {
      return false;
    }

    ItemTypeData that = (ItemTypeData) other;

    if (this.name.equals(that.name)
        && this.size.equals(that.size)
        && this.texture.equals(that.texture)
        && this.renderPriority == that.renderPriority
        && this.propertyParameters.equals(that.propertyParameters)) {
      return true;
    }

    return false;
  }


  @Override
  public void write(Json json) {
    json.writeValue("name", name);
    json.writeObjectStart("size");
    json.writeValue("width", size.width);
    json.writeValue("height", size.height);
    json.writeObjectEnd();

    Item item = new Item(this);

    json.writeArrayStart("properties");
    for (PropertyType type : this.propertyParameters.keySet()){
      json.writeValue(type.asJsonString());
    }
    json.writeArrayEnd();

    json.writeObjectStart("property_values");
    item.getProperties().forEach((prop) -> {
      json.writeValue(prop.getType().asJsonString(), prop);
    });
    json.writeObjectEnd();

    json.writeValue("texture", texture);
    json.writeValue("render_priority", renderPriority);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    String name = jsonData.getString("name");
    JsonValue sz = jsonData.get("size");
    Size size = new Size(sz.getInt("width"), sz.getInt("height"));

    HashMap<PropertyType, JsonValue> validProperties = new HashMap<>();

    JsonValue properties = jsonData.get("properties");
    JsonValue propertyValues = jsonData.get("property_values");

    // assume we have no prop vals
    properties.forEach((prop) -> {
      validProperties.put(PropertyType.fromString(prop.toString()), new JsonReader().parse("{}"));
    });

    if (propertyValues != null) {
      properties.forEach((prop) -> {
        JsonValue params = propertyValues.get(prop.toString());
        if (params != null) {
          validProperties.put(PropertyType.fromString(prop.toString()), params);
        }
      });
    }

    String texture = jsonData.getString("texture");
    int renderPriority = jsonData.getInt("render_priority", 1);

    // NOTE: For backwards compatability in Map serialization/deserialization.
    // For a texture with filename assets/entity_textures/texture.png
    if (texture.endsWith(".png")) texture = texture.substring(0, texture.length() - 4);


    init(name, category, size, texture, renderPriority, validProperties);
  }
}
