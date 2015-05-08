package ch.zhaw.photoflow.controller;

import ch.zhaw.photoflow.core.PhotoFlow;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;


public abstract class PhotoFlowController {
	
	@Inject
	protected PhotoFlow photoFlow;
	
	@VisibleForTesting
	protected PhotoFlow getPhotoFlow() {
		return photoFlow;
	}
	
	protected StringProperty stringProperty(Object bean, String property) {
		try {
			return JavaBeanStringPropertyBuilder.create().bean(bean).name(property).build();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected ReadOnlyFloatProperty numberProperty(Object bean, String property) {
		try {
			// Convert to float property so we can divide and get decimals if required.
			return ReadOnlyFloatProperty.readOnlyFloatProperty(
				JavaBeanIntegerPropertyBuilder.create().bean(bean).name(property).build()
			);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
			
	}
	
}
