package cs1396.escaperoom.ui.widgets;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.MenuScreen.CompletionHandler;

public class G24Dialog extends Dialog {
  protected AbstractBuilder<?,?> builder = new Builder("<no title>");

  /**
   * Convienience constructor
   */
  protected G24Dialog(String title){
    this(new Builder(title));
  }

  public <T> void waitFor(CompletableFuture<T> future, CompletionHandler<T> handler) {
    future.thenAccept((T t) -> {
      Gdx.app.postRunnable(() -> {
        handler.handle(t);
      });
    });
  }

  public interface CloseHandler {
    /**
     * Called on dialog close
     *
     * @return true if the dialog should close, false otherwise
     */
    public boolean onClose();
  }

  /**
   * A wrapper to express common content options
   *
   * @apiNote Add to this record as needed in order to express
   * more formatting options that can be passed to the cell
   */
  public record Content(Actor a, boolean row, int align) {
    public Content(Actor a){
      this(a, false, Align.center);
    }
  }


  /**
   * A basic builder for {@link G24Dialog}s
   */
  public static class Builder extends AbstractBuilder<G24Dialog, Builder> {
    public Builder(String title){
      super(title);
    }

    protected Builder self() { return this; }
    public G24Dialog build() { return new G24Dialog(this); }
  }


  /**
   * Two things motivate this:
   *
   * 1. Specialized dialogs want to define custom builders.
   *    - e.g. a {@link ConfirmDialog} has customizable confirm and cancel text
   *
   * 2. {@link AbstractBuilder#build()} should return the correct type
   *
   * Using generics like this allows subclasses of {@link G24Dialog} to extend
   * {@link AbstractBuilder} in a way that both of the above remain true
   */
  protected static abstract class AbstractBuilder<D extends G24Dialog, B extends AbstractBuilder<D,B>> {

    /**
     * Dialog title
     */
    protected String title = "<no title>";

    /**
     * Maps buttons to their {@link CloseHandler}s
     */
    protected HashMap<Button, CloseHandler> otherActions = new HashMap<>();

    /**
     * Array of {@link Content} to be added to the content table
     * of this dialog
     */
    protected Array<Content> content = new Array<>();

    /**
     * Optionally track a button that spawns this dialog
     * so that it may be disabled on show/hide events
     */
    protected Optional<Button> spawnedBy = Optional.empty();

    public AbstractBuilder(String title){
      this.title = title;
    }

    protected abstract B self();
    public abstract D build();

    /**
     * Add {@link Content} to this dialog content table
     *
     * Note that the order of these calls affects the layout
     * of the resulting table.
     *
     * @see Content
     */
    public B withContent(Content content){
      this.content.add(content); 
      return self();
    }

    /**
     * Add an actor to the dialog content table with default 
     * format options. 
     *
     * @see Content
     */
    public B withContent(Actor actor){
      this.content.add(new Content(actor)); 
      return self();
    }


    /**
     * Toggles whether {@code b} is enabled when showing and hiding this dialog
     *
     * Assuming {@code b} spawns this dialog, this prevents multiple dialogs
     * on repeated button presses
     *
     * @param b button to toggle enable status
     * @return the builder
     */
    public B disableSpawner(Button b){
      this.spawnedBy = Optional.of(b);
      return self();
    }

    /**
     * Add a {@link G24TextButton} with {@code buttonText} to the bottom 
     * row of the dialog. When pressed, {@code action} is executed
     */
    public B withButton(String buttonText, CloseHandler action){
      this.otherActions.put(
        new G24TextButton(buttonText), 
        action
      );

      return self();
    }
  }

  protected G24Dialog(AbstractBuilder<?,?> builder){
    super(builder.title, AbstractScreen.skin);

    G24Window.G24StyleWindow(this, builder.title);

    this.builder = builder;

    getContentTable().defaults().padLeft(5).padRight(5);

    builder.content.forEach(cs -> {
      getContentTable().add(cs.a).align(cs.align);
      if (cs.row) getContentTable().row();
    });

    builder.otherActions.forEach((b, a) -> button(b, a));
  }

  /**
   * Register the {@link Button} which will be 
   * enabled/disabled on show/hide
   */
  public void disableSpawner(Button b){
    builder.spawnedBy = Optional.of(b);
  }


  @Override
  public Dialog show(Stage stage) {
    BackManager.addBack(() -> {
      if (getStage() != null){
        hide();
        return true;
      } else {
        return false;
      }
    });

    builder.spawnedBy.ifPresent((b) -> b.setDisabled(true));
    return super.show(stage);
  }

  @Override
  protected void result(Object object) {

    if (object instanceof CloseHandler ca){
      if (!ca.onClose()){
        cancel();
        return;
      }
    }

    hide();
    cancel();
  }

  @Override
  public void hide() {
    builder.spawnedBy.ifPresent((b) -> b.setDisabled(false));
    super.hide();
  }
}
