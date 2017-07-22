package com.tommo.thisapprocks.data;

import java.util.Map;
import com.codename1.maps.Coord;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

public class Rock implements PropertyBusinessObject {

  public final Property<Long, Rock> id = new Property<>("id");
  public final Property<String, Rock> name = new Property<>("name");
  public final Property<String, Rock> descript = new Property<>("descript");
  public final Property<String, Rock> favorite = new Property<>("favorite");
  public final Property<String, Rock> userId = new Property<>("userId");
  public final Property<Double, Rock> latitude = new Property<>("latitude");
  public final Property<Double, Rock> longitude = new Property<>("longitude");
  public final Property<String, Rock> frontImage = new Property<>("frontImage");
  public final Property<String, Rock> backImage = new Property<>("backImage");
  public final PropertyIndex index = new PropertyIndex(this, "Rock", id, name, descript, favorite, userId, latitude, longitude, frontImage,
      backImage);

  public Rock() {
  }

  public Rock(String name, String descript, String favorite, String userId, Double latitude, Double longitude, String frontImage,
              String backImage) {
    this.name.set(name);
    this.descript.set(descript);
    this.userId.set(userId);
    this.favorite.set(favorite);
    this.latitude.set(latitude);
    this.longitude.set(longitude);
    this.frontImage.set(frontImage);
    this.backImage.set(backImage);
  }

  public Rock(Map<String, Object> rockMap) {
    index.populateFromMap(rockMap);
  }

  @Override
  public PropertyIndex getPropertyIndex() {
    return index;
  }

  public Coord getCoord() {
    return latitude.get() != 0 && longitude.get() != 0 ? new Coord(latitude.get(), longitude.get()) : null;
  }

  public String getCoordString() {
    return latitude.get() != 0 && longitude.get() != 0 ? latitude.get() + ", " + longitude.get() : "";
  }

}
