/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.loupefield;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXPasswordHandlerSAFE} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class LoupefieldMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = LoupefieldMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName SHOW_ONLY_METHOD_NAME = new JavaSymbolName(
            "showOnlyList");

    private static final JavaSymbolName FIND_USING_AJAX_METHOD_NAME = new JavaSymbolName(
            "findUsingAjax");

    private static final JavaType RESPONSE_LIST_MAP = new JavaType(
            "java.util.Map", 0, DataType.TYPE, null, Arrays.asList(
                    new JavaType("java.lang.String"), new JavaType(
                            "java.lang.String")));

    private static final JavaType RESPONSE_LIST = new JavaType(
            "java.util.List", 0, DataType.TYPE, null,
            Arrays.asList(RESPONSE_LIST_MAP));

    private static final JavaType RESPONSE_ENTITY = new JavaType(
            SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(RESPONSE_LIST));

    private static final JavaType ARRAY_LIST_MAP_STRING = new JavaType(
            "java.util.ArrayList", 0, DataType.TYPE, null,
            Arrays.asList(RESPONSE_LIST_MAP));

    private static final JavaType HASHMAP_STRING = new JavaType(
            "java.util.HashMap", 0, DataType.TYPE, null, Arrays.asList(
                    new JavaType("java.lang.String"), new JavaType(
                            "java.lang.String")));

    private static final JavaType MAP_STRING_OBJECT = new JavaType(
            "java.util.Map", 0, DataType.TYPE, null, Arrays.asList(
                    new JavaType("java.lang.String"), new JavaType(
                            "java.lang.Object")));

    private static final JavaType HASHMAP_STRING_OBJECT = new JavaType(
            "java.util.HashMap", 0, DataType.TYPE, null, Arrays.asList(
                    new JavaType("java.lang.String"), new JavaType(
                            "java.lang.Object")));

    private static final JavaType ITERATOR_STRING = new JavaType(
            "java.util.Iterator", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType("java.lang.String")));

    private static final JavaType LIST_COLUMNDEF = new JavaType(
            "java.util.List", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType(
                    "com.github.dandelion.datatables.core.ajax.ColumnDef")));

    private static final JavaType ARRAYLIST_COLUMNDEF = new JavaType(
            "java.util.ArrayList", 0, DataType.TYPE, null,
            Arrays.asList(new JavaType(
                    "com.github.dandelion.datatables.core.ajax.ColumnDef")));

    private static final JavaType DATASET_MAP_STRING = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DataSet", 0,
            DataType.TYPE, null, Arrays.asList(new JavaType("java.util.Map", 0,
                    DataType.TYPE, null, Arrays.asList(new JavaType(
                            "java.lang.String"), new JavaType(
                            "java.lang.String")))));

    /**
     * Itd builder helper
     */
    private ItdBuilderHelper helper;

    public LoupefieldMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata, JavaType entity) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding AUTOWIRED annotation
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>(
                1);
        annotations
                .add(new AnnotationMetadataBuilder(SpringJavaType.AUTOWIRED));

        // Creating conversionService_loupe field
        builder.addField(getField("conversionService_loupe", null,
                new JavaType(
                        "org.springframework.core.convert.ConversionService"),
                Modifier.PUBLIC, annotations));

        // Adding showOnlyList method
        builder.addMethod(getShowOnlyListMethod());

        // Adding findUsingAjax method
        builder.addMethod(getFindUsingAjaxMethod(entity));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>showOnlyList</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getShowOnlyListMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(new JavaType(
                        "org.springframework.ui.Model"), new JavaType(
                        "javax.servlet.http.HttpServletRequest"));

        AnnotationMetadataBuilder pathMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        pathMetadataBuilder.addStringAttribute("value", "path");

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                pathMetadataBuilder.build()));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SHOW_ONLY_METHOD_NAME,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        requestMappingMetadataBuilder.addStringAttribute("params", "selector");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "text/html");

        annotations.add(requestMappingMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("uiModel"));
        parameterNames.add(new JavaSymbolName("request"));
        parameterNames.add(new JavaSymbolName("listPath"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildShowOnlyListMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, SHOW_ONLY_METHOD_NAME,
                JavaType.STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>findUsingAjax</code> method. <br>
     * 
     * @param entity
     * @return
     */
    private MethodMetadata getFindUsingAjaxMethod(JavaType entity) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(new JavaType(
                        "org.springframework.web.context.request.WebRequest"));

        // Adding search param
        AnnotationMetadataBuilder searchMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        searchMetadataBuilder.addStringAttribute("value", "_search_");
        searchMetadataBuilder.addBooleanAttribute("required", false);

        // Adding id param
        AnnotationMetadataBuilder idMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        idMetadataBuilder.addStringAttribute("value", "_id_");
        idMetadataBuilder.addBooleanAttribute("required", false);

        // Adding pkField param
        AnnotationMetadataBuilder pkFieldMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        pkFieldMetadataBuilder.addStringAttribute("value", "_pkField_");

        // Adding max param
        AnnotationMetadataBuilder maxMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        maxMetadataBuilder.addStringAttribute("value", "_max_");
        maxMetadataBuilder.addBooleanAttribute("required", false);
        maxMetadataBuilder.addStringAttribute("defaultValue", "3");

        // Adding caption param
        AnnotationMetadataBuilder captionMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        captionMetadataBuilder.addStringAttribute("value", "_caption_");
        captionMetadataBuilder.addBooleanAttribute("required", false);

        // Adding additionalFields param
        AnnotationMetadataBuilder additionalFieldsMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        additionalFieldsMetadataBuilder.addStringAttribute("value",
                "_additionalFields_");
        additionalFieldsMetadataBuilder.addBooleanAttribute("required", false);

        // Adding field param
        AnnotationMetadataBuilder fieldMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        fieldMetadataBuilder.addStringAttribute("value", "_field_");

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                searchMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                idMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                pkFieldMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.INT_OBJECT,
                maxMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                captionMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                additionalFieldsMetadataBuilder.build()));
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                fieldMetadataBuilder.build()));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(FIND_USING_AJAX_METHOD_NAME,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        requestMappingMetadataBuilder.addStringAttribute("params",
                "findUsingAjax");
        requestMappingMetadataBuilder.addStringAttribute("headers",
                "Accept=application/json");

        annotations.add(requestMappingMetadataBuilder);
        annotations.add(new AnnotationMetadataBuilder(
                SpringJavaType.RESPONSE_BODY));

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("request"));
        parameterNames.add(new JavaSymbolName("search"));
        parameterNames.add(new JavaSymbolName("id"));
        parameterNames.add(new JavaSymbolName("pkField"));
        parameterNames.add(new JavaSymbolName("maxResult"));
        parameterNames.add(new JavaSymbolName("caption"));
        parameterNames.add(new JavaSymbolName("additionalFields"));
        parameterNames.add(new JavaSymbolName("field"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        buildFindUsingAjaxMethodBody(bodyBuilder, entity);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, FIND_USING_AJAX_METHOD_NAME,
                RESPONSE_ENTITY, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>showOnlyList</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildShowOnlyListMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Do common datatables operations: get entity list filtered by request");
        bodyBuilder.appendFormalLine("// parameters");

        // listDatatables(uiModel, request);
        bodyBuilder.appendFormalLine("listDatatables(uiModel, request);");

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Show only the list fragment (without footer, header, menu, etc.)");

        // return "forward:/WEB-INF/views/" + listPath + ".jspx";
        bodyBuilder
                .appendFormalLine("return \"forward:/WEB-INF/views/\" + listPath + \".jspx\";");

    }

    /**
     * Builds body method for <code>findUsingAjax</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildFindUsingAjaxMethodBody(
            InvocableMemberBodyBuilder bodyBuilder, JavaType entity) {

        // Adding comments
        bodyBuilder.appendFormalLine("// Declaring error utils");

        // List<Map<String, String>> errorList = new
        // ArrayList<Map<String,String>>();
        bodyBuilder.appendFormalLine(String.format("%s errorList = new %s();",
                helper.getFinalTypeName(RESPONSE_LIST),
                helper.getFinalTypeName(ARRAY_LIST_MAP_STRING)));

        // Map<String, String> error = new HashMap<String, String>();
        bodyBuilder.appendFormalLine(String.format("%s error = new %s();",
                helper.getFinalTypeName(RESPONSE_LIST_MAP),
                helper.getFinalTypeName(HASHMAP_STRING)));

        // HttpHeaders headers = new HttpHeaders();
        bodyBuilder.appendFormalLine(String.format(
                "%s headers = new HttpHeaders();", helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.http.HttpHeaders"))));
        // headers.add("Content-Type", "application/json; charset=utf-8");
        bodyBuilder
                .appendFormalLine("headers.add(\"Content-Type\",\"application/json; charset=utf-8\");");

        // Adding comments
        bodyBuilder.appendFormalLine("// Getting Entity");

        // BeanWrapper xxxxBean = new BeanWrapperImpl(XXX.class);
        bodyBuilder.appendFormalLine(String.format(
                "%s %sBean = new %s(%s.class);", helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.beans.BeanWrapper")),
                helper.getFinalTypeName(entity).toLowerCase(), helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.beans.BeanWrapperImpl")),
                helper.getFinalTypeName(entity)));

        // Adding comments
        bodyBuilder.appendFormalLine("// Getting field");

        // Class targetEntity = visitBean.getPropertyType(field);
        bodyBuilder.appendFormalLine(String.format(
                "Class targetEntity = %sBean.getPropertyType(field);", helper
                        .getFinalTypeName(entity).toLowerCase()));

        // BeanWrapper targetBean = new BeanWrapperImpl(targetEntity);
        bodyBuilder
                .appendFormalLine("BeanWrapper targetBean = new BeanWrapperImpl(targetEntity);");

        // Map<String, Object> baseSearchValuesMap = new HashMap<String,
        // Object>();
        bodyBuilder.appendFormalLine(String.format(
                "%s baseSearchValuesMap = new %s();",
                helper.getFinalTypeName(MAP_STRING_OBJECT),
                helper.getFinalTypeName(HASHMAP_STRING_OBJECT)));

        // String paramName;
        bodyBuilder.appendFormalLine("String paramName;");

        // Iterator<String> iter = request.getParameterNames();
        bodyBuilder.appendFormalLine(String.format(
                "%s iter = request.getParameterNames();",
                helper.getFinalTypeName(ITERATOR_STRING)));

        // while (iter.hasNext()) {
        bodyBuilder.appendFormalLine("while (iter.hasNext()) {");
        bodyBuilder.indent();

        // paramName = iter.next();
        bodyBuilder.appendFormalLine("paramName = iter.next();");

        // if (targetBean.isReadableProperty(paramName)) {
        bodyBuilder
                .appendFormalLine("if (targetBean.isReadableProperty(paramName)) {");
        bodyBuilder.indent();

        // baseSearchValuesMap.put(paramName,conversionService_loupe.convert(request.getParameter(paramName),targetBean.getPropertyType(paramName)));
        bodyBuilder
                .appendFormalLine("baseSearchValuesMap.put(paramName,conversionService_loupe.convert(request.getParameter(paramName),targetBean.getPropertyType(paramName)));");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder.appendFormalLine("// Getting Entity Manager");

        // EntityManager targetEntityManager = null;
        bodyBuilder.appendFormalLine(String.format(
                "%s targetEntityManager = null;", helper
                        .getFinalTypeName(new JavaType(
                                "javax.persistence.EntityManager"))));
        // try {
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();

        // Method entityManagerMethod = targetEntity.getMethod("entityManager");
        bodyBuilder
                .appendFormalLine(String
                        .format("%s entityManagerMethod = targetEntity.getMethod(\"entityManager\");",
                                helper.getFinalTypeName(new JavaType(
                                        "java.lang.reflect.Method"))));

        // targetEntityManager = (EntityManager)
        // entityManagerMethod.invoke(null);
        bodyBuilder
                .appendFormalLine("targetEntityManager = (EntityManager) entityManagerMethod.invoke(null);");

        bodyBuilder.indentRemove();

        // } catch (Exception e) {
        bodyBuilder.appendFormalLine("} catch (Exception e) {");
        bodyBuilder.indent();

        // return new ResponseEntity<List<Map<String, String>>>(null,
        // headers,HttpStatus.INTERNAL_SERVER_ERROR);
        bodyBuilder.appendFormalLine(String.format(
                "return new %s(null, headers,%s.INTERNAL_SERVER_ERROR);",
                helper.getFinalTypeName(RESPONSE_ENTITY), helper
                        .getFinalTypeName(new JavaType(
                                "org.springframework.http.HttpStatus"))));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder.appendFormalLine("// Creating ColumnDef - ID COLUMN");

        // List<ColumnDef> columnDefs = new ArrayList<ColumnDef>();
        bodyBuilder.appendFormalLine(String.format("%s columnDefs = new %s();",
                helper.getFinalTypeName(LIST_COLUMNDEF),
                helper.getFinalTypeName(ARRAYLIST_COLUMNDEF)));

        // ColumnDef idColumn = new ColumnDef();
        bodyBuilder.appendFormalLine("ColumnDef idColumn = new ColumnDef();");

        // idColumn.setName(pkField);
        bodyBuilder.appendFormalLine("idColumn.setName(pkField);");

        // idColumn.setFilterable(true);
        bodyBuilder.appendFormalLine("idColumn.setFilterable(true);");

        // columnDefs.add(idColumn);
        bodyBuilder.appendFormalLine("columnDefs.add(idColumn);");

        // Adding comments
        bodyBuilder.appendFormalLine("// Creating more columns to search");

        // if (StringUtils.isNotBlank(additionalFields)) {
        bodyBuilder.appendFormalLine(String.format(
                "if (%s.isNotBlank(additionalFields)) {", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));
        bodyBuilder.indent();

        // String[] fields = StringUtils.split(additionalFields, ",");
        bodyBuilder
                .appendFormalLine("String[] fields = StringUtils.split(additionalFields, \",\");");

        // if (fields.length > 0) {
        bodyBuilder.appendFormalLine("if (fields.length > 0) {");
        bodyBuilder.indent();

        // for (String aditionalField : fields) {
        bodyBuilder.appendFormalLine("for (String aditionalField : fields) {");
        bodyBuilder.indent();

        // ColumnDef aditionalColumn = new ColumnDef();
        bodyBuilder
                .appendFormalLine("ColumnDef aditionalColumn = new ColumnDef();");

        // aditionalColumn.setName(aditionalField);
        bodyBuilder
                .appendFormalLine("aditionalColumn.setName(aditionalField);");

        // aditionalColumn.setFilterable(true);
        bodyBuilder.appendFormalLine("aditionalColumn.setFilterable(true);");

        // columnDefs.add(aditionalColumn);
        bodyBuilder.appendFormalLine("columnDefs.add(aditionalColumn);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // SearchResults<?> searchResult = null;
        bodyBuilder.appendFormalLine(String.format(
                "%s<?> searchResult = null;",
                helper.getFinalTypeName(new JavaType(
                        "org.gvnix.web.datatables.query.SearchResults"))));

        // if (StringUtils.isNotBlank(id)) {
        bodyBuilder.appendFormalLine("if (StringUtils.isNotBlank(id)) {");
        bodyBuilder.indent();

        // targetBean.setConversionService(conversionService_loupe);
        bodyBuilder
                .appendFormalLine("targetBean.setConversionService(conversionService_loupe);");

        // Class idType = targetBean.getPropertyType(pkField);
        bodyBuilder
                .appendFormalLine("Class idType = targetBean.getPropertyType(pkField);");

        // String query = String.format("SELECT o FROM %s o WHERE o.%s = :id",
        // targetEntity.getSimpleName(), pkField);
        bodyBuilder
                .appendFormalLine("String query = String.format(\"SELECT o FROM %s o WHERE o.%s = :id\",targetEntity.getSimpleName(), pkField);");

        // TypedQuery<Object> q =
        // targetEntityManager.createQuery(query,targetEntity);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s<Object> q = targetEntityManager.createQuery(query,targetEntity);",
                                helper.getFinalTypeName(new JavaType(
                                        "javax.persistence.TypedQuery"))));

        // q.setParameter("id", targetBean.convertIfNecessary(id, idType));
        bodyBuilder
                .appendFormalLine("q.setParameter(\"id\", targetBean.convertIfNecessary(id, idType));");

        // searchResult = new SearchResults(q.getResultList(), 1, false, 0,
        // 1, 1);
        bodyBuilder
                .appendFormalLine("searchResult = new SearchResults(q.getResultList(), 1, false, 0, 1, 1);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();

        // DatatablesCriterias criterias = new DatatablesCriterias(search,
        // 0,maxResult, columnDefs, columnDefs, null);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s criterias = new DatatablesCriterias(search, 0,maxResult, columnDefs, columnDefs, null);",
                                helper.getFinalTypeName(new JavaType(
                                        "com.github.dandelion.datatables.core.ajax.DatatablesCriterias"))));

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Get all columns with results in id column and additional columns");

        // searchResult =
        // DatatablesUtils.findByCriteria(targetEntity,targetEntityManager,
        // criterias, baseSearchValuesMap);
        bodyBuilder
                .appendFormalLine(String
                        .format("searchResult = %s.findByCriteria(targetEntity,targetEntityManager, criterias, baseSearchValuesMap);",
                                helper.getFinalTypeName(new JavaType(
                                        "org.gvnix.web.datatables.util.DatatablesUtils"))));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Adding comments
        bodyBuilder
                .appendFormalLine("// Getting Result with only id column and additional columns");

        // DataSet<Map<String, String>> result =
        // DatatablesUtils.populateDataSet(searchResult.getResults(), pkField,
        // searchResult.getResultsCount(), searchResult.getResultsCount(),
        // columnDefs, null, conversionService_loupe);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s result = DatatablesUtils.populateDataSet(searchResult.getResults(), pkField, searchResult.getResultsCount(), searchResult.getResultsCount(), columnDefs, null, conversionService_loupe);",
                                helper.getFinalTypeName(DATASET_MAP_STRING)));

        // Adding comments
        bodyBuilder.appendFormalLine("// If No Data Found, return message");

        // if (result.getTotalDisplayRecords() == 0) {
        bodyBuilder
                .appendFormalLine("if (result.getTotalDisplayRecords() == 0) {");
        bodyBuilder.indent();

        // error.put("Error", "No Data Found");
        bodyBuilder
                .appendFormalLine("error.put(\"Error\", \"No Data Found\");");

        // errorList.add(error);
        bodyBuilder.appendFormalLine("errorList.add(error);");

        // return new ResponseEntity<List<Map<String, String>>>(errorList,
        // headers, HttpStatus.INTERNAL_SERVER_ERROR);
        bodyBuilder
                .appendFormalLine(String
                        .format("return new %s(errorList, headers, HttpStatus.INTERNAL_SERVER_ERROR);",
                                helper.getFinalTypeName(RESPONSE_ENTITY)));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // List<Map<String, String>> resultRows = result.getRows();
        bodyBuilder
                .appendFormalLine("List<Map<String, String>> resultRows = result.getRows();");

        // Map<String, String> captionRow = new HashMap<String, String>();
        bodyBuilder
                .appendFormalLine("Map<String, String> captionRow = new HashMap<String, String>();");

        // Adding comments
        bodyBuilder
                .appendFormalLine("// If caption is blank, use ConversionService to show item as String");

        // boolean notCaption = StringUtils.isBlank(caption);
        bodyBuilder
                .appendFormalLine("boolean notCaption = StringUtils.isBlank(caption);");

        // BeanWrapperImpl resultBean = new BeanWrapperImpl(targetEntity);
        bodyBuilder
                .appendFormalLine("BeanWrapperImpl resultBean = new BeanWrapperImpl(targetEntity);");

        // if (!notCaption && !resultBean.isReadableProperty(caption)) {
        bodyBuilder
                .appendFormalLine("if (!notCaption && !resultBean.isReadableProperty(caption)) {");
        bodyBuilder.indent();

        // error.put("Error", caption + " is not a valid field");
        bodyBuilder
                .appendFormalLine("error.put(\"Error\", caption + \" is not a valid field\");");

        // errorList.add(error);
        bodyBuilder.appendFormalLine("errorList.add(error);");

        // return new ResponseEntity<List<Map<String, String>>>(errorList,
        // headers, HttpStatus.INTERNAL_SERVER_ERROR);
        bodyBuilder
                .appendFormalLine("return new ResponseEntity<List<Map<String, String>>>(errorList, headers, HttpStatus.INTERNAL_SERVER_ERROR);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // List<?> results = searchResult.getResults();
        bodyBuilder
                .appendFormalLine("List<?> results = searchResult.getResults();");

        // Iterator<?> it = results.iterator();
        bodyBuilder.appendFormalLine("Iterator<?> it = results.iterator();");

        // Iterator<Map<String, String>> it2 = resultRows.iterator();
        bodyBuilder
                .appendFormalLine("Iterator<Map<String, String>> it2 = resultRows.iterator();");

        // Object rowCaption;
        bodyBuilder.appendFormalLine("Object rowCaption;");

        // while (it.hasNext() && it2.hasNext()) {
        bodyBuilder.appendFormalLine("while (it.hasNext() && it2.hasNext()) {");
        bodyBuilder.indent();

        // Object rowResult = it.next();
        bodyBuilder.appendFormalLine("Object rowResult = it.next();");

        // Map<String, String> rowItem = it2.next();
        bodyBuilder
                .appendFormalLine("Map<String, String> rowItem = it2.next();");

        // resultBean.setWrappedInstance(rowResult);
        bodyBuilder
                .appendFormalLine("resultBean.setWrappedInstance(rowResult);");

        // if (notCaption) {
        bodyBuilder.appendFormalLine("if (notCaption) {");
        bodyBuilder.indent();

        // rowCaption = rowResult;
        bodyBuilder.appendFormalLine("rowCaption = rowResult;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();

        // rowCaption = resultBean.getPropertyValue(caption);
        bodyBuilder
                .appendFormalLine("rowCaption = resultBean.getPropertyValue(caption);");

        // if (rowCaption == null) {
        bodyBuilder.appendFormalLine("if (rowCaption == null) {");
        bodyBuilder.indent();

        // rowCaption = rowResult;
        bodyBuilder.appendFormalLine("rowCaption = rowResult;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // rowItem.put("__caption__",
        // conversionService_loupe.convert(rowCaption, String.class));
        bodyBuilder
                .appendFormalLine("rowItem.put(\"__caption__\", conversionService_loupe.convert(rowCaption, String.class));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return new ResponseEntity<List<Map<String, String>>>(resultRows,
        // headers, HttpStatus.OK);
        bodyBuilder
                .appendFormalLine("return new ResponseEntity<List<Map<String, String>>>(resultRows, headers, HttpStatus.OK);");

    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getField(String name, String value,
            JavaType javaType, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = new JavaSymbolName(name);
        String initializer = value;
        FieldMetadata field = getOrCreateField(curName, javaType, initializer,
                modifier, annotations);
        return field;
    }

    /**
     * Gets or creates a field based on parameters.<br>
     * First try to get a suitable field (by name and type). If not found create
     * a new one (adding a counter to name if it's needed)
     * 
     * @param fielName
     * @param fieldType
     * @param initializer (String representation)
     * @param modifier See {@link Modifier}
     * @param annotations optional (can be null)
     * @return
     */
    private FieldMetadata getOrCreateField(JavaSymbolName fielName,
            JavaType fieldType, String initializer, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = fielName;

        // Check if field exist
        FieldMetadata currentField = governorTypeDetails
                .getDeclaredField(curName);
        if (currentField != null) {
            if (!currentField.getFieldType().equals(fieldType)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = curName;
                int i = 1;
                while (governorTypeDetails.getDeclaredField(newName) != null) {
                    newName = new JavaSymbolName(curName.getSymbolName()
                            .concat(StringUtils.repeat('_', i)));
                    i++;
                }
                curName = newName;
            }
        }
        if (currentField == null) {
            // create field
            if (annotations == null) {
                annotations = new ArrayList<AnnotationMetadataBuilder>(0);
            }
            // Using the FieldMetadataBuilder to create the field
            // definition.
            final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                    getId(), modifier, annotations, curName, // Field
                    fieldType); // Field type
            fieldBuilder.setFieldInitializer(initializer);
            currentField = fieldBuilder.build(); // Build and return a
            // FieldMetadata
            // instance
        }
        return currentField;

    }

}
