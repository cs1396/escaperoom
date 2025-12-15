package cs1396.escaperoom.screens;

import java.util.function.BiFunction;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import cs1396.escaperoom.engine.assets.maps.MapMetadata;
import cs1396.escaperoom.engine.assets.maps.MapMetadata.MapLocation;
import cs1396.escaperoom.services.Networking;
import cs1396.escaperoom.services.Types.Filters;
import cs1396.escaperoom.services.Types.ListMapsRequest;
import cs1396.escaperoom.services.Types.ListMapsResponse;
import cs1396.escaperoom.services.Types.Sort;
import cs1396.escaperoom.ui.FilterUI;
import cs1396.escaperoom.ui.widgets.G24TextButton;

public class Marketplace extends MapSelectScreen {
  private FilterUI filterUI;
  private G24TextButton filterButton = new G24TextButton("Filter Results");

  BiFunction<Filters, Sort, Void> onSearch = (Filters arr, Sort sort) -> {
    waitFor(Networking.listMapMetadata((ListMapsRequest)new ListMapsRequest().withLimit(10).withSort(sort).withFilters(arr)),
        (ListMapsResponse rsp) -> {
          entriesUI.clear();
          entries.clear();
          for (MapMetadata data : rsp.metadata) {
            data.locations = new MapLocation(data.mapID, false);
            MapEntry e = new MapEntry(data);
            entriesUI.addActor(e);
            entries.add(e);
          }
        }, "Applying filters...");
    return null;
  };

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

  public Marketplace() {
    filterUI = new FilterUI(onSearch, ListMapsRequest.Field.class);
    filterButton.addListener(onFilter);
    filterButton.setProgrammaticChangeEvents(false);
    waitFor(Networking.listMapMetadata((ListMapsRequest)new ListMapsRequest().withLimit(10).withSort(new Sort("name"))),
        (ListMapsResponse rsp) -> {
          rsp.metadata.forEach((meta) -> {
            meta.locations = new MapLocation(meta.mapID, false);
          });
          new MapSelectScreenBuilder(new OnlineMainMenu()).download().withMaps(rsp.metadata);
          init(MapSelectScreenBuilder.getMaps(), MapSelectScreenBuilder.getSettings(),
              MapSelectScreenBuilder.getReturnTo());
          rootTable.add(filterButton).bottom().right().expand();
          filterUI.setVisible(false);
          addUI(filterUI);
        }, "Fetching maps...");
  }
}
