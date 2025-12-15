package cs1396.escaperoom.editor.tools;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.screens.LevelEditor;

/**
 * A tool to select areas
 */
public class AreaSelector extends SelectionTool {

  /**
   * @param editor the {@link LevelEditor} that this selector will operate on 
   * @param region the mutable region that this selector will mutate
   */
	public AreaSelector(LevelEditor editor, Rectangle region) {
		super(editor);
    this.selection = new SelectionTool.Selection(region, new Array<>(), editor);
    editor.addOverlayDrawable(this);
	}

  /**
   * @return the actively selected region
   */
  public Rectangle getArea(){
    // if (selection == null) return new Rectangle(0, 0, 0, 0);
    return selection.area;
  }

  /**
   * Set the region which is being operated on
   *
   * @param region new value
   */
  public void setRegion(Rectangle region) {
    selection.setNewArea(region);
  }

	@Override
	public String getName() {
    return "Area Selector";
	}

  public void endSelection() { /* void the call */ }

  @Override
  public void cancel() {
    super.cancel();
    editor.removeOverlayDrawable(this);
  }
}
