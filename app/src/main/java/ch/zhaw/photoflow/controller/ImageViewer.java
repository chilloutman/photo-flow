package ch.zhaw.photoflow.controller;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.domain.Photo;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Inject;

public class ImageViewer{

	private Image img;
	
	@FXML
	private ImageView popupImage;
	
	
	@Inject
	private GuiceFXMLLoader fxmlLoader;
	
	public ImageViewer(Photo photo, FileHandler fileHandler) {
		// TODO Auto-generated constructor stub
		System.out.println("über direkt");
//		createImagePopup(photo, fileHandler);
		createCanvas(photo, fileHandler);
	}

	
	public void createCanvas(Photo photo, FileHandler fileHandler)
	{
		//load image
		try (InputStream file = fileHandler.loadPhoto(photo)) {
			img = new Image(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		Dialog<ButtonType> dialog = new Dialog<>();
		ScrollPane scroll = new ScrollPane();
		ImageView imageView = new ImageView();
		
		imageView.setImage(img);
		imageView.scaleXProperty();
		imageView.scaleYProperty();
		scroll.setContent(imageView);
		
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		
		dialog.getDialogPane().setContent(scroll);
		dialog.setResizable(true);
		dialog.setTitle("Photo Flow");
		dialog.setWidth(img.getWidth()*0.5);
		dialog.setHeight(img.getHeight()*0.5);
		
		dialog.show();
		
	}
}
