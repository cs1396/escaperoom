package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;

abstract public class UnaryConditional extends Conditional {
  Conditional child = new EmptyConditional();

  @Override
  public boolean requiresPoll() {
    return child.requiresPoll();
  }

  @Override 
  public boolean poll(GameContext ctx){
    return child.poll(ctx);
  }

	@Override
	public void write(Json json) {
    json.writeObjectStart("child");
    json.writeValue("type",child.getType());
    child.write(json);
    json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
    JsonValue data = jsonData.get("child");
    String type = data.getString("type");
    ConditionalType t = ConditionalType.valueOf(type);
    Conditional c = t.getConditional();
    c.read(new Json(), data);
    this.child = c;
	}

  @Override 
  public void setChildren(Conditional ... conds){
    if (conds.length > 0){
      child = conds[0];
    }
  }

  @Override
  public Array<Conditional> getChildren(){
    return Array.with(child);
  }

  @Override
  public Optional<Integer> childCount() {
    return Optional.of(1);
  }

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor){
    return Optional.empty();
  }
}
