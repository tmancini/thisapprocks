package com.tommo.thisapprocks.gui;

import com.codename1.ui.Button;
import com.codename1.ui.FontImage;

public class FavoriteButton extends Button {

  private static FontImage selectedImage = FontImage.createMaterial(FontImage.MATERIAL_FAVORITE, "Button", 6);
  private static FontImage unselectedImage = FontImage.createMaterial(FontImage.MATERIAL_FAVORITE_BORDER, "Button", 6);
  boolean isSelected;

  public FavoriteButton() {
    super(unselectedImage);
    isSelected = false;
    addActionListener(e -> setSelected(!isSelected));
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
    setIcon(isSelected ? selectedImage : unselectedImage);
    if(getParent() != null) {
      getParent().animateLayout(200);
    }
  }

}
