package com.tommo.thisapprocks.gui;

import java.util.List;

import com.codename1.ui.Command;
import com.codename1.ui.Dialog;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.Tabs;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.tommo.thisapprocks.ThisAppRocks;
import com.tommo.thisapprocks.data.Rock;

public class AddRockForm extends Form {

	private Form parent;
	private MapForm mapForm;
	private TextField nameField;
	private TextArea descriptField;
	private ImageContainer imageContainer;
	private ThisAppRocks app;
	private Rock rock;

	public AddRockForm(ThisAppRocks app, Form parent, Rock rock) {
		this(app, null, parent, rock);
	}

	public AddRockForm(ThisAppRocks app, MapForm mapForm, Form parent, Rock rock) {
		super("Add New Rock");
		this.app = app;
		this.mapForm = mapForm;
		this.parent = parent;
		this.rock = rock;
		setLayout(new BorderLayout());

		Command backCommand = new Command("", FontImage.createMaterial(FontImage.MATERIAL_ARROW_BACK, "Command", 4)) {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// If rock not null check if fields changes
				if (Dialog.show("Rock not saved!", "Are you sure you want to cancel?", Dialog.TYPE_ERROR, null, "Yes",
						"No")) {
					parent.showBack();
				}
			}
		};
		setBackCommand(backCommand);
		getToolbar().addCommandToLeftBar(backCommand);
		getToolbar().addCommandToRightBar("", FontImage.createMaterial(FontImage.MATERIAL_ADD, "Command", 4), e -> {
			if (nameField.getText().trim().length() == 0 || descriptField.getText().trim().length() == 0
			/* || imageContainer.getImagePaths().isEmpty() */) {// should picture be required?
				Dialog.show("Oh no!", "Please enter a name, description, and at least one picture.", Dialog.TYPE_ERROR,
						null, "OK", null);
				return;
			}
			if (mapForm != null) {
				mapForm.addMarker();
			} else {
				app.addRock(new Rock(nameField.getText(), descriptField.getText(), false));
				app.refreshRocks(parent);
			}
			parent.showBack();
		});

		nameField = new TextField("", "Name");
		descriptField = new TextArea(4, 50);
		imageContainer = new ImageContainer();

		if (rock != null) {
			nameField.setText(rock.name.get());
			descriptField.setText(rock.descript.get());
		}

		Tabs tabs = new Tabs();
		tabs.addTab("Info", FontImage.createMaterial(FontImage.MATERIAL_INFO_OUTLINE, "Tab", 4),
				BoxLayout.encloseY(new Label("Rock Name"), nameField, new Label("Description"), descriptField));
		tabs.addTab("Pics", FontImage.createMaterial(FontImage.MATERIAL_PHOTO, "Tab", 4), imageContainer);

		add(BorderLayout.CENTER, tabs);
	}

	public String getName() {
		return nameField.getText();
	}

	public String getDescript() {
		return descriptField.getText();
	}

	public List<String> getImagePaths() {
		return imageContainer.getImagePaths();
	}
}
