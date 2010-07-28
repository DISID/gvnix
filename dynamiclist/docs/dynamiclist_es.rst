=========================================================
 gvNIX dynamiclist Documentation
=========================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    ...
:Revision:  $Rev$
:Date:      $Date$

This work is licensed under the Creative Commons Attribution-Share Alike 3.0    Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

Dynamiclist es un componente (Custom Tag) que a�ade a una aplicaci�n web la visualizaci�n de los listados 
de las diferentes entidades de la aplicaci�n.
Este listado ofrece de forma din�mica opciones de creaci�n, visualizaci�n, borrado, modificaci�n y 
otras acciones adicionales de la entidad, as� como filtrado, agrupaci�n, ordenaci�n, paginaci�n del listado mostrado.     


Requerimientos
==============
Aplicaci�n web basada en springmvc.
Seguridad desarrollada con Spring-security (se utilizan las credenciales para guardar los filtros referenciando al usuario logeado)
Acceso a datos mediante JPA


Pasos a seguir para la utilizaci�n del componente:
=================================================

La aplicaci�n se distribuye en un fichero .zip generado por la orden de maven 'mvn assembly:assembly', 
este fichero se compone de el jar de dinamiclist m�s los jar de sus dependencias, ficheros documentaci�n, 
el c�digo fuente, y los recursos no empaquetados en el .jar.  

Para una mayor comprensi�n se van a indicar los diferentes pasos que se deber�an seguir para utilizar este componente. No es necesario 
realizarlo en el orden indicado en este documento.

El componente se puede dividir en tres partes:

1) Clases java (custonTag, Controllers, Services, DataAcces ...)
	
	Estas clases est�n empaquetadas en el fichero .jar de la distribuci�n.


2) Recursos distribuidos dentro del .jar
		
	/META-INF/js
	dynamicList.tld
	
	org/gvnix/dynamiclist/dynamiclistmessages_en.properties
	org/gvnix/dynamiclist/dynamiclistmessages.properties
	
	
3) Recursos no empaquetados en el fichero .jar de la distribuci�n (directorio /resources del fichero .zip).
	Estos recursos se deben copiar en la aplicaci�n a desarrollar.
	
	resources/scripts
		Se proporcionan los scripts para la creaci�n de las tablas necesarias para el funcionamiento interno del componente dynamiclist.
		DYNAMICLIST_CONFIG.sql
		DYNAMICLIST_CONFIG_data.sql (Este script es un ejemplo para a�adir datos iniciales, no necesario)
		
		Las tablas definidas en el script son:
	
		La tabla'GLOBAL_CONFIG' debe obligatoriamente tener insertada una fila por cada tipo de entidad.
			El desarrollador debe insertar las filas de forma manual en la Base de datos.			
			Se debe especificar el nombre de la entidad en la columna ENTITY
			Se debe especificar los nombres de las propiedades visibles en ENTITY_PROPERTIES
			Se debe especificar los nombres de las propiedades no visibles en HIDDEN_PROPERTIES
			Con ORDERBY se puede opcionalmente especificar el orden fijo por defecto para todos los usuarios. 
			Con WHEREFILTER_FIX se puede opcionalmente especificar el filtro fijo para todos los usuarios.
			Y la columna INFOFILTER_FIX opcional para informaci�n como comentario.   
			
			Un ejemplo est� en el script DYNAMICLIST_CONFIG_data.sql para la entidad 'client'
		
		La tabla 'GLOBAL_FILTER' opcionalmente se puede especificar filtros globales para una determinada 
		entidad que ser�n accesibles desde un 'select' de filtros. 
		El desarrollador debe insertar las filas de forma manual en la Base de datos. 
		
		La tabla 'USER_CONFIG' utilizada por la aplicaci�n para guardar el estado(order, filter, columns, group) 
		del usuario logeado. Tabla gestionada por dynamiclist, no es necesario insertar datos de forma manual.
				
		La tabla 'USER_FILTER'  filtros del usuario logeado para una determinada entidad que ser�n accesibles 
		desde el 'select' de filtros. Tabla gestionada por dynamiclist, no es necesario insertar datos de forma manual.
		
	resources/webapp/images
		Las im�genes utilizadas por defecto deben ir en el despliegue en el directorio /images
		Es posible cambiarlo si se le pasa por par�metro al declarar el Tag en la jsp.  
	
	resources/webapp/views/popups
		Los ficheros jsp�s deben ubicarse en la ruta /WEB-INF/jsp/dynamiclist de la applicaci�n web

	resources/webapp/css
		El fichero 'estilos.css' debe ser copiado en la aplicaci�n a desarrollar en directorio de despliege '/css/estilos.css'
		
		Ejemplo de l�nea que debe ser a�adida en los jsp�s que utilicen el componente:		
		<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />



		
