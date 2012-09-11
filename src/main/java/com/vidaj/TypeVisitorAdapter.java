package com.vidaj;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor7;

public class TypeVisitorAdapter<R, P> extends AbstractTypeVisitor7<R, P>{

	@Override
	public R visitPrimitive(PrimitiveType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitNull(NullType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitArray(ArrayType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitDeclared(DeclaredType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitError(ErrorType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitTypeVariable(TypeVariable t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitWildcard(WildcardType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitExecutable(ExecutableType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitNoType(NoType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public R visitUnion(UnionType t, P p) {
		// TODO Auto-generated method stub
		return null;
	}

}
