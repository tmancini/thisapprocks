package com.tommo.thisapprocks.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import com.codename1.capture.Capture;
import com.codename1.components.MultiButton;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.location.Location;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.util.Base64;
import com.tommo.thisapprocks.ThisAppRocks;
import com.tommo.thisapprocks.data.Rock;

public class AddRockForm extends Form {

  private TextField nameField;
  private TextArea descriptField;
  private TextField locationField;
  private Button getCurrentLocationButton;
  private FavoriteCommand favoriteCommand;
  private EncodedImage placeholder = null;
  private Label frontImageLabel;
  private Label backImageLabel;
  private Button frontImageButton;
  private Button backImageButton;

  public AddRockForm(ThisAppRocks app, MapForm mapForm, Rock rock) {
    super(rock == null ? "Add New Rock" : "Edit Rock");
    setLayout(BoxLayout.y());

    nameField = new TextField();
    descriptField = new TextArea(4, 50);
    locationField = new TextField("", "Location");
    locationField.setEnabled(false);
    getCurrentLocationButton = new Button("Set Current Location", FontImage.createMaterial(FontImage.MATERIAL_MY_LOCATION, "Button", 5));
    getCurrentLocationButton.addActionListener(evt -> {
      Location location = app.getCurrentLocation("Getting current location...");
      locationField.setText(location != null ? location.getLatitude() + ", " + location.getLongitude() : "");
    });
    favoriteCommand = new FavoriteCommand(getToolbar());

    placeholder = EncodedImage.createFromImage(
        Image.createImage(Display.getInstance().getDisplayWidth(), Display.getInstance().getDisplayWidth() / 4 * 3, 0), false);

    frontImageLabel = new Label();
    frontImageLabel.setHidden(true);
    backImageLabel = new Label();
    backImageLabel.setHidden(true);

    frontImageButton = new Button("Add/Edit Front Image", FontImage.createMaterial(FontImage.MATERIAL_ADD_A_PHOTO, "Button", 5));
    frontImageButton.addActionListener(e -> imageButtonEvent(frontImageButton, frontImageLabel));
    backImageButton = new Button("Add/Edit Back Image", FontImage.createMaterial(FontImage.MATERIAL_ADD_A_PHOTO, "Button", 5));
    backImageButton.addActionListener(e -> imageButtonEvent(backImageButton, backImageLabel));

    Command backCommand = new Command("", FontImage.createMaterial(FontImage.MATERIAL_CHEVRON_LEFT, "TitleCommand", 5)) {
      @Override
      public void actionPerformed(ActionEvent evt) {

        if((rock == null && nameField.getText().trim().length() == 0 && descriptField.getText().trim().length() == 0)
            || (rock != null
                && nameField.getText().equals(rock.name.get()) && descriptField.getText().equals(rock.descript.get())
                && favoriteCommand.isSelected() == Boolean.parseBoolean(rock.favorite.get()))
            || Dialog.show("Rock not saved!", "Are you sure you want to cancel?", Dialog.TYPE_ERROR, null, "Yes", "No")) {
          mapForm.getComponentForm().showBack();
        }
      }
    };
    setBackCommand(backCommand);
    getToolbar().addCommandToLeftBar(backCommand);
    getToolbar().addCommandToRightBar(favoriteCommand);
    getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_DONE, 5, e -> {
      if(nameField.getText().trim().length() == 0 || descriptField.getText().trim().length() == 0) {
        Dialog.show("Oh no!", "Please enter a name and description.", Dialog.TYPE_ERROR, null, "OK", null);
        return;
      }
      if(rock == null) {
        double latitude = 0.0;
        double longitude = 0.0;

        String location = locationField.getText();
        if(location.indexOf(",") > -1) {
          latitude = Double.valueOf(location.substring(0, location.indexOf(",")).trim());
          longitude = Double.valueOf(location.substring(location.indexOf(",") + 1).trim());
        }
        Rock newRock = addRockSync(new Rock(nameField.getText(), descriptField.getText(), String.valueOf(favoriteCommand.isSelected()),
            app.getUniqueId(), latitude, longitude, convertImage(frontImageLabel.getIcon()), convertImage(backImageLabel.getIcon())));
        if(newRock != null && newRock.getCoord() != null) {
          mapForm.addMarker(newRock);
        }
      }
      else {
        rock.name.set(nameField.getText());
        rock.descript.set(descriptField.getText());
        rock.favorite.set(String.valueOf(favoriteCommand.isSelected()));
        rock.frontImage.set(convertImage(frontImageLabel.getIcon()));
        rock.backImage.set(convertImage(backImageLabel.getIcon()));

        double latitude = 0.0;
        double longitude = 0.0;

        String location = locationField.getText();
        if(location.indexOf(",") > -1) {
          latitude = Double.valueOf(location.substring(0, location.indexOf(",")).trim());
          longitude = Double.valueOf(location.substring(location.indexOf(",") + 1).trim());
        }
        rock.latitude.set(latitude);
        rock.longitude.set(longitude);
        addRockSync(rock);
      }
      mapForm.getComponentForm().showBack();
      app.refreshRocks();
    });

