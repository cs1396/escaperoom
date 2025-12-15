package cs1396.escaperoom.ui;
import java.util.Arrays;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputGroupMap;
import cs1396.escaperoom.engine.control.bindings.MapGroup;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.control.input.Input.MappingDescription;
import cs1396.escaperoom.ui.widgets.G24Label;

public class KeyMapTable extends Container<ScrollPane> {
  Table inner = new Table();
  public KeyMapTable(){
    InputGroupMap keymaps = ControlsManager.registedMappings();
    MapGroup[] groups = keymaps.keySet().toArray(new MapGroup[0]);
    Arrays.sort(groups);
    inner.defaults().left().expandX();

    for (MapGroup group : groups){
      if (group == MapGroup.DEBUG) continue;
      inner.add(new G24Label(group.toString(), "title", 1f)).center().colspan(2).row();
      for (Input input : keymaps.get(group)){
        MappingDescription desc = input.description();
        inner.add(new G24Label(desc.desc, "bubble"));
        HorizontalGroup hg = new HorizontalGroup();
        hg.space(5);
        Arrays.stream(desc.bindings).forEach((bind) -> {
          hg.addActor(new G24Label(bind, "bubble_gray"));
        });
        inner.add(hg).row();
      }
    }

    ScrollPane scrollPane = new ScrollPane(inner);
    scrollPane.addListener(new InputListener(){
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (getStage() == null) return;
        getStage().setScrollFocus(scrollPane);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (getStage() == null) return;
        getStage().setScrollFocus(null);
      }
    });
    setActor(scrollPane);
    maxHeight(400);
  }
}
