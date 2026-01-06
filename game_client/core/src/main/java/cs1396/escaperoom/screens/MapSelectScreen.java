package cs1396.escaperoom.screens;

import java.io.File;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.maps.MapData;
import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.engine.assets.maps.MapManager;
import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapLocation;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapStats;
import cs1396.escaperoom.engine.assets.maps.MapSaver;
import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.types.Size;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.services.MapDownloader;
import cs1396.escaperoom.services.MapUploader;
import cs1396.escaperoom.services.Networking.UploadResponse;
import cs1396.escaperoom.services.User;
import cs1396.escaperoom.ui.ConfirmDialog;
import cs1396.escaperoom.ui.MapStatDialog;
import cs1396.escaperoom.ui.Tooltip;
import cs1396.escaperoom.ui.notifications.NotificationBus;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24Dialog;
import cs1396.escaperoom.ui.widgets.G24Label;
import cs1396.escaperoom.ui.widgets.G24Label.G24LabelStyle;
import cs1396.escaperoom.ui.widgets.G24NumberInput.IntInput;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;
import cs1396.escaperoom.ui.widgets.G24Dialog.Content;

public class MapSelectScreen extends MenuScreen {

  private static class MapSelectScreenSettings {
    boolean creation = false;
    boolean edit = false;
    boolean download = false;
    boolean verify = false;
    boolean upload = false;
    boolean play = false;
    boolean delete = false;
  }

  private MapSelectScreenSettings settings;
  protected Array<MapEntry> entries = new Array<>();
  protected VerticalGroup entriesUI = new VerticalGroup();
  private AbstractScreen returnTo;

  public static class StatLabel extends G24Label {
    public StatLabel(String label, String value) {
      super(label + ": " + value, "bubble_gray", 0.6f);
    }
  }

  protected class MapEntry extends ScrollPane {

    Table innerTable = new Table();
    MapMetadata data;

    private void addStats(MapStats stats) {
      HorizontalGroup statGroup = new HorizontalGroup();
      statGroup.addActor(new StatLabel("Creator", stats.creator));
      statGroup.addActor(new StatLabel("Downloads", Long.toString(stats.downloads)));
      statGroup.addActor(new StatLabel("Attempts", Long.toString(stats.attempts)));
      statGroup.addActor(new StatLabel("Clears", Long.toString(stats.clears)));
      statGroup.space(10);
      innerTable.add(statGroup).colspan(innerTable.getColumns()).center();
    }

    public MapEntry(MapMetadata metadata) {
      super(null, AbstractScreen.skin);
      setStyle(AbstractScreen.skin.get("transparent", ScrollPaneStyle.class));
      this.data = metadata;
      innerTable.setFillParent(true);
      innerTable.defaults().pad(10).center();
      innerTable.add(new G24Label(metadata.name, G24LabelStyle.Bubble));

      if (settings.play) {
        innerTable.add(new PlayButton());
      }

      if (settings.edit && metadata.mapID.isEmpty()) {
        innerTable.add(new EditButton());
      }

      if (settings.verify && User.isLoggedIn() && metadata.mapID.isEmpty()) {
        innerTable.add(new VerifyButton());
      }

      if (settings.upload && metadata.mapID.isEmpty()) {
        innerTable.add(new UploadButton());
      }

      if (settings.download) {
        innerTable.add(new DownloadButton());
      }

      if (!settings.download) {
        innerTable.add(new CopyButton());
      }

      if (settings.delete) {
        innerTable.add(new DeleteButton());
      }

      metadata.stats.ifPresent((s) -> {
        innerTable.add(new InfoButton(s));
        innerTable.row();
        addStats(s);
      });

      setActor(innerTable);
    }

