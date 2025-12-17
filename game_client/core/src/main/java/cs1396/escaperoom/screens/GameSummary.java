package cs1396.escaperoom.screens;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.engine.assets.maps.MapLoader;
import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.maps.MapSaver;
import cs1396.escaperoom.engine.types.Result;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapStats;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapStats.ValidStats;
import cs1396.escaperoom.screens.GameScreen.GameType;
import cs1396.escaperoom.screens.utils.ScreenManager;
import cs1396.escaperoom.services.GameStatistics;
import cs1396.escaperoom.services.MapUploader;
import cs1396.escaperoom.services.Networking;
import cs1396.escaperoom.services.User;
import cs1396.escaperoom.services.Networking.StatusCode;
import cs1396.escaperoom.services.Networking.UploadResponse;
import cs1396.escaperoom.ui.ConfirmDialog;
import cs1396.escaperoom.ui.StatTable;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24TextButton;
import cs1396.escaperoom.ui.widgets.G24TextInput;

public class GameSummary extends MenuScreen {
  private StatTable statTable;
  private GameType gameType;
  private MapMetadata previousMetadata;
  private GameStatistics stats;

  public GameSummary(GameStatistics stats, MapMetadata metadata, GameType from) {

    this.gameType = from;
    this.stats = stats;
    this.previousMetadata = metadata;

    statTable = new StatTable(stats);
    ScrollPane pane = new ScrollPane(statTable, skin, "transparent");

    HorizontalGroup buttonGroup = new HorizontalGroup();

    PlayAgainButton playAgainBtn = new PlayAgainButton();
    ContinueButton continueBtn = new ContinueButton();

    buttonGroup.expand().align(Align.center).space(100).pad(20);

    buttonGroup.addActor(playAgainBtn);
    buttonGroup.addActor(continueBtn);

    if (from == GameType.Verifying && User.isLoggedIn()) {
      UploadButton uploadButton = new UploadButton();
      buttonGroup.addActor(uploadButton);
      if (!stats.completedSucessfully) {
        uploadButton.setDisabled(true);
      }
    }

    if (User.isLoggedIn() && !metadata.mapID.isEmpty() && from != GameType.Verifying) {
      buttonGroup.addActor(new UpvoteButton());
      buttonGroup.addActor(new DownvoteButton());
    }

    if (User.isLoggedIn() && !metadata.mapID.isEmpty()) {
      waitFor(sendUpdates(stats, metadata), (StatusCode code) -> {
        if (code != StatusCode.OK) {
          Notifier.warn("Failed to upload stats, check your connection");
        } else {
          Notifier.info("Successfully uploaded your attempt!");
        }
      }, "Uploading your attempt");
    }

    rootTable.add(pane);
    rootTable.row();
    rootTable.add(buttonGroup).center().bottom().expand().pad(20);
  }

  private CompletableFuture<StatusCode> sendUpdates(GameStatistics stats, MapMetadata metadata) {
    Array<ValidStats> statUpdates = new Array<>();
    statUpdates.add(ValidStats.attempts);

    User.getRecord().update(stats, metadata);

    CompletableFuture<StatusCode> recordFuture = Networking.updatePlayerRecord(User.getRecord());
    CompletableFuture<StatusCode> updateFuture = Networking.updateMapStats(metadata.mapID, statUpdates);
    CompletableFuture<StatusCode> clearFuture;

    if (stats.completedSucessfully) {
      clearFuture = Networking.sendMapClear(metadata.mapID, stats.timeMilliseconds);
    } else {
      clearFuture = CompletableFuture.supplyAsync(() -> StatusCode.OK);
    }

    CompletableFuture<StatusCode> requests = CompletableFuture
        .allOf(updateFuture, clearFuture, recordFuture)
        .thenApply((__) -> {
          StatusCode updateStatus = updateFuture.join();
          StatusCode recordStatus = recordFuture.join();
          StatusCode clearStatus = clearFuture.join();
          if (updateStatus != StatusCode.OK) {
            System.err.println("update map stats failed");
            return updateStatus;
          } else if (recordStatus != StatusCode.OK) {
            System.err.println("update player record failed");
            return recordStatus;
          } else if(clearStatus != StatusCode.OK) {
            System.err.println("send map clear failed");
          }
          return clearStatus;
        });
    return requests;
  }

