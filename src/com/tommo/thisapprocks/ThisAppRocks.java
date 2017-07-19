package com.tommo.thisapprocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.MultiButton;
import com.codename1.io.AccessToken;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.social.FacebookConnect;
import com.codename1.social.Login;
import com.codename1.social.LoginCallback;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.util.CaseInsensitiveOrder;
import com.tommo.thisapprocks.data.ContactData;
import com.tommo.thisapprocks.data.FacebookData;
import com.tommo.thisapprocks.data.Rock;
import com.tommo.thisapprocks.data.UserData;
import com.tommo.thisapprocks.gui.AddRockForm;
import com.tommo.thisapprocks.gui.MapForm;

public class ThisAppRocks {

	private Form current;
	private Resources theme;
	private List<Rock> rockInventory = new ArrayList<>();

	private String fullName;
	private String uniqueId;
	private String imageURL;
	private static String tokenPrefix;
	boolean first = true;
	private FacebookData facebookData;
	private ContactData[] contacts;

	public void init(Object context) {
		theme = UIManager.initFirstTheme("/theme");

		// Enable Toolbar on all Forms by default
		Toolbar.setGlobalToolbar(true);

		fullName = Preferences.get("fullName", null);
		uniqueId = Preferences.get("uniqueId", null);
		imageURL = Preferences.get("imageURL", null);

		Log.bindCrashProtection(true);
	}

	public void start() {
		if (current != null) {
			current.show();
			return;
		}
		tokenPrefix = "facebook";
		Login fb = FacebookConnect.getInstance();
		fb.setClientId("237374320116602");
		fb.setRedirectURI("http://www.thomasmancini.com/");
		fb.setClientSecret("d10c76a88f467a892ed8e1aa98a5b8ee");

		facebookData = new FacebookData(this);
		doLogin(fb, facebookData, false);
	}

	public void showMainUI() {
		Form form = new Form("My Rocks");

		Toolbar.setOnTopSideMenu(true);
		Toolbar tb = form.getToolbar();

		FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PERSON, "Label", 12);
		EncodedImage placeholder = EncodedImage.createFromImage(p, false);
		Container topBar = BorderLayout
				.center(new Label(URLImage.createToStorage(placeholder, "userPic" + uniqueId, imageURL)));
		topBar.add(BorderLayout.SOUTH, new Label(fullName, "SideCommand"));
		tb.addComponentToSideMenu(topBar);
		tb.addMaterialCommandToSideMenu("My Rocks", FontImage.MATERIAL_HOME, e -> showMainUI());
		tb.addMaterialCommandToSideMenu("Rock Map", FontImage.MATERIAL_MAP, e -> {
			Form mapFormHolder = new Form("Rock Map");
			mapFormHolder.setLayout(new BorderLayout());
			Command backCommand = new Command("",
					FontImage.createMaterial(FontImage.MATERIAL_ARROW_BACK, "Command", 4)) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					form.showBack();
				}
			};
			mapFormHolder.setBackCommand(backCommand);
			mapFormHolder.getToolbar().addCommandToLeftBar(backCommand);
			MapForm mapForm = new MapForm(this, form);
			mapFormHolder.add(BorderLayout.CENTER, mapForm);
			mapFormHolder.show();
		});

		// form.setLayout(new BorderLayout());
		// Tabs tabs = new Tabs();
		// tabs.addTab("Map", FontImage.createMaterial(FontImage.MATERIAL_MAP,
		// "TabIcon", 4), new MapForm(this, form));
		// tabs.addTab("Tab 2", FontImage.createMaterial(FontImage.MATERIAL_MAP,
		// "TabIcon", 4), new Label("Tab 1"));
		// form.add(BorderLayout.CENTER, tabs);

		form.setLayout(BoxLayout.y());
		form.getContentPane().addPullToRefresh(() -> refreshRocks(form));
		refreshRocks(form);

		form.getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_ADD_CIRCLE_OUTLINE,
				e -> new AddRockForm(this, form, null).show());
		form.show();
	}

	public void refreshRocks(Form form) {
		form.removeAll();
		// rockInventory = getRocks();
		for (Rock rock : rockInventory) {
			MultiButton multiButton = new MultiButton(rock.name.get());
			multiButton.setTextLine4(rock.descript.get());
			multiButton.setIcon(theme.getImage("rock.png"));
			multiButton.setEmblem(FontImage.createMaterial(FontImage.MATERIAL_KEYBOARD_ARROW_RIGHT, "MultiButton", 4));
			multiButton.addActionListener(e -> new AddRockForm(this, form, rock).show());
			form.add(multiButton);
		}
	}

	public void stop() {
		current = Display.getInstance().getCurrent();
		if (current instanceof Dialog) {
			((Dialog) current).dispose();
			current = Display.getInstance().getCurrent();
		}
	}

	public void destroy() {
	}

	public Resources getTheme() {
		return theme;
	}

	public void doLogin(Login lg, UserData data, boolean forceLogin) {
		if (!forceLogin) {
			if (lg.isUserLoggedIn()) {
				showMainUI();
				return;
			}

			// if the user already logged in previously and we have a token
			String t = Preferences.get(tokenPrefix + "token", (String) null);
			if (t != null) {
				// we check the expiration of the token which we previously stored as System
				// time
				long tokenExpires = Preferences.get(tokenPrefix + "tokenExpires", (long) -1);
				if (tokenExpires < 0 || tokenExpires > System.currentTimeMillis()) {
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
					Preferences.set("fullName", fullName);
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
		if (first) { // Don't know why but this seems to need to be called twice initially
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
		if (expires != null && expires.length() > 0) {
			try {
				// when it will expire in seconds
				long l = (long) (Float.parseFloat(expires) * 1000);
				return System.currentTimeMillis() + l;
			} catch (NumberFormatException err) {
				// ignore invalid input
			}
		}
		return -1;
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

				for (ContactData d : contacts) {
					contactsContainer.addComponent(new MultiButton(d.name));
				}
				contactsContainer.revalidate();
			});
		});
	}

	public void addRock(Rock rock) {
		rockInventory.add(rock);
	}

	// private List<Rock> getRocks() {
	// List<Rock> rockList = new ArrayList<>();
	// for (int i = 1; i <= 5; i++) {
	// Rock rock = new Rock();
	// rock.getPropertyIndex().populateFromMap(createRockMap(i));
	// rockList.add(rock);
	// }
	// return rockList;
	// }
	//
	// private Map<String, Object> createRockMap(int id) {
	// return new HashMap<String, Object>() {
	// {
	// put("id", id);
	// put("name", "Rock " + id);
	// put("descript", "Descript " + id);
	// put("favorite", false);
	// }
	// };
	//
	// }
}
