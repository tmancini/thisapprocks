package com.tommo.thisapprocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.MultiButton;
import com.codename1.io.AccessToken;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.social.FacebookConnect;
import com.codename1.social.Login;
import com.codename1.social.LoginCallback;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Tabs;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.URLImage;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.table.TableLayout;
import com.codename1.ui.util.Resources;
import com.codename1.util.Base64;
import com.codename1.util.CaseInsensitiveOrder;
import com.codename1.util.MathUtil;
import com.tommo.thisapprocks.data.ContactData;
import com.tommo.thisapprocks.data.FacebookData;
import com.tommo.thisapprocks.data.Rock;
import com.tommo.thisapprocks.data.User;
import com.tommo.thisapprocks.data.UserData;
import com.tommo.thisapprocks.gui.AddRockForm;
import com.tommo.thisapprocks.gui.MapForm;

public class ThisAppRocks {

  private Form current;
  private Resources theme;
  private Container myRockContainer;
  private Container rockFeedContainer;

  private String fullName;
  private String uniqueId;
  private String imageURL;
  private static String tokenPrefix;
  boolean first = true;
  private ContactData[] contacts;
  private MapForm mapForm;
  private Image rockImage;

  private TextField nameDescriptField;
  private MultiButton radiusButton;
  private final String[] radiusOptions = {"1", "5", "10", "25", "50", "100", "200"};
  public static final String URL = "http://localhost:8080/";

  public void init(Object context) {
    theme = UIManager.initFirstTheme("/native");
    rockImage = theme.getImage("rock.png");

    // Enable Toolbar on all Forms by default
    Toolbar.setGlobalToolbar(true);

    fullName = Preferences.get("fullName", null);
    uniqueId = Preferences.get("uniqueId", null);
    imageURL = Preferences.get("imageURL", null);

    Log.bindCrashProtection(true);
  }

  public void start() {
    if(current != null) {
      current.show();
      return;
    }
    tokenPrefix = "facebook";
    Login fb = FacebookConnect.getInstance();
    fb.setClientId("237374320116602");
    fb.setRedirectURI("http://www.thomasmancini.com/");
    fb.setClientSecret("d10c76a88f467a892ed8e1aa98a5b8ee");

    doLogin(fb, new FacebookData(this), false);
  }

  public void showMainUI() {
    updateUserSync(new User(uniqueId, fullName));

    Form form = new Form("This App Rocks");

    Style s = UIManager.getInstance().getComponentStyle("SideCommand");
    FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
    EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(rockImage.getWidth(), rockImage.getHeight()), false);
    Label me = new Label(URLImage.createToStorage(placeholder, "userImage", imageURL));

    TextField displayNameField = new TextField(fullName);
    displayNameField.setEditable(false);
    displayNameField.setUIID("SideCommand");

    Container topBar = new Container(BoxLayout.y());

    FontImage editIcon = FontImage.createMaterial(FontImage.MATERIAL_EDIT, "SideCommand", 5);
    Button editButton = new Button(editIcon);

    FontImage doneIcon = FontImage.createMaterial(FontImage.MATERIAL_DONE, "SideCommand", 5);
    Button doneButton = new Button(doneIcon);
    doneButton.setHidden(true);

    editButton.addActionListener(e -> {
      editButton.setHidden(true);
      doneButton.setHidden(false);
      displayNameField.setEditable(true);
      displayNameField.setUIID("TextField");
      topBar.animateLayout(200);
      displayNameField.startEditingAsync();
    });
    doneButton.addActionListener(e -> {
      editButton.setHidden(false);
      doneButton.setHidden(true);
      displayNameField.stopEditing();
      displayNameField.setEditable(false);
      displayNameField.setUIID("SideCommand");
      topBar.animateLayout(200);
      if(!fullName.equals(displayNameField.getText())) {
        updateUserSync(new User(uniqueId, displayNameField.getText()));
        Preferences.set("fullName", displayNameField.getText());
      }
    });

    Container nameContainer = BorderLayout.center(displayNameField);
    nameContainer.add(BorderLayout.EAST, BoxLayout.encloseX(editButton, doneButton));
    nameContainer.add(BorderLayout.WEST, me);
    topBar.add(nameContainer);
    form.getToolbar().addComponentToSideMenu(topBar);

    form.setLayout(new BorderLayout());
    Tabs tabs = new Tabs();

    myRockContainer = new Container(BoxLayout.y());
    myRockContainer.setScrollableY(true);
    myRockContainer.addPullToRefresh(() -> refreshRocks(myRockContainer, uniqueId, nameDescriptField.getText()));

    mapForm = new MapForm(this);

    rockFeedContainer = new Container(BoxLayout.y());
    rockFeedContainer.setScrollableY(true);
    rockFeedContainer.addPullToRefresh(() -> refreshRocks(rockFeedContainer, "", nameDescriptField.getText(), true));

    tabs.addTab("Rock Feed", FontImage.MATERIAL_HOME, 4, rockFeedContainer);
    tabs.addTab("Rock Map", FontImage.MATERIAL_MAP, 4, mapForm);
    tabs.addTab("My Rocks", FontImage.MATERIAL_PERSON_OUTLINE, 4, myRockContainer);

