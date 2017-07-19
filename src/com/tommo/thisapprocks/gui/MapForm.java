package com.tommo.thisapprocks.gui;

import com.codename1.googlemaps.MapContainer;
import com.codename1.ui.Container;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.layouts.BorderLayout;
import com.tommo.thisapprocks.ThisAppRocks;
import com.tommo.thisapprocks.data.Rock;

public class MapForm extends Container {

  private Form parent;
  private ThisAppRocks app;
  private static final String HTML_API_KEY = "AIzaSyDzOD3oa-MeduBFgWrlzOdZ5lMTkha_dEQ";
  private MapContainer mapContainer;
  private Image rockImg;

  public MapForm(ThisAppRocks app, Form parent) {
    super(new BorderLayout());
    this.app = app;
    this.parent = parent;
    setLayout(new BorderLayout());

    mapContainer = new MapContainer(HTML_API_KEY);
    rockImg = app.getTheme().getImage("rock.png");
    add(BorderLayout.CENTER, mapContainer);
  }

  public void addMarker(Rock rock) {
// mapContainer.setCameraPosition(new Coord(41.889, -87.622));
    mapContainer.addMarker(EncodedImage.createFromImage(rockImg, false), mapContainer.getCameraPosition(), "", "",
        evt -> new AddRockForm(app, this, null, rock).show());
  }
}
