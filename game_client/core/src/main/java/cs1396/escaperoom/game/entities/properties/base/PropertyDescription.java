package cs1396.escaperoom.game.entities.properties.base;

import java.util.HashSet;
import java.util.Set;

import cs1396.escaperoom.game.entities.properties.PropertyType;

public class PropertyDescription {
  public final String name;
  public final String shortDesc;
  public final String longDesc;
  public final HashSet<PropertyType> mutallyExclusiveWith;

  public final static HashSet<PropertyType> TEXTURE_CONFLICTS = new HashSet<>(Set.of(
    PropertyType.Animated,
    PropertyType.Spinable,
    PropertyType.TiledBrushable,
    PropertyType.Toggleable
  ));

  public final static HashSet<PropertyType> CONNECTOR_CONFLICTS = new HashSet<>(Set.of(
    PropertyType.Connector,
    PropertyType.ConnectorBridge,
    PropertyType.ConnectorSource,
    PropertyType.ConnectorSink
  ));

  public PropertyDescription(String propertyName, String shortDescription, String longDescription, HashSet<PropertyType> conflictsWith){
    name = propertyName;
    shortDesc = shortDescription;
    longDesc = longDescription;
    if (conflictsWith == null) mutallyExclusiveWith = new HashSet<>();
    else mutallyExclusiveWith = conflictsWith;

    mutallyExclusiveWith.add(PropertyType.Player);
  }
}
