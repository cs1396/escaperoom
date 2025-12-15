package cs1396.escaperoom.game.entities.properties.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;


public class PropertyMap extends HashMap<PropertyType, ItemProperty<? extends ItemPropertyValue>> implements Json.Serializable{
  Item owner;
  // Some properties may be dependent on other properties (i.e. they may need to know what other properies a given item has).
  // They can register functions here that get called when the map is built
  public static HashSet<Function<Void, Void>> onMapCompletion = new HashSet<>();

  public void readWrapper(Item owner, Json json, JsonValue jsonData){
    this.owner = owner;
    this.read(json, jsonData);
    applyCallbacks();
  }

	@Override
	public void write(Json json) {
    this.forEach((PropertyType type, ItemProperty<? extends ItemPropertyValue> property) -> {
      json.writeObjectStart();
      json.writeValue("property_type", type.asJsonString());
      property.write(json);
      json.writeObjectEnd();
    });
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
    jsonData.forEach((JsonValue propData) -> {
      PropertyType propType = PropertyType.fromString(propData.getString("property_type"));
      if (propType == PropertyType.InvalidProperty) return;
      ItemProperty<? extends ItemPropertyValue> prop = propType.getEmptyProperty();
      prop.setOwner(owner);
      prop.read(json, propData);
      this.put(propType, prop);
    });
	}

  static public void applyCallbacks() {
    onMapCompletion.forEach((f) -> f.apply(null));
    onMapCompletion.clear();
  }

}
