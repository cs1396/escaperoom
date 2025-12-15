package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.LevelEditor;


/**
 * A base class for conditonals with two children
 */
public abstract class BinaryConditional extends Conditional {

  private Logger log = Logger.getLogger(BinaryConditional.class.getName());

  /**
   * The left child of the conditon
   */
  protected Conditional left = new EmptyConditional();

  /**
   * The right child of the conditon
   */
  protected Conditional right = new EmptyConditional();

  @Override
  public boolean requiresPoll(){
    return left.requiresPoll() || right.requiresPoll();
  }

  @Override
  public boolean poll(GameContext ctx){
    boolean leftNeeds = left.poll(ctx);
    boolean rightNeeds = right.poll(ctx);
    return leftNeeds || rightNeeds;
  }

	@Override
	public void write(Json json) {
    json.writeObjectStart("left");
    json.writeValue("type", left.getType());
    left.write(json);
    json.writeObjectEnd();

    json.writeObjectStart("right");
    json.writeValue("type", right.getType());
    right.write(json);
    json.writeObjectEnd();
	}

  private Conditional readCond( JsonValue data){
    String type = data.getString("type");
    ConditionalType t = ConditionalType.valueOf(type);
    Conditional c = t.getConditional();
    c.read(new Json(), data);
    return c;
  }

  @Override 
  public void setChildren(Conditional ... conds){
    if (conds.length > 2){
      log.warning("BinaryConditional setChildren got more than two values");
    }
    if (conds.length > 0){
      left = conds[0];
    }
    if (conds.length > 1){
      right = conds[1];
    }
  }


	@Override
	public void read(Json json, JsonValue jsonData) {
    left = readCond(jsonData.get("left"));
    right = readCond(jsonData.get("right"));
	}

  @Override
  public Array<Conditional> getChildren(){
    return Array.with(left, right);
  }


  @Override
  public Optional<Integer> childCount() {
    return Optional.of(2);
  }


	@Override
	public Optional<Actor> getEditorConfiguration(LevelEditor editor) {
    return Optional.empty();
	}

}