    nameDescriptField = new TextField("");
    radiusButton = new MultiButton("10");
    radiusButton.addActionListener(e -> {
      Dialog d = new Dialog();
      d.setLayout(BoxLayout.y());
      d.getContentPane().setScrollableY(true);
      for(String radius : radiusOptions) {
        MultiButton mb = new MultiButton(radius);
        d.add(mb);
        mb.addActionListener(ee -> {
          radiusButton.setTextLine1(mb.getTextLine1());
          d.dispose();
          radiusButton.revalidate();
        });
      }
      d.showPopupDialog(radiusButton);
    });
    Container top = new Container(BoxLayout.y());
    top.setHidden(true);

    Button searchButton = new Button("Search");
    searchButton.addActionListener(e -> {
      refreshRocks(rockFeedContainer, "", nameDescriptField.getText(), true);
      top.setHidden(true);
      top.animateLayout(250);
    });
    Button resetButton = new Button("Reset");
    resetButton.addActionListener(e -> {
      nameDescriptField.clear();
      radiusButton.setTextLine1("10");
      refreshRocks(rockFeedContainer, "", nameDescriptField.getText(), true);
      top.setHidden(true);
      top.animateLayout(250);
    });

    top.addAll(TableLayout.encloseIn(2, new Label("Name/Descript:"), nameDescriptField, new Label("Radius (miles):"), radiusButton,
        searchButton, resetButton));

    form.add(BorderLayout.NORTH, top);
    form.add(BorderLayout.CENTER, tabs);

    refreshRocks();

    tabs.addSelectionListener((oldSelected, newSelected) -> {
      if(!top.isHidden()) {
        top.setHidden(true);
        top.animateLayout(250);
      }
    });

