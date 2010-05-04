package org.jasig.portlet.emailpreview.mvc;

import java.io.Serializable;

import org.apache.commons.collections.Factory;

/**
 * commons-collections Factory that creates new {@link Attribute}s
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeFactory implements Factory, Serializable {
    private static final long serialVersionUID = 1L;

    public Attribute create() {
        return new Attribute();
    }
}