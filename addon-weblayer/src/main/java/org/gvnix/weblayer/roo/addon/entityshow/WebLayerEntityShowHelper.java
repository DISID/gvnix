package org.gvnix.weblayer.roo.addon.entityshow;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gvnix.weblayer.roo.addon.GvNIXRooUtils;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.StringUtils;

public class WebLayerEntityShowHelper {

  public static final String INDENT = "    ";

  private final JavaType entityType;

  private final String layoutName;

  private final String indent;

  // map from property name to class name
  private final Map<String, String> fieldMap = new LinkedHashMap<String, String>();

  public WebLayerEntityShowHelper(MetadataService metadataService,
      MemberDetailsScanner memberDetailsScanner, JavaType entityType,
      String indent, String layoutName) {
    this.entityType = entityType;
    this.indent = indent;
    this.layoutName = layoutName;

    // get metadata for the entity
    EntityMetadata em = (EntityMetadata) metadataService.get(EntityMetadata
        .createIdentifier(entityType, Path.SRC_MAIN_JAVA));

    MemberDetails memberDetails = GvNIXRooUtils.getMemberDetails(entityType,
        metadataService, memberDetailsScanner, this.getClass().getName());
    Map<JavaSymbolName, MethodMetadata> accessors = GvNIXRooUtils.getAccessors(
        entityType, em, memberDetails, true);

    for (JavaSymbolName propertyName : accessors.keySet()) {
      String propertyNameString = StringUtils.uncapitalize(propertyName
          .getSymbolName());
      FieldMetadata fieldMetadata = BeanInfoUtils.getFieldForPropertyName(
          memberDetails, propertyName);
      if (null == fieldMetadata) {
        continue;
      }
      JavaType type = accessors.get(propertyName).getReturnType();

      if (type.isCommonCollectionType()) {
        boolean domainTypeCollection = false;
        for (JavaType genericType : type.getParameters()) {
          if (GvNIXRooUtils.isDomainTypeInProject(metadataService, genericType)) {
            domainTypeCollection = true;
          }
        }
        if (domainTypeCollection) {
          fieldMap.put(propertyNameString, "Label");
        }
      }
      else if (!GvNIXRooUtils.isEmbeddedFieldType(fieldMetadata)) {
        // ignoring embedded types
        if (GvNIXRooUtils.isDomainTypeInProject(metadataService, type)) {
          fieldMap.put(propertyNameString, "Label");
        }
        else if (JavaType.BOOLEAN_PRIMITIVE.equals(type)
            || JavaType.BOOLEAN_OBJECT.equals(type)) {
          fieldMap.put(propertyNameString, "Label");
        }
        else if (new JavaType("java.util.Date").equals(type)) {
          fieldMap.put(propertyNameString, "Label");
        }
        else {
          fieldMap.put(propertyNameString, "Label");
        }
      }
    }
  }

  public String getFieldDeclarations() {
    StringBuilder builder = new StringBuilder();

    // imports are in the template

    for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
      // add field
      builder.append(declareFieldSection(getFieldName(entry.getKey()),
          entry.getValue()));
    }
    return builder.toString();
  }

  public String getFieldCreationStatements() {
    StringBuilder builder = new StringBuilder();

    // imports are in the template

    if (fieldMap.isEmpty()) {
      builder.append(createLabelSection("label",
          "No fields in " + entityType.getSimpleTypeName()));
    }
    else {
      int topPos = 20;
      for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
        // add field construction
        builder.append(createFieldSection(getFieldName(entry.getKey()),
            entry.getValue(), getCaption(entry.getKey()), entry.getKey(),
            topPos));
        topPos += 40;
      }
    }

    return builder.toString();
  }

  private String getCaption(String propertyId) {
    propertyId = propertyId.replaceAll("([A-Z])", " $1");
    propertyId = propertyId.substring(0, 1).toUpperCase()
        + propertyId.substring(1);
    return propertyId.trim();
  }

  private String getFieldName(String propertyId) {
    return propertyId + "Field";
  }

  private Object declareFieldSection(String name, String fieldClass) {
    StringBuilder builder = new StringBuilder();

    builder.append(indent + "@AutoGenerated\n");
    builder.append(indent + "private " + fieldClass + " " + name + ";\n");

    return builder.toString();
  }

  private String createLabelSection(String name, String value) {
    StringBuilder builder = new StringBuilder();

    String indent = this.indent + this.indent;

    builder.append(indent + "// " + name + "\n");
    builder.append(indent + "Label " + name + " = new Label();\n");
    builder.append(indent + name + ".setWidth(\"-1px\");\n");
    builder.append(indent + name + ".setHeight(\"-1px\");\n");
    builder.append(indent + name + ".setValue(\"" + value + "\");\n");
    builder.append(indent + name + ".setImmediate(false);\n");
    builder.append(indent + layoutName + ".addComponent(" + name
        + ", \"top:20.0px;left:20.0px;\");\n");
    builder.append("\n");

    return builder.toString();
  }

  private String createFieldSection(String name, String fieldClass,
                                    String caption, String value, int topPos) {
    StringBuilder builder = new StringBuilder();

    String indent = this.indent + this.indent;

    builder.append(indent + "// " + name + "\n");
    builder.append(indent + name + " = new " + fieldClass + "();\n");
    builder.append(indent + name + ".setWidth(\"-1px\");\n");
    builder.append(indent + name + ".setHeight(\"-1px\");\n");
    builder.append(indent + name + ".setCaption(\"" + caption + "\");\n");
    builder.append(indent + name + ".setValue(\"" + value + "\");\n");
    builder.append(indent + name + ".setImmediate(true);\n");

    builder.append(indent + layoutName + ".addComponent(" + name + ", \"top:"
        + topPos + ".0px;left:20.0px;\");\n");
    builder.append("\n");

    return builder.toString();
  }

}
