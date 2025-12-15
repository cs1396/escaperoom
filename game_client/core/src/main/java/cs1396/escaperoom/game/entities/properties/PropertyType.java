package cs1396.escaperoom.game.entities.properties;
import java.util.logging.Logger;

import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.values.ItemPropertyValue;
import cs1396.escaperoom.game.entities.properties.connectors.*;


/**
 * As a convention, properties that hold some value or state
 * should be "NameProperty" but a property that is a marker property (extends
 * {@link PhantomProperty})
 * should be just a "Name"
 *
 * e.g. Interactable is a marker, ContainsItemProperty holds values
 */
public enum PropertyType {
  Animated("animated", AnimatedProperty.class),
  Barrier("barrier", Barrier.class),
  Brushable("brush", Brushable.class),
  Collideable("collideable", Collideable.class),
  Connector("connector", Connector.class),
  ConnectorSource("connector_source", ConnectorSource.class),
  ConnectorRelay("connector_relay", ConnectorRelay.class),
  ConnectorSink("connector_sink", ConnectorSink.class),
  ConnectorBridge("connector_bridge", ConnectorBridge.class),
  CompletesLevel("completes_level", CompletesLevel.class),
  ConditionallyActive("conditionally_active", ConditionallyActive.class),
  ConditionallyVisible("conditionally_visible", ConditionallyVisible.class),
  Containable("containable", ContainableProperty.class),
  ContainsItemsProperty("containsitems", ContainsItemProperty.class),
  CoveringProperty("covering", CoveringProperty.class),
  Fragile("fragile", FragileProperty.class),
  Interactable("interactable", InteractableProperty.class),
  InvalidProperty("invalid", null),
  LockedProperty("locked", LockedProperty.class),
  Obtainable("obtainable", ObtainableProperty.class),
  Player("player", PlayerProperty.class),
  ReadableProperty("readable", ReadableProperty.class),
  Spinable("spinable", SpinnableProperty.class),
  Stylable("stylable", Stylable.class),
  TiledBrushable("tiled_brushable", TiledBrushable.class),
  Toggleable("toggleable", Toggleable.class),
  Unique("unique", Unique.class),
  UnlocksProperty("unlocker", UnlockerProperty.class),
  Viewable("viewable", ViewableProperty.class);

  private String jsonName;
  private Class<? extends ItemProperty<? extends ItemPropertyValue>> propClass;

  public Class<? extends ItemProperty<? extends ItemPropertyValue>> getPropertyClass() {
    return this.propClass;
  }

  private PropertyType(String s, Class<? extends ItemProperty<? extends ItemPropertyValue>> c){
    this.jsonName = s;
    this.propClass = c;
  }

  private static Logger log = Logger.getLogger(PropertyType.class.getName());

  public String asJsonString(){
    return this.jsonName;
  }

  public ItemProperty<? extends ItemPropertyValue> getEmptyProperty(){
    if (this == InvalidProperty) return null;
    try {
      return this.propClass.getConstructor().newInstance();
    } catch (Exception e){
      e.printStackTrace();
      throw new UnsupportedOperationException("Cannot build property with unhandled type");
    }
  }
  public static PropertyType fromString(String s){
    for (PropertyType t : PropertyType.values()){
      if (t.jsonName.equals(s)){
        return t;
      }
    }
    log.warning("Parsed Invalid Property: " + s);
    return InvalidProperty;
  }
}
