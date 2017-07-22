package com.tommo.thisapprocks.data;

import java.util.Map;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

public class User implements PropertyBusinessObject {

  public final Property<String, Rock> userId = new Property<>("userId");
  public final Property<String, Rock> displayName = new Property<>("displayName");
  private final PropertyIndex index = new PropertyIndex(this, "User", userId, displayName);

  public User() {
  }

  public User(String userId, String displayName) {
    this.userId.set(userId);
    this.displayName.set(displayName);
  }

  public User(Map<String, Object> userMap) {
    index.populateFromMap(userMap);
  }

  @Override
  public PropertyIndex getPropertyIndex() {
    return index;
  }

}
