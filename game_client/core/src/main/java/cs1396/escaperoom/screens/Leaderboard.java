package cs1396.escaperoom.screens;

import java.util.function.BiFunction;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;

import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.services.Networking;
import cs1396.escaperoom.services.PlayerRecord;
import cs1396.escaperoom.services.Networking.StatusCode;
import cs1396.escaperoom.services.Types.Filters;
import cs1396.escaperoom.services.Types.ListPlayerRecordRequest;
import cs1396.escaperoom.services.Types.ListPlayerRecordResponse;
import cs1396.escaperoom.services.Types.Sort;
import cs1396.escaperoom.ui.FilterUI;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;

public class Leaderboard extends MenuScreen {
  G24TextButton backButton;
  private FilterUI filterUI;
  private G24TextButton filterButton = new G24TextButton("Filter Results");
  private Table entries = new Table();

  BiFunction<Filters, Sort, Void> onSearch = (Filters arr, Sort sort) -> {
    waitFor(
        Networking.listPlayerRecords(
            (ListPlayerRecordRequest) new ListPlayerRecordRequest().withLimit(10).withSort(sort).withFilters(arr)),
        (ListPlayerRecordResponse rsp) -> {
          entries.clear();
          addHeader();
          for (PlayerRecord data : rsp.records) {
            addEntry(data);
          }
        }, "Applying filters...");
    return null;
  };
  ChangeListener onBackButton = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      ScreenManager.instance().showScreen(new OnlineMainMenu());
    }
  };

  private void addEntry(PlayerRecord record) {
    // Username
    entries.add(new G24Label(record.username, G24LabelStyle.White));
    // attempts
    entries.add(new G24Label(Integer.toString(record.attempts), G24LabelStyle.White));
    // clears
    entries.add(new G24Label(Integer.toString(record.clears), G24LabelStyle.White));
    // uniqueclears
    entries.add(new G24Label(Integer.toString(record.clearList.size), G24LabelStyle.White));
    entries.row();
  }

  private void addHeader() {
    // Username
    entries.add(new G24Label("Username", G24LabelStyle.Title));
    // attempts
    entries.add(new G24Label("Attempts", G24LabelStyle.Title));
    // clears
    entries.add(new G24Label("Map Clears", G24LabelStyle.Title));
    // uniqueclears
    entries.add(new G24Label("Unique Clears", G24LabelStyle.Title));
    entries.row();
  }

  private ChangeListener onFilter = new ChangeListener() {

    @Override
    public void changed(ChangeEvent event, Actor actor) {
      filterButton.setChecked(false);
      filterUI.pack();
      filterUI.toFront();
      filterUI.setPosition(getUIStage().getWidth() / 2 - filterUI.getWidth() / 2,
          getUIStage().getHeight() / 2 - filterUI.getHeight() / 2);
      filterUI.setVisible(true);
    }
  };

  public Leaderboard() {
    backButton = new G24TextButton("Back");
    backButton.addListener(onBackButton);
    entries.defaults().center().pad(10);
    AssetManager.instance().load("textures/bkg.9.png", Texture.class);
    AssetManager.instance().finishLoadingAsset("textures/bkg.9.png");
    Texture bkg = AssetManager.instance().get("textures/bkg.9.png", Texture.class);
    NinePatch ninePatch = new NinePatch(bkg, 10, 10, 10, 10);
    NinePatchDrawable drawable = new NinePatchDrawable(ninePatch);
    drawable.tint(new Color(1,1,1,1));
    entries.setBackground(drawable);

    addHeader();
    filterUI = new FilterUI(onSearch, ListPlayerRecordRequest.Field.class);
    filterButton.addListener(onFilter);
    filterButton.setProgrammaticChangeEvents(false);
    waitFor(
        Networking.listPlayerRecords(
            (ListPlayerRecordRequest) new ListPlayerRecordRequest().withLimit(10).withSort(new Sort("name"))),
        (ListPlayerRecordResponse rsp) -> {


        Image title = new Image(AssetManager.instance().loadTextureBlocking("textures/leaderboard.png"));
        title.setScaling(Scaling.contain);
          rootTable.add(title).padTop(200).maxWidth(800).colspan(2).center().row();
          if (rsp.code == StatusCode.OK) {
            rsp.records.forEach((r) -> {
              addEntry(r);
            });
          } else {
            Notifier.warn("Error fetching leaderboard...");
          }
          ScrollPane p = new ScrollPane(entries, AbstractScreen.skin, "nobkg");
          p.setFadeScrollBars(false);
          p.setScrollBarPositions(false, true);
          p.setForceScroll(false, true);
          rootTable.add(p).top().colspan(2).maxHeight(0.75f * getUIStage().getHeight()).expandX().row();
          p.pack();
          rootTable.add(backButton).bottom().left().expand();
          rootTable.add(filterButton).bottom().right().expand();
          filterUI.setVisible(false);
          addUI(filterUI);
        }, "Fetching maps...");
  }
}