  private class PlayAgainButton extends G24TextButton {
    public PlayAgainButton() {
      super("Play Again");

      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (PlayAgainButton.this.isChecked()) {
            switch (gameType) {
              case Editor:
                MapLoader.tryLoadMap(previousMetadata).inspect((m) -> {
                  ScreenManager.instance().showScreen(new SinglePlayerGame(m));
                });
                break;
              case Standard:
                MapLoader.tryLoadMap(previousMetadata).inspect((m) -> {
                  ScreenManager.instance().showScreen(new SinglePlayerGame(m, false));
                });
                break;
              case Verifying:
                MapLoader.tryLoadMap(previousMetadata).inspect((m) -> {
                  ScreenManager.instance().showScreen(new SinglePlayerGame(m, true));
                });
                break;
              default:
                break;
            }
          }
        }
      });
    }
  }

  private class ContinueButton extends G24TextButton {
    public ContinueButton() {
      super("Continue");
      addListener(new ChangeListener() {

        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (ContinueButton.this.isChecked()) {
            ScreenManager.instance().showScreen(new MapSelectScreen.MapSelectScreenBuilder(true).build());
          }
        }
      });
    }
  }

  private class UpvoteButton extends ImageButton {
    public UpvoteButton() {
      super(skin, "upvote");
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (UpvoteButton.this.isChecked()) {
            UpvoteButton.this.setDisabled(true);
            Networking.updateMapStats(previousMetadata.mapID, Array.with(ValidStats.upvotes))
                .thenAccept((StatusCode s) -> {
                  Gdx.app.postRunnable(() -> {
                    if (s != StatusCode.OK) {
                      Notifier.error("Error sending upvote!");
                    }
                  });
                });
          }
        }
      });
    }
  }

  private class DownvoteButton extends ImageButton {
    public DownvoteButton() {
      super(skin, "downvote");
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (DownvoteButton.this.isChecked()) {
            DownvoteButton.this.setDisabled(true);
            Networking.updateMapStats(previousMetadata.mapID, Array.with(ValidStats.downvotes))
                .thenAccept((StatusCode s) -> {
                  Gdx.app.postRunnable(() -> {
                    if (s != StatusCode.OK) {
                      Notifier.error("Error sending downvote!");
                    }
                  });
                });
          }
        }
      });
    }
  }

  private class UploadButton extends G24TextButton {
    public UploadButton() {
      super("Upload");
      setProgrammaticChangeEvents(false);
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (UploadButton.this.isChecked()) {
            UploadButton.this.setChecked(false);

            G24TextInput userInput = new G24TextInput();
            userInput.setMultiline(true);
            userInput.setPrefRows(3);
            userInput.setMaxLength(200);

            new ConfirmDialog.Builder("Description for your new map:")
                .withContent(new ConfirmDialog.Content(userInput, true, Align.center))
                .onConfirm(() -> {
                  String desc = userInput.getText();
                  if (desc.isEmpty() || desc.isBlank()) {
                    Notifier.error("Please enter a description!", userInput);
                    return false;
                  }

                  previousMetadata.stats = Optional.of(MapStats.fromGameStats(stats));
                  previousMetadata.stats.get().description = desc;

                  waitFor(MapUploader.uploadMap(previousMetadata), (Result<UploadResponse, String> output) -> {
                    output
                      .inspect((rsp) -> {
                        Notifier.info(String.format("%s successfully uploaded!", previousMetadata.name));
                        previousMetadata.mapID = rsp.mapID;
                        if (!MapSaver.updateMetadata(previousMetadata)) {
                          Notifier.warn(String.format("%s failed to update metadata!", previousMetadata.name));
                        }
                        UploadButton.this.setDisabled(true);
                      })
                      .inspect_err(err -> Notifier.error(err));
                  }, "Uploading");
                  return true;
                })
                .disableSpawner(UploadButton.this)
                .build()
                .show(getUIStage());
          }
        }
      });
    }
  }
}
