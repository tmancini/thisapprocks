package com.tommo.thisapprocks.data;

import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

public class Rock implements PropertyBusinessObject {

	private static int rockId = 1;

	public final Property<Integer, Rock> id = new Property<>("id");
	public final Property<String, Rock> name = new Property<>("name");
	public final Property<String, Rock> descript = new Property<>("descript");
	public final Property<Boolean, Rock> favorite = new Property<>("favorite");
	public final PropertyIndex index = new PropertyIndex(this, "Rock", id, name, descript, favorite);

	public Rock(String name, String descript, boolean favorite) {
		this.id.set(rockId++);
		this.name.set(name);
		this.descript.set(descript);
		this.favorite.set(favorite);
	}

	@Override
	public PropertyIndex getPropertyIndex() {
		return index;
	}

}
