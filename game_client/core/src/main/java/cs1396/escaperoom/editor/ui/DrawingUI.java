package cs1396.escaperoom.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.editor.ui.DrawingPane.BrushShape;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSlot;
import cs1396.escaperoom.ui.widgets.G24ImageButton;
import cs1396.escaperoom.ui.widgets.G24NumberInput;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;

public class DrawingUI extends Table implements HandlesMenuClose {
  DrawingPane pane;
  CheckBox itemBrushCheckBox;

  private class ListEntry {
    CheckBox check;
    G24Label label;
    ItemSlot slot;
    Item item;

    public ListEntry(Item i) {
      item = i;
      check = new CheckBox("", AbstractScreen.skin);
      check.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (check.isChecked()) {
            itemBrushCheckBox.setChecked(true);
            pane.setItemBrushItem(item);
          }
        }
      });

      label = new G24Label(item.getItemName(), G24LabelStyle.Default);
      slot = new ItemSlot(item);
    }

    public CheckBox getCheck() {
      return this.check;
    }
  }

  private class ColorButton extends G24ImageButton {
    public ColorButton(Color color) {
      getImage().setColor(color);
      setProgrammaticChangeEvents(false);
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (ColorButton.this.isChecked()) {
            pane.setDrawColor(color);
          }
        }
      });
    }
  }

  public DrawingUI(Pixmap pixmap, AbstractScreen screen) {
    LevelEditor editor = (LevelEditor)screen;
    Stack canvas = new Stack();

    AssetManager.instance().load("textures/bkg.9.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/bkg.9.png");
    Texture bkg = AssetManager.instance().get("textures/bkg.9.png", Texture.class);
    NinePatch ninePatch = new NinePatch(bkg, 10, 10, 10, 10);
    NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
    Image bkgImg = new Image(drawable);
    bkgImg.setTouchable(Touchable.disabled);
    canvas.setSize(pixmap.getWidth() + 40, pixmap.getHeight() + 40);
    canvas.add(bkgImg);

    this.pane = new DrawingPane(pixmap);
    Container<DrawingPane> paneContainer = new Container<>(pane);
    paneContainer.pad(20);
    canvas.add(paneContainer);
    ButtonGroup<ColorButton> btnGroup = new ButtonGroup<>();
    btnGroup.setMinCheckCount(0);
    btnGroup.setMaxCheckCount(1);

    btnGroup.add(new ColorButton(Color.RED));
    btnGroup.add(new ColorButton(Color.GREEN));
    btnGroup.add(new ColorButton(Color.BLUE));
    btnGroup.add(new ColorButton(Color.BLACK));

    G24Label label = new G24Label("Brush Size:", G24LabelStyle.Bubble);
    G24NumberInput numberInput = new G24NumberInput();
    numberInput.setText(Integer.toString(DrawingPane.getBrushSize()));
    numberInput.setMaxLength(2);
    numberInput.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        try {
          int size = Integer.parseInt(numberInput.getText());
          pane.setBrushSize(size);
        } catch (Exception e) {
          pane.setBrushSize(1);
        }
      }
    });

    HorizontalGroup hg = new HorizontalGroup();
    for (ColorButton btn : btnGroup.getButtons()) {
      hg.addActor(btn);
    }
    G24TextButton clearButton = new G24TextButton("Clear");
    clearButton.setProgrammaticChangeEvents(false);
    clearButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        clearButton.setChecked(false);
        pane.clear();
      }
    });
    hg.addActor(label);
    hg.addActor(numberInput);

    ButtonGroup<CheckBox> itemSelectionGroup = new ButtonGroup<>();
    itemSelectionGroup.setMinCheckCount(0);
    itemSelectionGroup.setMaxCheckCount(1);
    Table itemList = new Table();
    itemList.padRight(15);
    itemList.padTop(15);
    itemList.defaults().space(5).left();
    Array<Item> items = new Array<>();
    for (Item i : editor.getItemPrototypes()) {
      items.add(i);
      ListEntry e = new ListEntry(i);
      itemList.add(e.getCheck());
      itemList.add(e.label);
      itemList.add(e.slot).row();
      itemSelectionGroup.add(e.getCheck());
    }


    ScrollPane itemSelectPane = new ScrollPane(itemList, AbstractScreen.skin, "default");
    itemSelectPane.setFadeScrollBars(false);
    itemSelectPane.setForceScroll(false, true);
    itemSelectPane.setScrollBarPositions(false, true);

    HorizontalGroup brushShapes = new HorizontalGroup();
    ButtonGroup<CheckBox> shapeGroup = new ButtonGroup<>();
    shapeGroup.setMinCheckCount(0);
    shapeGroup.setMaxCheckCount(1);

    CheckBox circle = new CheckBox("Circle", AbstractScreen.skin);
    circle.setChecked(true);
    circle.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (circle.isChecked()) {
          itemSelectionGroup.uncheckAll();
          pane.setBrushShape(BrushShape.Circle);
        }
      }
    });
    CheckBox square = new CheckBox("Square", AbstractScreen.skin);
    square.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (square.isChecked()) {
          itemSelectionGroup.uncheckAll();
          pane.setBrushShape(BrushShape.Square);
        }
      }
    });
    itemBrushCheckBox = new CheckBox("Item", AbstractScreen.skin);
    itemBrushCheckBox.addListener(new ChangeListener() {

      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (itemBrushCheckBox.isChecked()) {
          pane.setBrushShape(BrushShape.Item);
        }
      }
    });
    shapeGroup.add(circle, square, itemBrushCheckBox);
    brushShapes.addActor(circle);
    brushShapes.addActor(itemBrushCheckBox);
    brushShapes.addActor(square);


    add(canvas).center().colspan(2);
    row();
    add(hg).center().colspan(2);
    row();
    add(label).left();
    add(numberInput).left().maxWidth(60);
    row();
    add(new G24Label("Shape:", G24LabelStyle.Bubble)).left();
    add(brushShapes).left();
    row();
    add(new G24Label("Item Brush", G24LabelStyle.Title)).center().colspan(2);
    row();
    add(itemSelectPane).center().maxHeight(300).colspan(2);
    row();
    add(clearButton).center().colspan(2);
  }

  @Override
  public void handle() {
  }

}
