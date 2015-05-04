package ch.zhaw.photoflow.controller;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ch.zhaw.photoflow.core.domain.Tag;

public class PopUpHandlerDummy extends PopUpHandler{

	
	private String name;
	private String desc;
	private List<Tag> tags;
	
	
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
	
	
	public PopUpHandlerDummy(String name, String desc, List<Tag> tags)
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



	public List<Tag> getTags() {
		return tags;
	}



	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	


	
	

}
