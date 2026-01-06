package cs1396.escaperoom.ui;

import java.util.Optional;
import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.ui.Tooltip.Builder.LazyUIStage;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;

public class Tooltip extends Table {
  public static float DEFAULT_FADE_TIME = 0.3f;
  public static float DEFAULT_APPEAR_DELAY = 0.8f;
  public static Color color = new Color(1, 1, 1, 1);
  private Actor target;
  private Actor content;
  private LazyUIStage getUiStage;
  DisplayTask displayTask;
  TooltipInputListener listener;

  private Tooltip(Builder builder) {
    super();
    init(builder.target, builder.uiStage, builder.content);
  }

  private void init(Actor target, LazyUIStage uiStage, Actor content) {
    this.target = target;
    this.content = content;
    this.getUiStage = uiStage;
    this.listener = new TooltipInputListener();
    this.displayTask = new DisplayTask();

    add(content).padLeft(3).padRight(3).padBottom(2);
    pack();
    attach();

    // add listener for tooltip to show when we are over it
    addListener(new InputListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        toFront();
        return true;
      }

      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer == -1) {
          clearActions();
          addAction(Actions.sequence(Actions.fadeIn(DEFAULT_FADE_TIME, Interpolation.fade)));
        }
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (pointer == -1) {
          fadeOut();
        }
      }
    });
  }

  private void attach() {
    target.addListener(listener);
  }

  private void fadeOut() {
    clearActions();
    addAction(Actions.sequence(Actions.fadeOut(DEFAULT_FADE_TIME, Interpolation.fade), Actions.removeActor()));
  }

  private Table fadeIn() {
    clearActions();
    setColor(color);
    addAction(Actions.sequence(Actions.fadeIn(DEFAULT_FADE_TIME)));
    toFront();
    return this;
  }

  private class TooltipInputListener extends InputListener {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      displayTask.cancel();
      Tooltip.this.toFront();
      fadeOut();
      return true;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
      if (pointer == -1) {

        getUiStage.apply().ifPresent(uiStage -> {
          // get screen coords
          Vector2 targetPos = target.localToScreenCoordinates(new Vector2(target.getWidth() / 2, 0));

          // project onto UI stage
          targetPos = uiStage.getViewport().unproject(targetPos);

          setX(targetPos.x);
          float stageHeight = uiStage.getHeight();

          // is there enough space to display above widget?
          if (stageHeight - content.getHeight() > stageHeight)
            setY(targetPos.y + 6); // display above widget
          else
            setY(targetPos.y - 20); // display below

          displayTask.cancel();
          Timer.schedule(displayTask, DEFAULT_APPEAR_DELAY);
        });
      }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
      if (pointer == -1) {
        displayTask.cancel();
        fadeOut();
      }
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
      if (isVisible() && getActions().size == 0)
        fadeOut();
      return false;
    }
  }

  private class DisplayTask extends Task {
    @Override
    public void run() {
      getUiStage.apply().ifPresent((uiStage) -> {
        Table t = fadeIn();
        uiStage.addActor(t);
      });
    }
  }
  public static LazyUIStage stageHelper(Actor a) {
    return () -> {
      if (a.getStage() == null){
        return Optional.empty();
      }else {
        return Optional.of(a.getStage());
      }
    };
  }
  public static Function<Void, Optional<Stage>> stageHelperItem(Item a) {
    return (Void) -> {
      if (a.map.getUIStage() == null){
        return Optional.empty();
      }else {
        return Optional.of(a.map.getUIStage());
      }
    };
  }

  public static class Builder {
    private final Actor content;
    private Actor target = null;
    private LazyUIStage uiStage;

    public Builder(Actor content) {
      this.content = content;
    }

    public Builder(String text) {
      this(text, Align.center);
    }

    public Builder(String text, int align) {
      Label label = new G24Label(text, G24LabelStyle.Bubble);
      label.setAlignment(align);
      this.content = label;
    }


    public interface LazyUIStage {
      public Optional<Stage> apply();
    }

    public Builder target(Actor target, LazyUIStage getUIStage) {
      this.target = target;
      this.uiStage = getUIStage;
      return this;
    }

    public Builder target(Actor target) {
      this.target = target;
      this.uiStage = Tooltip.stageHelper(target);
      return this;
    }

    public Tooltip build() {
      return new Tooltip(this);
    }
  }
}