    form.getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_FILTER_LIST, 5, e -> {
      top.setHidden(!top.isHidden());
      top.animateLayout(250);
    });
    form.getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_ADD_CIRCLE_OUTLINE, 5,
        e -> new AddRockForm(this, mapForm, null).show());
    form.show();
  }

  public void refreshRocks() {
    Dialog dlg = new InfiniteProgress().showInifiniteBlocking();
    refreshRocks(rockFeedContainer, "", nameDescriptField.getText(), true);
    refreshRocks(myRockContainer, uniqueId, "");
    dlg.dispose();
  }

  public void refreshRocks(Container container, String userId, String filter) {
    refreshRocks(container, userId, filter, false);
  }

  public void refreshRocks(Container container, String userId, String filter, boolean addToMap) {
    Coord currentLocationCoord = null;
    if(addToMap) {
      Location location = getCurrentLocation("Getting current location...");
      if(location != null) {
        currentLocationCoord = new Coord(location.getLatitude(), location.getLongitude());
      }
      mapForm.clearAllMarkers();
    }

    container.removeAll();

    for(Rock rock : getRocksSync(userId, filter)) {
      if(addToMap) {
        if(rock.getCoord() != null
            && (currentLocationCoord == null
                || getDistance(rock.getCoord(), currentLocationCoord) <= getDouble(radiusButton.getTextLine1()))) {
          mapForm.addMarker(rock);
        }
        else {
          continue;
        }
      }
      MultiButton multiButton = new MultiButton(rock.name.get());
      multiButton.setTextLine4(rock.descript.get());
      try {
        multiButton.setIcon(
            EncodedImage.create(Base64.decode(rock.frontImage.get().getBytes())).scaled(rockImage.getWidth(), rockImage.getHeight()));
      }
      catch(Exception ex) {
        multiButton.setIcon(rockImage);
      }
      multiButton.setEmblem(FontImage.createMaterial(FontImage.MATERIAL_KEYBOARD_ARROW_RIGHT, "MultiButton", 4));
      multiButton.addActionListener(e -> new AddRockForm(this, mapForm, rock).show());
      container.add(multiButton);
    }
  }

  public void stop() {
    current = Display.getInstance().getCurrent();
    if(current instanceof Dialog) {
      ((Dialog)current).dispose();
      current = Display.getInstance().getCurrent();
    }
  }

  public void destroy() {
  }

  public Resources getTheme() {
    return theme;
  }

  public void doLogin(Login lg, UserData data, boolean forceLogin) {
    if(!forceLogin) {
      if(lg.isUserLoggedIn()) {
        showMainUI();
        return;
      }

      // if the user already logged in previously and we have a token
      String t = Preferences.get(tokenPrefix + "token", (String)null);
      if(t != null) {
        // we check the expiration of the token which we previously stored as System
        // time
        long tokenExpires = Preferences.get(tokenPrefix + "tokenExpires", (long)-1);
        if(tokenExpires < 0 || tokenExpires > System.currentTimeMillis()) {
          // we are still logged in
          showMainUI();
          return;
        }
      }
    }
    lg.setCallback(new LoginCallback() {
      @Override
      public void loginFailed(String errorMessage) {
        Dialog.show("Error Logging In", "There was an error logging in: " + errorMessage, "OK", null);
      }

      @Override
      public void loginSuccessful() {
        // when login is successful we fetch the full data
        data.fetchData(lg.getAccessToken().getToken(), () -> {
          // we store the values of result into local variables
          uniqueId = data.getId();
          fullName = data.getName();
          imageURL = data.getImage();

          // we then store the data into local cached storage so they will be around when
          // we run the app next time
          Preferences.set("fullName", Preferences.get("fullName", fullName));
          Preferences.set("uniqueId", uniqueId);
          Preferences.set("imageURL", imageURL);
          Preferences.set(tokenPrefix + "token", lg.getAccessToken().getToken());

          // token expiration is in seconds from the current time, we convert it to a
          // System.currentTimeMillis value so we can
          // reference it in the future to check expiration
          Preferences.set(tokenPrefix + "tokenExpires", tokenExpirationInMillis(lg.getAccessToken()));
          showMainUI();
        });
      }
    });
    lg.doLogin();
    if(first) { // Don't know why but this seems to need to be called twice initially
      lg.doLogin();
      first = false;
    }
  }

  /**
   * token expiration is in seconds from the current time, we convert it to a
   * System.currentTimeMillis value so we can reference it in the future to check
   * expiration
   */
  long tokenExpirationInMillis(AccessToken token) {
    String expires = token.getExpires();
    if(expires != null && expires.length() > 0) {
      try {
        // when it will expire in seconds
        long l = (long)(Float.parseFloat(expires) * 1000);
        return System.currentTimeMillis() + l;
      }
      catch(NumberFormatException err) {
        // ignore invalid input
      }
    }
    return -1;
  }

  public Rock[] getRocksSync(String userId, String filter) {
    ConnectionRequest request = new ConnectionRequest(URL + "rock", false);
    request.addArgument("userId", userId);
    request.addArgument("filter", filter);
    request.setContentType("application/json");
    NetworkManager.getInstance().addToQueueAndWait(request);
    if(request.getResponseData() != null) {
      JSONParser p = new JSONParser();
      InputStream response = new ByteArrayInputStream(request.getResponseData());
      try {
        Map<String, Object> result = p.parseJSON(new InputStreamReader(response, "UTF-8"));
        List<Map<String, Object>> resultList = (List<Map<String, Object>>)result.get("root");
        Rock[] arr = new Rock[resultList.size()];
        for(int iter = 0; iter < arr.length; iter++) {
          arr[iter] = new Rock(resultList.get(iter));
        }
        return arr;
      }
      catch(IOException err) {
        Log.e(err);
      }
    }
    return null;
  }

  public User updateUserSync(User user) {
    Dialog dlg = new InfiniteProgress().showInifiniteBlocking();
    final String json = user.getPropertyIndex().toJSON();
    ConnectionRequest request = new ConnectionRequest(ThisAppRocks.URL + "user", true);
    request.setRequestBody(json);
    request.setContentType("application/json");
    request.setHttpMethod("PUT");
    NetworkManager.getInstance().addToQueueAndWait(request);
    JSONParser.setUseLongs(true);
    dlg.dispose();
    if(request.getResponseData() != null) {
      JSONParser p = new JSONParser();
      InputStream response = new ByteArrayInputStream(request.getResponseData());
      try {
        Map<String, Object> result = p.parseJSON(new InputStreamReader(response, "UTF-8"));
        return new User(result);
      }
      catch(IOException err) {
        Log.e(err);
      }
    }
    return null;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public Location getCurrentLocation(String msg) {
    Dialog ip = new InfiniteProgress().showInifiniteBlocking();
    Location location = LocationManager.getLocationManager().getCurrentLocationSync(5000);
    ip.dispose();
    if(location == null) {
      try {
        location = LocationManager.getLocationManager().getCurrentLocation();
      }
      catch(IOException err) {
        Dialog.show("Location Error", "Unable to find your current location, please be sure that your Location Services are turned on",
            "OK", null);
      }
    }
    return location;
  }

  private double getDistance(Coord coord1, Coord coord2) {
    return getDistance(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude());
  }

  private double getDistance(double lat1, double lon1, double lat2, double lon2) {
    double theta = lon1 - lon2;
    double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
    dist = MathUtil.acos(dist);
    dist = Math.toDegrees(dist);
    dist = dist * 60 * 1.1515;
    return dist;
  }

  private double getDouble(String value) {
    try {
      return Double.valueOf(value);
    }
    catch(Exception ex) {
      return 0.0;
    }
  }

  private void loadContacts(UserData data, InfiniteProgress ip, Container contactsContainer) {
    // we sort the contacts by name which is pretty concise code thanks to Java 8
    // lambdas
    Display.getInstance().scheduleBackgroundTask(() -> {
      contacts = data.getContacts();
      CaseInsensitiveOrder co = new CaseInsensitiveOrder();
      Arrays.sort(contacts, (ContactData o1, ContactData o2) -> {
        return co.compare(o1.name, o2.name);
      });

      Display.getInstance().callSerially(() -> {
        contactsContainer.removeComponent(ip);

        for(ContactData d : contacts) {
          contactsContainer.addComponent(new MultiButton(d.name));
        }
        contactsContainer.revalidate();
      });
    });
  }
}
