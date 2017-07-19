package com.tommo.thisapprocks.gui;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.maps.Coord;
import com.codename1.ui.Container;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.layouts.BorderLayout;
import com.tommo.thisapprocks.ThisAppRocks;

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

		FloatingActionButton addButton = FloatingActionButton.createFAB(FontImage.MATERIAL_ADD);
		addButton.addActionListener(e -> {
			AddRockForm addRockForm = new AddRockForm(app, this, parent, null);
			addRockForm.show();

		});
		add(BorderLayout.CENTER, addButton.bindFabToContainer(mapContainer));
	}

	public void addMarker() {
		mapContainer.setCameraPosition(new Coord(41.889, -87.622));
		mapContainer.addMarker(EncodedImage.createFromImage(rockImg, false), mapContainer.getCameraPosition(), "", "",
				evt -> {
					ToastBar.showMessage("You clicked the marker", FontImage.MATERIAL_PLACE);
				});
	}
}
