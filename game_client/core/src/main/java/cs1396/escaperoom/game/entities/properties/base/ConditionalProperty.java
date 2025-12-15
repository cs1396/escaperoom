package cs1396.escaperoom.game.entities.properties.base;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.ConditionalUI;
import cs1396.escaperoom.editor.ui.ConfigurationMenu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.game.entities.conditions.AlwaysActive;
import cs1396.escaperoom.game.entities.conditions.Conditional;
import cs1396.escaperoom.game.entities.conditions.ConditionalType;
import cs1396.escaperoom.game.entities.conditions.EmptyConditional;
import cs1396.escaperoom.game.entities.player.PlayerAction;
import cs1396.escaperoom.game.state.GameContext;

public abstract class ConditionalProperty extends ItemProperty<Conditional> {
  protected Conditional condition;

  public boolean requiresPoll(){
    return condition.requiresPoll();
  }

  /**
   * Return whether or not this property still needs to be polled 
   */
  public boolean poll(GameContext ctx){
    return condition.poll(ctx);
  }

  @Override
  public void write(Json json) {
    if (condition == null) condition = new AlwaysActive();
    json.writeObjectStart("condition");
    json.writeValue("type", condition.getType());
    condition.write(json);
    json.writeObjectEnd();
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue condition = jsonData.get("condition");

    if (condition != null) {
      String type = condition.getString("type");
      ConditionalType t = ConditionalType.valueOf(type);
      Conditional c = t.getConditional();
      c.read(json, condition);
      this.condition = c;
    } else {
      this.condition = new AlwaysActive();
    }
  }

  @Override
  public Array<Conditional> getPotentialValues() {
    Array<Conditional> conditions = new Array<>();
    for (ConditionalType c : ConditionalType.values()) {
      conditions.add(c.getConditional());
    }
    return conditions;
  }

  public boolean isValid(GameContext ctx) {
    return this.condition.evaluate(ctx);
  }

  public void update(GameContext ctx){
  }

  @Override
  public void set(Conditional value) {
    if (value == null) this.condition = new EmptyConditional();

    this.condition = value;
  }

  @Override
  public Conditional getCurrentValue() {
    return condition;
  }

  @Override
  protected Array<PlayerAction> getAvailableActions() {
    return new Array<>();
  }

  @Override
  public MenuType getInputType() {
    return MenuType.PopOut;
  }

  @Override
  public ConfigurationMenu<ConditionalUI> getPopOut(MenuEntry parent) {
    ConfigurationMenu<ConditionalUI> m  =  new ConfigurationMenu<>(parent, getDisplayName(), parent.getScreen());
    m.setContent(new ConditionalUI(this, m));
    return m;
  }

  @Override
  public Class<Conditional> getValueClass() {
    return Conditional.class;
  }
}
