package cs1396.escaperoom.editor.ui;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.control.CursorManager;
import cs1396.escaperoom.engine.control.CursorManager.CursorType;
import cs1396.escaperoom.game.entities.conditions.Conditional;
import cs1396.escaperoom.game.entities.conditions.ConditionalType;
import cs1396.escaperoom.game.entities.conditions.EmptyConditional;
import cs1396.escaperoom.game.entities.properties.base.ConditionalProperty;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24Label;

public class ConditionalUI extends Table implements HandlesMenuClose {
  Logger log = Logger.getLogger(ConditionalUI.class.getName());
  private LevelEditor editor;
  static int colorInd = 0;
  static final Color[] bkgColors = { new Color(1, 0, 0, 1), new Color(0, 1, 0, 1), new Color(0, 0, 1, 1),
      new Color(1, 1, 0, 1), new Color(0, 1, 1, 1) };

  public class EmptyBlock extends ConditionalBlock {

    EmptyBlock() {
      super();
      dragAndDrop.addTarget(new DragAndDrop.Target(EmptyBlock.this) {
        @Override
        public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
          return true;
        }

        @Override
        public void drop(Source source, Payload payload, float x, float y, int pointer) {
          HorizontalGroup parentChildren = (HorizontalGroup) EmptyBlock.this.getParent();
          ConditionalBlock parentCb = (ConditionalBlock) parentChildren.getParent().getParent();

          SnapshotArray<Actor> blocks = parentChildren.getChildren();
          int thisInd = blocks.indexOf(EmptyBlock.this, false);
          parentChildren.addActorAt(thisInd, new ConditionalBlock((Conditional) payload.getObject()));
          if (!parentCb.val.childCount().isEmpty()) {
            EmptyBlock.this.remove();
          }
          setProperty();
        }
      });
    }
  }

  private class ConditionalBlock extends Stack {

    // holds all children conditional blocks (they can be empty)
    HorizontalGroup children = new HorizontalGroup();
    VerticalGroup innerVGroup = new VerticalGroup();
    G24Label label;
    Conditional val;

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ConditionalBlock) {
        return ConditionalBlock.class.cast(obj).val.equals(this.val);
      }
      return false;
    }

    private ConditionalBlock() {
      this(new EmptyConditional());
    }

    private ConditionalBlock(Conditional cond) {
      AssetManager.instance().load("textures/bkg.9.png", Texture.class);
      AssetManager.instance().finishLoadingAsset("textures/bkg.9.png");
      Texture bkg = AssetManager.instance().get("textures/bkg.9.png", Texture.class);
      NinePatch ninePatch = new NinePatch(bkg, 10, 10, 10, 10);
      NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
      Image bkgImg = new Image(drawable);
      bkgImg.setColor(bkgColors[colorInd]);
      colorInd = (colorInd + 1) % bkgColors.length;

      this.add(bkgImg);

      val = cond;

      // Style our vertical group
      innerVGroup.space(10);
      innerVGroup.align(Align.center);

      label = cond.getType() == ConditionalType.EmptyConditional ? new G24Label("<empty>", "bubble")
          : new G24Label(cond.getName(), "bubble");
      innerVGroup.addActor(label);

      // add a remove source
      if (cond.getType() != ConditionalType.EmptyConditional) {
        dragAndDrop.addSource(new ConditionalBlockSource(this));
      }

      // Add a listener to the label to get the config display in a popup
      label.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          cond.getEditorConfiguration(editor).ifPresent((conf) -> {
            parent.getParent().createNewMenu(new ConfigurationMenu<>(parent.getParent(), conf, cond.getName(), editor));
          });
        }
      });

      // populate the graph if we have a property
      cond.childCount().ifPresentOrElse((count) -> {
        if (count > 0) {
          children.space(10);
          children.align(Align.center);
          Array<Conditional> childrenConds = val.getChildren();
          for (int i = 0; i < count; i++) {
            if (i >= childrenConds.size || childrenConds.get(i).getType() == ConditionalType.EmptyConditional) {
              children.addActor(new EmptyBlock());
            } else {
              children.addActor(new ConditionalBlock(childrenConds.get(i)));
            }
          }
          innerVGroup.addActor(children);
        }
      }, () -> {
        children.space(10);
        children.align(Align.center);
        Array<Conditional> childrenConds = val.getChildren();
        for (int i = 0; i < childrenConds.size + 1; i++) {
          if (i >= childrenConds.size || childrenConds.get(i).getType() == ConditionalType.EmptyConditional) {
            children.addActor(new EmptyBlock());
          } else {
            children.addActor(new ConditionalBlock(childrenConds.get(i)));
          }
        }
        innerVGroup.addActor(children);
      });
      this.add(innerVGroup);
    }
  }

  private class ConditionalBlockSource extends DragAndDrop.Source {
    ConditionalBlock source;

    ConditionalBlockSource(ConditionalBlock source) {
      super(source.label);
      this.source = source;
      source.label.addListener(CursorManager.hoverHelper(CursorType.Hand));
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
      Payload p = new Payload();
      source.setVisible(false);
      G24Label valid = new G24Label(source.val.getName(), "bubble");
      valid.setColor(0.75f, 1, 0.75f, 0.5f);

      G24Label invalid = new G24Label(source.val.getName(), "bubble");
      invalid.setColor(1, 0.75f, 0.75f, 0.5f);

      p.setValidDragActor(valid);
      p.setInvalidDragActor(invalid);
      p.setDragActor(valid);

      return p;
    }

    @Override
    public void drag(InputEvent event, float x, float y, int pointer) {
    }

    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, DragAndDrop.Target target) {
      if (target == null) {
        source.setVisible(true);
        return;
      }
      // the source was the child of another ConditionalBlock
      if (source.getParent() instanceof HorizontalGroup) {
        HorizontalGroup parentChildren = (HorizontalGroup) source.getParent();
        SnapshotArray<Actor> blocks = parentChildren.getChildren();
        int thisInd = blocks.indexOf(source, false);
        ConditionalBlock cb = (ConditionalBlock) parentChildren.getParent().getParent();
        if (cb.val.childCount().isPresent()) {
          parentChildren.addActorAt(thisInd, new EmptyBlock());
        }
        source.remove();
        setProperty();
      } else {
        workspace.clearRoot();
      }
      dragAndDrop.removeSource(this);
      CursorManager.restoreDefault();
    }
  }

  private class ConditionalSource extends DragAndDrop.Source {
    Conditional c;

    public ConditionalSource(Conditional c) {
      super(new G24Label(c.getName(), "bubble"));
      getActor().addListener(CursorManager.hoverHelper(CursorType.Hand));
      this.c = c;
    }

    @Override
    public Payload dragStart(InputEvent event, float x, float y, int pointer) {
      Payload p = new Payload();

      G24Label valid = new G24Label(c.getName(), "bubble");
      valid.setColor(0.75f, 1, 0.75f, 0.5f);

      G24Label invalid = new G24Label(c.getName(), "bubble");
      invalid.setColor(1, 0.75f, 0.75f, 0.5f);

      p.setValidDragActor(valid);
      p.setInvalidDragActor(invalid);
      p.setDragActor(valid);

      p.setObject(c.clone());
      return p;
    }
    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
      CursorManager.restoreDefault();
    }
  }

  private class WorkSpace extends Container<ConditionalBlock> {
    private Optional<ConditionalBlock> rootBlock = Optional.empty();
    DragAndDrop.Target rootTarg;

    public WorkSpace(Conditional root) {
      super();
      if (root == null) {
        setActor(new EmptyBlock());

      } else {
        rootBlock = Optional.of(new ConditionalBlock(root));
        setActor(rootBlock.get());
      }
      minSize(100, 150);
      AssetManager.instance().load("textures/bkg.9.png", Texture.class);
      AssetManager.instance().finishLoadingAsset("textures/bkg.9.png");
      Texture bkg = AssetManager.instance().get("textures/bkg.9.png", Texture.class);
      NinePatch ninePatch = new NinePatch(bkg, 10, 10, 10, 10);
      NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
      setBackground(drawable);

      rootTarg = new DragAndDrop.Target(this) {
        @Override
        public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
          if (rootBlock.isPresent()) {
            return false;
          }
          return true;
        }

        @Override
        public void drop(Source source, Payload payload, float x, float y, int pointer) {
          setRootBlock(new ConditionalBlock((Conditional) payload.getObject()));
          dragAndDrop.removeTarget(rootTarg);
        }
      };

    }

    public void setRootBlock(ConditionalBlock block) {
      this.rootBlock = Optional.of(block);
      setActor(block);
      setProperty();
    }

    public void clearRoot() {
      this.rootBlock.ifPresent((r) -> {
        r.remove();
      });

      dragAndDrop.addTarget(rootTarg);
      this.rootBlock = Optional.empty();
      setActor(new EmptyBlock());
      setProperty();
    }
  }

  VerticalGroup conditionals = new VerticalGroup();
  DragAndDrop dragAndDrop = new DragAndDrop();
  WorkSpace workspace;
  ConditionalProperty property;
  Menu parent;

  public ConditionalUI(ConditionalProperty conditionalProperty, Menu parentMenu) {
    property = conditionalProperty;
    parent = parentMenu;
    editor = (LevelEditor)parent.getScreen();

    defaults().left();

    conditionals.space(10);
    for (Conditional cond : conditionalProperty.getPotentialValues()) {
      if (cond.getType() == ConditionalType.EmptyConditional) {
        continue;
      }
      ConditionalSource s = new ConditionalSource(cond);
      dragAndDrop.addSource(s);
      conditionals.addActor(s.getActor());
    }

    Container<VerticalGroup> condWrapper = new Container<>(conditionals);
    condWrapper.padLeft(20);
    condWrapper.minWidth(200);
    condWrapper.pack();

    ScrollPane scrollPane = new ScrollPane(condWrapper, AbstractScreen.skin, "default");
    scrollPane.setFadeScrollBars(false);
    scrollPane.setScrollBarPositions(false, false);
    scrollPane.setForceScroll(false, true);
    scrollPane.addListener(new InputListener(){
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        editor.getUIStage().setScrollFocus(scrollPane);
      }

      @Override
      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        editor.getUIStage().setScrollFocus(null);
      }
    });

    Container<ScrollPane> wrapper = new Container<>(scrollPane);
    wrapper.pack();
    wrapper.maxHeight(200);
    wrapper.minWidth(200);

    dragAndDrop.addTarget(new DragAndDrop.Target(wrapper) {
      @Override
      public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
        if (source instanceof ConditionalBlockSource) {
          return true;
        }
        return false;
      }

      @Override
      public void drop(Source source, Payload payload, float x, float y, int pointer) {
      }
    });

    Conditional c = conditionalProperty.getCurrentValue();

    workspace = new WorkSpace(c);

    G24TextButton clearButton = new G24TextButton("Clear");
    clearButton.setProgrammaticChangeEvents(false);
    clearButton.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (clearButton.isChecked()) {
          clearButton.setChecked(false);
          workspace.clearRoot();
        }
      }

    });

    add(wrapper);
    add(workspace);
    row();
    add(clearButton);
  }

  /**
   * Takes the tree representation and builds the actually conditional.
   */
  private void setProperty() {
    workspace.pack();
    workspace.invalidateHierarchy();
    pack();
    parent.pack();
    workspace.rootBlock.ifPresentOrElse(
        (rootBlock) -> {
          Conditional cond = buildConditional(rootBlock).get();
          property.set(cond);
        },
        () -> {
          property.set((Conditional)null);
        });

  }

  private Optional<Conditional> buildConditional(ConditionalBlock block) {

    Conditional c = block.val;

    if (c.getType() == ConditionalType.EmptyConditional) {
      return Optional.empty();
    }

    SnapshotArray<Actor> children = block.children.getChildren();
    Array<Conditional> childConditionals = new Array<>(Conditional.class);
    for (int i = 0; i < children.size; i++) {
      ConditionalBlock child = (ConditionalBlock) children.get(i);
      buildConditional(child).ifPresent((cond) -> {
        childConditionals.add(cond);
      });
    }
    c.setChildren(childConditionals.toArray());
    return Optional.of(c);
  }

  @Override
  public void handle() {
  }

}
