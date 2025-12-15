package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;

public class EmptyConditional extends Conditional {

	@Override
	public void write(Json json) {
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
	}

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor){
    return Optional.empty();
  }

	@Override
	public boolean evaluate(GameContext ctx) {
    return false;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.EmptyConditional;
	}

	@Override
	public String getName() {
    return "empty";
	}
}
