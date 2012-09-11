package com.vidaj;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

public class ProcessingUtils {

	public static AnnotationMirror getAnnotationRecursive(Element element, String annotationName) {
		if (element == null) {
			return null;
		}
		AnnotationMirror annotation = getAnnotation(element, annotationName);
		if (annotation != null) {
			return annotation;
		}

		return getAnnotationRecursive(element.getEnclosingElement(), annotationName);
	}
	
	public static AnnotationMirror getAnnotation(Element element, String annotationName) {
		for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
			if (getName(annotationMirror).equals(annotationName)) {
				return annotationMirror;
			}
		}
		return null;
	}
	
	public static Collection<AnnotationMirror> getAnnotations(Element element, Collection<String> annotationNames) {
		Collection<AnnotationMirror> foundAnnotations = new ArrayList<>();
		for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
			String annotationName = getName(annotationMirror);
			if (annotationNames.contains(annotationName)) {
				foundAnnotations.add(annotationMirror);
			}
		}
		return foundAnnotations;
	}
	
	public static String getName(AnnotationMirror annotation) {
		if (annotation == null) {
			return null;
		}
		return annotation.getAnnotationType().toString();
	}
	
	public static Collection<String> getNames(Collection<AnnotationMirror> annotations) {
		Collection<String> names = new ArrayList<>();
		for (AnnotationMirror annotation : annotations) {
			names.add(getName(annotation));
		}
		return names;
	}
	
	public static Element getParentOfKind(Element element, ElementKind ... kindArray) {
		if (element == null) {
			return null;
		}
		Set<ElementKind> kinds = new HashSet<>(asList(kindArray));
		if (kinds.contains(element.getKind())) {
			return element;
		}
		return getParentOfKind(element.getEnclosingElement(), kindArray);
	}
	
	public static Collection<AnnotationMirror> getAnnotationsAnnotatedWith(Element element, String annotationName) {
		Collection<AnnotationMirror> annotations = new ArrayList<>();
		
		for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
			List<? extends AnnotationMirror> annotatedWith = annotation.getAnnotationType().asElement().getAnnotationMirrors();
			for (AnnotationMirror innerAnnotation : annotatedWith) {
				if (getName(innerAnnotation).equals(annotationName)) {
					annotations.add(annotation);
				}
			}
		}
		
		return annotations;
	}
	
	public static AnnotationValue getAnnotationValue(AnnotationMirror annotation, String property) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
		
		for (ExecutableElement key: values.keySet()) {
			if (key.getSimpleName().toString().equals(property)) {
				return values.get(key);
			}
		}
		return null;
	}
	
	public static boolean isAnnotationAnnotatedWith(AnnotationMirror annotation, String annotationName) {
		return !getAnnotationsAnnotatedWith(annotation.getAnnotationType().asElement(), annotationName).isEmpty();
	}
}
