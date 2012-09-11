package com.vidaj;

import static javax.tools.Diagnostic.Kind.ERROR;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes({ "javax.ws.rs.PathParam" })
public class PathParamProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) { return false; }

		for (TypeElement annotation : annotations) {
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
			for (Element element : elements) {
				Set<String> pathParts = getPathParts(element);

				AnnotationMirror pathParam = ProcessingUtils.getAnnotation(element, "javax.ws.rs.PathParam");
				AnnotationValue pathParamValue = ProcessingUtils.getAnnotationValue(pathParam, "value");
				String stringValue = pathParamValue.getValue().toString();

				if (pathParts.isEmpty()) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "No template variables present in Path",
							element, pathParam, pathParamValue);
				} else if (!pathParts.contains(stringValue)) {
					String errorMessage = "PathParam value '" + stringValue + "' is not one of "
							+ setToString(pathParts);
					processingEnv.getMessager().printMessage(Kind.ERROR, errorMessage, element, pathParam,
							pathParamValue);
				}
			}
		}

		return false;
	}

	private Set<String> getPathParts(Element element) {
		AnnotationMirror path = ProcessingUtils.getAnnotationRecursive(element, "javax.ws.rs.Path");
		AnnotationValue value = ProcessingUtils.getAnnotationValue(path, "value");

		Pattern regex = Pattern.compile("[{]([^:}\\s]*):?.*?[}]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = regex.matcher(value.getValue().toString());

		Set<String> pathParts = new HashSet<>();
		while (matcher.find()) {
			pathParts.add(matcher.group(1));
		}
		return pathParts;
	}

	private String setToString(Collection<String> strings) {
		if (strings == null || strings.size() == 0) { return "[]"; }

		StringBuilder builder = new StringBuilder();
		builder.append("[");

		Iterator<String> iterator = strings.iterator();
		builder.append("'");
		builder.append(iterator.next());
		builder.append("'");

		while (iterator.hasNext()) {
			builder.append(", ");
			builder.append("'");
			builder.append(iterator.next());
			builder.append("'");
		}

		builder.append("]");
		return builder.toString();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		SourceVersion[] values = SourceVersion.values();
		return values[values.length - 1];
	}

}
