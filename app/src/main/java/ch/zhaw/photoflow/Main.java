package ch.zhaw.photoflow;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ch.zhaw.photoflow.controller.MainController;
import ch.zhaw.photoflow.core.PhotoFlow;

public class Main extends Application {
	
	public static final PhotoFlow photoFlow = new PhotoFlow();


	public static void main(String[] args) {
		launch(Main.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//View
		try {
			//main View
			 FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view/main_gui.fxml"));
			    fxmlLoader.setController(new MainController(photoFlow.getProjectDao()));
			   // fxmlLoader.setRoot(this);
			    Parent root = (Parent)fxmlLoader.load();

			    
				Scene scene = new Scene(root);
				primaryStage.setScene(scene);
				URL imagepath = getClass().getResource("app_icon_32.png");
				Image image = new Image(imagepath.toString());
				primaryStage.getIcons().add(image);
				primaryStage.setTitle("Photo Flow");
				
				primaryStage.show();	
			} catch (Exception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("I failed :-(");
			}	
		}
}
