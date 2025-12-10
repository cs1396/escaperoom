package group24.escaperoom.ui;

import java.util.Optional;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import group24.escaperoom.engine.assets.AssetManager;
import group24.escaperoom.engine.assets.maps.MapManager;
import group24.escaperoom.engine.assets.maps.MapMetadata;
import group24.escaperoom.engine.assets.maps.MapMetadata.MapStats;
import group24.escaperoom.ui.widgets.G24Dialog;
import group24.escaperoom.ui.widgets.G24TextButton;
import group24.escaperoom.ui.widgets.G24Label;

public class MapStatDialog extends G24Dialog {
  Container<Image> thumbnailContainer;
  MapMetadata metadata;

  public static class StatRow extends HorizontalGroup {
    public StatRow(String label, String value) {
      this(label, new G24Label(value, "default", 0.6f));
    }

    public StatRow(String label, Actor value) {
      G24Label name = new G24Label(label + ":", "bubble", 0.6f);
      space(10);
      addActor(name);
      addActor(value);
    }
  }

  public MapStatDialog(MapMetadata met, MapStats stats) {
    super(new Builder(met.name));

    this.metadata = met;
    Table t = getContentTable();

    VerticalGroup statGroup = new VerticalGroup();
    statGroup.addActor(new StatRow("Creator", stats.creator));
    statGroup.addActor(new StatRow("Downloads", Long.toString(stats.downloads)));
    statGroup.addActor(new StatRow("Attempts", Long.toString(stats.attempts)));
    statGroup.addActor(new StatRow("Clears", Long.toString(stats.clears)));
    statGroup.addActor(new StatRow("Upvotes", Long.toString(stats.upvotes)));
    statGroup.addActor(new StatRow("Downvotes", Long.toString(stats.downvotes)));
    statGroup.space(10);
    statGroup.columnLeft();
    statGroup.padTop(20);

    VerticalGroup wrGroup = new VerticalGroup();
    wrGroup.addActor(new StatRow("Record Holder", stats.record.username));

    if (stats.record.fastestms == -1) {
      wrGroup.addActor(new StatRow("Time Taken", "No Record"));
    } else {
      long ms = stats.record.fastestms;
      long minutes = ms / (60 * 1000);
      ms -= minutes * 1000 * 60;
      long seconds = ms / (1000);
      ms -= seconds * 1000;
      wrGroup.addActor(new StatRow("Fastest Clear", String.format("%d:%02d:%03d", minutes, seconds, ms)));
    }
    wrGroup.space(10);
    wrGroup.columnLeft();
    wrGroup.padTop(20);

    t.row();

    Stack thumbnailStack = new Stack();
    thumbnailStack.setSize(220, 220);
    AssetManager.instance().load("textures/bkg.9.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/bkg.9.png");
    Texture bkg = AssetManager.instance().get("textures/bkg.9.png", Texture.class);
    NinePatch ninePatch = new NinePatch(bkg, 10, 10, 10, 10);
    NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
    Image bkgImg = new Image(drawable);
    bkgImg.setTouchable(Touchable.disabled);
    thumbnailStack.add(bkgImg);

    AssetManager.instance().load("textures/test_thumbnail.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/test_thumbnail.png");

    Image thumbnail = new Image(AssetManager.instance().get("textures/test_thumbnail.png", Texture.class));
    thumbnail.setScaling(Scaling.fit);
    thumbnailContainer = new Container<>(thumbnail);
    thumbnailContainer.setSize(220, 220);
    thumbnailContainer.pad(10);
    thumbnailStack.add(thumbnailContainer);
    t.add(thumbnailStack).top().pad(20).size(220, 220);
    if (stats.description.isEmpty()) {
      stats.description = "<no description provided by creator>";
    }
    VerticalGroup desc = new VerticalGroup();
    desc.space(10);
    desc.columnLeft();

    G24Label titleLabel = new G24Label("Description", "title", 0.65f);
    desc.addActor(titleLabel);

    G24Label descLabel = new G24Label(stats.description, "default", 0.65f);
    descLabel.setWrap(true);
    descLabel.setAlignment(Align.left);

    Container<G24Label> descWrap = new Container<>(descLabel);
    descWrap.width(300);
    descWrap.left();
    desc.addActor(descWrap);

    t.add(desc).left().top().padTop(20).padRight(10);
    t.row();

    t.add(new G24Label("Map Statstics", "title")).center();
    t.add(new G24Label("World Record", "title")).center();
    t.row();
    t.add(statGroup).minWidth(200).top();
    t.add(wrGroup).minWidth(200).top();

    button(new G24TextButton("Done"));
  }

  @Override
  public Dialog show(Stage stage) {
    Dialog d = super.show(stage);
    System.out.println(metadata.locations.mapBasePath);

    waitFor(MapManager.fetchThumbnail(metadata), (Optional<String> oPath) -> {
      oPath.ifPresent((p) -> {
        MapManager.loadThumbNail(p).ifPresent((i) -> {
          i.setScaling(Scaling.fit);
          thumbnailContainer.setActor(i);
        });
      });
    });
    return d;

  }
}
