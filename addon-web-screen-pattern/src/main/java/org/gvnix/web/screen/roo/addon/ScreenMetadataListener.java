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
package org.gvnix.web.screen.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultItdTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.classpath.scanner.MemberDetailsScannerImpl;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.process.manager.event.ProcessManagerStatus;
import org.springframework.roo.process.manager.event.ProcessManagerStatusListener;
import org.springframework.roo.process.manager.event.ProcessManagerStatusProvider;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Listens for {@link WebScaffoldMetadata} and produces JSPs when requested by
 * that metadata.
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD
 *         Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class ScreenMetadataListener implements // MetadataProvider,
        MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private MetadataService metadataService;

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    @Reference
    private ProcessManagerStatusProvider processManagerStatus;

    @Reference
    private ScreenOperations operations;

    private StatusListener statusListener;

    private WebScaffoldMetadata webScaffoldMetadata;

    private EntityMetadata entityMetadata;

    private ComponentContext context;

    private final Set<String> upstreamDependencyList = new HashSet<String>();

    private static Logger logger = Logger
            .getLogger(ScreenMetadataListener.class.getName());

    protected void activate(ComponentContext context) {
        this.context = context;
        metadataDependencyRegistry.addNotificationListener(this);
        statusListener = new StatusListener(this);
        processManagerStatus.addProcessManagerStatusListener(statusListener);
    }

    /** {@inheritDoc} */
    public void notify(String upstreamDependency, String downstreamDependency) {

        if (!operations.isProjectAvailable()
                || !operations.isSpringMvcProject()
                || !operations.isActivated()) {
            return;
        }

        // DiSiD: Refactor to see upstreanMetadata and metadata, replace
        // JspMetadata with PhysicalTypeIdentifier and check Controller sufix
        // exists
        String upstreamMetadata = MetadataIdentificationUtils
                .getMetadataClass(upstreamDependency);
        String metadata = MetadataIdentificationUtils
                .getMetadataClass(PhysicalTypeIdentifier
                        .getMetadataIdentiferType());
        if (upstreamMetadata.equals(metadata)
                && upstreamDependency.endsWith("Controller")) {
            // Add upstreamDependency to a Stack.
            upstreamDependencyList.add(upstreamDependency);
        }

    }

    /**
     * return indicates if disk file was changed (ie updated or created)
     */
    private boolean writeToDiskIfNecessary(String jspFilename, Document proposed) {

        String controllerPath = webScaffoldMetadata.getAnnotationValues()
                .getPath();

        if (controllerPath.startsWith("/")) {
            controllerPath = controllerPath.substring(1);
        }

        String jspxPath = pathResolver
                .getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/views/"
                        + controllerPath + "/" + jspFilename + ".jspx");

        Assert.isTrue(fileManager.exists(jspxPath), jspFilename
                + ".jspx not found");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
                byteArrayOutputStream, proposed);
        String proposedString = byteArrayOutputStream.toString();

        MutableFile mutableFile = null;
        if (fileManager.exists(jspxPath)) {
            String originalString = null;

            try {
                originalString = FileCopyUtils.copyToString(new FileReader(
                        jspxPath));
            } catch (Exception e) {
                throw new IllegalStateException("Could not get the file:\t'"
                        + jspxPath + "'", e.getCause());
            }

            if (!proposedString.equals(originalString)) {
                mutableFile = fileManager.updateFile(jspxPath);
            }

            try {
                if (mutableFile != null) {

                    FileCopyUtils.copy(proposedString, new OutputStreamWriter(
                            mutableFile.getOutputStream()));
                    fileManager.scan();
                }
            } catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        return true;
    }

    /**
     * Retrieves the fields of the entity wrapped by the controller with the
     * defined annotation.
     * 
     * @param upstreamDependency
     *            JspMetadata to retrieve Entity Controller information.
     * @param annotationPath
     *            Complete Class name of the annotation. ie:
     *            "javax.persistence.OneToMany"
     * @return FieldMetada {@link List} list of entity fields with the
     *         annotation. If there aren't any fields, returns an empty
     *         ArrayList.
     */
    private List<FieldMetadata> getAnnotatedFields(String upstreamDependency,
            String annotationPath) {

        // DiSiD: Use PhysicalTypeIdentifier instead of JspMetadata
        JavaType javaType = PhysicalTypeIdentifier
                .getJavaType(upstreamDependency);
        Path webPath = PhysicalTypeIdentifier.getPath(upstreamDependency);

        String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
                javaType, webPath);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataKey);

        this.webScaffoldMetadata = webScaffoldMetadata;

        // DiSiD: set entityMetatada based on backingObject
        WebScaffoldAnnotationValues annotationValues = webScaffoldMetadata
                .getAnnotationValues();
        JavaType backingObject = annotationValues.getFormBackingObject();
        entityMetadata = (EntityMetadata) metadataService.get(EntityMetadata
                .createIdentifier(backingObject, webPath));

        // DiSiD: we need details about entity ITD
        DefaultItdTypeDetails defaultItdTypeDetails = (DefaultItdTypeDetails) entityMetadata
                .getMemberHoldingTypeDetails();

        // Retrieve the fields that are defined as OneToMany relationship.
        List<FieldMetadata> fieldMetadataList = MemberFindingUtils
                .getFieldsWithAnnotation(defaultItdTypeDetails.getGovernor(),
                        new JavaType(annotationPath));

        return fieldMetadataList;
    }

    /**
     * Returns the jspx file updated to show the relations using defined view
     * tagx.
     * 
     * @param viewName
     *            Name of the view to update.
     * @param oneToManyFieldMetadatas
     *            List of relation fields to show.
     * 
     * @param selectedDecorator
     *            Decorator element name to show realtionships.
     * 
     * @return Updated Document with the new fields.
     */
    private Document updateView(String viewName,
            List<FieldMetadata> oneToManyFieldMetadatas,
            String selectedDecorator) {

        // DiSiD: We need some info from main entity (the one with oneToMany
        // relations)
        DefaultItdTypeDetails defaultItdTypeDetails = (DefaultItdTypeDetails) entityMetadata
                .getMemberHoldingTypeDetails();
        String entityName = defaultItdTypeDetails.getGovernor().getName()
                .getSimpleTypeName();

        String controllerPath = webScaffoldMetadata.getAnnotationValues()
                .getPath();

        if (controllerPath.startsWith("/")) {
            controllerPath = controllerPath.substring(1);
        }

        String jspxPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/views/" + controllerPath + "/" + viewName + ".jspx");

        Assert.isTrue(fileManager.exists(jspxPath), viewName
                + ".jspx not found");

        Document jspxView;

        try {
            jspxView = XmlUtils.getDocumentBuilder().parse(
                    fileManager.getInputStream(jspxPath));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element documentRoot = jspxView.getDocumentElement();

        Element defaultField = null;
        // Views to create.
        Element oldGroupViews = null;
        Element groupViews = null;
        Element views = null;

        Element divView = XmlUtils.findFirstElement("/div", documentRoot);
        Assert.isTrue(divView != null, "There is no div tagx defined in "
                + viewName + ".jspx.");

        // Add the new view if doesn't exists
        oldGroupViews = XmlUtils.findFirstElement("/div/" + selectedDecorator
                + "[@id='relations']", documentRoot);

        // There aren't relationships to update or remove.
        if (oneToManyFieldMetadatas.isEmpty() && oldGroupViews == null) {
            return null;
        } else
        // It doesn't have relationships. Remove old relation views and
        // attributes.
        if (oneToManyFieldMetadatas.isEmpty() && oldGroupViews != null) {

            divView.removeAttribute("xmlns:relations");
            divView.removeAttribute("xmlns:relation");
            divView.removeAttribute("xmlns:table");

            oldGroupViews.getParentNode().removeChild(oldGroupViews);

            return jspxView;
        }

        // Create element for group views.
        // relations="urn:jsptagdir:/WEB-INF/tags/relations"
        if (oldGroupViews == null) {

            // Add tagx attributes to div bean.
            divView.setAttribute("xmlns:relations",
                    "urn:jsptagdir:/WEB-INF/tags/relations");
            divView.setAttribute("xmlns:relation",
                    "urn:jsptagdir:/WEB-INF/tags/relations/decorators");
            divView.setAttribute("xmlns:table",
                    "urn:jsptagdir:/WEB-INF/tags/form/fields");

        }

        // Create group views.
        groupViews = new XmlElementBuilder("relations:" + selectedDecorator,
                jspxView).addAttribute("id", "relations").build();

        WebScaffoldMetadata relatedWebScaffoldMetadata;

        // Show initialized table properties.
        String create = "false";
        String delete = "false";
        String update = "false";

        String z = "user-managed";

        // Add the relationship views.
        for (FieldMetadata fieldMetadata : oneToManyFieldMetadatas) {

            if (viewName.compareTo("show") == 0) {

                defaultField = XmlUtils.findFirstElement(
                        "/div/show/display[@field='"
                                + fieldMetadata.getFieldName() + "']",
                        documentRoot);
                delete = "false";
            } else if (viewName.compareTo("update") == 0) {
                defaultField = XmlUtils.findFirstElement(
                        "/div/update/simple[@field='"
                                + fieldMetadata.getFieldName() + "']",
                        documentRoot);
                delete = "true";
            }

            if ((defaultField != null)
                    && ((defaultField.getAttribute("render").compareTo("") == 0) || (defaultField
                            .getAttribute("render").compareTo("true") == 0))) {
                // Don't show the default view of relationship.
                defaultField.setAttribute("render", "false");
                defaultField.setAttribute("z", z);
            }

            // Retrieve Related Entities Metadata
            JavaType relationshipJavaType = fieldMetadata.getFieldType();
            // ignoring java.util.Map field types (see ROO-194)
            if (relationshipJavaType.getFullyQualifiedTypeName().equals(
                    Set.class.getName())) {

                Assert.isTrue(
                        relationshipJavaType.getParameters().size() == 1,
                        "A set is defined without specification of its type (via generics) - unable to create view for it");
            }

            relationshipJavaType = relationshipJavaType.getParameters().get(0);

            // TODO No WebScaffoldMetadata can be obtained error; then no
            // create, delete, update and path
            relatedWebScaffoldMetadata = getRelatedEntityWebScaffoldMetadata(relationshipJavaType);

            // Check if exists the path.
            String path;

            // If doesn't exist related controller to entity relation.
            // Don't enable table action properties.
            if (relatedWebScaffoldMetadata == null) {
                create = "false";
                delete = "false";
                update = "false";
                path = "/";
            } else {
                create = "true";
                update = "true";
                path = "/".concat(relatedWebScaffoldMetadata
                        .getAnnotationValues().getPath());
            }

            String entityFullyQualifiedTypeName = defaultItdTypeDetails
                    .getGovernor().getName().getFullyQualifiedTypeName();

            String propertyName = fieldMetadata.getFieldName().getSymbolName();

            views = new XmlElementBuilder("relation:".concat(selectedDecorator)
                    .concat("view"), jspxView)
                    .addAttribute(
                            "data",
                            "${".concat(StringUtils.uncapitalize(entityName))
                                    .concat("}"))
                    .addAttribute("id",
                    // DiSiD: Roo now uses '_' separator instead of '.' on i18n
                    // tags
                    // TODO Unify i18n tags generation method
                            "s_".concat(entityFullyQualifiedTypeName)
                                    .concat(".").concat(propertyName)
                                    .replace('.', '_'))
                    .addAttribute("field",
                            fieldMetadata.getFieldName().getSymbolName())
                    .addAttribute(
                            "fieldId",
                            "l_".concat(entityFullyQualifiedTypeName)
                                    .concat(".").concat(propertyName)
                                    .replace('.', '_'))
                    .addAttribute("messageCode", "entity_reference_not_managed")
                    .addAttribute("messageCodeAttribute", entityName)
                    .addAttribute("path", path)
                    .addAttribute("delete", delete)
                    .addAttribute("create", create)
                    .addAttribute("update", update)
                    .addAttribute("dataResult", propertyName.concat("list"))
                    .addAttribute(
                            "relatedEntitySet",
                            "${".concat(StringUtils.uncapitalize(entityName))
                                    .concat(".").concat(propertyName)
                                    .concat("}")).build();

            // Create column fields and group the elements created.
            String idEntityMetadata = physicalTypeMetadataProvider
                    .findIdentifier(relationshipJavaType);
            // DiSiD: Retrieve the PhysicalTypeMetadata of the related entity
            PhysicalTypeMetadata relatedEntityMetadata = (PhysicalTypeMetadata) metadataService
                    .get(idEntityMetadata);

            Assert.notNull(/* relatedBeanInfoMetadata */relatedEntityMetadata,
                    "There is no metadata related for this identifier:\t"
                            + idEntityMetadata);

            List<FieldMetadata> fieldMetadataList = getElegibleFields(
                    relatedEntityMetadata, relationshipJavaType);
            Element columnField;

            String relatedEntityClassName = relatedEntityMetadata
                    .getMemberHoldingTypeDetails().getName()
                    .getFullyQualifiedTypeName();

            String property;

            for (FieldMetadata relatedEntityfieldMetadata : fieldMetadataList) {

                property = relatedEntityfieldMetadata.getFieldName()
                        .getSymbolName();
                columnField = new XmlElementBuilder("table:column", jspxView)
                        .addAttribute("id",
                        // TODO DiSiD: Roo now uses '_' separator
                        // instead of '.' on i18n tags
                                "s_".concat(relatedEntityClassName.concat(".")
                                        .concat(property).replace('.', '_')))
                        .addAttribute("property", property)
                        .addAttribute("z", z).build();

                views.appendChild(columnField);
            }

            groupViews.appendChild(views);
        }

        if (oldGroupViews == null) {
            jspxView.getLastChild().appendChild(groupViews);

            // DiSiD: Overwrite relation table position to avoid invisibility
            // TODO Move inline style to a addon new CSS file
            Node style = documentRoot.getOwnerDocument().createElement("style");
            style.setTextContent("body .dijitAlignClient {position: relative;}");
            jspxView.getLastChild().appendChild(style);

        } else {
            oldGroupViews.getParentNode().replaceChild(groupViews,
                    oldGroupViews);
        }
        return jspxView;
    }

    /**
     * Retrieve the associated WebScaffoldMetada from a JavaType. <br/>
     * 
     * TODO No WebScaffoldMetadata can be obtained error<br/>
     * 
     * TODO Mirar de implementar esto usando WebScaffoldMetadataProvider.<br/>
     * En ReportMetadataProvider se hace usando este provider.
     * 
     * @param relationshipJavaType
     *            Relationship JavaType
     * @return
     */
    private WebScaffoldMetadata getRelatedEntityWebScaffoldMetadata(
            JavaType relationshipJavaType) {

        WebScaffoldMetadata relationshipMetadata = null;

        FileDetails srcRoot = new FileDetails(new File(
                pathResolver.getRoot(Path.SRC_MAIN_JAVA)), null);
        String antPath = pathResolver.getRoot(Path.SRC_MAIN_JAVA)
                + File.separatorChar + "**" + File.separatorChar + "*.java";
        SortedSet<FileDetails> entries = fileManager
                .findMatchingAntPath(antPath);

        for (FileDetails file : entries) {
            String fullPath = srcRoot.getRelativeSegment(file
                    .getCanonicalPath());
            fullPath = fullPath.substring(1, fullPath.lastIndexOf(".java"))
                    .replace(File.separatorChar, '.'); // ditch the first /
            // and .java
            JavaType javaType = new JavaType(fullPath);
            String id = physicalTypeMetadataProvider.findIdentifier(javaType);
            if (id != null) {
                PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
                        .get(id);
                // DiSiD: nothing to do if we have a non valid
                // physicalTypeMetadata
                if (ptm == null
                        || ptm.getMemberHoldingTypeDetails() == null
                        || !(ptm.getMemberHoldingTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {

                    continue;
                }

                // DiSiD: Get a reference of the physical java class
                ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm
                        .getMemberHoldingTypeDetails();

                if (Modifier.isAbstract(cid.getModifier())) {
                    continue;
                }

                if (relationshipJavaType.compareTo(ptm
                        .getMemberHoldingTypeDetails().getName()) == 0) {

                    Set<String> downstream = metadataDependencyRegistry
                            .getDownstream(ptm.getId());
                    // check to see if this entity metadata has a web
                    // scaffold metadata listening to it
                    for (String ds : downstream) {
                        if (WebScaffoldMetadata.isValid(ds)) {
                            // there is already a controller for this
                            // entity
                            String entityMetadataKey = WebScaffoldMetadata
                                    .createIdentifier(
                                            WebScaffoldMetadata.getJavaType(ds),
                                            WebScaffoldMetadata.getPath(ds));
                            relationshipMetadata = (WebScaffoldMetadata) metadataService
                                    .get(entityMetadataKey);
                            return relationshipMetadata;
                        }
                    }
                }
            }
        }
        return relationshipMetadata;
    }

    /**
     * Retrieve BeanInfoMetadata fields. <br/>
     * 
     * TODO: quizas se pueda hacer usando:
     * 
     * <pre>
     * List<\FieldMetadata\> elegibleFields = webMetadataService.getScaffoldEligibleFieldMetadata(fromBackingObject,
     *                         memberDetails, governorPhysicalTypeMetadata.getId());
     * </pre>
     * 
     * Mirar si sirve de ejemplo:
     * ReportMetadata.installJasperReportTemplate(String, JavaType)
     * 
     * @param beanInfoMetadata
     *            metadata of the type we want retrieve the fields
     * @param type
     *            JavaType form we want get its fields
     * @return {@link FieldMetadata} list.
     */
    private List<FieldMetadata> getElegibleFields(
            PhysicalTypeMetadata beanInfoMetadata, JavaType type) {

        List<FieldMetadata> fields = new ArrayList<FieldMetadata>();

        // DiSiD: Get declared fields instead of public accessors and from
        // JavaParserClassMetadata instead from BeanInfoMetadata
        // TODO No aspect properties are considered, as generated by database
        // reverse engineering
        for (FieldMetadata method : beanInfoMetadata
                .getMemberHoldingTypeDetails().getDeclaredFields()) {

            JavaSymbolName propertyName = method.getFieldName();

            List<MemberHoldingTypeDetails> details = new ArrayList<MemberHoldingTypeDetails>();
            details.add(beanInfoMetadata.getMemberHoldingTypeDetails());

            MemberDetails memberDetails = getMemeberDetails(type);
            FieldMetadata field = BeanInfoUtils.getFieldForPropertyName(
                    memberDetails, propertyName);

            if (field != null && hasMutator(field, beanInfoMetadata, type)) {

                // Never include id field (it shouldn't normally have a mutator
                // anyway, but the user might have added one)
                if (MemberFindingUtils
                        .getAnnotationOfType(field.getAnnotations(),
                                new JavaType("javax.persistence.Id")) != null) {
                    continue;
                }
                // Never include version field (it shouldn't normally have a
                // mutator anyway, but the user might have added one)
                if (MemberFindingUtils.getAnnotationOfType(field
                        .getAnnotations(), new JavaType(
                        "javax.persistence.Version")) != null) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * TODO: Si se cambia la forma de obtener los elegible fields este método
     * sobra
     * 
     * @param fieldMetadata
     * @param beanInfoMetadata
     * @param type
     * @return
     */
    private boolean hasMutator(FieldMetadata fieldMetadata,
            PhysicalTypeMetadata beanInfoMetadata, JavaType type) {

        // DiSiD: Get declared fields instead of public mutators and from
        // JavaParserClassMetadata instead from BeanInfoMetadata
        // TODO No aspect properties are considered, as generated by database
        // reverse engineering
        for (FieldMetadata mutator : beanInfoMetadata
                .getMemberHoldingTypeDetails().getDeclaredFields()) {

            JavaSymbolName propertyName = mutator.getFieldName();
            List<MemberHoldingTypeDetails> details = new ArrayList<MemberHoldingTypeDetails>();
            details.add(beanInfoMetadata.getMemberHoldingTypeDetails());

            MemberDetails memberDetails = getMemeberDetails(type);
            FieldMetadata field = BeanInfoUtils.getFieldForPropertyName(
                    memberDetails, propertyName);

            // DiSiD: Compare fields directly instead of use BeanInfoMetadata
            if (fieldMetadata.equals(field))
                return true;
        }
        return false;
    }

    /**
     * Return the member details of the given Type
     * 
     * @param type
     * @return
     */
    private MemberDetails getMemeberDetails(JavaType type) {
        MemberDetailsScanner memberDetailsScanner = new MemberDetailsScannerImpl();
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(PhysicalTypeIdentifier.createIdentifier(type,
                        Path.SRC_MAIN_JAVA));

        Assert.notNull(
                physicalTypeMetadata,
                "Unable to obtain physical type metdata for type "
                        + type.getFullyQualifiedTypeName());

        return memberDetailsScanner.getMemberDetails(getClass().getName(),
                (ClassOrInterfaceTypeDetails) physicalTypeMetadata
                        .getMemberHoldingTypeDetails());

    }

    class StatusListener implements ProcessManagerStatusListener {

        ScreenMetadataListener screenMetadataListener;

        public StatusListener(ScreenMetadataListener screenMetadataListener) {
            super();
            this.screenMetadataListener = screenMetadataListener;
        }

        public void onProcessManagerStatusChange(
                ProcessManagerStatus oldStatus, ProcessManagerStatus newStatus) {
            if (newStatus == ProcessManagerStatus.AVAILABLE) {
                // logger.warning("Operations delayed.");
                screenMetadataListener.performDelayed();
            }

        }

    }

    /**
     * Execute delayed operations.
     * 
     */
    public void performDelayed() {

        // Work out the MIDs of the other metadata we depend on
        String annotationPath = "javax.persistence.OneToMany";

        Iterator<String> iter = upstreamDependencyList.iterator();
        if (!iter.hasNext()) {
            return;
        }

        String upstreamDependency;

        while (iter.hasNext()) {
            upstreamDependency = iter.next();

            List<FieldMetadata> oneToManyFieldMetadatas = getAnnotatedFields(
                    upstreamDependency, annotationPath);

            // Retrieve the associated jspx (show an update).

            Document showDocument = updateView("show", oneToManyFieldMetadatas,
                    "tab");
            if (showDocument != null) {
                writeToDiskIfNecessary("show", showDocument);
            }

            Document updateDocument = updateView("update",
                    oneToManyFieldMetadatas, "tab");
            if (updateDocument != null) {
                writeToDiskIfNecessary("update", updateDocument);
            }

            iter.remove();
        }

    }
}
