package cs1396.escaperoom.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import cs1396.escaperoom.services.GameStatistics;
import cs1396.escaperoom.ui.widgets.G24Label;

public class StatTable extends Table {
  public StatTable(GameStatistics stats){
    defaults().pad(30);
    if (stats.completedSucessfully) {
      add(new G24Label("Map Completed!", "title")).colspan(2);
    } else {
      add(new G24Label("You Surrendered", "bubble")).colspan(2);
    }

    long ms = stats.timeMilliseconds;
    long minutes = ms / (60 * 1000);
    ms -= minutes * 1000 * 60;
    long seconds = ms / (1000);
    ms -= seconds * 1000;

    addLabel("Time taken", String.format("%d:%02d.%03d", minutes, seconds, ms), "");
    addLabel("Distance traveled", String.format("%.2f", stats.player.distanceTraveled), "m");
    addLabel("Average Speed", String.format("%.2f", stats.player.avgSpeed), "m/s");
    addLabel("Actions Performed", stats.player.actionsPerformed, "actions");
    addLabel("Items collected", stats.player.itemsCollected, "items");

  }

  private <T> void addLabel(String label, T value, String suffix) {
    row();
    add(new G24Label(label + ":", "bubble")).center().expandX();
    add(new G24Label(value.toString() + " " + suffix, "white")).left();
  }
}
