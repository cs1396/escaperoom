package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;

public abstract class KaryConditional extends Conditional {
  protected Array<Conditional> children = new Array<>();

  @Override
  public boolean requiresPoll(){
    boolean needsPoll = false;
    for (Conditional c : children){
      if (c.requiresPoll()){
        needsPoll = true;
      }
    } 
    return needsPoll;
  }

  @Override
  public boolean poll(GameContext ctx){
    boolean shouldPoll = false;
    for (Conditional c : children){
      if (c.poll(ctx)){
        shouldPoll = true;
      }
    } 
    return shouldPoll;
  }

  @Override
  public void write(Json json) {
    json.writeArrayStart("children");
    for (Conditional c : children) {
      json.writeObjectStart();
      json.writeValue("type", c.getType());
      c.write(json);
      json.writeObjectEnd();
    }
    json.writeArrayEnd();
  }

	@Override
	public void read(Json json, JsonValue jsonData) {
    JsonValue arr = jsonData.get("children");
    for (JsonValue v : arr){
      children.add(readCond(v));
    }
	}

  private Conditional readCond(JsonValue data) {
    String type = data.getString("type");
    ConditionalType t = ConditionalType.valueOf(type);
    Conditional c = t.getConditional();
    c.read(new Json(), data);
    return c;
  }

  @Override
  public Array<Conditional> getChildren(){
    return children;
  }

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor){
    return Optional.empty();
  }

  @Override
  public Optional<Integer> childCount() {
    return Optional.empty();
  }

  @Override
  public void setChildren(Conditional... conds) {
    children.clear();
    children.addAll(conds);
  }
}
