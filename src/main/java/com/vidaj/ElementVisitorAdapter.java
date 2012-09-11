package com.vidaj;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor7;

public class ElementVisitorAdapter<R> extends AbstractElementVisitor7<R, R>{

	@Override
	public R visitPackage(PackageElement e, R p) {
		// TODO Auto-generated method stub
		return p;
	}

	@Override
	public R visitType(TypeElement e, R p) {
		// TODO Auto-generated method stub
		return p;
	}

	@Override
	public R visitVariable(VariableElement e, R p) {
		// TODO Auto-generated method stub
		return p;
	}

	@Override
	public R visitExecutable(ExecutableElement e, R p) {
		// TODO Auto-generated method stub
		return p;
	}

	@Override
	public R visitTypeParameter(TypeParameterElement e, R p) {
		// TODO Auto-generated method stub
		return p;
	}

}