    private class PlayButton extends ImageButton {
      PlayButton() {
        super(skin, "play");
        new Tooltip.Builder("Play").target(this).build();
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (PlayButton.this.isChecked()) {
              MapLoader.tryLoadMap(data, settings.creation).inspect((g) -> {
                ScreenManager.instance().showScreen(new SinglePlayerGame(g, false));
              })
              .inspect_err((e) -> Notifier.error(e.reason()));
            }
          }
        });
      }
    }


    private class CopyButton extends ImageButton {
      CopyButton() {
        super(skin, "copy");
        new Tooltip.Builder("Copy Map").target(this).build();
        setProgrammaticChangeEvents(false);
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (CopyButton.this.isChecked()) {
              CopyButton.this.setChecked(false);

              G24TextInput nameInput = new G24TextInput();

              new ConfirmDialog.Builder("Copy Map")
                .withContent(new G24Label("Copy Name:", G24LabelStyle.Underline))
                .withContent(nameInput)
                .confirmText("Copy")
                .onConfirm(() -> {
                  String newName = nameInput.getText();
                  if (newName.isBlank() || newName.isEmpty()){
                    Notifier.warn("No name provided!", nameInput);
                    return false;
                  }

                  File newfile = new File(new MapLocation(newName, false).mapBasePath);
                  if (newfile.exists()){
                    Notifier.error("Map with name \"" + newName + "\" already exists!");
                    return false;
                  }

                  MapManager.copy(data, newName).ifPresentOrElse(
                    (md) -> {
                      entriesUI.addActor(new MapEntry(md));
                      entriesUI.pack();
                      rootTable.pack();
                    },
                    () -> Notifier.warn("Copy Failed")
                  );

                  return true;
                })
                .build().show(getUIStage());
            }
          }
        });
      }
    }

    private class DeleteButton extends ImageButton {
      DeleteButton() {
        super(skin, "toggleForbidden");
        new Tooltip.Builder("Delete Map").target(this).build();
        setProgrammaticChangeEvents(false);
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (DeleteButton.this.isChecked()) {
              new ConfirmDialog.Builder("Are you sure?")
                .withContent(new Content(new G24Label(
                  "Are you sure you want to delete " + MapEntry.this.data.name + "?"), true, Align.center))
                .withContent(new Content(new G24Label(
                  "This will delete any custom object data or textures in this directory"), true, Align.center))
                .withContent(new Content(new G24Label(
                  "This cannot be undone", G24LabelStyle.Underline), true, Align.center))
                .confirmText("Delete")
                .onConfirm(() -> {
                  MapSaver.deleteMap(MapEntry.this.data);
                  MapEntry.this.remove();
                  MapSelectScreenBuilder.maps.removeValue(MapEntry.this.data, false);
                  Notifier.info("Successfully deleted map.");
                  return true;
                })
                .disableSpawner(DeleteButton.this)
                .build()
                .show(DeleteButton.this.getStage());
            }
          }
        });
      }
    }

    private class UploadButton extends ImageButton {
      UploadButton() {
        super(skin, "upload");
        setProgrammaticChangeEvents(false);
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (UploadButton.this.isChecked()) {
              UploadButton.this.setChecked(false);
              if (!User.isLoggedIn()) {
                Notifier.error("You must be logged in to upload a map!");
                return;
              }

              waitFor(MapUploader.uploadMap(data), (Result<UploadResponse, String> output) -> {
                output
                  .inspect(rsp -> Notifier.info(String.format("%s successfully uploaded!", data.name)))
                  .inspect_err(e -> Notifier.error(e));
              }, "Uploading");
            }
          }
        });
      }
    }

    private class DownloadButton extends ImageButton {
      DownloadButton() {
        super(skin, "download");
        new Tooltip.Builder("Download Map").target(this).build();
        if (new File(data.locations.mapBasePath).exists()) {
          setDisabled(true);
          return;
        }
        setProgrammaticChangeEvents(false);
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (DownloadButton.this.isChecked()) {
              DownloadButton.this.setChecked(false);
              if (!User.isLoggedIn()) {
                Notifier.error("You must be logged in to download a map!");
                return;
              }
              waitFor(MapDownloader.downloadMap(data), output -> {
                output
                  .inspect_err(e -> Notifier.error(e))
                  .inspect(rsp -> {
                    DownloadButton.this.setDisabled(true);
                    Notifier.info(String.format("%s successfully downloaded!", data.name));
                  });
              }, "Downloading " + data.name);
            }
          }
        });
      }
    }

    private class InfoButton extends ImageButton {
      InfoButton(MapStats stats) {
        super(skin, "info");
        new Tooltip.Builder("Map Information").target(this).build();
        setProgrammaticChangeEvents(false);
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (InfoButton.this.isChecked()) {
              InfoButton.this.setChecked(false);
              new MapStatDialog(data, stats).show(InfoButton.this.getStage());
            }
          }
        });
      }
    }

    private class EditButton extends ImageButton {
      EditButton() {
        super(skin, "edit");
        new Tooltip.Builder("Edit Map").target(this).build();
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (EditButton.this.isChecked()) {
              MapLoader.tryLoadMap(data, settings.creation).inspect((g) -> {
                ScreenManager.instance().showScreen(new LevelEditor(g));
              })
              .inspect_err(e -> Notifier.error(e.reason()));
            }
          }

        });
      }
    }

    private class VerifyButton extends G24TextButton {
      VerifyButton() {
        super("Verify");
        new Tooltip.Builder("Verify map to upload").target(this).build();
        pack();
        addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (VerifyButton.this.isChecked()) {
              MapLoader.tryLoadMap(data, settings.creation).inspect((g) -> {
                ScreenManager.instance().showScreen(new SinglePlayerGame(g, true));
              })
              .inspect_err(e -> Notifier.error(e.reason()));
            }
          }
        });
      }
    }
  }

  G24TextButton backButton;
  ChangeListener onBackButton = new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
      if (returnTo == null) {
        ScreenManager.instance().showScreen(new MainMenu());
      } else {
        NotificationBus.get().removeListener(returnTo.getNotificationOverlay());
        ScreenManager.instance().showScreen(returnTo);
      }
    }
  };

  protected MapSelectScreen() { }

  protected MapSelectScreen(Array<MapMetadata> maps, MapSelectScreenSettings settings, AbstractScreen returnTo) {
    init(maps, settings, returnTo);
  }

  protected void init(Array<MapMetadata> maps, MapSelectScreenSettings settings, AbstractScreen returnTo) {
    this.settings = settings;
    this.returnTo = returnTo;
    backButton = new G24TextButton("Back", "med-text");
    backButton.addListener(onBackButton);

    rootTable.defaults().pad(10);

    Image title = null;
    if (this instanceof Marketplace){
      title = new Image(AssetManager.instance().loadTextureBlocking("textures/marketplace.png"));
    } else {
      title = new Image(AssetManager.instance().loadTextureBlocking("textures/map_select.png"));
    }
    title.setScaling(Scaling.contain);

    rootTable.add(title).padTop(200).colspan(2).center().maxWidth(800);
    rootTable.row();
    entriesUI.pad(25);
    entriesUI.space(30);
    entriesUI.fill();
    entriesUI.grow();
    for (MapMetadata data : maps) {
      MapEntry e = new MapEntry(data);
      entriesUI.addActor(e);
      entries.add(e);
    }
    ScrollPane p = new ScrollPane(entriesUI, AbstractScreen.skin, "nobkg");
    p.setFadeScrollBars(false);
    p.setScrollBarPositions(false, true);
    p.setForceScroll(false, true);
    rootTable.add(p).top().colspan(2).maxHeight(0.75f * getUIStage().getHeight()).expandX();
    p.pack();

    rootTable.row();
    rootTable.add(backButton).bottom().left().expand();

    if (settings.creation) {
      rootTable.add(new CreateNewMapButton()).bottom().right().expand();
    }

    BackManager.addBack(() -> {
      if (User.isLoggedIn()) {
        ScreenManager.instance().showScreen(new OnlineMainMenu());
      } else {
        ScreenManager.instance().showScreen(new MainMenu());
      }
    });
  }


  public static class MapSelectScreenBuilder {
    private static Array<MapMetadata> maps = new Array<>();
    private static AbstractScreen returnTo = null;
    private static MapSelectScreenSettings settings = new MapSelectScreenSettings();

    public MapSelectScreenBuilder(AbstractScreen back) {
      settings = new MapSelectScreenSettings();
      maps = new Array<>();
      returnTo = back;
      NotificationBus.get().removeListener(returnTo.getNotificationOverlay());
    }

    public static Array<MapMetadata> getMaps() {
      return maps;
    }

    public static AbstractScreen getReturnTo() {
      return returnTo;
    }

    public static MapSelectScreenSettings getSettings() {
      return settings;
    }

    public MapSelectScreenBuilder(boolean previousSettings) {
      if (!previousSettings) {
        settings = new MapSelectScreenSettings();
        maps = new Array<>();
        returnTo = null;
      } else {
        // always refresh maps 
        maps = MapLoader.discoverMaps();
      }
    }

    public MapSelectScreenBuilder withMaps(Array<MapMetadata> maps) {
      MapSelectScreenBuilder.maps = maps;
      return this;
    }

    public MapSelectScreenBuilder creation() {
      settings.creation = true;
      return this;
    }

    public MapSelectScreenBuilder play() {
      settings.play = true;
      return this;
    }

    public MapSelectScreenBuilder edit() {
      settings.edit = true;
      return this;
    }

    public MapSelectScreenBuilder verify() {
      settings.verify = true;
      return this;
    }

    public MapSelectScreenBuilder delete() {
      settings.delete = true;
      return this;
    }

    public MapSelectScreenBuilder download() {
      settings.download = true;
      return this;
    }

    public MapSelectScreenBuilder upload() {
      settings.upload = true;
      return this;
    }

    public MapSelectScreen build() {
      return new MapSelectScreen(maps, settings, returnTo);
    }
  }

  private class CreateNewMapButton extends G24TextButton {
    private G24Dialog getConfirmDialog(){
      G24Label nameLabel = new G24Label("Map Name", G24LabelStyle.Underline);
      G24TextInput mapNameInput = new G24TextInput();
      mapNameInput.setFilter(
          (c) -> Character.isAlphabetic(c) ||
              Character.isDigit(c) ||
              Character.isWhitespace(c),
          "Map names can only contain letters, numbers, or spaces");
      mapNameInput.setMessageText("\'My new map\"");

      Size s = new Size(WORLD_WIDTH, WORLD_HEIGHT);
      IntInput widthInput = new IntInput(AbstractScreen.WORLD_WIDTH, (int val) -> {
        s.width = val;
      });

      IntInput heightInput = new IntInput(AbstractScreen.WORLD_WIDTH, (int val) -> {
        s.height = val;
      });

      G24Dialog d = new ConfirmDialog.Builder("Create a new Map")
          .disableSpawner(this)
          .onConfirm(() -> {
            String newMap = mapNameInput.getText();
            if (newMap.isBlank() || newMap.isEmpty()){
              Notifier.warn("Must enter a map name!", mapNameInput);
              return false;
            }

            if (new File(new MapLocation(newMap, false).mapBasePath).exists()) {
              Notifier.warn("Map with name \"" + newMap + "\" already exists!", mapNameInput);
              return false;
            }

            MapMetadata newMetadata = new MapMetadata(newMap, false);
            MapData newMapData = new MapData(newMetadata);
            newMapData.registerGrid(MapData.DEFAULT_GRID_NAME, new Grid(s.width, s.height));
            newMapData.registerStart(MapData.DEFAULT_GRID_NAME);

            if (!MapSaver.saveMap(newMapData)) {
              Notifier.warn("Error creating map \"" + newMap + "\"");
              return true;
            }

            entriesUI.addActor(new MapEntry(newMetadata));

            entriesUI.pack();
            rootTable.pack();
            return true;
          })
          .cancelText("Cancel")
          .build();

      d.getContentTable().add(nameLabel).colspan(2).row();
      d.getContentTable().add(mapNameInput).colspan(2).row();
      d.getContentTable().add(new G24Label("Map Height", G24LabelStyle.Underline));
      d.getContentTable().add(heightInput).row();
      d.getContentTable().add(new G24Label("Map Width", G24LabelStyle.Underline));
      d.getContentTable().add(widthInput).row();
      return d;
    }


    CreateNewMapButton() {
      super("Create New Map", "med-text");
      setProgrammaticChangeEvents(false);

      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (isChecked()) {
            setChecked(false);
            setDisabled(true);
            getConfirmDialog().show(getUIStage());
          }
        }
      });
    }
  }

}
