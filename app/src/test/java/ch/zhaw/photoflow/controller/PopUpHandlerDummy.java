package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PopUpHandlerDummy extends PopUpHandler{

	private Stage stage; 
	private FXMLLoader root;
	
	private String name;
	private String desc;
	private String tags;
	
	
	@FXML
	private TextField textfieldProjectName;
	
	@FXML
	private TextArea textareaProjectDescription;
	
	@FXML
	private TextArea textareaProjectTags;
	
	@FXML
	private Button buttonCreateProject;
	
	@FXML
	private Button buttonCancelProject;
	
	
	public PopUpHandlerDummy(String name, String desc, String tags)
	{		
		setName(name);
		setDesc(desc);
		setTags(tags);
	}

	
	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getDesc() {
		return desc;
	}



	public void setDesc(String desc) {
		this.desc = desc;
	}



	public String getTags() {
		return tags;
	}



	public void setTags(String tags) {
		this.tags = tags;
	}
	


	
	

}
