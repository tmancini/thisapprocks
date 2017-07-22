package com.tommo.thisapprocks.gui;

import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.FontImage;
import com.codename1.ui.Toolbar;
import com.codename1.ui.events.ActionEvent;

public class FavoriteCommand extends Command {

  private static FontImage selectedImage = FontImage.createMaterial(FontImage.MATERIAL_FAVORITE, "Button", 5);
  private static FontImage unselectedImage = FontImage.createMaterial(FontImage.MATERIAL_FAVORITE_BORDER, "Button", 5);
  private Toolbar toolbar;
  boolean isSelected;

  public FavoriteCommand(Toolbar toolbar) {
    super("", unselectedImage);
    this.toolbar = toolbar;
    isSelected = false;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
    Button b = toolbar.findCommandComponent(this);
    b.setIcon(isSelected ? selectedImage : unselectedImage);
    toolbar.animateLayout(250);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    setSelected(!isSelected);
  }

}
