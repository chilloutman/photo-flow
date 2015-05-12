package ch.zhaw.photoflow;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ch.zhaw.photoflow.core.PhotoFlow;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;

/**
 * Main Photo Flow Applicaton.
 * {@link GuiceApplication} is used to configure dependency injection for the FXML controllers.
 */
public class PhotoFlowApplication extends GuiceApplication {

	@Inject
	private GuiceFXMLLoader fxmlLoader;

	public static void main(String[] args) {
		launch(PhotoFlowApplication.class, args);
	}

	@Override
	public void init(List<Module> modules) {
		modules.add(new Module() {
			
			@Override
			public void configure(Binder binder) {
				// Configure dependency injection.
				binder.bind(PhotoFlow.class).toInstance(new PhotoFlow());
			}
		});
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		System.out.println("Photo Flow");
		URL fxml = getClass().getResource("view/main.fxml");
		System.out.println("Loading main fxml: " + fxml);
		
		Parent root = fxmlLoader.load(fxml).getRoot();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Photo Flow");

		URL imagepath = getClass().getResource("app_icon_32.png");
		Image image = new Image(imagepath.toString());
		primaryStage.getIcons().add(image);
		
		primaryStage.show();
	}

}
