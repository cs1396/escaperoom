package cs1396.escaperoom.editor.ui;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.ui.Tooltip;
import cs1396.escaperoom.ui.widgets.G24NumberInput.IntInput;
import cs1396.escaperoom.ui.widgets.G24TextInput.StringInput;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24NumberInput.FloatInput;

public class PropertyConfiguration extends Table implements HandlesMenuClose {

  public static class Select<T> extends VerticalGroup {
    public interface OnSelect{
      public void onChange(Object newVal);
    }
    public interface OnDeselect{
      public void onChange(Object newVal);
    }
    public interface Stringify{
      public String stringify(Object val);
    }

    public Select(
      OnSelect onSelect,
      OnDeselect onDeselect,
      T[] options,
      Stringify toDisplay,
      int maxSelected,
      T ... initialSelections
    ){
      columnLeft();
      ButtonGroup<CheckBox> checkBtnGroup = new ButtonGroup<>();
      checkBtnGroup.setMaxCheckCount(maxSelected);
      checkBtnGroup.setMinCheckCount(1);

      HashMap<T, CheckBox> buttonMap = new HashMap<>();

      for (T val : options){
        CheckBox check = new CheckBox(toDisplay.stringify(val), AbstractScreen.skin);

        buttonMap.put(val, check);
        checkBtnGroup.add(check);

        check.addListener(new ChangeListener(){
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (check.isChecked()){
              onSelect.onChange(val);
            } else {
              onDeselect.onChange(val);
            }
          }
        });

        addActor(check);
      }

      for (T val : initialSelections){
        CheckBox box = buttonMap.get(val);
        if (box != null){
          box.setChecked(true);
        }
      }
    }
  }


  public interface OnToggle{
    public void onToggle(boolean toggled);
  }

  public void addToggle(String label, String helpString, boolean initialValue, OnToggle onToggle){
    CheckBox checkBox = new CheckBox(label, AbstractScreen.skin);

    checkBox.addListener(new ChangeListener(){
      @Override
      public void changed(ChangeEvent event, Actor actor) {
          onToggle.onToggle(checkBox.isChecked());
      }
    });

    checkBox.setChecked(initialValue);

    addElement(label, helpString, checkBox, true);
  }

  public void addNumberInput(String label, String helpString, IntInput numberInput){
    addElement(label, helpString, numberInput, false);
  }

  public void addNumberInput(String label, String helpString, FloatInput numberInput){
    addElement(label, helpString, numberInput, false);
  }

  public void addStringInput(String label, String helpString, StringInput stringInput){
    addElement(label, helpString, stringInput, false);
  }

  public<T> void addSelect(String label, String helpString, Select<T> select){
    addElement(label, helpString, select, true);
  }

  public void addElement(String label, String helpString, Actor a, boolean separateByLine){
    G24Label labelEl = new G24Label(label + ":", "default");

    if (helpString != null && !helpString.isEmpty()){
      new Tooltip.Builder(helpString).target(labelEl, Tooltip.stageHelper(labelEl)).build();
    }

    add(labelEl).left();

    if (separateByLine) row();

    add(a).left().row();
  }

  public void addLine() {
    add(new G24Label("", "default")).row();
  }

	@Override
	public void handle() {
	}
}
