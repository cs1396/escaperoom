package cs1396.escaperoom.game.entities.properties;

import java.util.HashMap;
import java.util.Optional;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.editor.ui.PropertyConfiguration.Select;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.PlayerAction;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.base.LockingMethod;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.entities.properties.locks.LockingMethodType;
import cs1396.escaperoom.game.entities.properties.locks.TrivialLock;
import cs1396.escaperoom.game.entities.properties.util.PropertyMap;
import cs1396.escaperoom.screens.GameScreen;

public class LockedProperty extends ItemProperty<LockingMethod> {

  private static final PropertyDescription description = new PropertyDescription(
    "Locked",
    "Prevents some interactions while locked",
    "Locked items may have various Locking methods, but all of them prevent some player interactions while locks.\nItems which contain other items cannot be opened while locked.\nItems which are barriers cannot be passed through while locked.",
    null
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  private LockingMethod currentMethod = new TrivialLock();
  private HashMap<LockingMethodType, LockingMethod> availableMethods = new HashMap<>();

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public LockedProperty() {}

  public boolean isLocked() {
    return currentMethod.isLocked();
  }

  @Override
  public Array<LockingMethod> getPotentialValues() {
    Array<LockingMethod> methods = new Array<>();
    availableMethods.values().forEach((m) -> methods.add(m));
    return methods;
  }

  @Override
  public String getDisplayName() {
    return "Lock Property";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.LockedProperty;
  }

  @Override
  public void set(LockingMethod value) {
    currentMethod.onDetatch();
    currentMethod = value;
    currentMethod.onAttach(owner);
  }

  @Override
  public LockingMethod getCurrentValue() {
    return currentMethod;
  }

  @Override
  public MenuType getInputType() {
    return MenuType.SelectOne;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return currentMethod.getActions();
  }

  @Override
  public LockedProperty cloneProperty(Item newOwner) {
    LockedProperty ret = new LockedProperty(); 
    ret.owner = newOwner;
    ret.currentMethod = this.currentMethod.clone(newOwner);
    ret.availableMethods = new HashMap<>();
    this.availableMethods.forEach((t, m) -> ret.availableMethods.put(t, m));
    return ret;
  }

  @Override
  public void write(Json json) {
    json.writeObjectStart("default");
    json.writeValue("type", currentMethod.getType().key);
    currentMethod.write(json);
    json.writeObjectEnd();
    json.writeArrayStart("available");
    availableMethods.forEach((t, m) -> {
      json.writeObjectStart();
      json.writeValue("type", t.key);
      m.write(json);
      json.writeObjectEnd();
    });
    json.writeArrayEnd();
  }

  public LockingMethod getEmptyLockMethod(LockingMethodType type){
    try {
      LockingMethod m_i = type.clz.getConstructor().newInstance();
      return m_i;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void read(Json json, JsonValue jsonData) {

    // get available
    JsonValue available = jsonData.get("available");

    if (available != null){
      available.forEach((data) -> {
        LockingMethodType type_i = LockingMethodType.fromString(data.getString("type"));
        LockingMethod m_i = getEmptyLockMethod(type_i);
        m_i.read(json, data);
        this.availableMethods.put(m_i.getType(), m_i);
      });
    }

    // get current
    JsonValue currentData = jsonData.get("default");

    // if there isn't a current, default to the first available
    if (currentData == null) {
      if (availableMethods.isEmpty()) {
        // if there are no methods, just assume trivial
        this.currentMethod = new TrivialLock();
      } else {
        this.currentMethod = availableMethods.values().iterator().next();
      }
    } else {

      LockingMethodType type = LockingMethodType.fromString(currentData.getString("type"));

      currentMethod = availableMethods.get(type);
      if (currentMethod == null) {
        currentMethod = getEmptyLockMethod(type);
      }
    }

    PropertyMap.onMapCompletion.add((Void a) ->{
      currentMethod.onAttach(owner);
      return null;
    });

  }

  @Override
  public Class<LockingMethod> getValueClass() {
    return LockingMethod.class;
  }

  @Override
  public void onGameLoad(GameScreen screen) {
    currentMethod.onGameLoad(screen);
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();

    config.addSelect(
      "Availble Lock methods",
      null,
      new Select<LockingMethodType>(
        (val) -> availableMethods.put((LockingMethodType)val, getEmptyLockMethod((LockingMethodType)val)),
        (val) -> availableMethods.remove((LockingMethodType)val),
        LockingMethodType.values(),
        (val) -> ((LockingMethodType)val).name(),
        LockingMethodType.values().length,
        availableMethods.keySet().toArray(new LockingMethodType[0])
      )
    );

    return Optional.of(config);
  }
}
