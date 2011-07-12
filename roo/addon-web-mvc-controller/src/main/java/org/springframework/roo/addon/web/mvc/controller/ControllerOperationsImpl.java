package org.springframework.roo.addon.web.mvc.controller;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * Provides Controller configuration operations.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
@Component 
@Service 
public class ControllerOperationsImpl implements ControllerOperations {
	@Reference private FileManager fileManager;
	@Reference private MetadataService metadataService;
	@Reference private ProjectOperations projectOperations;
	@Reference private WebMvcOperations webMvcOperations;
	@Reference private MetadataDependencyRegistry dependencyRegistry;
	@Reference private TypeLocationService typeLocationService;
	@Reference private TypeManagementService typeManagementService;
	
	private static final JavaType ROO_WEB_SCAFFOLD = new JavaType(RooWebScaffold.class.getName());
	private static final JavaType CONTROLLER = new JavaType("org.springframework.stereotype.Controller");
	private static final JavaType REQUEST_MAPPING = new JavaType("org.springframework.web.bind.annotation.RequestMapping");
	private static final Logger log = HandlerUtils.getLogger(ControllerOperationsImpl.class);
	
	public boolean isNewControllerAvailable() {
		return projectOperations.isProjectAvailable();
	}
	
	public boolean isScaffoldAvailable() {
		return fileManager.exists(projectOperations.getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/spring/webmvc-config.xml"));
	}
	
	public void setup() {
		webMvcOperations.installAllWebMvcArtifacts();
	}

	public void generateAll(final JavaPackage javaPackage) {
		for (ClassOrInterfaceTypeDetails cid : typeLocationService.findClassesOrInterfaceDetailsWithTag(PersistenceCustomDataKeys.PERSISTENT_TYPE)) {
			if (Modifier.isAbstract(cid.getModifier())) {
				continue;
			}
			
			JavaType javaType = cid.getName();
			Path path = PhysicalTypeIdentifier.getPath(cid.getDeclaredByMetadataId());
			
			// Check to see if this entity metadata has a web scaffold metadata listening to it
			String downstreamWebScaffoldMetadataId = WebScaffoldMetadata.createIdentifier(javaType, path);
			if (dependencyRegistry.getDownstream(cid.getDeclaredByMetadataId()).contains(downstreamWebScaffoldMetadataId)) {
				// There is already a controller for this entity
				continue;
			}
			
			PluralMetadata pluralMetadata = (PluralMetadata) metadataService.get(PluralMetadata.createIdentifier(javaType, path));
			if (pluralMetadata == null) {
				continue;
			}
			
			// To get here, there is no listening controller, so add one
			JavaType controller = new JavaType(javaPackage.getFullyQualifiedPackageName() + "." + javaType.getSimpleTypeName() + "Controller");
			createAutomaticController(controller, javaType, new HashSet<String>(), pluralMetadata.getPlural().toLowerCase());
		}
	}

	public void createAutomaticController(JavaType controller, JavaType entity, Set<String> disallowedOperations, String path) {
		Assert.notNull(controller, "Controller Java Type required");
		Assert.notNull(entity, "Entity Java Type required");
		Assert.notNull(disallowedOperations, "Set of disallowed operations required");
		Assert.hasText(path, "Controller base path required");
		JavaSymbolName pathName = new JavaSymbolName("path");
		JavaSymbolName value = new JavaSymbolName("value");
		
		// Check if a controller mapping for this path exists already
		for (ClassOrInterfaceTypeDetails coitd : typeLocationService.findClassesOrInterfaceDetailsWithAnnotation(REQUEST_MAPPING)) {
			StringAttributeValue mappingAttribute = (StringAttributeValue) MemberFindingUtils.getAnnotationOfType(coitd.getAnnotations(), REQUEST_MAPPING).getAttribute(value);
			if (mappingAttribute != null) {
				String stringPath = mappingAttribute.getValue();
				if (StringUtils.hasText(stringPath) && stringPath.equalsIgnoreCase("/" + path)) {
					log.warning("Your application already contains a mapping to '" + path + "'. Please provide a different path.");
					return;
				}
			}
		}
		
		webMvcOperations.installConversionService(controller.getPackage());
		
		String resourceIdentifier = typeLocationService.getPhysicalLocationCanonicalPath(controller, Path.SRC_MAIN_JAVA);
		if (fileManager.exists(resourceIdentifier)) {
			return; // Type exists already - nothing to do
		}
		
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		
		// Create annotation @RooWebScaffold(path = "/test", formBackingObject = MyObject.class)
		List<AnnotationAttributeValue<?>> rooWebScaffoldAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		rooWebScaffoldAttributes.add(new StringAttributeValue(pathName, path));
		rooWebScaffoldAttributes.add(new ClassAttributeValue(new JavaSymbolName("formBackingObject"), entity));
		for (String operation : disallowedOperations) {
			rooWebScaffoldAttributes.add(new BooleanAttributeValue(new JavaSymbolName(operation), false));
		}
		annotations.add(new AnnotationMetadataBuilder(ROO_WEB_SCAFFOLD, rooWebScaffoldAttributes));
		
		// Create annotation @RequestMapping("/myobject/**")
		List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		requestMappingAttributes.add(new StringAttributeValue(new JavaSymbolName("value"), "/" + path));
		annotations.add(new AnnotationMetadataBuilder(REQUEST_MAPPING, requestMappingAttributes));
		
		// Create annotation @Controller
		List<AnnotationAttributeValue<?>> controllerAttributes = new ArrayList<AnnotationAttributeValue<?>>();
		annotations.add(new AnnotationMetadataBuilder(CONTROLLER, controllerAttributes));
		
		String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(controller, projectOperations.getPathResolver().getPath(resourceIdentifier));
		ClassOrInterfaceTypeDetailsBuilder typeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(declaredByMetadataId, Modifier.PUBLIC, controller, PhysicalTypeCategory.CLASS);
		typeDetailsBuilder.setAnnotations(annotations);
		
		typeManagementService.generateClassFile(typeDetailsBuilder.build());
	}
}
