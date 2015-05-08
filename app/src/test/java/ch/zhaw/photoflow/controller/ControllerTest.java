package ch.zhaw.photoflow.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.stage.Stage;

import org.junit.Before;

import ch.zhaw.photoflow.core.PhotoFlow;

import com.cathive.fx.guice.GuiceApplication;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.sun.javafx.application.PlatformImpl;

/**
 * This is not yet working.
 * We need to somehow inject stuff into the controller for them to be testable.
 */
public abstract class ControllerTest<T> {
	
	private GuiceFXMLLoader fxmlLoader;
	
	@Before
	public void initJavaFX() throws Exception {
		// Initialize Platform (required by FX components).
		PlatformImpl.startup(() -> {
			// Nothing
		});
		
		// Initialize injector and fxml loader.
		TestApplication app = new TestApplication();
		app.init();
		fxmlLoader = app.getInjector().getInstance(GuiceFXMLLoader.class);
	}
	
	protected T loadController(URL fxml) throws IOException {
		return fxmlLoader.load(fxml).getController();
	}
	
	public static class TestApplication extends GuiceApplication {
		
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
		public void start(Stage primaryStage) {
			// No need to display anything.
		}
	}
	
}
