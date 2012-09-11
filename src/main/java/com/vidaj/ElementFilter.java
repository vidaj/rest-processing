package com.vidaj;

import javax.lang.model.element.Element;

public interface ElementFilter {

	public boolean isValid(Element element);
	
	public boolean accepts(Element e);
}
