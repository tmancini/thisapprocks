package com.tommo.thisapprocks;

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

public class AddRockForm extends Form {

	private Form parent;
	private MapForm mapForm;
	private TextField nameField;
	private TextArea descriptField;
	private ImageContainer imageContainer;

	public AddRockForm(MapForm mapForm, Form parent) {
		super("Add New Rock");
		this.mapForm = mapForm;
		this.parent = parent;
		setLayout(new BorderLayout());

		Command backCommand = new Command("Back",
				FontImage.createMaterial(FontImage.MATERIAL_ARROW_BACK, "Command", 4)) {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (Dialog.show("Rock not saved!", "Are you sure you want to cancel?", Dialog.TYPE_ERROR, null, "Yes",
						"No")) {
					parent.showBack();
				}
			}
		};
		setBackCommand(backCommand);
		getToolbar().addCommandToLeftBar(backCommand);
		getToolbar().addCommandToRightBar("Add Rock", FontImage.createMaterial(FontImage.MATERIAL_ADD, "Command", 4),
				e -> {
					if (nameField.getText().trim().length() == 0 || descriptField.getText().trim().length() == 0
							|| imageContainer.getImagePaths().isEmpty()) {
						Dialog.show("Oh no!", "Please enter a name, description, and at least one picture.",
								Dialog.TYPE_ERROR, null, "OK", null);
						return;
					}
					mapForm.addMarker();
					parent.showBack();
				});

		nameField = new TextField("", "Name");
		descriptField = new TextArea(4, 50);
		imageContainer = new ImageContainer();

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
