package com.vidaj;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class ExecutableElementFilter implements ElementFilter {
	
	private final ElementKind kind;
	
	private final String name;
	
	private final Modifier[] modifiers;
	
	private final Integer numParameters;

	private final Set<String> parameterTypes;
	
	private final String returnType;
	
	public ExecutableElementFilter(ElementKind kind, String name, Modifier[] modifiers, Integer numParameters, String[] parameterTypes, String returnType) {
		this.kind = kind;
		this.name = name;
		this.modifiers = modifiers;
		this.numParameters = numParameters;
		this.returnType = returnType;
		
		if (parameterTypes != null) {
			this.parameterTypes = new HashSet<String>(Arrays.asList(parameterTypes));
		} else {
			this.parameterTypes = Collections.emptySet();
		}
	}

	@Override
	public boolean isValid(Element element) {
		return isValid((ExecutableElement) element);
	}
	
	protected boolean isValid(ExecutableElement e) {
		if (kind != null && !e.getKind().equals(kind)) {
			return false;
		}
		if (name != null && !e.getSimpleName().toString().equals(name)) {
			return false;
		}
		if (modifiers != null) {
			for (Modifier modifier: modifiers) {
				if (!e.getModifiers().contains(modifier)) {
					return false;
				}
			}
		}
		if (numParameters != null && e.getParameters().size() != numParameters) {
			return false;
		}
		if (!parameterTypes.isEmpty()) {
			List<? extends VariableElement> parameters = e.getParameters();
			for (VariableElement variableElement : parameters) {
				String variableType = variableElement.asType().toString();
				if (!parameterTypes.contains(variableType)) {
					return false;
				}
			}
		}
		if (returnType != null) {
			if (!e.getReturnType().toString().equals(returnType)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean accepts(Element e) {
		return e instanceof ExecutableElement;
	}
	
	public static class Builder {
		
		private String name;
		
		private ElementKind kind;
		
		private Modifier[] modifiers;

		private int numParameters;

		private String[] parameterTypes;
		
		private String returnType;
		
		public Builder() {
		}
		
		public Builder withName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder withKind(ElementKind kind) {
			this.kind = kind;
			return this;
		}
		
		public Builder withModifiers(Modifier ... modifiers) {
			this.modifiers = modifiers;
			return this;
		}
		
		public Builder withParameterCount(int count) {
			this.numParameters = count;
			return this;
		}
		
		public Builder withParameterTypes(String ... parameterTypes) {
			this.parameterTypes = parameterTypes;
			return this;
		}
		
		public Builder withReturnType(String returnType) {
			this.returnType = returnType;
			return this;
		}
		
		public ExecutableElementFilter build() {
			return new ExecutableElementFilter(kind, name, modifiers, numParameters, parameterTypes, returnType);
		}
	}

}
