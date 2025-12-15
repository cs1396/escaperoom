package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.AreaUI;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;

public class PlayerInTiles extends Conditional {

  Rectangle targetRegion = new Rectangle();

  @Override
  public void write(Json json) {
    json.writeValue("x", targetRegion.x);
    json.writeValue("y", targetRegion.y);
    json.writeValue("width", targetRegion.width);
    json.writeValue("height", targetRegion.height);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    int x = jsonData.getInt("x", 0);
    int y = jsonData.getInt("y", 0);
    int width = jsonData.getInt("width", 0);
    int height = jsonData.getInt("height", 0);
    this.targetRegion = new Rectangle(x, y, width, height);
  }


  @Override
  public boolean evaluate(GameContext ctx) {
    return ctx.player.getOccupiedRegion().overlaps(this.targetRegion);
  }

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor) {
    return Optional.of(new AreaUI(editor, this.targetRegion));
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.PlayerInRegion;
  }

  @Override
  public String getName() {
    return "Player in area";
  }

}
