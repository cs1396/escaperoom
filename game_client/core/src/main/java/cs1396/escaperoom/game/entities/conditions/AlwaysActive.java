package cs1396.escaperoom.game.entities.conditions;


import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;

/**
 * A trivially true {@link Conditional}
 */
public class AlwaysActive extends Conditional{

	@Override
	public void write(Json json) {
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
	}

	@Override
	public boolean evaluate(GameContext ctx) {
    return true;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.AlwaysActive;
	}

	@Override
	public String getName() {
    return "Always Active";
	}

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor){
    return Optional.empty();
  }

}
