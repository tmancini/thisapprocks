package com.tommo.thisapprocks.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.social.FacebookConnect;
import com.tommo.thisapprocks.ThisAppRocks;

public class FacebookData implements UserData {
	String name;
	String id;
	String token;
	ThisAppRocks app;

	public FacebookData(ThisAppRocks app) {
		this.app = app;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getImage() {
		return "http://graph.facebook.com/v2.4/" + id + "/picture";
	}

	@Override
	public void fetchData(String token, Runnable callback) {
		this.token = token;
		ConnectionRequest req = new ConnectionRequest() {
			@Override
			protected void readResponse(InputStream input) throws IOException {
				JSONParser parser = new JSONParser();
				Map<String, Object> parsed = parser.parseJSON(new InputStreamReader(input, "UTF-8"));
				name = (String) parsed.get("name");
				id = (String) parsed.get("id");
			}

			@Override
			protected void postResponse() {
				callback.run();
			}

			@Override
			protected void handleErrorResponseCode(int code, String message) {
				// access token not valid anymore
				if (code >= 400 && code <= 410) {
					app.doLogin(FacebookConnect.getInstance(), FacebookData.this, true);
					return;
				}
				super.handleErrorResponseCode(code, message);
			}
		};
		req.setPost(false);
		req.setUrl("https://graph.facebook.com/v2.4/me");
		req.addArgumentNoEncoding("access_token", token);
		NetworkManager.getInstance().addToQueue(req);
	}

	@Override
	public ContactData[] getContacts() {
		ArrayList<ContactData> dat = new ArrayList<>();
		ConnectionRequest req = new ConnectionRequest() {
			@Override
			protected void readResponse(InputStream input) throws IOException {
				JSONParser parser = new JSONParser();
				Map<String, Object> parsed = parser.parseJSON(new InputStreamReader(input, "UTF-8"));
				// name = (String) parsed.get("name");
				java.util.List<Object> data = (java.util.List<Object>) parsed.get("data");
				for (Object current : data) {
					Map<String, Object> cMap = (Map<String, Object>) current;
					String name = (String) cMap.get("name");
					if (name == null) {
						continue;
					}
					String id = cMap.get("id").toString();
					ContactData cd = new ContactData();
					cd.name = name;
					cd.uniqueId = id;
					cd.imageUrl = "http://graph.facebook.com/v2.4/" + id + "/picture";
					dat.add(cd);
				}
			}

			@Override
			protected void handleErrorResponseCode(int code, String message) {
				// access token not valid anymore
				if (code >= 400 && code <= 410) {
					app.doLogin(FacebookConnect.getInstance(), FacebookData.this, true);
					return;
				}
				super.handleErrorResponseCode(code, message);
			}
		};
		req.setPost(false);
		req.setUrl("https://graph.facebook.com/v2.4/me/friends");
		if (token == null) {
			token = Preferences.get("facebooktoken", (String) null);
		}
		req.addArgumentNoEncoding("access_token", token);
		NetworkManager.getInstance().addToQueueAndWait(req);

		ContactData[] cd = new ContactData[dat.size()];
		dat.toArray(cd);
		return cd;
	}
}