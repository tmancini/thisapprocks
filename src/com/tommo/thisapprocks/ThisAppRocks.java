package com.tommo.thisapprocks;

import com.codename1.io.Log;
import com.codename1.social.FacebookConnect;
import com.codename1.social.Login;
import com.codename1.social.LoginCallback;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.Tabs;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;

public class ThisAppRocks {

	private Form current;
	private Resources theme;

	public void init(Object context) {
		theme = UIManager.initFirstTheme("/theme");

		// Enable Toolbar on all Forms by default
		Toolbar.setGlobalToolbar(true);

		Log.bindCrashProtection(true);
	}

	public void start() {
		if (current != null) {
			current.show();
			return;
		}

		Login fb = FacebookConnect.getInstance();

		fb.setClientId("9999999");
		fb.setRedirectURI("http://www.youruri.com/");
		fb.setClientSecret("-------");

		// Sets a LoginCallback listener
		fb.setCallback(new LoginCallback() {
			public void loginSuccessful() {
				// we can now start fetching stuff from Facebook!
			}

			public void loginFailed(String errorMessage) {
			}
		});

		// trigger the login if not already logged in
		if (!fb.isUserLoggedIn()) {
			fb.doLogin();
		} else {
			// get the token and now you can query the Facebook API
			String token = fb.getAccessToken().getToken();
			Form form = new Form("This App Rocks");
			form.setLayout(new BorderLayout());

			Tabs tabs = new Tabs();
			tabs.addTab("Map", FontImage.createMaterial(FontImage.MATERIAL_MAP, "TabIcon", 4), new MapForm(this, form));
			tabs.addTab("Tab 2", FontImage.createMaterial(FontImage.MATERIAL_MAP, "TabIcon", 4), new Label("Tab 1"));

			form.add(BorderLayout.CENTER, tabs);
			form.show();
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

}
