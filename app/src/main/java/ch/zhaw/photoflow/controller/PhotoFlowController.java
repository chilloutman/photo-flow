package ch.zhaw.photoflow.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import ch.zhaw.photoflow.core.PhotoFlow;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public abstract class PhotoFlowController {
	
	@Inject
	protected PhotoFlow photoFlow;
	
	/**
	 * Shared string properties, so that multiple controllers can use the same binding.
	 */
	private static final Map<Object, ObjectProperty<Object>> OBJECT_PROPERTIES = new HashMap<>();
	
	/**
	 * Shared string properties, so that multiple controllers can use the same binding.
	 */
	private static final Map<Object, StringProperty> STRING_PROPERTIES = new HashMap<>();
	
	@VisibleForTesting
	protected PhotoFlow getPhotoFlow() {
		return photoFlow;
	}
	
	protected static ObjectProperty<Object> objectProperty(Object bean, String propertyName) {
		Object key = key(bean, propertyName);
		if (OBJECT_PROPERTIES.containsKey(key)) {
			return OBJECT_PROPERTIES.get(key);
		}
		try {
			@SuppressWarnings("unchecked")
			ObjectProperty<Object> property = JavaBeanObjectPropertyBuilder.create().bean(bean).name(propertyName).build();
			OBJECT_PROPERTIES.put(key, property);
			return property;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected static StringProperty stringProperty(Object bean, String propertyName) {
		Object key = key(bean, propertyName);
		if (STRING_PROPERTIES.containsKey(key)) {
			return STRING_PROPERTIES.get(key);
		}
		try {
			StringProperty property = JavaBeanStringPropertyBuilder.create().bean(bean).name(propertyName).build();
			STRING_PROPERTIES.put(key, property);
			return property;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected BooleanProperty booleanProperty(Object bean, String propertyName) {
		try {
			return JavaBeanBooleanPropertyBuilder.create().bean(bean).name(propertyName).build();
		}
		catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected static ReadOnlyFloatProperty numberProperty(Object bean, String property) {
		try {
			// Convert to float property so we can divide and get decimals if required.
			return ReadOnlyFloatProperty.readOnlyFloatProperty(
				JavaBeanIntegerPropertyBuilder.create().bean(bean).name(property).build()
			);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
			
	}
	
	private static Object key(Object bean, String property) {
		return Arrays.asList(bean, property);
	}
	
}
