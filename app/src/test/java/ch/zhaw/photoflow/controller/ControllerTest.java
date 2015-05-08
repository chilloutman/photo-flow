package ch.zhaw.photoflow.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import org.junit.BeforeClass;

import ch.zhaw.photoflow.PhotoFlowApplication;
import ch.zhaw.photoflow.core.PhotoFlow;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;

/**
 * This is not yet working.
 * We need to somehow inject stuff into the controller for them to be testable.
 */
public abstract class ControllerTest<T> {
	
	private static GuiceFXMLLoader fxmlLoader;
	
	@BeforeClass
	public static void initJavaFX() throws InterruptedException {
		System.out.println("initJavaFX");
		Thread t = new Thread("JavaFX Init Thread") {
			@Override
			public void run() {
				Application.launch(PhotoFlowApplication.class, new String[0]);
				//Application.launch(ControllerTest.TestApplication.class, new String[0]);
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	protected T initController(URL fxml) throws IOException {
		System.out.println("init");
		
		FXMLLoader loader = new FXMLLoader(fxml);
		loader.load();
		return loader.getController();
		
		//return fxmlLoader.load(fxml).getController();
	}
	
	public static class TestApplication extends GuiceApplication {
		@Inject
		private GuiceFXMLLoader loader;
		
		@Override
		public void init(List<Module> modules) throws Exception {
			modules.add(new Module() {
				@Override
				public void configure(Binder binder) {
					// Configure dependency injection.
					binder.bind(PhotoFlow.class).toInstance(new PhotoFlow(true));
				}
			});
		}
		
		@Override
		public void start(Stage primaryStage) throws Exception {
			System.out.println("start");
			fxmlLoader = loader;
			//ControllerTest.this.fxmlLoader = fxmlLoader;
//			Parent root = fxmlLoader.load(getClass().getResource("view/main_gui.fxml")).getRoot();
//			Scene scene = new Scene(root);
		}
	}
	
}
