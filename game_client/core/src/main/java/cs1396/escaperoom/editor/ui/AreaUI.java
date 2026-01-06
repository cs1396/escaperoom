package cs1396.escaperoom.editor.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

import cs1396.escaperoom.editor.tools.AreaSelector;
import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;
import cs1396.escaperoom.ui.widgets.G24NumberInput;
import cs1396.escaperoom.ui.widgets.G24TextButton;

/**
 * A UI for area selection.
 */
public class AreaUI extends Table implements HandlesMenuClose {
  Integer x, y, width, height;
  G24NumberInput xi, yi, wi, hi;
  boolean selecting = false;
  AreaSelector selector;
  G24TextButton selectAreaButton;
  LevelEditor editor;

  private interface ModifiesArea {
    void modifyarea(int val);
  }

  private G24NumberInput numericField(String name, int val, int defaultValue, ModifiesArea mod){
    G24NumberInput inp = new G24NumberInput(Integer.toString(val));

    inp.setProgrammaticChangeEvents(false);

    inp.setOnEnter(() -> {
      if (inp.getText().isEmpty() || inp.getText().isBlank()){
        inp.setText(Integer.toString(defaultValue));
      } else {
        mod.modifyarea(Integer.parseInt(inp.getText()));
      }
    });
    
    inp.addListener(new FocusListener() {
      @Override
      public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
        if (focused) setAreaSelectorActive(false);
        // else mod.modifyarea(Integer.parseInt(inp.getText()));
      }
    });

    return inp;
  }

  private void setAreaSelectorActive(boolean isActive){
    if (isActive) {
      selector.select();
    } else {
      CursorManager.restoreDefault();
    }

    if (isActive != selectAreaButton.isChecked()) 
      selectAreaButton.setChecked(isActive);

    selecting = isActive;
    editor.setPanEnabled(!isActive);
  }

  /**
   * @param editor the {@link LevelEditor} this UI will be on
   * @param region the mutable region which this UI will adjust
   *
   */
  public AreaUI(LevelEditor editor, Rectangle region){
    selector = new AreaSelector(editor, region);
    this.editor = editor;

    xi = numericField("x", (int) region.x, 0, i -> {
      region.setX(i);
      selector.setRegion(region);
    });

    yi = numericField("y", (int) region.y, 0, i -> {
      region.setY(i);
      selector.setRegion(region);
    });

    wi = numericField("width", (int) region.width, 0, i -> {
      region.setWidth(i);
      selector.setRegion(region);
    });

    hi = numericField("height", (int) region.height, 0, i -> {
      region.setHeight(i);
      selector.setRegion(region);
    });

    add(new G24Label("x", G24LabelStyle.Bubble));
    add(xi).row();

    add(new G24Label("y", G24LabelStyle.Bubble));
    add(yi).row();

    add(new G24Label("width", G24LabelStyle.Bubble));
    add(wi).row();

    add(new G24Label("height", G24LabelStyle.Bubble));
    add(hi).row();

    selectAreaButton = new G24TextButton("SelectArea");
    selectAreaButton.addListener(new ChangeListener(){
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        setAreaSelectorActive(selectAreaButton.isChecked());
        // Escapeable
        BackManager.addBack(() -> {
          if (selectAreaButton.isChecked()) {
            selectAreaButton.setChecked(false);
            return true;
          }; 
          return false;
        });
      }
    });
    add(selectAreaButton).colspan(2);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (selecting) {
      xi.setText(Integer.toString((int)selector.getArea().getX()));
      yi.setText(Integer.toString((int)selector.getArea().getY()));
      wi.setText(Integer.toString((int)selector.getArea().getWidth()));
      hi.setText(Integer.toString((int)selector.getArea().getHeight()));
    }
  }

  @Override
  public void handle() {
    selector.cancel();
    editor.setPanEnabled(true);
  }
}