    if(rock != null) {
      nameField.setText(rock.name.get());
      descriptField.setText(rock.descript.get());
      favoriteCommand.setSelected(Boolean.parseBoolean(rock.favorite.get()));
      try { // TODO
        frontImageLabel.setIcon(EncodedImage.create(Base64.decode(rock.frontImage.get().getBytes())));
        frontImageLabel.setHidden(false);
      }
      catch(Exception ex) {
      }
      try { // TODO
        backImageLabel.setIcon(EncodedImage.create(Base64.decode(rock.backImage.get().getBytes())));
        backImageLabel.setHidden(false);
      }
      catch(Exception ex) {
      }
      locationField.setText(rock.getCoordString());
    }

    addAll(new Label("Name"), nameField, new Label("Description"), descriptField, getCurrentLocationButton, locationField, frontImageButton,
        frontImageLabel, backImageButton, backImageLabel);
  }

  void imageButtonEvent(Button imageButton, Label imageLabel) {
    Dialog d = new Dialog();
    d.setLayout(BoxLayout.y());
    MultiButton cameraButton = new MultiButton("Camera");
    cameraButton.setIcon(FontImage.createMaterial(FontImage.MATERIAL_ADD_A_PHOTO, "Button", 5));
    cameraButton.addActionListener(evt -> {
      String path = Capture.capturePhoto();
      if(path != null) {
        try {
          Image image = Image.createImage(path);
          imageLabel.setIcon(image.scaledLargerRatio(placeholder.getWidth(), placeholder.getHeight()));
          imageLabel.setHidden(false);
          animateLayout(250);
        }
        catch(Exception ex) {
          Log.e(ex);
        }
      }
      d.dispose();
    });
    d.add(cameraButton);
    MultiButton browseButton = new MultiButton("Browse");
    browseButton.setIcon(FontImage.createMaterial(FontImage.MATERIAL_PHOTO_LIBRARY, "Button", 5));
    browseButton.addActionListener(evt -> {
      Display.getInstance().openGallery((event) -> {
        if(event != null && event.getSource() != null) {
          try {
            String path = (String)event.getSource();
            Image image = Image.createImage(path);
            imageLabel.setIcon(image.scaledLargerRatio(placeholder.getWidth(), placeholder.getHeight()));
            imageLabel.setHidden(false);
            animateLayout(250);
          }
          catch(Exception ex) {
            Log.e(ex);
          }
        }
      }, Display.GALLERY_IMAGE);
      d.dispose();
    });
    d.add(browseButton);
    d.showPopupDialog(imageButton);
  }

  public String convertImage(Image image) {
    return image != null ? Base64.encode(EncodedImage.createFromImage(image, true).getImageData()) : null;
  }

  public Rock addRockSync(Rock rock) {
    final String json = rock.getPropertyIndex().toJSON();
    ConnectionRequest request = new ConnectionRequest(ThisAppRocks.URL + "rock", true);
    request.setRequestBody(json);
    request.setContentType("application/json");
    request.setHttpMethod("PUT");
    NetworkManager.getInstance().addToQueueAndWait(request);
    JSONParser.setUseLongs(true);
    if(request.getResponseData() != null) {
      JSONParser p = new JSONParser();
      InputStream response = new ByteArrayInputStream(request.getResponseData());
      try {
        Map<String, Object> result = p.parseJSON(new InputStreamReader(response, "UTF-8"));
        return new Rock(result);
      }
      catch(IOException err) {
        Log.e(err);
      }
    }
    return null;
  }
}
