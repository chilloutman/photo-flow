package ch.zhaw.photoflow.controller;

import java.io.IOException;
import java.io.InputStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.domain.Photo;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Inject;

public class ImageViewer{

	private Image img;
	private double scaleFactor = 0.3;
	private ErrorHandler errorHandler = new ErrorHandler();
	
	@FXML
	private ImageView popupImage;
	
	public ImageViewer(Photo photo, FileHandler fileHandler) {
		createCanvas(photo, fileHandler);
	}

	
	public void createCanvas(Photo photo, FileHandler fileHandler)
	{
		//load image
		try (InputStream file = fileHandler.loadPhoto(photo)) {
			img = new Image(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileHandlerException e) {
			errorHandler.spawnError("Hmm, we could not load your photo. Just try again. It will work this time. We hope...");
		}
		
		Dialog<ButtonType> dialog = new Dialog<>();
		ScrollPane scroll = new ScrollPane();
		ImageView imageView = new ImageView();
		
		imageView.setImage(img);
		if(img.getWidth() <= 800 || img.getHeight() <= 800 )
		{
			scaleFactor = 0.7;
		}
		else if(img.getWidth() >= 2500 || img.getHeight() >= 2500)
		{
			scaleFactor = 0.2;
		}
		else if(img.getWidth() >= 3500 || img.getHeight() >= 3500)
		{
			scaleFactor = 0.15;
		}
		
		imageView.setFitHeight(dialog.getDialogPane().getHeight());
		imageView.setFitWidth(dialog.getDialogPane().getWidth());
		imageView.setPreserveRatio(true);
		scroll.setFitToHeight(true);
		scroll.setFitToWidth(true);
		dialog.getDialogPane().setMaxWidth(img.getWidth());
		dialog.getDialogPane().setMaxHeight(img.getHeight());
		dialog.initModality(Modality.NONE);
		
		
		imageView.setOnZoom((event) -> {
			imageView.setFitHeight(img.getHeight()*scaleFactor*event.getTotalZoomFactor());
			imageView.setFitWidth(img.getWidth()*scaleFactor*event.getTotalZoomFactor());
		});
		
		
		dialog.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldDialogWidth, Number newDialogWidth) {
		        imageView.setFitHeight(dialog.getDialogPane().getHeight());
		    }
		});
		dialog.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldDialogHeight, Number newDialogHeight) {
		        imageView.setFitWidth(dialog.getDialogPane().getWidth());
		    }
		});
		
		scroll.setOnKeyPressed((event) -> {
			if((KeyCode.ESCAPE.equals(event.getCode())))
			{
				dialog.close();
			}
		});

	
		scroll.setContent(imageView);
		scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);	
		dialog.getDialogPane().setContent(scroll);
		dialog.setResizable(true);
		dialog.setTitle("Photo Flow");
		
		//add Icon
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("../app_icon_32.png").toString()));
		
		dialog.show();
		
	}
}