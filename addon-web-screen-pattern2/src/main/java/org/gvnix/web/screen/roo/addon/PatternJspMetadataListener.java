/*
 * Copyright 2011 DiSiD Technologies S.L.L. All rights reserved.
 *
 * Project  : DiSiD org.gvnix.web.screen.roo.addon2
 * SVN Id   : $Id$
 */
package org.gvnix.web.screen.roo.addon;

import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.customdata.PersistenceCustomDataKeys;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Component(immediate = true)
@Service
public class PatternJspMetadataListener implements MetadataProvider,
        MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;
    @Reference
    private MetadataService metadataService;
    @Reference
    private WebMetadataService webMetadataService;
    @Reference
    private FileManager fileManager;
    @Reference
    private TilesOperations tilesOperations;
    @Reference
    private MenuOperations menuOperations;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private PropFileOperations propFileOperations;

    private ComponentContext context;
    private WebScaffoldMetadata webScaffoldMetadata;
    private JavaType formbackingType;
    private String entityName;
    private Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes;
    private JavaTypePersistenceMetadataDetails formbackingTypePersistenceMetadata;
    private JavaTypeMetadataDetails formbackingTypeMetadata;
    private List<FieldMetadata> eligibleFields;
    private WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PatternMetadata.getMetadataIdentiferType(), getProvidesType());
        this.context = context;
    }

    public MetadataItem get(String metadataIdentificationString) {
        JavaType javaType = PatternJspMetadata
                .getJavaType(metadataIdentificationString);
        Path path = PatternJspMetadata.getPath(metadataIdentificationString);
        String patternMetadataKey = PatternMetadata.createIdentifier(javaType,
                path);
        PatternMetadata patternMetadata = (PatternMetadata) metadataService
                .get(patternMetadataKey);

        if (patternMetadata == null || !patternMetadata.isValid()) {
            return null;
        }

        webScaffoldMetadata = patternMetadata.getWebScaffoldMetadata();
        Assert.notNull(webScaffoldMetadata, "Web scaffold metadata required");

        webScaffoldAnnotationValues = webScaffoldMetadata.getAnnotationValues();
        Assert.notNull(webScaffoldAnnotationValues,
                "Web scaffold annotation values required");

        formbackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        Assert.notNull(formbackingType, "formbackingType required");
        entityName = uncapitalize(formbackingType.getSimpleTypeName());

        MemberDetails memberDetails = webMetadataService
                .getMemberDetails(formbackingType);
        JavaTypeMetadataDetails formBackingTypeMetadataDetails = webMetadataService
                .getJavaTypeMetadataDetails(formbackingType, memberDetails,
                        metadataIdentificationString);
        Assert.notNull(
                formBackingTypeMetadataDetails,
                "Unable to obtain metadata for type "
                        + formbackingType.getFullyQualifiedTypeName());

        eligibleFields = webMetadataService.getScaffoldEligibleFieldMetadata(
                formbackingType, memberDetails, metadataIdentificationString);

        if (eligibleFields.size() == 0) {
            return null;
        }

        relatedDomainTypes = webMetadataService
                .getRelatedApplicationTypeMetadata(formbackingType,
                        memberDetails, metadataIdentificationString);
        Assert.notNull(relatedDomainTypes, "Related domain types required");

        formbackingTypeMetadata = relatedDomainTypes.get(formbackingType);
        Assert.notNull(formbackingTypeMetadata,
                "Form backing type metadata required");

        formbackingTypePersistenceMetadata = formbackingTypeMetadata
                .getPersistenceDetails();
        if (formbackingTypePersistenceMetadata == null) {
            return null;
        }

        for (String definedPattern : patternMetadata.getDefinedPatterns()) {
            installMvcArtifacts(definedPattern);
        }

        return new PatternJspMetadata(metadataIdentificationString,
                patternMetadata);
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Assert.isTrue(
                    MetadataIdentificationUtils.getMetadataClass(
                            upstreamDependency).equals(
                            MetadataIdentificationUtils
                                    .getMetadataClass(PatternMetadata
                                            .getMetadataIdentiferType())),
                    "Expected class-level notifications only for gvNIX Report metadata (not '"
                            + upstreamDependency + "')");

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            JavaType javaType = PatternMetadata.getJavaType(upstreamDependency);
            Path path = PatternMetadata.getPath(upstreamDependency);
            downstreamDependency = PatternJspMetadata.createIdentifier(
                    javaType, path);

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                    .contains(downstreamDependency)) {
                return;
            }
        }

        // We should now have an instance-specific "downstream dependency" that
        // can be processed by this class
        Assert.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        /*
         * metadataService.evict(downstreamDependency); if
         * (get(downstreamDependency) != null) {
         * metadataDependencyRegistry.notifyDownstream(downstreamDependency); }
         */
        metadataService.get(downstreamDependency, true);

    }

    private void installMvcArtifacts(String pattern) {
        /*
         * TODO: Check if Screen Patterns artifacts exist in project and copy
         * them if they are not
         */
        installPatternArtifacts();

        String[] patternNameType = pattern.split("=");

        PathResolver pathResolver = projectOperations.getPathResolver();
        String controllerPath = webScaffoldMetadata.getAnnotationValues()
                .getPath();
        Assert.notNull(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + webScaffoldMetadata.getAnnotationValues()
                                .getGovernorTypeDetails().getName() + "'");
        if (controllerPath.startsWith("/")) {
            controllerPath = controllerPath.substring(1);
        }

        // Make the holding directory for this controller
        String destinationDirectory = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/views/" + controllerPath);
        if (!fileManager.exists(destinationDirectory)) {
            fileManager.createDirectory(destinationDirectory);
        } else {
            File file = new File(destinationDirectory);
            Assert.isTrue(file.isDirectory(), destinationDirectory
                    + " is a file, when a directory was expected");
        }

        /*
         * TODO: next test may be replaced by a test over allow or not create
         * operation of the entity
         */
        // JspViewManager viewManager = new JspViewManager(eligibleFields,
        // webScaffoldMetadata.getAnnotationValues(), relatedTypeMd);
        if (true) {
            String patternPath = destinationDirectory.concat("/")
                    .concat(patternNameType[0]).concat(".jspx");
            writeToDiskIfNecessary(patternPath, getUpdateDocument());
        }

    }

    private void installPatternArtifacts() {
        installStaticResource("images/pattern/enEdicion.gif");
        installStaticResource("images/pattern/plis_on.gif");
        installStaticResource("scripts/quicklinks.js");
        installStaticResource("styles/pattern.css");
        PathResolver pathResolver = projectOperations.getPathResolver();
        // copy util to tags/util
        copyDirectoryContents("tags/util/*.tagx", pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "/WEB-INF/tags/util"));
        // copy pattern to tags/pattern
        copyDirectoryContents("tags/pattern/*.tagx",
                pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                        "/WEB-INF/tags/pattern"));

        // modify load-scripts.tagx
        modifyLoadScriptsTagx();

    }

    private void modifyLoadScriptsTagx() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String loadScriptsTagx = pathResolver.getIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/tags/util/load-scripts.tagx");

        if (!fileManager.exists(loadScriptsTagx)) {
            // load-scripts.tagx doesn't exist, so nothing to do
            return;
        }

        InputStream loadScriptsIs = fileManager.getInputStream(loadScriptsTagx);

        Document loadScriptsXml;
        try {
            loadScriptsXml = XmlUtils.getDocumentBuilder().parse(loadScriptsIs);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open load-scripts.tagx file", ex);
        }

        Element lsRoot = loadScriptsXml.getDocumentElement();

        Node nextSibiling;

        // spring:url elements
        Element testElement = XmlUtils.findFirstElement(
                "/root/url[@var='qljs_url']", lsRoot);
        if (testElement == null) {
            Element urlPatternCss = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/styles/pattern.css")
                    .addAttribute("var", "pattern_css_url").build();
            List<Element> springUrlElements = XmlUtils.findElements(
                    "/root/url", lsRoot);
            // Element lastSpringUrl = null;
            if (!springUrlElements.isEmpty()) {
                Element lastSpringUrl = springUrlElements.get(springUrlElements
                        .size() - 1);
                if (lastSpringUrl != null) {
                    nextSibiling = lastSpringUrl.getNextSibling()
                            .getNextSibling();
                    lsRoot.insertBefore(urlPatternCss, nextSibiling);
                }
            } else {
                // Add at the end of the document
                lsRoot.appendChild(urlPatternCss);
            }
        }

        // pattern.css stylesheet element
        testElement = XmlUtils.findFirstElement(
                "/root/link[@href='${pattern_css_url}']", lsRoot);
        if (testElement == null) {
            Element linkPatternCss = new XmlElementBuilder("link",
                    loadScriptsXml).addAttribute("rel", "stylesheet")
                    .addAttribute("type", "text/css")
                    .addAttribute("media", "screen")
                    .addAttribute("href", "${pattern_css_url}")
                    .setText("<!-- required for FF3 and Opera -->").build();
            Node linkTrundraCssNode = XmlUtils.findFirstElement(
                    "/root/link[@href='${tundra_url}']", lsRoot);
            if (linkTrundraCssNode != null) {
                nextSibiling = linkTrundraCssNode.getNextSibling()
                        .getNextSibling();
                lsRoot.insertBefore(linkPatternCss, nextSibiling);
            } else {
                // Add ass last link element
                // Element lastLink = null;
                List<Element> linkElements = XmlUtils.findElements(
                        "/root/link", lsRoot);
                if (!linkElements.isEmpty()) {
                    Element lastLink = linkElements
                            .get(linkElements.size() - 1);
                    if (lastLink != null) {
                        nextSibiling = lastLink.getNextSibling()
                                .getNextSibling();
                        lsRoot.insertBefore(linkPatternCss, nextSibiling);
                    }
                } else {
                    // Add at the end of document
                    lsRoot.appendChild(linkPatternCss);
                }
            }
        }

        // quicklinks.js script element
        testElement = XmlUtils.findFirstElement(
                "/root/script[@src='${qljs_url}']", lsRoot);
        if (testElement == null) {
            Element urlQlJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute("value", "/resources/scripts/quicklinks.js")
                    .addAttribute("var", "qljs_url").build();
            Element scriptQlJs = new XmlElementBuilder("script", loadScriptsXml)
                    .addAttribute("src", "${qljs_url}")
                    .addAttribute("type", "text/javascript")
                    .setText("<!-- required for FF3 and Opera -->").build();
            List<Element> scrtiptElements = XmlUtils.findElements(
                    "/root/script", lsRoot);
            // Element lastScript = null;
            if (!scrtiptElements.isEmpty()) {
                Element lastScript = scrtiptElements
                        .get(scrtiptElements.size() - 1);
                if (lastScript != null) {
                    nextSibiling = lastScript.getNextSibling().getNextSibling();
                    lsRoot.insertBefore(urlQlJs, nextSibiling);
                    lsRoot.insertBefore(scriptQlJs, nextSibiling);
                }
            } else {
                // Add at the end of document
                lsRoot.appendChild(urlQlJs);
                lsRoot.appendChild(scriptQlJs);
            }
        }

        writeToDiskIfNecessary(loadScriptsTagx,
                loadScriptsXml.getDocumentElement());
    }

    private void installStaticResource(String path) {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String imageFile = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                path);
        if (!fileManager.exists(imageFile)) {
            try {
                FileCopyUtils.copy(
                        TemplateUtils.getTemplate(getClass(), path),
                        fileManager.createFile(
                                pathResolver.getIdentifier(
                                        Path.SRC_MAIN_WEBAPP, path))
                                .getOutputStream());
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of resources for MVC JSP addon.",
                        e);
            }
        }
    }

    /**
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath
     *            the source path
     * @param targetDirectory
     *            the target directory
     */
    private void copyDirectoryContents(String sourceAntPath,
            String targetDirectory) {
        Assert.hasText(sourceAntPath, "Source path required");
        Assert.hasText(targetDirectory, "Target directory required");

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        String path = TemplateUtils.getTemplatePath(getClass(), sourceAntPath);
        Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(
                context.getBundleContext(), path);
        Assert.notNull(urls,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            if (!fileManager.exists(targetDirectory + fileName)) {
                try {
                    FileCopyUtils.copy(url.openStream(), fileManager
                            .createFile(targetDirectory + fileName)
                            .getOutputStream());
                } catch (IOException e) {
                    new IllegalStateException(
                            "Encountered an error during copying of resources for MVC JSP addon.",
                            e);
                }
            }
        }
    }

    private Document getUpdateDocument() {

        String controllerPath = webScaffoldAnnotationValues.getPath();
        Assert.notNull(controllerPath,
                "Path is not specified in the @RooWebScaffold annotation for '"
                        + webScaffoldAnnotationValues.getGovernorTypeDetails()
                                .getName() + "'");
        if (!controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }
        JavaTypeMetadataDetails formbackingTypeMetadata = relatedDomainTypes
                .get(formbackingType);
        Assert.notNull(formbackingTypeMetadata,
                "Form backing type metadata required");
        JavaTypePersistenceMetadataDetails formbackingTypePersistenceMetadata = formbackingTypeMetadata
                .getPersistenceDetails();
        Assert.notNull(formbackingTypePersistenceMetadata,
                "Persistence metadata required for form backing type");

        DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        Document document = builder.newDocument();

        // Add document namespaces
        Element div = (Element) document.appendChild(new XmlElementBuilder(
                "div", document)
                .addAttribute("xmlns:form",
                        "urn:jsptagdir:/WEB-INF/tags/pattern")
                .addAttribute("xmlns:field",
                        "urn:jsptagdir:/WEB-INF/tags/pattern")
                .addAttribute("xmlns:jsp", "http://java.sun.com/JSP/Page")
                .addAttribute("version", "2.0")
                .addChild(
                        new XmlElementBuilder("jsp:directive.page", document)
                                .addAttribute("contentType",
                                        "text/html;charset=UTF-8").build())
                .addChild(
                        new XmlElementBuilder("jsp:output", document)
                                .addAttribute("omit-xml-declaration", "yes")
                                .build()).build());

        // Add form update element
        Element formUpdate = new XmlElementBuilder("form:update", document)
                .addAttribute(
                        "id",
                        XmlUtils.convertId("fu:"
                                + formbackingType.getFullyQualifiedTypeName()))
                .addAttribute("modelAttribute", entityName).build();

        if (!controllerPath.toLowerCase().equals(
                formbackingType.getSimpleTypeName().toLowerCase())) {
            formUpdate.setAttribute("path", controllerPath);
        }
        if (!"id".equals(formbackingTypePersistenceMetadata
                .getIdentifierField().getFieldName().getSymbolName())) {
            formUpdate.setAttribute("idField",
                    formbackingTypePersistenceMetadata.getIdentifierField()
                            .getFieldName().getSymbolName());
        }
        if (null == formbackingTypePersistenceMetadata
                .getVersionAccessorMethod()) {
            formUpdate.setAttribute("versionField", "none");
        } else if (!"version"
                .equals(BeanInfoUtils
                        .getPropertyNameForJavaBeanMethod(formbackingTypePersistenceMetadata
                                .getVersionAccessorMethod()))) {
            String methodName = formbackingTypePersistenceMetadata
                    .getVersionAccessorMethod().getMethodName().getSymbolName();
            formUpdate.setAttribute("versionField", methodName.substring(3));
        }

        createFieldsForCreateAndUpdate(entityName, relatedDomainTypes,
                eligibleFields, document, formUpdate, false);
        formUpdate.setAttribute("z",
                XmlRoundTripUtils.calculateUniqueKeyFor(formUpdate));
        div.appendChild(formUpdate);

        return document;
    }

    private void createFieldsForCreateAndUpdate(String entityName,
            Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes,
            List<FieldMetadata> formFields, Document document, Element root,
            boolean isCreate) {
        for (FieldMetadata field : formFields) {
            String fieldName = field.getFieldName().getSymbolName();
            JavaType fieldType = field.getFieldType();
            AnnotationMetadata annotationMetadata;

            // Ignoring java.util.Map field types (see ROO-194)
            if (fieldType.equals(new JavaType(Map.class.getName()))) {
                continue;
            }
            // Fields contained in the embedded Id type have been added
            // separately to the field list
            if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.EMBEDDED_ID_FIELD)) {
                continue;
            }

            fieldType = getJavaTypeForField(field);

            JavaTypeMetadataDetails typeMetadataHolder = relatedDomainTypes
                    .get(fieldType);
            JavaTypePersistenceMetadataDetails typePersistenceMetadataHolder = null;
            if (typeMetadataHolder != null) {
                typePersistenceMetadataHolder = typeMetadataHolder
                        .getPersistenceDetails();
            }

            Element fieldElement = null;

            if (fieldType.getFullyQualifiedTypeName().equals(
                    Boolean.class.getName())
                    || fieldType.getFullyQualifiedTypeName().equals(
                            boolean.class.getName())) {
                fieldElement = document.createElement("field:checkbox");
                // Handle enum fields
            } else if (typeMetadataHolder != null
                    && typeMetadataHolder.isEnumType()) {
                fieldElement = new XmlElementBuilder("field:select", document)
                        .addAttribute(
                                "items",
                                "${"
                                        + typeMetadataHolder.getPlural()
                                                .toLowerCase() + "}")
                        .addAttribute("path", getPathForType(fieldType))
                        .build();
            } else if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.ONE_TO_MANY_FIELD)) {
                // OneToMany relationships are managed from the 'many' side of
                // the relationship, therefore we provide a link to the relevant
                // form
                // the link URL is determined as a best effort attempt following
                // Roo REST conventions, this link might be wrong if custom
                // paths are used
                // if custom paths are used the developer can adjust the path
                // attribute in the field:reference tag accordingly
                if (typePersistenceMetadataHolder != null) {
                    fieldElement = new XmlElementBuilder("field:simple",
                            document)
                            .addAttribute("messageCode",
                                    "entity_reference_not_managed")
                            .addAttribute(
                                    "messageCodeAttribute",
                                    new JavaSymbolName(fieldType
                                            .getSimpleTypeName())
                                            .getReadableSymbolName()).build();
                } else {
                    continue;
                }
            } else if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.MANY_TO_ONE_FIELD)
                    || field.getCustomData()
                            .keySet()
                            .contains(
                                    PersistenceCustomDataKeys.MANY_TO_MANY_FIELD)
                    || field.getCustomData()
                            .keySet()
                            .contains(
                                    PersistenceCustomDataKeys.ONE_TO_ONE_FIELD)) {
                JavaType referenceType = getJavaTypeForField(field);
                JavaTypeMetadataDetails referenceTypeMetadata = relatedDomainTypes
                        .get(referenceType);
                if (referenceType != null/** fix for ROO-1888 --> **/
                && referenceTypeMetadata != null
                        && referenceTypeMetadata.isApplicationType()
                        && typePersistenceMetadataHolder != null) {
                    fieldElement = new XmlElementBuilder("field:select",
                            document)
                            .addAttribute(
                                    "items",
                                    "${"
                                            + referenceTypeMetadata.getPlural()
                                                    .toLowerCase() + "}")
                            .addAttribute(
                                    "itemValue",
                                    typePersistenceMetadataHolder
                                            .getIdentifierField()
                                            .getFieldName().getSymbolName())
                            .addAttribute(
                                    "path",
                                    "/"
                                            + getPathForType(getJavaTypeForField(field)))
                            .build();

                    if (field
                            .getCustomData()
                            .keySet()
                            .contains(
                                    PersistenceCustomDataKeys.MANY_TO_MANY_FIELD)) {
                        fieldElement.setAttribute("multiple", "true");
                    }
                }
            } else if (fieldType.getFullyQualifiedTypeName().equals(
                    Date.class.getName())
                    || fieldType.getFullyQualifiedTypeName().equals(
                            Calendar.class.getName())) {
                // Only include the date picker for styles supported by Dojo
                // (SMALL & MEDIUM)
                fieldElement = new XmlElementBuilder("field:datetime", document)
                        .addAttribute(
                                "dateTimePattern",
                                "${" + entityName + "_"
                                        + fieldName.toLowerCase()
                                        + "_date_format}").build();

                if (null != MemberFindingUtils.getAnnotationOfType(field
                        .getAnnotations(), new JavaType(
                        "javax.validation.constraints.Future"))) {
                    fieldElement.setAttribute("future", "true");
                } else if (null != MemberFindingUtils.getAnnotationOfType(field
                        .getAnnotations(), new JavaType(
                        "javax.validation.constraints.Past"))) {
                    fieldElement.setAttribute("past", "true");
                }
            } else if (field.getCustomData().keySet()
                    .contains(PersistenceCustomDataKeys.LOB_FIELD)) {
                fieldElement = new XmlElementBuilder("field:textarea", document)
                        .build();
            }
            if (null != (annotationMetadata = MemberFindingUtils
                    .getAnnotationOfType(field.getAnnotations(), new JavaType(
                            "javax.validation.constraints.Size")))) {
                AnnotationAttributeValue<?> max = annotationMetadata
                        .getAttribute(new JavaSymbolName("max"));
                if (max != null) {
                    int maxValue = (Integer) max.getValue();
                    if (fieldElement == null && maxValue > 30) {
                        fieldElement = new XmlElementBuilder("field:textarea",
                                document).build();
                    }
                }
            }
            // Use a default input field if no other criteria apply
            if (fieldElement == null) {
                fieldElement = document.createElement("field:input");
            }
            addCommonAttributes(field, fieldElement);
            fieldElement.setAttribute("field", fieldName);
            fieldElement.setAttribute(
                    "id",
                    XmlUtils.convertId("c:"
                            + formbackingType.getFullyQualifiedTypeName() + "."
                            + field.getFieldName().getSymbolName()));
            fieldElement.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(fieldElement));

            root.appendChild(fieldElement);
        }
    }

    private JavaType getJavaTypeForField(FieldMetadata field) {
        if (field.getFieldType().isCommonCollectionType()) {
            // Currently there is no scaffolding available for Maps (see
            // ROO-194)
            if (field.getFieldType().equals(new JavaType(Map.class.getName()))) {
                return null;
            }
            List<JavaType> parameters = field.getFieldType().getParameters();
            if (parameters.size() == 0) {
                throw new IllegalStateException(
                        "Unable to determine the parameter type for the "
                                + field.getFieldName().getSymbolName()
                                + " field in "
                                + formbackingType.getSimpleTypeName());
            }
            return parameters.get(0);
        }
        return field.getFieldType();
    }

    private String getPathForType(JavaType type) {
        JavaTypeMetadataDetails javaTypeMetadataHolder = relatedDomainTypes
                .get(type);
        Assert.notNull(
                javaTypeMetadataHolder,
                "Unable to obtain metadata for type "
                        + type.getFullyQualifiedTypeName());
        return javaTypeMetadataHolder.getControllerPath();
    }

    private void addCommonAttributes(FieldMetadata field, Element fieldElement) {
        AnnotationMetadata annotationMetadata;
        if (field.getFieldType().equals(new JavaType(Integer.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(int.class.getName())
                || field.getFieldType().equals(
                        new JavaType(Short.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(short.class.getName())
                || field.getFieldType().equals(
                        new JavaType(Long.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(long.class.getName())
                || field.getFieldType().equals(
                        new JavaType("java.math.BigInteger"))) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_integer");
        } else if (uncapitalize(field.getFieldName().getSymbolName()).contains(
                "email")) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_email");
        } else if (field.getFieldType().equals(
                new JavaType(Double.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(double.class.getName())
                || field.getFieldType().equals(
                        new JavaType(Float.class.getName()))
                || field.getFieldType().getFullyQualifiedTypeName()
                        .equals(float.class.getName())
                || field.getFieldType().equals(
                        new JavaType("java.math.BigDecimal"))) {
            fieldElement.setAttribute("validationMessageCode",
                    "field_invalid_number");
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType("javax.validation.constraints.Min")))) {
            AnnotationAttributeValue<?> min = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (min != null) {
                fieldElement.setAttribute("min", min.getValue().toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType("javax.validation.constraints.Max")))
                && !"field:textarea".equals(fieldElement.getTagName())) {
            AnnotationAttributeValue<?> maxA = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (maxA != null) {
                fieldElement.setAttribute("max", maxA.getValue().toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.DecimalMin")))
                && !"field:textarea".equals(fieldElement.getTagName())) {
            AnnotationAttributeValue<?> decimalMin = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (decimalMin != null) {
                fieldElement.setAttribute("decimalMin", decimalMin.getValue()
                        .toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(
                                field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.DecimalMax")))) {
            AnnotationAttributeValue<?> decimalMax = annotationMetadata
                    .getAttribute(new JavaSymbolName("value"));
            if (decimalMax != null) {
                fieldElement.setAttribute("decimalMax", decimalMax.getValue()
                        .toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(field.getAnnotations(), new JavaType(
                        "javax.validation.constraints.Pattern")))) {
            AnnotationAttributeValue<?> regexp = annotationMetadata
                    .getAttribute(new JavaSymbolName("regexp"));
            if (regexp != null) {
                fieldElement.setAttribute("validationRegex", regexp.getValue()
                        .toString());
            }
        }
        if ("field:input".equals(fieldElement.getTagName())
                && null != (annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(field.getAnnotations(),
                                new JavaType(
                                        "javax.validation.constraints.Size")))) {
            AnnotationAttributeValue<?> max = annotationMetadata
                    .getAttribute(new JavaSymbolName("max"));
            if (max != null) {
                fieldElement.setAttribute("max", max.getValue().toString());
            }
            AnnotationAttributeValue<?> min = annotationMetadata
                    .getAttribute(new JavaSymbolName("min"));
            if (min != null) {
                fieldElement.setAttribute("min", min.getValue().toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(field.getAnnotations(), new JavaType(
                        "javax.validation.constraints.NotNull")))) {
            String tagName = fieldElement.getTagName();
            if (tagName.endsWith("textarea") || tagName.endsWith("input")
                    || tagName.endsWith("datetime")
                    || tagName.endsWith("textarea")
                    || tagName.endsWith("select")
                    || tagName.endsWith("reference")) {
                fieldElement.setAttribute("required", "true");
            }
        }
        if (field.getCustomData().keySet()
                .contains(PersistenceCustomDataKeys.COLUMN_FIELD)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) field
                    .getCustomData()
                    .get(PersistenceCustomDataKeys.COLUMN_FIELD);
            if (values.keySet().contains("nullable")
                    && ((Boolean) values.get("nullable")) == false) {
                fieldElement.setAttribute("required", "true");
            }
        }
        // Disable form binding for nested fields (mainly PKs)
        if (field.getFieldName().getSymbolName().contains(".")) {
            fieldElement.setAttribute("disableFormBinding", "true");
        }
    }

    /** return indicates if disk was changed (ie updated or created) */
    private void writeToDiskIfNecessary(String jspFilename, Document proposed) {
        Document original = null;
        if (fileManager.exists(jspFilename)) {
            original = XmlUtils
                    .readXml(fileManager.getInputStream(jspFilename));
            if (XmlRoundTripUtils.compareDocuments(original, proposed)) {
                XmlUtils.removeTextNodes(original);
                fileManager.createOrUpdateTextFileIfRequired(jspFilename,
                        XmlUtils.nodeToString(original), false);
            }
        } else {
            fileManager.createOrUpdateTextFileIfRequired(jspFilename,
                    XmlUtils.nodeToString(proposed), false);
        }
    }

    private boolean writeToDiskIfNecessary(String filePath, Element body) {
        // Build a string representation of the JSP
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Transformer transformer = XmlUtils.createIndentingTransformer();
        XmlUtils.writeXml(transformer, byteArrayOutputStream,
                body.getOwnerDocument());
        String viewContent = byteArrayOutputStream.toString();

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (fileManager.exists(filePath)) {
            // First verify if the file has even changed
            File f = new File(filePath);
            String existing = null;
            try {
                existing = FileCopyUtils.copyToString(new FileReader(f));
            } catch (IOException ignoreAndJustOverwriteIt) {
            }

            if (!viewContent.equals(existing)) {
                mutableFile = fileManager.updateFile(filePath);
            }
        } else {
            mutableFile = fileManager.createFile(filePath);
            Assert.notNull(mutableFile, "Could not create '" + filePath + "'");
        }

        if (mutableFile != null) {
            try {
                // We need to write the file out (it's a new file, or the
                // existing file has different contents)
                FileCopyUtils.copy(viewContent, new OutputStreamWriter(
                        mutableFile.getOutputStream()));
                // Return and indicate we wrote out the file
                return true;
            } catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }

    private String uncapitalize(String term) {
        // [ROO-1790] this is needed to adhere to the JavaBean naming
        // conventions (see JavaBean spec section 8.8)
        return Introspector.decapitalize(StringUtils.capitalize(term));
    }

    public String getProvidesType() {
        return PatternJspMetadata.getMetadataIdentiferType();
    }

}
