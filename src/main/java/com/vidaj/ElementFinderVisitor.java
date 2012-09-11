package com.vidaj;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor7;

public class ElementFinderVisitor extends AbstractElementVisitor7<Element, ElementFilter> {

	@Override
	public Element visitPackage(PackageElement e, ElementFilter p) {
		if (p.accepts(e) && p.isValid(e)) {
			return e;
		}
		return null;
	}

	@Override
	public Element visitType(TypeElement e, ElementFilter p) {
		if (p.accepts(e) && p.isValid(e)) {
			return e;
		}
		return null;
	}

	@Override
	public Element visitVariable(VariableElement e, ElementFilter p) {
		if (p.accepts(e) && p.isValid(e)) {
			return e;
		}
		return null;
	}

	@Override
	public Element visitExecutable(ExecutableElement e, ElementFilter p) {
		if (p.accepts(e) && p.isValid(e)) {
			return e;
		}
		return null;
	}

	@Override
	public Element visitTypeParameter(TypeParameterElement e, ElementFilter p) {
		if (p.accepts(e) && p.isValid(e)) {
			return e;
		}
		return null;
	}
	
}