Ficheros de la aplicaci�n web que deben ser configurados:

	web.xml:
		Se debe a�adir un servlet de spring para recuperar los recursos de los .jar (dynamiclis.js)
		<servlet>
			<servlet-name>Resource Servlet</servlet-name>
			<servlet-class>org.springframework.js.resource.ResourceServlet</servlet-class>
		</servlet>	
		<servlet-mapping>
			<servlet-name>Resource Servlet</servlet-name>
			<url-pattern>/resources/*</url-pattern>
		</servlet-mapping>
		
		
	ficheros.jsp:
		En los ficheros que se desee utilizar los custonTag se debe a�adir:

		//para a�adir la referencia al tld de dynamiclist
		<%@ taglib prefix="dynamiclist" uri="dynamiclist" %>
	
		//Para a�adir el .js del c�digo javascript.
		<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>
	
	
		A�adir los elementos del Custom Tag a la jsp de b�squeda:
			Par�metros obligatorios:		
				par�metro url_base: Se debe especificar la url base para que el resolutor de vistas pueda direccionar al jsp correcto 
				par�metro classObject: string utilizado como base para la b�squeda en los ficheros de internacionalizaci�n
				par�metro list: collection de elementos gen�ricos obtenido del request.
			Par�metros importantes:
				par�metro pk: nombre de la clave primaria. Por defecto 'id' ('tableTag')				
		
			Ejemplo de utilizaci�n de los Tags de dynamiclist para el listado del mantenimiento de clientes:					

				<dynamiclist:buttonsTag url_base="client" classObject="client">
 					<dynamiclist:actionsTag /> 	
 					<dynamiclist:headerTableTag>
 						<dynamiclist:tableTag list="${clients}"/>
 					</dynamiclist:headerTableTag>
 					<dynamiclist:footerTableTag/> 	
				</dynamiclist:buttonsTag>
	
	

	Configuraci�n del contexto de spring:

		- Para la parte correspondiente a la capa web:
	
			<context:component-scan base-package="org.gvnix.dynamiclist.web" use-default-filters="false">
				<context:include-filter expression="org.springframework.stereotype.Controller" type="annotation"/>
			</context:component-scan>
	
			<mvc:annotation-driven />
			
			En el resolutor de fuente de mensajes se deben a�adir los proporcionados en dynamiclist: 
			
			Por ejemplo si el basename de la aplicaci�n es "messages" se debe a�adir el basename "dynamiclistmessages"
				<bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource" >
					<property name="basenames">
						<list>
							<value>messages</value>
							<value>org.gvnix.dynamiclist.dynamiclistmessages</value>												
						</list>
					</property>
				</bean>	
			
		
		- Para la parte correspondiente a la declaraci�n de beans:
			<context:component-scan base-package="org.gvnix.dynamiclist.service" />
			<context:component-scan base-package="org.gvnix.dynamiclist.jpa.dao" />
	
	
	En los controladores creados de la aplicaci�n para poder utilizar la estructura de mapeo realizada en dynamiclist se debe incluir:
	
		- Incluir instancia del DynamiclistService utilizado para recuperar los metadatos de las entidades (como por ejemplo el nombre de sus atributos).
	
			@Autowired
    		protected DynamiclistService dynamiclistService = null;
    	
    	- Incluir las siguientes anotaciones en las funciones Crear, modificar, mostrar, eliminar y buscar:
	    	(Ejemplo tomado de un Controller para entidad Client, sustituir por la entidad a mostrar en dynamiclist)
    	
    		//crear
    		@RequestMapping(value="/client/form", method=RequestMethod.POST)
    		public void form(Client client, Model model) {
	    	
    		//modificar
    		@RequestMapping(value = "/client/{id}/form", method = RequestMethod.GET)
    		public String updateForm(@PathVariable("id") Integer id, ModelMap modelMap) { ... }
    	
    		//mostrar
    		@RequestMapping(value = "/client/{id}/show", method = RequestMethod.GET)
    		public String show(@PathVariable("id") Integer id, ModelMap modelMap) { ... }
    	
    		//borrar
    		@RequestMapping(value = "/client/{id}/delete", method = RequestMethod.GET)
    		public String show(@PathVariable("id") Integer id, @RequestParam(value = "page", required = false) Integer page, 
    		@RequestParam(value = "size", required = false) Integer size) { ... }
    	    	
    		//buscar
    		@RequestMapping(value="/client/search", method=RequestMethod.GET)
    		public String search(
	    		@RequestParam(value = "page", required = false) Integer page, 
	    		@RequestParam(value = "size", required = false) Integer size, 
	    		@RequestParam(value = "groupBy", required = false) String groupBy,
	    		@RequestParam(value = "orderBy", required = false) String orderBy,
	    		@RequestParam(value = "orderByColumn", required = false) String orderByColumn,
	    		HttpServletRequest request,
	    		Model modelMap) {
    	
    			try {
    				dynamiclistService.search(Client.class, page, size, groupBy, orderBy, orderByColumn, request, modelMap);    			
    			} catch (DynamiclistException e) { ... }
    		}
	
			La funci�n 'search' el par�metro 'size' por defecto es de 10 elementos (es decir el n�mero de filas por p�gina del listado), 
			si se requiere un valor distinto es posible cambiarlo en la llamada de busqueda del servicio 'dynamiclistService'.
			 

Dependencias de proyecto
------------------------

spring 3.0.3.RELEASE
spring.security 2.0.5.RELEASE
aspectj1.6.6.RELEASE
javax.persistence 1.0.0
javax.servlet 2.4.0
javax.servlet.jsp 2.0.0
javax.servlet.jsp.jstl 1.1.2
org.apache.taglibs.standard 1.1.2
org.apache.commons.lang 2.1.0
org.apache.commons.beanutils 1.8.0

