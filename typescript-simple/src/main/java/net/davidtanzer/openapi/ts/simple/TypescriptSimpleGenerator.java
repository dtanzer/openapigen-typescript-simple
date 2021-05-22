package net.davidtanzer.openapi.ts.simple;

import org.openapitools.codegen.*;
import io.swagger.models.properties.*;
import org.openapitools.codegen.languages.AbstractTypeScriptClientCodegen;
import io.swagger.v3.oas.models.media.ComposedSchema;

import java.util.*;
import java.io.File;

public class TypescriptSimpleGenerator extends AbstractTypeScriptClientCodegen implements CodegenConfig {

  // source folder where to write the files
  protected String sourceFolder = "src";
  protected String apiVersion = "1.0.0";

  /**
   * Configures the type of generator.
   * 
   * @return  the CodegenType for this generator
   * @see     org.openapitools.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.OTHER;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -g flag.
   * 
   * @return the friendly name for the generator
   */
  public String getName() {
    return "typescript-simple";
  }

  /**
   * Provides an opportunity to inspect and modify operation data before the code is generated.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {

    // to try debugging your code generator:
    // set a break point on the next line.
    // then debug the JUnit test called LaunchGeneratorInDebugger

    Map<String, Object> results = super.postProcessOperationsWithModels(objs, allModels);

    Map<String, Object> ops = (Map<String, Object>)results.get("operations");
    ArrayList<CodegenOperation> opList = (ArrayList<CodegenOperation>)ops.get("operation");

    // iterate over the operation and perhaps modify something
    for(CodegenOperation co : opList){
      if(co.returnBaseType != null && co.returnBaseType.indexOf(" | ") >= 0) {
        co.vendorExtensions.put("ts-simple-return-types", co.returnBaseType.split(" \\| "));
        co.vendorExtensions.put("ts-simple-return-union", true);
      }
      // example:
      // co.httpMethod = co.httpMethod.toLowerCase();
    }

    this.addOperationModelImportInfomation(results);
    this.updateOperationParameterEnumInformation(results);
    this.addOperationObjectResponseInformation(results);
    this.addOperationPrefixParameterInterfacesInformation(results);
    this.escapeOperationIds(results);

    return results;
  }

  private void escapeOperationIds(Map<String, Object> operations) {
      Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
      List<CodegenOperation> operationList = (List<CodegenOperation>) _operations.get("operation");
      for (CodegenOperation op : operationList) {
          String param = op.operationIdCamelCase + "Request";
          if (op.imports.contains(param)) {
              // we import a model with the same name as the generated operation, escape it
              op.operationIdCamelCase += "Operation";
              op.operationIdLowerCase += "operation";
              op.operationIdSnakeCase += "_operation";
          }
      }
  }

  private void addOperationModelImportInfomation(Map<String, Object> operations) {
      // This method will add extra infomation to the operations.imports array.
      // The api template uses this infomation to import all the required
      // models for a given operation.
      List<Map<String, Object>> imports = (List<Map<String, Object>>) operations.get("imports");
      for (Map<String, Object> im : imports) {
          im.put("className", im.get("import").toString().replace(modelPackage() + ".", ""));
      }
  }

  private void updateOperationParameterEnumInformation(Map<String, Object> operations) {
      // This method will add extra infomation as to whether or not we have enums and
      // update their names with the operation.id prefixed.
      Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
      List<CodegenOperation> operationList = (List<CodegenOperation>) _operations.get("operation");
      boolean hasEnum = false;
      for (CodegenOperation op : operationList) {
          for (CodegenParameter param : op.allParams) {
              if (Boolean.TRUE.equals(param.isEnum)) {
                  hasEnum = true;
                  param.datatypeWithEnum = param.datatypeWithEnum
                          .replace(param.enumName, op.operationIdCamelCase + param.enumName);
              }
          }
      }

      operations.put("hasEnums", hasEnum);
  }

  private void addOperationObjectResponseInformation(Map<String, Object> operations) {
      // This method will modify the infomation on the operations' return type.
      // The api template uses this infomation to know when to return a text
      // response for a given simple response operation.
      Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
      List<CodegenOperation> operationList = (List<CodegenOperation>) _operations.get("operation");
      for (CodegenOperation op : operationList) {
          op.vendorExtensions.put("escapedPath", op.path.replaceAll("\\{", "\\${"));
          if("object".equals(op.returnType)) {
              op.isMap = true;
              op.returnSimpleType = false;
          }
      }
  }

  private void addOperationPrefixParameterInterfacesInformation(Map<String, Object> operations) {
      Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
      operations.put("prefixParameterInterfaces", false);
  }

  @Override
  public Map<String, Object> postProcessModels(Map<String, Object> objs) {
      List<Object> models = (List<Object>) postProcessModelsEnum(objs).get("models");

      // process enum in models
      for (Object _mo : models) {
          Map<String, Object> mo = (Map<String, Object>) _mo;
          CodegenModel cm = (CodegenModel) mo.get("model");
          cm.imports = new TreeSet(cm.imports);
          /* Don't need that?
          // name enum with model name, e.g. StatusEnum => Pet.StatusEnum
          for (CodegenProperty var : cm.vars) {
              if (Boolean.TRUE.equals(var.isEnum)) {
                  // behaviour for enum names is specific for Typescript Fetch, not using namespaces
                  var.datatypeWithEnum = var.datatypeWithEnum.replace(var.enumName, cm.classname + var.enumName);
              }
          }
          if (cm.parent != null) {
              for (CodegenProperty var : cm.allVars) {
                  if (Boolean.TRUE.equals(var.isEnum)) {
                      var.datatypeWithEnum = var.datatypeWithEnum
                              .replace(var.enumName, cm.classname + var.enumName);
                  }
              }
          }
          */
          if (!cm.oneOf.isEmpty()) {
              // For oneOfs only import $refs within the oneOf
              TreeSet<String> oneOfRefs = new TreeSet<>();
              for (String im : cm.imports) {
                  if (cm.oneOf.contains(im)) {
                      oneOfRefs.add(im);
                  }
              }
              cm.imports = oneOfRefs;
          }
      }

      return objs;
  }

  @Override
  public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
      Map<String, Object> result = super.postProcessAllModels(objs);
      for (Map.Entry<String, Object> entry : result.entrySet()) {
          Map<String, Object> inner = (Map<String, Object>) entry.getValue();
          List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
          for (Map<String, Object> model : models) {
              CodegenModel codegenModel = (CodegenModel) model.get("model");
              model.put("hasImports", codegenModel.imports.size() > 0);
          }
      }
      return result;
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   * 
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a typescript-simple client library.";
  }

  public TypescriptSimpleGenerator() {
    super();

    typeMapping.put("date", "Date");
    typeMapping.put("DateTime", "Date");
    typeMapping.put("String", "string");
    typeMapping.put("UUID", "string");
    typeMapping.put("Set", "Array");
    typeMapping.put("set", "Array");

    // set the output folder here
    outputFolder = "generated-code/typescript-simple";

    useOneOfInterfaces = true;
    legacyDiscriminatorBehavior = false;

    defaultIncludes.add("Array");
    defaultIncludes.add("Date");
    defaultIncludes.add("DateTime");
    defaultIncludes.add("Set");
    
    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put(
      "model.mustache", // the template to use
      ".ts");       // the extension for each file to write

    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    apiTemplateFiles.put(
      "api.mustache",   // the template to use
      ".ts");       // the extension for each file to write

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "typescript-simple";

    /**
     * Api Package.  Optional, if needed, this can be used in templates
     */
    apiPackage = "";

    /**
     * Model Package.  Optional, if needed, this can be used in templates
     */
    modelPackage = "";

    /**
     * Reserved words.  Override this with reserved words specific to your language
     */
    reservedWords = new HashSet<String> (
      Arrays.asList(
        "sample1",  // replace with static values
        "sample2")
    );

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    supportingFiles.add(new SupportingFile("common.mustache",   // the input template or file
      "",                                                       // the destination folder, relative `outputFolder`
      "common.ts")                                          // the output file 
    );

    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
      Arrays.asList(
        "Type1",      // replace these with your types
        "Type2")
    );
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reserved words
   * 
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + /*"/" + sourceFolder +*/ "/" + modelPackage().replace('.', File.separatorChar);
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + /*"/" + sourceFolder +*/ "/" + apiPackage().replace('.', File.separatorChar);
  }

  /**
   * override with any special text escaping logic to handle unsafe
   * characters so as to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with unsafe characters removed or escaped
   */
  @Override
  public String escapeUnsafeCharacters(String input) {
    //TODO: check that this logic is safe to escape unsafe characters to avoid code injection
    return input;
  }

  /**
   * Escape single and/or double quote to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with quotation mark removed or escaped
   */
  public String escapeQuotationMark(String input) {
    //TODO: check that this logic is safe to escape quotation mark to avoid code injection
    return input.replace("\"", "\\\"");
  }
}