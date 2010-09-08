/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.layer.roo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Addon for Handle Service Layer
 * 
 * @author Ricardo García ( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface ServiceLayerOperations {

    /**
     * Is service layer command available on Roo console ? 
     * 
     * @return Service layer command available on Roo console
     */
    boolean isProjectAvailable();

    /**
     * <p>
     * Create a Service class.
     * </p>
     * 
     * @param serviceClass
     *            class to be created.
     */
    public void createServiceClass(JavaType serviceClass);

    /**
     * <p>
     * Adds an operation to a class.
     * </p>
     * 
     * @param operationName
     *            Operation Name to be created.
     * @param returnType
     *            Operation java return Type.
     * @param className
     *            Class to insert the operation.
     */
    public void addServiceOperation(JavaSymbolName operationName,
	    JavaType returnType, JavaType className);

    /**
     * <p>
     * Adds an input parameter into selected class method.
     * </p>
     * 
     * @param className
     *            Class to update the method with the new parameter.
     * @param method
     *            Method name.
     * @param paramName
     *            Input parameter names.
     * @param paramType
     *            Input parameter Type.
     */
    public void addServiceOperationParameter(JavaType className,
	    JavaSymbolName method, String paramName, JavaType paramType);
    
}
