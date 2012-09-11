package com.vidaj;

import static com.vidaj.ProcessingUtils.getAnnotationRecursive;
import static com.vidaj.ProcessingUtils.getAnnotations;
import static com.vidaj.ProcessingUtils.getAnnotationsAnnotatedWith;
import static com.vidaj.ProcessingUtils.getParentOfKind;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.tools.Diagnostic.Kind.ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor7;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes(value = { "javax.ws.rs.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MethodProcessor extends AbstractProcessor {

	private Messager messager;

	private final Set<String> allParameterAnnotations = new HashSet<>();

	{
		allParameterAnnotations.add("javax.ws.rs.PathParam");
		allParameterAnnotations.add("javax.ws.rs.QueryParam");
		allParameterAnnotations.add("javax.ws.rs.MatrixParam");
		allParameterAnnotations.add("javax.ws.rs.HeaderParam");
		allParameterAnnotations.add("javax.ws.rs.CookieParam");

	}

	private final Set<String> postParameterAnnotations = new HashSet<>();
	{
		postParameterAnnotations.add("javax.ws.rs.FormParam");
	}

	private final Set<String> httpMethodAnnotations = new HashSet<>();

	{
		httpMethodAnnotations.add("GET");
		httpMethodAnnotations.add("PUT");
		httpMethodAnnotations.add("POST");
		httpMethodAnnotations.add("DELETE");
		httpMethodAnnotations.add("TRACE");
		httpMethodAnnotations.add("HEAD");
	}

	private void print(String s) {
		messager.printMessage(ERROR, s);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		messager = processingEnv.getMessager();

		final Set<Element> files = getClassesAndInterfacesFromAnnotations(annotations, env);

		for (final Element file : files) {
			List<? extends Element> methods = file.getEnclosedElements();
			for (Element method : methods) {
				method.accept(new MethodVisitor(), null);
				method.accept(new PathValidator(), null);
			}
		}
		return false;
	}

	private Set<Element> getClassesAndInterfacesFromAnnotations(Set<? extends TypeElement> annotations,
			RoundEnvironment env) {
		final Set<Element> files = new HashSet<>();
		for (final TypeElement te : annotations) {
			final Set<? extends Element> elements = env.getElementsAnnotatedWith(te);

			for (final Element element : elements) {
				files.add(getParentOfKind(element, INTERFACE, CLASS));
			}
		}
		return files;
	}

	private class PathValidator extends ElementVisitorAdapter<Void> {

		@Override
		public Void visitExecutable(ExecutableElement e, Void p) {
			findPath(e);

			return null;
		}

		private void findPath(ExecutableElement e) {
			AnnotationMirror path = getAnnotationRecursive(e, "javax.ws.rs.Path");
			if (path == null) {
				messager.printMessage(ERROR, "No Path present", e);
			}
		}
	}

	private class MethodVisitor extends ElementVisitorAdapter<Void> {
		@Override
		public Void visitExecutable(ExecutableElement e, Void p) {
			String httpMethod = checkHttpMethodPresentAndReturnName(e);
			if (httpMethod == null) { return null; }

			if (!e.getModifiers().contains(Modifier.PUBLIC)) {
				messager.printMessage(Kind.WARNING, "Only public methods will be exposed", e);
			}

			List<? extends VariableElement> typeParameters = e.getParameters();

			Collection<VariableElement> entityBodies = new ArrayList<>();

			for (final VariableElement variable : typeParameters) {

				final Collection<AnnotationMirror> annotations = getAnnotations(variable, allParameterAnnotations);
				final Collection<AnnotationMirror> postAnnotations = getAnnotations(variable, postParameterAnnotations);
				if (httpMethod.equals("GET") && !postAnnotations.isEmpty()) {
					messager.printMessage(ERROR, postAnnotations.iterator().next().toString()
							+ " not allowed with HTTP-method GET");
				}
				if (annotations.size() > 1) {
					messager.printMessage(ERROR, "More than one parameter annotation", variable);
				}
				if (httpMethod.equals("GET") && annotations.size() == 0) {
					messager.printMessage(ERROR, "Missing parameter annotation", variable);
				}
				if (annotations.size() == 0) {
					entityBodies.add(variable);
				}

				if (annotations.size() == 0) {
					continue;
				}

			}

			if (!httpMethod.equals("GET") && entityBodies.size() > 1) {
				messager.printMessage(ERROR, "Only one entity-body allowed", entityBodies.iterator().next());
			}
			

			return null;
		}


		private String checkHttpMethodPresentAndReturnName(ExecutableElement e) {
			Collection<AnnotationMirror> httpMethods = getAnnotationsAnnotatedWith(e, "javax.ws.rs.HttpMethod");
			if (httpMethods.size() > 1) {
				messager.printMessage(ERROR, "More than one HTTP-method annotation present", e);
				return null;
			}

			if (httpMethods.size() == 0) {
				httpMethods = getAnnotationsAnnotatedWith(e.getEnclosingElement(), "javax.ws.rs.HttpMethod");
				if (httpMethods.size() == 0) {
					messager.printMessage(ERROR, "HTTP-method annotation expected", e);
					return null;
				}
				if (httpMethods.size() > 1) {
					messager.printMessage(ERROR, "More than one HTTP-method annotation present",
							e.getEnclosingElement());
					return null;
				}
			}

			AnnotationMirror annotation = httpMethods.iterator().next();
			AnnotationMirror httpMethod = ProcessingUtils.getAnnotation(annotation.getAnnotationType().asElement(),
					"javax.ws.rs.HttpMethod");
			AnnotationValue httpMethodValue = ProcessingUtils.getAnnotationValue(httpMethod, "value");
			return httpMethodValue.getValue().toString();
		}

	}

}
