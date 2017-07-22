package com.tommo.thisapprocks.gui;

import com.codename1.components.MultiButton;
import com.codename1.googlemaps.MapContainer;
import com.codename1.maps.Coord;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.tommo.thisapprocks.ThisAppRocks;
import com.tommo.thisapprocks.data.Rock;

public class MapForm extends Container {

  private ThisAppRocks app;
  private static final String HTML_API_KEY = "AIzaSyDzOD3oa-MeduBFgWrlzOdZ5lMTkha_dEQ";
  private MapContainer mapContainer;
  private Image rockImg;

  public MapForm(ThisAppRocks app) {
    super(new BorderLayout());
    this.app = app;
    setLayout(new BorderLayout());

    mapContainer = new MapContainer(HTML_API_KEY);
    mapContainer.setShowMyLocation(true);
    mapContainer.addTapListener(e -> {
      Dialog d = new Dialog();
      d.setLayout(BoxLayout.y());
      MultiButton addNewRockButton = new MultiButton("Add New Rock");
      addNewRockButton.setIcon(FontImage.createMaterial(FontImage.MATERIAL_ADD_CIRCLE_OUTLINE, "Button", 5));
      addNewRockButton.addActionListener(evt -> {
        d.dispose();
        Coord coord = mapContainer.getCoordAtPosition(e.getX(), e.getY());
        new AddRockForm(app, this, new Rock("", "", "", app.getUniqueId(), coord.getLatitude(), coord.getLongitude(), null, null)).show();
      });
      d.add(addNewRockButton);
      MultiButton selectExistingButton = new MultiButton("Select Existing");
      selectExistingButton.setIcon(FontImage.createMaterial(FontImage.MATERIAL_SEARCH, "Button", 5));
      selectExistingButton.addActionListener(evt -> {
        d.dispose();
      });
      d.add(selectExistingButton);
      d.showPopupDialog(getComponentForm().getToolbar());
    });

    rockImg = app.getTheme().getImage("rock.png");
    add(BorderLayout.CENTER, mapContainer);
  }

  public void addMarker(Rock rock) {
    mapContainer.setCameraPosition(rock.getCoord());
    mapContainer.addMarker(EncodedImage.createFromImage(rockImg, false), mapContainer.getCameraPosition(), "", "",
        evt -> new AddRockForm(app, this, rock).show());
  }

  public void clearAllMarkers() {
    mapContainer.clearMapLayers();
  }
}
