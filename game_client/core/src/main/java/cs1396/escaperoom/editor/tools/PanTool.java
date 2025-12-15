package cs1396.escaperoom.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.editor.core.ToolManager.ToolType;

public class PanTool extends EditorTool {
  private Vector2 lastScreenPoint;

	public PanTool(LevelEditor stage) {
		super(stage);
	}

  //----------------------------------------------------------------------------
  // Input handling
  //----------------------------------------------------------------------------
  
  public void handleTouchDown(){
    lastScreenPoint = new Vector2(Gdx.input.getX(), Gdx.input.getY());
  }

  public void handleDrag(){

    Vector2 newPoint = new Vector2(Gdx.input.getX(), Gdx.input.getY());

    Vector2 p1 = editor.getViewport().unproject(lastScreenPoint);
    Vector2 p2 = editor.getViewport().unproject(newPoint.cpy());

    editor.panCamera(p1.x - p2.x, p1.y - p2.y);
    lastScreenPoint = newPoint;
  }

  private InputListener panListener = new InputListener() {
    public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
      handleTouchDown();
      return true;
    }

    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      handleDrag();
    }
  };

  //---------------------------------------------------------------------------
  // Tool lifecycle
  //---------------------------------------------------------------------------

	@Override
	public void select() {
    CursorManager.setCursor(CursorType.Hand);
    editor.addListener(panListener);
	}

	@Override
	public void cancel() {
    CursorManager.restoreDefault();
    editor.removeListener(panListener);
	}

	@Override
	public String getName() {
    return "Pan Tool";
	}

  @Override
  public String getButtonStyle() {
    return "pan";
  }

  @Override
  public ToolType getType(){
    return ToolType.Pan;
  }
}
