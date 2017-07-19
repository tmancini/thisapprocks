package com.tommo.thisapprocks.gui;

import java.util.List;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
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

  private TextField nameField;
  private TextArea descriptField;
  private FavoriteButton favoriteButton;
  private ImageContainer imageContainer;

  public AddRockForm(ThisAppRocks app, MapForm mapForm, Container parent, Rock rock) {
    super(rock == null ? "Add New Rock" : "Edit Rock");
    setLayout(new BorderLayout());

    Command backCommand = new Command("", FontImage.createMaterial(FontImage.MATERIAL_CHEVRON_LEFT, "TitleCommand", 5)) {
      @Override
      public void actionPerformed(ActionEvent evt) {

        if((rock == null && nameField.getText().trim().length() == 0 && descriptField.getText().trim().length() == 0)
            || (rock != null
                && nameField.getText().equals(rock.name.get()) && descriptField.getText().equals(rock.descript.get())
                && favoriteButton.isSelected() == rock.favorite.get())
            || Dialog.show("Rock not saved!", "Are you sure you want to cancel?", Dialog.TYPE_ERROR, null, "Yes", "No")) {
          mapForm.getComponentForm().showBack();
        }
      }
    };
    setBackCommand(backCommand);
    getToolbar().addCommandToLeftBar(backCommand);
    getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_SAVE, 5, e -> {
      if(nameField.getText().trim().length() == 0 || descriptField.getText().trim().length() == 0
      /* || imageContainer.getImagePaths().isEmpty() */) {// should picture be required?
        Dialog.show("Oh no!", "Please enter a name, description, and at least one picture.", Dialog.TYPE_ERROR, null, "OK", null);
        return;
      }
      if(rock == null) {
        Rock newRock = new Rock(nameField.getText(), descriptField.getText(), favoriteButton.isSelected());
        app.addRock(newRock);
        mapForm.addMarker(newRock);
      }
      else {
        rock.name.set(nameField.getText());
        rock.descript.set(descriptField.getText());
        rock.favorite.set(favoriteButton.isSelected());
      }
      if(parent != null) { // TODO
        app.refreshRocks(parent);
      }
      mapForm.getComponentForm().showBack();
    });

    nameField = new TextField("", "Name");
    descriptField = new TextArea(4, 50);
    favoriteButton = new FavoriteButton();
    imageContainer = new ImageContainer();

    if(rock != null) {
      nameField.setText(rock.name.get());
      descriptField.setText(rock.descript.get());
      favoriteButton.setSelected(rock.favorite.get());
    }

    Tabs tabs = new Tabs();
    tabs.addTab("Info", FontImage.MATERIAL_INFO_OUTLINE, 4,
        BoxLayout.encloseY(new Label("Rock Name"), nameField, new Label("Description"), descriptField, favoriteButton));
    tabs.addTab("Pics", FontImage.MATERIAL_PHOTO, 4, imageContainer);

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
