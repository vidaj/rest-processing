package com.vidaj;

import static javax.tools.Diagnostic.Kind.ERROR;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;

@SupportedAnnotationTypes({ "javax.ws.rs.PathParam", 
							"javax.ws.rs.PathParam", 
							"javax.ws.rs.QueryParam",
							"javax.ws.rs.MatrixParam", 
							"javax.ws.rs.HeaderParam", 
							"javax.ws.rs.CookieParam", 
							"javax.ws.rs.FormParam" })
public class ParamTypeProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) { return false; }

		Messager messager = processingEnv.getMessager();
		
		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatatedElements = roundEnv.getElementsAnnotatedWith(annotation);
			for (Element element : annotatatedElements) {
				
				element.asType().accept(new ValidTypeVisitor(messager, annotation.toString()), element);
			}
		}
		
		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		SourceVersion[] values = SourceVersion.values();
		return values[values.length - 1];
	}
	
	private class ValidTypeVisitor extends TypeVisitorAdapter<Boolean, Element> {
		
		private final Messager messager;
		
		private final String annotation;
		
		private final Set<String> collectionTypes = new HashSet<>();
		
		{
			collectionTypes.add("java.util.List<E>");
			collectionTypes.add("java.util.Set<E>");
			collectionTypes.add("java.util.Sorted<E>");
		}
		
		public ValidTypeVisitor(Messager messager, String annotation) {
			this.messager = messager;
			this.annotation = annotation;
		}

		@Override
		public Boolean visitPrimitive(PrimitiveType t, Element p) {
			if (t.getKind() == TypeKind.CHAR){
				messager.printMessage(ERROR, "char is not allowed as target for " + annotation, p);
				return false;
			}
			return true;
		}

		@Override
		public Boolean visitDeclared(DeclaredType t, Element p) {
			Element element = t.asElement();
			if (t.toString().equals("java.lang.Character")) {
				messager.printMessage(ERROR, "java.lang.Character is not allowed as target for " + annotation, p);
				return false;
			}
			
			String elementType = element.asType().toString();
			if (collectionTypes.contains(elementType)) {
				element = getElementFromCollectionGeneric(t);
			}

			ElementFilter stringConstructorFilter = new ExecutableElementFilter.Builder()
					.withKind(ElementKind.CONSTRUCTOR)
					.withParameterCount(1)
					.withParameterTypes("java.lang.String").build();
			
			ElementFilter staticValueOfFilter = new ExecutableElementFilter.Builder()
					.withKind(ElementKind.METHOD)
					.withModifiers(Modifier.STATIC)
					.withName("valueOf")
					.withParameterCount(1)
					.withParameterTypes("java.lang.String")
					.withReturnType(element.asType().toString()).build();
			
			ElementFilter staticFromStringFilter = new ExecutableElementFilter.Builder()
					.withKind(ElementKind.METHOD)
					.withModifiers(Modifier.STATIC)
					.withName("fromString")
					.withParameterCount(1)
					.withParameterTypes("java.lang.String")
					.withReturnType(element.asType().toString()).build();

			if (!matchesOneOf(element, stringConstructorFilter, staticValueOfFilter, staticFromStringFilter)) {
				messager.printMessage(ERROR, element.asType().toString() + " cannot be instantiated by JAX-RS provider. Missing one of Constructor(String), static valueOf(String) or static fromString(String) methods. Cannot inject value from HTTP-request", p);
				return false;
			}
			
			return true;
		}
		
		private boolean matchesOneOf(Element e, ElementFilter ... filters) {
			ElementFinderVisitor filterVisitor = new ElementFinderVisitor();
			for (Element enclosedElement : e.getEnclosedElements()) {
				for (ElementFilter filter : filters) {
					if (enclosedElement.accept(filterVisitor, filter) != null) {
						return true;
					}
				}
			}
			return false;
		}

		private Element getElementFromCollectionGeneric(DeclaredType t) {
			Element element;
			element = t.getTypeArguments().get(0).accept(new TypeVisitorAdapter<Element, Void>() {

				@Override
				public Element visitDeclared(DeclaredType t, Void p) {
					return t.asElement();
				}

			}, null);
			return element;
		}
		
	}
}
