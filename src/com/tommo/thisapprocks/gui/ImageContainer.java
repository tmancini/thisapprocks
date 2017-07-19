package com.tommo.thisapprocks.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.codename1.capture.Capture;
import com.codename1.charts.util.ColorUtil;
import com.codename1.components.FloatingActionButton;
import com.codename1.components.ImageViewer;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.util.ImageIO;

public class ImageContainer extends Container {
	private ImageViewer imageViewer;
	private Label statusLabel;
	private Button removeButton;
	private List<String> imagePaths;
	private FloatingActionButton cameraFab;
	private FloatingActionButton browseFab;
	public final static int WIDTH = 1024;
	public final static int HEIGHT = 768;

	public ImageContainer() {
		super(new BorderLayout());

		imagePaths = new ArrayList<>();

		Container imageContainer = new Container(new BorderLayout());
		imageViewer = new ImageViewer();
		imageViewer.setSwipeThreshold(0.2f);
		statusLabel = new Label("0 of 0");
		removeButton = new Button("");
		removeButton.setUIID("Container");
		removeButton.getStyle().setFgColor(ColorUtil.rgb(255, 0, 0));
		FontImage.setMaterialIcon(removeButton, FontImage.MATERIAL_DELETE, 6);
		removeButton.setVisible(false);
		removeButton.addActionListener(event -> {
			int selectedIndex = imageViewer.getImageList().getSelectedIndex();
			if (selectedIndex > -1) {
				imageViewer.getImageList().removeItem(selectedIndex);
				imagePaths.remove(selectedIndex);
				if (imageViewer.getImageList().getSize() == 0) {
					statusLabel.setText("0 of 0");
					imageViewer.setImageNoReposition(null);
					imageViewer.initComponent();
					removeButton.setVisible(false);
					cameraFab.setText("Camera");
					browseFab.setText("Browse");
				}
			}
		});

		imageContainer.add(BorderLayout.CENTER, imageViewer);
		imageContainer.add(BorderLayout.SOUTH, FlowLayout.encloseCenter(statusLabel));

		FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ADD_TO_PHOTOS);
		cameraFab = fab.createSubFAB(FontImage.MATERIAL_ADD_A_PHOTO, "Camera");
		cameraFab.addActionListener(event -> {
			if (imageViewer.getImageList() == null || imageViewer.getImageList().getSize() < 5) {
				String filePath = Capture.capturePhoto();
				if (filePath != null) {
					addImageToImageViewer(filePath);
				}
			} else {
				Dialog.show("Max Images", "Only 5 pictures are allowed.", Dialog.TYPE_INFO, null, "OK", null);
			}
		});
		browseFab = fab.createSubFAB(FontImage.MATERIAL_PHOTO_LIBRARY, "Browse");
		browseFab.addActionListener(event -> {
			if (imageViewer.getImageList() == null || imageViewer.getImageList().getSize() < 5) {
				Display.getInstance().openGallery((e) -> {
					if (e != null && e.getSource() != null) {
						String filePath = (String) e.getSource();
						addImageToImageViewer(filePath);
					}
				}, Display.GALLERY_IMAGE);
			} else {
				Dialog.show("Max Images", "Only 5 pictures are allowed.", Dialog.TYPE_INFO, null, "OK", null);
			}
		});

		add(BorderLayout.CENTER,
				LayeredLayout.encloseIn(fab.bindFabToContainer(imageContainer), FlowLayout.encloseRight(removeButton)));
	}

	public void addImageToImageViewer(String filePath) {
		try {
			DefaultListModel<Image> m = (DefaultListModel<Image>) imageViewer.getImageList();
			Image image = Image.createImage(filePath);
			Image scaledImage = null;
			if (image.getWidth() > WIDTH || image.getHeight() > WIDTH) {
				scaledImage = image.getWidth() > image.getHeight() ? image.scaledLargerRatio(WIDTH, HEIGHT)
						: image.scaledLargerRatio(HEIGHT, WIDTH);
			} else {
				scaledImage = image;
			}
			if (m == null) {
				m = new DefaultListModel<>(scaledImage);
				imageViewer.setImageList(m);
				imageViewer.setImage(scaledImage);
				m.addSelectionListener((oldSelected, newSelected) -> statusLabel
						.setText(newSelected + 1 + " of " + imageViewer.getImageList().getSize()));
			} else {
				m.addItem(scaledImage);
			}

			String path = filePath.substring(0, filePath.lastIndexOf(".")) + imageViewer.getImageList().getSize()
					+ filePath.substring(filePath.lastIndexOf("."));
			OutputStream os = FileSystemStorage.getInstance().openOutputStream(path);
			ImageIO.getImageIO().save(scaledImage, os, ImageIO.FORMAT_JPEG, .5f);
			Util.cleanup(os);

			Log.p("image filePath: " + path);
			imagePaths.add(path);
			m.setSelectedIndex(m.getSize() - 1);
			removeButton.setVisible(true);
			cameraFab.setText("");
			browseFab.setText("");
		} catch (IOException err) {
			Log.e(err);
		}
	}

	public List<String> getImagePaths() {
		return imagePaths;
	}
}
