/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util.codegen;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.GlobalVarLocation;
import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.Action;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.ExecutableMultiReturnExpr;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.Operator;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryArithmeticExpression;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FieldAccessExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONFieldAccessExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionRollbackStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BConnectorType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionReturnCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Generates Ballerina bytecode instructions.
 *
 * @since 0.87
 */
public class CodeGenerator implements NodeVisitor {
    private static final int INT_OFFSET = 0;
    private static final int FLOAT_OFFSET = 1;
    private static final int STRING_OFFSET = 2;
    private static final int BOOL_OFFSET = 3;
    private static final int REF_OFFSET = 4;

    private int[] maxRegIndexes = {-1, -1, -1, -1, -1};

    // This int array hold then current local variable index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] lvIndexes = {-1, -1, -1, -1, -1};

    // This int array hold then current register index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] regIndexes = {-1, -1, -1, -1, -1};

    // This int array hold then current register index of following types
    // index 0 - int, 1 - float, 2 - string, 3 - boolean, 4 - reference(BValue)
    private int[] fieldIndexes = {-1, -1, -1, -1, -1};

    // Package level variable indexes. This includes package constants, package level variables and
    //  service level variables
    private int[] gvIndexes = {-1, -1, -1, -1, -1};

    private ProgramFile programFile = new ProgramFile();
    private CallableUnitInfo callableUnitInfo;
    private String currentPkgPath;
    private int currentPkgCPIndex = -1;
    private PackageInfo currentPkgInfo;

    private ServiceInfo currentServiceInfo;

    // Required variables to generate code for assignment statements
    private int rhsExprRegIndex = -1;
    private boolean varAssignment;
    private boolean arrayMapAssignment;
    private boolean structAssignment;

    private Stack<List<Instruction>> breakInstructions = new Stack<>();

    public ProgramFile getProgramFile() {
        return programFile;
    }

    @Override
    public void visit(BLangProgram bLangProgram) {
        for (BLangPackage bLangPackage : bLangProgram.getPackages()) {
            bLangPackage.setSymbolsDefined(false);
        }

        BLangPackage mainPkg = bLangProgram.getMainPackage();

        if (bLangProgram.getProgramCategory() == BLangProgram.Category.MAIN_PROGRAM) {
            mainPkg.accept(this);
            programFile.setMainPackageName(mainPkg.getName());
        } else if (bLangProgram.getProgramCategory() == BLangProgram.Category.SERVICE_PROGRAM) {
            BLangPackage[] servicePackages = bLangProgram.getServicePackages();
            for (BLangPackage servicePkg : servicePackages) {
                servicePkg.accept(this);
                programFile.addServicePackage(servicePkg.getName());
            }
        } else {
            BLangPackage[] libraryPackages = bLangProgram.getLibraryPackages();
            for (BLangPackage libraryPkg : libraryPackages) {
                libraryPkg.accept(this);
            }
        }

        // Add global variable indexes to the ProgramFile
        prepareIndexes(gvIndexes);
        programFile.setGlobalVarIndexes(gvIndexes);
    }

    @Override
    public void visit(BLangPackage bLangPackage) {
        for (BLangPackage dependentPkg : bLangPackage.getDependentPackages()) {
            // TODO Validate the following logic
            if (dependentPkg.isSymbolsDefined()) {
                continue;
            }

            dependentPkg.accept(this);
        }

        currentPkgPath = bLangPackage.getPackagePath();
        UTF8CPEntry pkgPathCPEntry = new UTF8CPEntry(currentPkgPath);
        currentPkgInfo = new PackageInfo(currentPkgPath);
        currentPkgInfo.setProgramFile(programFile);
        programFile.addPackageInfo(currentPkgPath, currentPkgInfo);

        // Insert the package reference to the constant pool of the Ballerina program
        int pkgNameCPIndex = programFile.addCPEntry(pkgPathCPEntry);
        PackageRefCPEntry packageRefCPEntry = new PackageRefCPEntry(pkgNameCPIndex);
        packageRefCPEntry.setPackageInfo(currentPkgInfo);
        programFile.addCPEntry(new PackageRefCPEntry(pkgNameCPIndex));

        // Insert the package reference to current package's constant pool
        pkgNameCPIndex = currentPkgInfo.addCPEntry(pkgPathCPEntry);
        packageRefCPEntry = new PackageRefCPEntry(pkgNameCPIndex);
        packageRefCPEntry.setPackageInfo(currentPkgInfo);
        currentPkgCPIndex = currentPkgInfo.addCPEntry(packageRefCPEntry);

        visitConstants(bLangPackage.getConsts());
        visitGlobalVariables(bLangPackage.getGlobalVariables());
        createStructInfoEntries(bLangPackage.getStructDefs());
        createConnectorInfoEntries(bLangPackage.getConnectors());
        createServiceInfoEntries(bLangPackage.getServices());
        createFunctionInfoEntries(bLangPackage.getFunctions());

        // Create function info for the package function
        BallerinaFunction pkgInitFunction = bLangPackage.getInitFunction();
        createFunctionInfoEntries(new Function[] {pkgInitFunction});

        for (CompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            compilationUnit.accept(this);
        }

        // Visit package init function
        pkgInitFunction.accept(this);
        currentPkgInfo.setInitFunctionInfo(currentPkgInfo.getFunctionInfo(pkgInitFunction.getName()));

        currentPkgCPIndex = -1;
        currentPkgPath = null;
    }

    private void visitConstants(ConstDef[] constDefs) {
        for (ConstDef constDef : constDefs) {
            BType varType = constDef.getType();
            int regIndex = getNextIndex(varType.getTag(), gvIndexes);
            GlobalVarLocation globalVarLocation = new GlobalVarLocation(regIndex);
            constDef.setMemoryLocation(globalVarLocation);
        }
    }

    private void visitGlobalVariables(GlobalVariableDef[] globalVariableDefs) {
        for (GlobalVariableDef varDef : globalVariableDefs) {
            BType varType = varDef.getType();
            int regIndex = getNextIndex(varType.getTag(), gvIndexes);
            GlobalVarLocation globalVarLocation = new GlobalVarLocation(regIndex);
            varDef.setMemoryLocation(globalVarLocation);
        }
    }

    private void createServiceInfoEntries(Service[] services) {
        for (Service service : services) {
            // Add Connector name as an UTFCPEntry to the constant pool
            UTF8CPEntry serviceNameCPEntry = new UTF8CPEntry(service.getName());
            int serviceNameCPIndex = currentPkgInfo.addCPEntry(serviceNameCPEntry);

            ServiceInfo serviceInfo = new ServiceInfo(currentPkgCPIndex, serviceNameCPIndex);
            serviceInfo.setServiceName(service.getName());
            currentPkgInfo.addServiceInfo(service.getName(), serviceInfo);

            // Assign field indexes for Connector variables
            for (VariableDefStmt varDefStmt : service.getVariableDefStmts()) {
                VariableDef varDef = varDefStmt.getVariableDef();
                BType fieldType = varDef.getType();

                int fieldIndex = getNextIndex(fieldType.getTag(), gvIndexes);
                GlobalVarLocation globalVarLocation = new GlobalVarLocation(fieldIndex);
                varDef.setMemoryLocation(globalVarLocation);
            }

            // TODO Create the init function info
//            createFunctionInfoEntries(new Function[]{serviceInfo.getInitFunction()});

            // Create resource info entries for all resource
            createResourceInfoEntries(service.getResources(), serviceInfo);
        }
    }

    private void createResourceInfoEntries(Resource[] resources, ServiceInfo serviceInfo) {
        for (Resource resource : resources) {

            // Add resource name as an UTFCPEntry to the constant pool
            UTF8CPEntry resourceNameCPEntry = new UTF8CPEntry(resource.getName());
            int resourceNameCPIndex = currentPkgInfo.addCPEntry(resourceNameCPEntry);

            ResourceInfo resourceInfo = new ResourceInfo(currentPkgPath, currentPkgCPIndex,
                    resource.getName(), resourceNameCPIndex);
            resourceInfo.setParamTypes(getParamTypes(resource.getParameterDefs()));
            resourceInfo.setPackageInfo(currentPkgInfo);

            serviceInfo.addResourceInfo(resource.getName(), resourceInfo);
        }
    }

    // TODO We need to create StructInfoEntry
    private void createStructInfoEntries(StructDef[] structDefs) {
        for (StructDef structDef : structDefs) {

            // Add Struct name as an UTFCPEntry to the constant pool
            UTF8CPEntry structNameCPEntry = new UTF8CPEntry(structDef.getName());
            int structNameCPIndex = currentPkgInfo.addCPEntry(structNameCPEntry);

            StructInfo structInfo = new StructInfo(currentPkgCPIndex, structNameCPIndex);
            currentPkgInfo.addStructInfo(structDef.getName(), structInfo);

            VariableDefStmt[] fieldDefStmts = structDef.getFieldDefStmts();
            BType[] structFieldTypes = new BType[fieldDefStmts.length];
            for (int i = 0; i < fieldDefStmts.length; i++) {
                VariableDefStmt fieldDefStmt = fieldDefStmts[i];
                VariableDef fieldDef = fieldDefStmt.getVariableDef();
                BType fieldType = fieldDef.getType();
                structFieldTypes[i] = fieldType;

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                StructVarLocation structVarLocation = new StructVarLocation(fieldIndex);
                fieldDef.setMemoryLocation(structVarLocation);
            }

            structInfo.setFieldCount(Arrays.copyOf(prepareIndexes(fieldIndexes), fieldIndexes.length));
            structInfo.setFieldTypes(structFieldTypes);
            resetIndexes(fieldIndexes);
        }
    }

    private void createConnectorInfoEntries(BallerinaConnectorDef[] connectorDefs) {
        for (BallerinaConnectorDef connectorDef : connectorDefs) {
            // Add Connector name as an UTFCPEntry to the constant pool
            UTF8CPEntry connectorNameCPEntry = new UTF8CPEntry(connectorDef.getName());
            int connectorNameCPIndex = currentPkgInfo.addCPEntry(connectorNameCPEntry);

            ConnectorInfo connectorInfo = new ConnectorInfo(currentPkgCPIndex, connectorNameCPIndex);
            currentPkgInfo.addConnectorInfo(connectorDef.getName(), connectorInfo);

            // TODO Temporary solution to get both executors working
            int fieldTypeCount = 0;
            BType[] connectorFieldTypes = new BType[connectorDef.getParameterDefs().length +
                    connectorDef.getVariableDefStmts().length];

            // Assign field indexes for Connector parameters
            for (ParameterDef parameterDef : connectorDef.getParameterDefs()) {
                BType fieldType = parameterDef.getType();
                connectorFieldTypes[fieldTypeCount++] = fieldType;

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                ConnectorVarLocation connectorVarLocation = new ConnectorVarLocation(fieldIndex);
                parameterDef.setMemoryLocation(connectorVarLocation);
            }

            // Assign field indexes for Connector variables
            for (VariableDefStmt varDefStmt : connectorDef.getVariableDefStmts()) {
                VariableDef varDef = varDefStmt.getVariableDef();
                BType fieldType = varDef.getType();
                connectorFieldTypes[fieldTypeCount++] = fieldType;

                int fieldIndex = getNextIndex(fieldType.getTag(), fieldIndexes);
                ConnectorVarLocation connectorVarLocation = new ConnectorVarLocation(fieldIndex);
                varDef.setMemoryLocation(connectorVarLocation);
            }

            connectorInfo.setFieldCount(Arrays.copyOf(prepareIndexes(fieldIndexes), fieldIndexes.length));
            connectorInfo.setFieldTypes(connectorFieldTypes);
            resetIndexes(fieldIndexes);

            // Create the init function info
            createFunctionInfoEntries(new Function[]{connectorDef.getInitFunction()});

            // Create function info entries for all actions
            createActionInfoEntries(connectorDef.getActions(), connectorInfo);

            // Create the init native action info
            if (connectorDef.getInitAction() != null) {
                createActionInfoEntries(new Action[]{connectorDef.getInitAction()}, connectorInfo);
            }
        }
    }

    private void createActionInfoEntries(Action[] actions, ConnectorInfo connectorInfo) {
        for (Action action : actions) {
            // Add action name as an UTFCPEntry to the constant pool
            UTF8CPEntry actionNameCPEntry = new UTF8CPEntry(action.getName());
            int actionNameCPIndex = currentPkgInfo.addCPEntry(actionNameCPEntry);

            ActionInfo actionInfo = new ActionInfo(currentPkgPath, currentPkgCPIndex,
                    action.getName(), actionNameCPIndex);
            actionInfo.setParamTypes(getParamTypes(action.getParameterDefs()));
            actionInfo.setRetParamTypes(getParamTypes(action.getReturnParameters()));
            actionInfo.setNative(action.isNative());
            actionInfo.setPackageInfo(currentPkgInfo);

            connectorInfo.addActionInfo(action.getName(), actionInfo);
        }
    }

    private void createFunctionInfoEntries(Function[] functions) {
        for (Function function : functions) {

            // Add function name as an UTFCPEntry to the constant pool
            UTF8CPEntry funcNameCPEntry = new UTF8CPEntry(function.getName());
            int funcNameCPIndex = currentPkgInfo.addCPEntry(funcNameCPEntry);

            FunctionInfo funcInfo = new FunctionInfo(currentPkgPath, currentPkgCPIndex,
                    function.getName(), funcNameCPIndex);
            funcInfo.setParamTypes(getParamTypes(function.getParameterDefs()));
            funcInfo.setRetParamTypes(getParamTypes(function.getReturnParameters()));
            funcInfo.setNative(function.isNative());
            funcInfo.setPackageInfo(currentPkgInfo);

            currentPkgInfo.addFunctionInfo(function.getName(), funcInfo);
        }
    }

    @Override
    public void visit(BallerinaFile bFile) {

    }

    @Override
    public void visit(ImportPackage importPkg) {

    }

    @Override
    public void visit(ConstDef constant) {

    }

    @Override
    public void visit(GlobalVariableDef globalVar) {

    }

    @Override
    public void visit(Service service) {
//        BallerinaFunction initFunction = connectorDef.getInitFunction();
//        visit(initFunction);

        currentServiceInfo = currentPkgInfo.getServiceInfo(service.getName());
        AnnotationAttachment[] annotationAttachments = service.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            currentServiceInfo.addAttributeInfo(AttributeInfo.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        for (Resource resource : service.getResources()) {
            resource.accept(this);
        }
    }

    @Override
    public void visit(BallerinaConnectorDef connectorDef) {
        BallerinaFunction initFunction = connectorDef.getInitFunction();
        visit(initFunction);

        ConnectorInfo connectorInfo = currentPkgInfo.getConnectorInfo(connectorDef.getName());
        AnnotationAttachment[] annotationAttachments = connectorDef.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            connectorInfo.addAttributeInfo(AttributeInfo.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        for (BallerinaAction action : connectorDef.getActions()) {
            action.accept(this);
        }
    }

    @Override
    public void visit(Resource resource) {
        callableUnitInfo = currentServiceInfo.getResourceInfo(resource.getName());

        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry(AttributeInfo.CODE_ATTRIBUTE);
        int codeAttribNameIndex = currentPkgInfo.addCPEntry(codeUTF8CPEntry);
        callableUnitInfo.codeAttributeInfo.setAttributeNameIndex(codeAttribNameIndex);
        callableUnitInfo.codeAttributeInfo.setCodeAddrs(nextIP());

        // Read annotations attached to this function
        AnnotationAttachment[] annotationAttachments = resource.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            callableUnitInfo.addAttributeInfo(AttributeInfo.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        // Add local variable indexes to the parameters and return parameters
        visitCallableUnitParameterDefs(resource.getParameterDefs(), callableUnitInfo);

        resource.getCallableUnitBody().accept(this);

        endCallableUnit();
    }

    @Override
    public void visit(BallerinaFunction function) {
        callableUnitInfo = currentPkgInfo.getFunctionInfo(function.getName());

        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry(AttributeInfo.CODE_ATTRIBUTE);
        int codeAttribNameIndex = currentPkgInfo.addCPEntry(codeUTF8CPEntry);
        callableUnitInfo.codeAttributeInfo.setAttributeNameIndex(codeAttribNameIndex);
        callableUnitInfo.codeAttributeInfo.setCodeAddrs(nextIP());

        // Read annotations attached to this function
        AnnotationAttachment[] annotationAttachments = function.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo annotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            callableUnitInfo.addAttributeInfo(AttributeInfo.ANNOTATIONS_ATTRIBUTE, annotationsAttribute);
        }

        // Add local variable indexes to the parameters and return parameters
        visitCallableUnitParameterDefs(function.getParameterDefs(), callableUnitInfo);

        // Visit return parameter defs
        for (ParameterDef parameterDef : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
                parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            }

            parameterDef.accept(this);
        }

        if (!function.isNative()) {
            function.getCallableUnitBody().accept(this);
        }

        endCallableUnit();
    }

    @Override
    public void visit(BTypeMapper typeMapper) {

    }

    @Override
    public void visit(BallerinaAction action) {
        ConnectorInfo connectorInfo = currentPkgInfo.getConnectorInfo(action.getConnectorDef().getName());

        // Now find out the ActionInfo
        callableUnitInfo = connectorInfo.getActionInfo(action.getName());

        UTF8CPEntry codeUTF8CPEntry = new UTF8CPEntry("Code");
        int codeAttribNameCPIndex = currentPkgInfo.addCPEntry(codeUTF8CPEntry);
        callableUnitInfo.codeAttributeInfo.setAttributeNameIndex(codeAttribNameCPIndex);
        callableUnitInfo.codeAttributeInfo.setCodeAddrs(nextIP());

        // Read annotations attached to this function
        AnnotationAttachment[] annotationAttachments = action.getAnnotations();
        if (annotationAttachments.length > 0) {
            AnnotationAttributeInfo paramAnnotationsAttribute = getAnnotationAttributeInfo(annotationAttachments);
            callableUnitInfo.addAttributeInfo(AttributeInfo.ANNOTATIONS_ATTRIBUTE, paramAnnotationsAttribute);
        }

        // Add local variable indexes to the parameters and return parameters
        visitCallableUnitParameterDefs(action.getParameterDefs(), callableUnitInfo);

        for (ParameterDef parameterDef : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (parameterDef.getName() != null) {
                int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
                parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            }

            parameterDef.accept(this);
        }

        if (!action.isNative()) {
            action.getCallableUnitBody().accept(this);
        }

        endCallableUnit();
    }

    @Override
    public void visit(Worker worker) {

    }

    @Override
    public void visit(AnnotationAttachment annotation) {

    }

    @Override
    public void visit(ParameterDef parameterDef) {

    }

    @Override
    public void visit(VariableDef variableDef) {

    }

    @Override
    public void visit(StructDef structDef) {

    }

    @Override
    public void visit(AnnotationAttributeDef annotationAttributeDef) {

    }

    @Override
    public void visit(AnnotationDef annotationDef) {
    }

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        int opcode;
        int lvIndex;

        Expression rhsExpr = varDefStmt.getRExpr();
        if (rhsExpr != null) {
            rhsExpr.accept(this);
            rhsExprRegIndex = rhsExpr.getTempOffset();
        } else {
            // TODO get the default value;
        }

        MemoryLocation stackVarLocation;
        VariableDef variableDef = varDefStmt.getVariableDef();

        MemoryLocation memoryLocation = varDefStmt.getVariableDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableDef.getType().getTag(),
                    InstructionCodes.ISTORE, lvIndexes);
            opcode = opcodeAndIndex.opcode;
            lvIndex = opcodeAndIndex.index;
            stackVarLocation = new StackVarLocation(lvIndex);
            variableDef.setMemoryLocation(stackVarLocation);
            if (rhsExpr != null) {
                emit(opcode, rhsExpr.getTempOffset(), lvIndex);
            }

        } else {
            // TODO
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        // Evaluate the rhs expression
        Expression rExpr = assignStmt.getRExpr();
        if (rExpr == null) {
            return;
        }

        rExpr.accept(this);

        int[] rhsExprRegIndexes;
        if (assignStmt.getRExpr() instanceof CallableUnitInvocationExpr) {
            rhsExprRegIndexes = ((CallableUnitInvocationExpr) assignStmt.getRExpr()).getOffsets();
        } else {
            rhsExprRegIndexes = new int[]{assignStmt.getRExpr().getTempOffset()};
        }

        Expression[] lhsExprs = assignStmt.getLExprs();
        for (int i = 0; i < lhsExprs.length; i++) {
            rhsExprRegIndex = rhsExprRegIndexes[i];
            Expression lExpr = lhsExprs[i];

            if (lExpr instanceof VariableRefExpr) {
                varAssignment = true;
                lExpr.accept(this);
                varAssignment = false;
            } else if (lExpr instanceof ArrayMapAccessExpr) {
                arrayMapAssignment = true;
                lExpr.accept(this);
                arrayMapAssignment = false;
            } else if (lExpr instanceof FieldAccessExpr) {
                structAssignment = true;
                lExpr.accept(this);
                structAssignment = false;
            }
        }
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            stmt.accept(this);

            for (int i = 0; i < maxRegIndexes.length; i++) {
                if (maxRegIndexes[i] < regIndexes[i]) {
                    maxRegIndexes[i] = regIndexes[i];
                }
            }

            resetIndexes(regIndexes);
        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        // TODO Support null checks
        Expression ifCondExpr = ifElseStmt.getCondition();
        ifCondExpr.accept(this);
        Instruction gotoInstruction;
        List<Instruction> gotoInstructionList = new ArrayList<>();

        int opcode = getIfOpcode(ifCondExpr);

        // TODO operand2 should be the jump address  else-if or else or to the next instruction after then block
        Instruction ifInstruction = new Instruction(opcode, regIndexes[BOOL_OFFSET], 0);
        emit(ifInstruction);

        ifElseStmt.getThenBody().accept(this);

        // Check whether this then block is the last block of code
        gotoInstruction = new Instruction(InstructionCodes.GOTO, 0);
        emit(gotoInstruction);
        gotoInstructionList.add(gotoInstruction);

        ifInstruction.setOperand(1, nextIP());

        // Process else-if parts
        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);
            opcode = getIfOpcode(elseIfCondition);
            ifInstruction = new Instruction(opcode, regIndexes[BOOL_OFFSET], 0);
            emit(ifInstruction);

            elseIfBlock.getElseIfBody().accept(this);
            gotoInstruction = new Instruction(InstructionCodes.GOTO, 0);
            emit(gotoInstruction);
            gotoInstructionList.add(gotoInstruction);
            ifInstruction.setOperand(1, nextIP());
            // TODO check whether there exits 'next' instruction
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }

        int nextIP = nextIP();
        for (Instruction instruction : gotoInstructionList) {
            instruction.setOperand(0, nextIP);
        }
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        if (replyStmt.getReplyExpr() != null) {
            replyStmt.getReplyExpr().accept(this);
            emit(InstructionCodes.REP, replyStmt.getReplyExpr().getTempOffset());
        } else {
            emit(InstructionCodes.REP, -1);
        }
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        int[] regIndexes;
        if (returnStmt.getExprs().length == 1 &&
                returnStmt.getExprs()[0] instanceof ExecutableMultiReturnExpr) {
            ExecutableMultiReturnExpr multiReturnExpr = (ExecutableMultiReturnExpr) returnStmt.getExprs()[0];
            returnStmt.getExprs()[0].accept(this);
            regIndexes = multiReturnExpr.getOffsets();

        } else {
            regIndexes = new int[returnStmt.getExprs().length];
            for (int i = 0; i < returnStmt.getExprs().length; i++) {
                Expression expr = returnStmt.getExprs()[i];
                expr.accept(this);
                regIndexes[i] = expr.getTempOffset();
            }
        }

        FunctionReturnCPEntry funcRetCPEntry = new FunctionReturnCPEntry(regIndexes);
        int funcRetCPEntryIndex = currentPkgInfo.addCPEntry(funcRetCPEntry);
        emit(InstructionCodes.RET, funcRetCPEntryIndex);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression conditionExpr = whileStmt.getCondition();
        Instruction gotoInstruction = new Instruction(InstructionCodes.GOTO, nextIP());

        conditionExpr.accept(this);
        int opcode = getIfOpcode(conditionExpr);
        Instruction ifInstruction = new Instruction(opcode, regIndexes[BOOL_OFFSET], 0);
        emit(ifInstruction);

        breakInstructions.push(new ArrayList<>());
        whileStmt.getBody().accept(this);

        emit(gotoInstruction);
        int nextIP = nextIP();
        ifInstruction.setOperand(1, nextIP);
        List<Instruction> brkInstructions = breakInstructions.pop();
        for (Instruction instruction : brkInstructions) {
            instruction.setOperand(0, nextIP);
        }
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        Instruction gotoInstruction = new Instruction(InstructionCodes.GOTO, 0);
        emit(gotoInstruction);
        breakInstructions.peek().add(gotoInstruction);
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {

    }

    @Override
    public void visit(ThrowStmt throwStmt) {

    }

    @Override
    public void visit(FunctionInvocationStmt functionIStmt) {
        visit(functionIStmt.getFunctionInvocationExpr());
    }

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        visit(actionIStmt.getActionInvocationExpr());
    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {

    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {

    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {

    }

    @Override
    public void visit(TransformStmt transformStmt) {
        transformStmt.getBody().accept(this);
    }

    @Override
    public void visit(TransactionRollbackStmt transactionRollbackStmt) {

    }

    @Override
    public void visit(AbortStmt abortStmt) {

    }


    // Expressions

    @Override
    public void visit(BasicLiteral basicLiteral) {
        int opcode;
        int typeTag = basicLiteral.getType().getTag();

        switch (typeTag) {
            case TypeTags.INT_TAG:
                basicLiteral.setTempOffset(++regIndexes[INT_OFFSET]);
                long intVal = basicLiteral.getBValue().intValue();
                if (intVal >= 0 && intVal <= 5) {
                    opcode = InstructionCodes.ICONST_0 + (int) intVal;
                    emit(opcode, basicLiteral.getTempOffset());
                } else {
                    IntegerCPEntry intCPEntry = new IntegerCPEntry(basicLiteral.getBValue().intValue());
                    int intCPEntryIndex = currentPkgInfo.addCPEntry(intCPEntry);
                    emit(InstructionCodes.ICONST, intCPEntryIndex, basicLiteral.getTempOffset());
                }
                break;

            case TypeTags.FLOAT_TAG:
                basicLiteral.setTempOffset(++regIndexes[FLOAT_OFFSET]);
                double floatVal = basicLiteral.getBValue().floatValue();
                if (floatVal == 0 || floatVal == 1 || floatVal == 2 ||
                        floatVal == 3 || floatVal == 4 || floatVal == 5) {
                    opcode = InstructionCodes.FCONST_0 + (int) floatVal;
                    emit(opcode, basicLiteral.getTempOffset());
                } else {
                    FloatCPEntry floatCPEntry = new FloatCPEntry(basicLiteral.getBValue().floatValue());
                    int floatCPEntryIndex = currentPkgInfo.addCPEntry(floatCPEntry);
                    emit(InstructionCodes.FCONST, floatCPEntryIndex, basicLiteral.getTempOffset());
                }
                break;

            case TypeTags.STRING_TAG:
                basicLiteral.setTempOffset(++regIndexes[STRING_OFFSET]);
                String strValue = basicLiteral.getBValue().stringValue();
                UTF8CPEntry utf8CPEntry = new UTF8CPEntry(strValue);
                int stringValCPIndex = currentPkgInfo.addCPEntry(utf8CPEntry);

                StringCPEntry stringCPEntry = new StringCPEntry(stringValCPIndex, strValue);
                int strCPIndex = currentPkgInfo.addCPEntry(stringCPEntry);

                emit(InstructionCodes.SCONST, strCPIndex, basicLiteral.getTempOffset());
                break;

            case TypeTags.BOOLEAN_TAG:
                basicLiteral.setTempOffset(++regIndexes[BOOL_OFFSET]);
                boolean booleanVal = basicLiteral.getBValue().booleanValue();
                if (!booleanVal) {
                    opcode = InstructionCodes.BCONST_0;
                } else {
                    opcode = InstructionCodes.BCONST_1;
                }
                emit(opcode, basicLiteral.getTempOffset());
                break;
        }
    }

    @Override
    public void visit(NullLiteral nullLiteral) {
        int regIndex = ++regIndexes[REF_OFFSET];
        nullLiteral.setTempOffset(regIndex);
        emit(InstructionCodes.RCONST_NULL, regIndex);
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {
        Expression rExpr = unaryExpr.getRExpr();
        rExpr.accept(this);

        OpcodeAndIndex opcodeAndIndex;
        int opcode;
        int exprIndex;
        if (Operator.SUB.equals(unaryExpr.getOperator())) {
            opcodeAndIndex = getOpcodeAndIndex(unaryExpr.getType().getTag(),
                    InstructionCodes.INEG, regIndexes);
            opcode = opcodeAndIndex.opcode;
            exprIndex = opcodeAndIndex.index;
            emit(opcode, rExpr.getTempOffset(), exprIndex);

        } else if (Operator.NOT.equals(unaryExpr.getOperator())) {

            // TODO
            exprIndex = rExpr.getTempOffset();
        } else {
            // "+" operator
            // Nothing to do
            exprIndex = rExpr.getTempOffset();
        }

        unaryExpr.setTempOffset(exprIndex);
    }


    // Binary arithmetic expressions

    @Override
    public void visit(AddExpression addExpr) {
        emitBinaryArithmeticExpr(addExpr, InstructionCodes.IADD);
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        emitBinaryArithmeticExpr(subtractExpr, InstructionCodes.ISUB);
    }

    @Override
    public void visit(MultExpression multExpr) {
        emitBinaryArithmeticExpr(multExpr, InstructionCodes.IMUL);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        emitBinaryArithmeticExpr(divideExpr, InstructionCodes.IDIV);
    }

    @Override
    public void visit(ModExpression modExpr) {
        emitBinaryArithmeticExpr(modExpr, InstructionCodes.IMOD);
    }


    // Binary logical expressions

    @Override
    public void visit(AndExpression andExpression) {

    }

    @Override
    public void visit(OrExpression orExpression) {

    }


    // Binary equality expressions

    @Override
    public void visit(EqualExpression equalExpr) {
        emitBinaryCompareAndEqualityExpr(equalExpr, InstructionCodes.ICMP);
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        emitBinaryCompareAndEqualityExpr(notEqualExpr, InstructionCodes.ICMP);
    }


    // Binary comparison expressions

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        emitBinaryCompareAndEqualityExpr(greaterEqualExpr, InstructionCodes.ICMP);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        emitBinaryCompareAndEqualityExpr(greaterThanExpr, InstructionCodes.ICMP);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        emitBinaryCompareAndEqualityExpr(lessEqualExpr, InstructionCodes.ICMP);
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        emitBinaryCompareAndEqualityExpr(lessThanExpr, InstructionCodes.ICMP);
    }


    // Callable unit invocation expressions

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        int pkgCPIndex = addPackageCPEntry(funcIExpr.getPackagePath());

        String funcName = funcIExpr.getName();
        UTF8CPEntry funcNameCPEntry = new UTF8CPEntry(funcName);
        int funcNameCPIndex = currentPkgInfo.addCPEntry(funcNameCPEntry);

        // Find the package info entry of the function and from the package info entry find the function info entry
        String pkgPath = funcIExpr.getPackagePath();
        PackageInfo funcPackageInfo = programFile.getPackageInfo(pkgPath);
        FunctionInfo functionInfo = funcPackageInfo.getFunctionInfo(funcName);

        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgCPIndex, funcNameCPIndex);
        funcRefCPEntry.setFunctionInfo(functionInfo);
        int funcRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);
        int funcCallIndex = getCallableUnitCallCPIndex(funcIExpr);

        if (functionInfo.isNative()) {
            // TODO Move this to the place where we create function info entry
            functionInfo.setNativeFunction((AbstractNativeFunction) funcIExpr.getCallableUnit());
            emit(InstructionCodes.NCALL, funcRefCPIndex, funcCallIndex);
        } else {
            emit(InstructionCodes.CALL, funcRefCPIndex, funcCallIndex);
        }
    }

    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        int pkgCPIndex = addPackageCPEntry(actionIExpr.getPackagePath());
        BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) actionIExpr.getArgExprs()[0].getType();

        String pkgPath = actionIExpr.getPackagePath();
        PackageInfo actionPackageInfo = programFile.getPackageInfo(pkgPath);

        // Get the connector ref CP index
        ConnectorInfo connectorInfo = actionPackageInfo.getConnectorInfo(connectorDef.getName());
        int connectorRefCPIndex = getConnectorRefCPIndex(connectorDef);

        String actionName = actionIExpr.getName();
        UTF8CPEntry actionNameCPEntry = new UTF8CPEntry(actionName);
        int actionNameCPIndex = currentPkgInfo.addCPEntry(actionNameCPEntry);

        ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgCPIndex, connectorRefCPIndex, actionNameCPIndex);
        ActionInfo actionInfo = connectorInfo.getActionInfo(actionName);
        actionRefCPEntry.setActionInfo(actionInfo);
        int actionRefCPIndex = currentPkgInfo.addCPEntry(actionRefCPEntry);
        int actionCallIndex = getCallableUnitCallCPIndex(actionIExpr);

        if (actionInfo.isNative()) {
            // TODO Move this to the place where we create action info entry
            actionInfo.setNativeAction((AbstractNativeAction) actionIExpr.getCallableUnit());
            emit(InstructionCodes.NACALL, actionRefCPIndex, actionCallIndex);
        } else {
            emit(InstructionCodes.ACALL, actionRefCPIndex, actionCallIndex);
        }
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {

    }

    @Override
    public void visit(TypeCastExpression typeCastExpr) {
        Expression rExpr = typeCastExpr.getRExpr();
        rExpr.accept(this);

        // TODO Handle multi-return support

        int opCode = typeCastExpr.getOpcode();
//        if (opCode < 0) {
//            throw new IllegalStateException("Instruction not supported");
//        }

        // Ignore  NOP opcode
        if (opCode != 0) {
            int targetRegIndex = getNextIndex(typeCastExpr.getType().getTag(), regIndexes);
            typeCastExpr.setTempOffset(targetRegIndex);
            typeCastExpr.setOffsets(new int[]{targetRegIndex});
            emit(opCode, rExpr.getTempOffset(), targetRegIndex, -1);
        } else {
            // TODO improve
            typeCastExpr.setTempOffset(rExpr.getTempOffset());
            typeCastExpr.setOffsets(new int[]{rExpr.getTempOffset()});
        }
    }

    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {
        Expression rExpr = typeConversionExpr.getRExpr();
        rExpr.accept(this);

        // TODO Handle multi-return support

        int opCode = typeConversionExpr.getOpcode();
//        if (opCode < 0) {
//            throw new IllegalStateException("Instruction not supported");
//        }

        // Ignore  NOP opcode
        if (opCode != 0) {
            int targetRegIndex = getNextIndex(typeConversionExpr.getType().getTag(), regIndexes);
            typeConversionExpr.setTempOffset(targetRegIndex);
            typeConversionExpr.setOffsets(new int[]{targetRegIndex});
            emit(opCode, rExpr.getTempOffset(), targetRegIndex, -1);
        } else {
            // TODO improve
            typeConversionExpr.setTempOffset(rExpr.getTempOffset());
            typeConversionExpr.setOffsets(new int[]{rExpr.getTempOffset()});
        }
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {

    }


    // Init expressions

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        BType elementType = ((BArrayType) arrayInitExpr.getType()).getElementType();

        // Emit create array instruction
        int opcode = getOpcode(elementType.getTag(), InstructionCodes.INEWARRAY);
        int arrayVarRegIndex = ++regIndexes[REF_OFFSET];
        arrayInitExpr.setTempOffset(arrayVarRegIndex);
        emit(opcode, arrayVarRegIndex);

        // Emit instructions populate initial array values;
        Expression[] argExprs = arrayInitExpr.getArgExprs();
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);

            BasicLiteral indexLiteral = new BasicLiteral(arrayInitExpr.getNodeLocation(),
                    null, new BInteger(i));
            indexLiteral.setType(BTypes.typeInt);
            indexLiteral.accept(this);

            opcode = getOpcode(argExpr.getType().getTag(), InstructionCodes.IASTORE);
            emit(opcode, arrayVarRegIndex, indexLiteral.getTempOffset(), argExpr.getTempOffset());
        }
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        int varRegIndex = ++regIndexes[REF_OFFSET];
        refTypeInitExpr.setTempOffset(varRegIndex);

        BType bType = refTypeInitExpr.getType();
        if (bType == BTypes.typeMessage) {
            emit(InstructionCodes.NEWMESSAGE, varRegIndex);
        } else if (bType == BTypes.typeDatatable) {
            emit(InstructionCodes.NEWDATATABLE, varRegIndex);
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connectorInitExpr.getType();
        PackageInfo connectorPkgInfo = programFile.getPackageInfo(connectorDef.getPackagePath());
        int pkgCPIndex = addPackageCPEntry(connectorDef.getPackagePath());

        UTF8CPEntry nameUTF8CPEntry = new UTF8CPEntry(connectorDef.getName());
        int nameIndex = currentPkgInfo.getCPEntryIndex(nameUTF8CPEntry);

        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, nameIndex);
        ConnectorInfo connectorInfo = connectorPkgInfo.getConnectorInfo(connectorDef.getName());
        structureRefCPEntry.setStructureTypeInfo(connectorInfo);
        int structureRefCPIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);

        //Emit an instruction to create a new connector.
        int connectorRegIndex = ++regIndexes[REF_OFFSET];
        emit(InstructionCodes.NEWCONNECTOR, structureRefCPIndex, connectorRegIndex);
        connectorInitExpr.setTempOffset(connectorRegIndex);

        // Set all the connector arguments
        Expression[] argExprs = connectorInitExpr.getArgExprs();
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);

            ParameterDef paramDef = connectorDef.getParameterDefs()[i];
            int fieldIndex = ((ConnectorVarLocation) paramDef.getMemoryLocation()).getConnectorMemAddrOffset();

            int opcode = getOpcode(paramDef.getType().getTag(), InstructionCodes.IFIELDSTORE);
            emit(opcode, connectorRegIndex, fieldIndex, argExpr.getTempOffset());
        }

        // Invoke Connector init function
        Function initFunction = connectorDef.getInitFunction();

        UTF8CPEntry nameCPEntry = new UTF8CPEntry(initFunction.getName());
        int initFuncNameIndex = currentPkgInfo.addCPEntry(nameCPEntry);

        FunctionRefCPEntry funcRefCPEntry = new FunctionRefCPEntry(pkgCPIndex, initFuncNameIndex);
        funcRefCPEntry.setFunctionInfo(connectorPkgInfo.getFunctionInfo(initFunction.getName()));
        int initFuncRefCPIndex = currentPkgInfo.addCPEntry(funcRefCPEntry);

        FunctionCallCPEntry initFuncCallCPEntry = new FunctionCallCPEntry(new int[]{connectorRegIndex}, new int[0]);
        int initFuncCallIndex = currentPkgInfo.addCPEntry(initFuncCallCPEntry);

        emit(InstructionCodes.CALL, initFuncRefCPIndex, initFuncCallIndex);

        // Invoke Connector init native action if any
        Action action = connectorDef.getInitAction();
        if (action == null) {
            return;
        }

        String actionName = action.getName();
        UTF8CPEntry actionNameCPEntry = new UTF8CPEntry(actionName);
        int actionNameCPIndex = currentPkgInfo.addCPEntry(actionNameCPEntry);
        int connectorRefCPIndex = getConnectorRefCPIndex(connectorDef);
        ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgCPIndex, connectorRefCPIndex, actionNameCPIndex);

        ActionInfo actionInfo = connectorInfo.getActionInfo(actionName);
        actionRefCPEntry.setActionInfo(actionInfo);
        int actionRefCPIndex = currentPkgInfo.addCPEntry(actionRefCPEntry);

        actionInfo.setNativeAction((AbstractNativeAction) action);
        emit(InstructionCodes.NACALL, actionRefCPIndex, initFuncCallIndex);
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        StructDef structDef = (StructDef) structInitExpr.getType();
        int pkgCPIndex = addPackageCPEntry(structDef.getPackagePath());

        UTF8CPEntry structNameCPEntry = new UTF8CPEntry(structDef.getName());
        int structNameCPIndex = currentPkgInfo.addCPEntry(structNameCPEntry);

        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex, structNameCPIndex);
        structureRefCPEntry.setStructureTypeInfo(currentPkgInfo.getStructInfo(structDef.getName()));
        int structCPEntryIndex = currentPkgInfo.addCPEntry(structureRefCPEntry);

        //Emit an instruction to create a new struct.
        int structRegIndex = ++regIndexes[REF_OFFSET];
        emit(InstructionCodes.NEWSTRUCT, structCPEntryIndex, structRegIndex);
        structInitExpr.setTempOffset(structRegIndex);

        List<String> initializedFieldNameList = new ArrayList<>(structDef.getFieldDefStmts().length);

        for (Expression expr : structInitExpr.getArgExprs()) {
            KeyValueExpr keyValueExpr = (KeyValueExpr) expr;
            VariableRefExpr varRefExpr = (VariableRefExpr) keyValueExpr.getKeyExpr();
            int fieldIndex = ((StructVarLocation) varRefExpr.getMemoryLocation()).getStructMemAddrOffset();

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            int opcode = getOpcode(varRefExpr.getType().getTag(), InstructionCodes.IFIELDSTORE);
            emit(opcode, structRegIndex, fieldIndex, valueExpr.getTempOffset());
            initializedFieldNameList.add(varRefExpr.getVarName());
        }

        // Initialize default values in a struct definition
        for (VariableDefStmt fieldDefStmt : structDef.getFieldDefStmts()) {
            VariableRefExpr varRefExpr = (VariableRefExpr) fieldDefStmt.getLExpr();
            if (fieldDefStmt.getRExpr() == null || initializedFieldNameList.contains(varRefExpr.getVarName())) {
                continue;
            }

            int fieldIndex = ((StructVarLocation) varRefExpr.getMemoryLocation()).getStructMemAddrOffset();
            fieldDefStmt.getRExpr().accept(this);

            int opcode = getOpcode(varRefExpr.getType().getTag(), InstructionCodes.IFIELDSTORE);
            emit(opcode, structRegIndex, fieldIndex, fieldDefStmt.getRExpr().getTempOffset());
        }
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        int mapVarRegIndex = ++regIndexes[REF_OFFSET];
        mapInitExpr.setTempOffset(mapVarRegIndex);
        emit(InstructionCodes.NEWMAP, mapVarRegIndex);

        // Handle Map init stuff
        Expression[] argExprs = mapInitExpr.getArgExprs();
        for (Expression argExpr : argExprs) {
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;

            Expression keyExpr = keyValueExpr.getKeyExpr();
            keyExpr.accept(this);

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            emit(InstructionCodes.MAPSTORE, mapVarRegIndex, keyExpr.getTempOffset(), valueExpr.getTempOffset());
        }
    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {
        int jsonVarRegIndex = ++regIndexes[REF_OFFSET];
        jsonInitExpr.setTempOffset(jsonVarRegIndex);
        emit(InstructionCodes.NEWJSON, jsonVarRegIndex);

        Expression[] argExprs = jsonInitExpr.getArgExprs();
        for (Expression argExpr : argExprs) {
            KeyValueExpr keyValueExpr = (KeyValueExpr) argExpr;

            Expression keyExpr = keyValueExpr.getKeyExpr();
            keyExpr.accept(this);

            Expression valueExpr = keyValueExpr.getValueExpr();
            valueExpr.accept(this);

            emit(InstructionCodes.JSONSTORE, jsonVarRegIndex, keyExpr.getTempOffset(), valueExpr.getTempOffset());
        }

    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {
        int jsonVarRegIndex = ++regIndexes[REF_OFFSET];
        jsonArrayInitExpr.setTempOffset(jsonVarRegIndex);
        Expression[] argExprs = jsonArrayInitExpr.getArgExprs();

        BasicLiteral arraySizeLiteral = new BasicLiteral(jsonArrayInitExpr.getNodeLocation(),
                null, new BInteger(argExprs.length));
        arraySizeLiteral.setType(BTypes.typeInt);
        arraySizeLiteral.accept(this);

        emit(InstructionCodes.JSONNEWARRAY, jsonVarRegIndex, arraySizeLiteral.getTempOffset());

        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);

            BasicLiteral indexLiteral = new BasicLiteral(jsonArrayInitExpr.getNodeLocation(),
                    null, new BInteger(i));
            indexLiteral.setType(BTypes.typeInt);
            indexLiteral.accept(this);

            emit(InstructionCodes.JSONASTORE, jsonVarRegIndex, indexLiteral.getTempOffset(), argExpr.getTempOffset());
        }
    }

    @Override
    public void visit(KeyValueExpr keyValueExpr) {

    }


    // Variable reference expressions

    @Override
    public void visit(FieldAccessExpr fieldAccessExpr) {
        FieldAccessExpr childSFAccessExpr = fieldAccessExpr;

        while (true) {
            boolean isAssignment = childSFAccessExpr.getFieldExpr() == null && structAssignment;

            int regIndex = -1;
            if (childSFAccessExpr instanceof JSONFieldAccessExpr) {
                Expression varRef = childSFAccessExpr.getVarRef();
                int jsonValueRegIndex = regIndexes[REF_OFFSET];
                varRef.accept(this);

                if (varRef.getType() == BTypes.typeString) {
                    if (isAssignment) {
                        emit(InstructionCodes.JSONSTORE, jsonValueRegIndex, varRef.getTempOffset(), rhsExprRegIndex);
                    } else {
                        regIndex = ++regIndexes[REF_OFFSET];
                        emit(InstructionCodes.JSONLOAD, jsonValueRegIndex, varRef.getTempOffset(), regIndex);
                    }
                } else if (varRef.getType() == BTypes.typeInt) {
                    // JSON array access
                    if (isAssignment) {
                        emit(InstructionCodes.JSONASTORE, jsonValueRegIndex, varRef.getTempOffset(), rhsExprRegIndex);
                    } else {
                        regIndex = ++regIndexes[REF_OFFSET];
                        emit(InstructionCodes.JSONALOAD, jsonValueRegIndex, varRef.getTempOffset(), regIndex);
                    }
                } else {
                    throw new BallerinaException("Invalid json access field type: " + varRef.getType());
                }

            } else {
                ReferenceExpr referenceExpr = (ReferenceExpr) childSFAccessExpr.getVarRef();
                if (referenceExpr instanceof VariableRefExpr) {
                    varAssignment = isAssignment;
                    referenceExpr.accept(this);
                    varAssignment = false;

                } else if (referenceExpr instanceof ArrayMapAccessExpr) {
                    arrayMapAssignment = isAssignment;
                    referenceExpr.accept(this);
                    arrayMapAssignment = false;
                }
                regIndex = referenceExpr.getTempOffset();
            }

            if (isAssignment || childSFAccessExpr.getFieldExpr() == null) {
                fieldAccessExpr.setTempOffset(regIndex);
                break;
            }
            childSFAccessExpr = childSFAccessExpr.getFieldExpr();
        }
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        Expression arrayMapVarExpr = arrayMapAccessExpr.getRExpr();
        arrayMapVarExpr.accept(this);

        Expression[] indexExprs = arrayMapAccessExpr.getIndexExprs();
        if (arrayMapVarExpr.getType() == BTypes.typeMap) {
            // This is a map access expression
            Expression indexExpr = indexExprs[0];
            indexExpr.accept(this);

            if (arrayMapAssignment) {
                emit(InstructionCodes.MAPSTORE, arrayMapVarExpr.getTempOffset(),
                        indexExpr.getTempOffset(), rhsExprRegIndex);
            } else {
                int mapValueRegIndex = ++regIndexes[REF_OFFSET];
                emit(InstructionCodes.MAPLOAD, arrayMapVarExpr.getTempOffset(),
                        indexExpr.getTempOffset(), mapValueRegIndex);
                arrayMapAccessExpr.setTempOffset(mapValueRegIndex);
            }
            return;
        }

        // This is an array access expression
        for (int i = indexExprs.length - 1; i >= 0; i--) {
            // Here we assume that the array reference is stored in the current reference register;
            int arrayRegIndex = regIndexes[REF_OFFSET];

            Expression indexExpr = indexExprs[i];
            indexExpr.accept(this);

            if (i == 0) {
                if (arrayMapAssignment) {
                    int opcode = getOpcode(arrayMapAccessExpr.getType().getTag(), InstructionCodes.IASTORE);
                    emit(opcode, arrayRegIndex, indexExpr.getTempOffset(), rhsExprRegIndex);
                } else {
                    OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(arrayMapAccessExpr.getType().getTag(),
                            InstructionCodes.IALOAD, regIndexes);
                    arrayMapAccessExpr.setTempOffset(opcodeAndIndex.index);
                    emit(opcodeAndIndex.opcode, arrayRegIndex, indexExpr.getTempOffset(), opcodeAndIndex.index);
                }
            } else {
                // reg, index, reg
                emit(InstructionCodes.RALOAD, arrayRegIndex,
                        indexExpr.getTempOffset(), ++regIndexes[REF_OFFSET]);
            }
        }
    }

    @Override
    public void visit(JSONFieldAccessExpr jsonPathExpr) {

    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        int opcode;
        int exprRegIndex;

        MemoryLocation memoryLocation = variableRefExpr.getVariableDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            int lvIndex = ((StackVarLocation) memoryLocation).getStackFrameOffset();
            if (varAssignment) {
                opcode = getOpcode(variableRefExpr.getType().getTag(),
                        InstructionCodes.ISTORE);

                emit(opcode, rhsExprRegIndex, lvIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableRefExpr.getType().getTag(),
                        InstructionCodes.ILOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;
                emit(opcode, lvIndex, exprRegIndex);
                variableRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof StructVarLocation) {
            int fieldIndex = ((StructVarLocation) memoryLocation).getStructMemAddrOffset();

            // Since we are processing a struct field here, the struct reference must be stored in the current
            //  reference register index.
            int structRegIndex = regIndexes[REF_OFFSET];

            if (varAssignment) {
                opcode = getOpcode(variableRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDSTORE);
                emit(opcode, structRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;

                emit(opcode, structRegIndex, fieldIndex, exprRegIndex);
                variableRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof ConnectorVarLocation) {
            int fieldIndex = ((ConnectorVarLocation) memoryLocation).getConnectorMemAddrOffset();

            // Since we are processing a connector field here, the connector reference must be stored in the current
            //  reference register index.
            int connectorRegIndex = ++regIndexes[REF_OFFSET];

            // The connector is always the first parameter of the action
            emit(InstructionCodes.RLOAD, 0, connectorRegIndex);

            if (varAssignment) {
                opcode = getOpcode(variableRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDSTORE);
                emit(opcode, connectorRegIndex, fieldIndex, rhsExprRegIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableRefExpr.getType().getTag(),
                        InstructionCodes.IFIELDLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;

                emit(opcode, connectorRegIndex, fieldIndex, exprRegIndex);
                variableRefExpr.setTempOffset(exprRegIndex);
            }

        } else if (memoryLocation instanceof GlobalVarLocation) {
            int gvIndex = ((GlobalVarLocation) memoryLocation).getStaticMemAddrOffset();

            if (varAssignment) {
                opcode = getOpcode(variableRefExpr.getType().getTag(),
                        InstructionCodes.IGSTORE);

                emit(opcode, rhsExprRegIndex, gvIndex);
            } else {
                OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(variableRefExpr.getType().getTag(),
                        InstructionCodes.IGLOAD, regIndexes);
                opcode = opcodeAndIndex.opcode;
                exprRegIndex = opcodeAndIndex.index;
                emit(opcode, gvIndex, exprRegIndex);
                variableRefExpr.setTempOffset(exprRegIndex);
            }
        }
    }

    @Override
    public void visit(StackVarLocation stackVarLocation) {

    }

    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {

    }

    @Override
    public void visit(GlobalVarLocation globalVarLocation) {

    }

    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {

    }

    @Override
    public void visit(ConstantLocation constantLocation) {

    }

    @Override
    public void visit(StructVarLocation structVarLocation) {

    }

    @Override
    public void visit(ResourceInvocationExpr resourceIExpr) {

    }

    @Override
    public void visit(MainInvoker mainInvoker) {

    }

    @Override
    public void visit(WorkerVarLocation workerVarLocation) {

    }


    // Private methods

    private void endCallableUnit() {
        callableUnitInfo.codeAttributeInfo.setMaxLongLocalVars(lvIndexes[INT_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxDoubleLocalVars(lvIndexes[FLOAT_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxStringLocalVars(lvIndexes[STRING_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxIntLocalVars(lvIndexes[BOOL_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxBValueLocalVars(lvIndexes[REF_OFFSET] + 1);

        callableUnitInfo.codeAttributeInfo.setMaxLongRegs(maxRegIndexes[INT_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxDoubleRegs(maxRegIndexes[FLOAT_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxStringRegs(maxRegIndexes[STRING_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxIntRegs(maxRegIndexes[BOOL_OFFSET] + 1);
        callableUnitInfo.codeAttributeInfo.setMaxBValueRegs(maxRegIndexes[REF_OFFSET] + 1);
        callableUnitInfo = null;

        resetIndexes(lvIndexes);
        resetIndexes(regIndexes);
    }

    private void resetIndexes(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = -1;
        }
    }

    private int[] prepareIndexes(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] += 1;
        }
        return indexes;
    }

    private OpcodeAndIndex getOpcodeAndIndex(int typeTag, int baseOpcode, int[] indexes) {
        int index;
        int opcode;
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                index = ++indexes[INT_OFFSET];
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + FLOAT_OFFSET;
                index = ++indexes[FLOAT_OFFSET];
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + STRING_OFFSET;
                index = ++indexes[STRING_OFFSET];
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + BOOL_OFFSET;
                index = ++indexes[BOOL_OFFSET];
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                index = ++indexes[REF_OFFSET];
                break;
        }

        return new OpcodeAndIndex(opcode, index);
    }

    private int getNextIndex(int typeTag, int[] indexes) {
        return getOpcodeAndIndex(typeTag, -1, indexes).index;
    }

    private int getOpcode(int typeTag, int baseOpcode) {
        int opcode;
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + FLOAT_OFFSET;
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + STRING_OFFSET;
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + BOOL_OFFSET;
                break;
            default:
                opcode = baseOpcode + REF_OFFSET;
                break;
        }

        return opcode;
    }

    private String getFunctionDescriptor(Function function) {
        StringBuilder sb = new StringBuilder("(");
        ParameterDef[] paramDefs = function.getParameterDefs();
        sb.append(getParamDefSig(paramDefs));
        sb.append(")");

        ParameterDef[] retParamDefs = function.getReturnParameters();
        sb.append(getParamDefSig(retParamDefs));
        return sb.toString();
    }

    private String getParamDefSig(ParameterDef[] paramDefs) {
        StringBuilder sb = new StringBuilder();
        if (paramDefs.length == 0) {
            sb.append(TypeEnum.VOID.getSig());
        } else {
            for (int i = 0; i < paramDefs.length; i++) {
                sb.append(paramDefs[i].getType().getSig());
            }
        }
        return sb.toString();
    }

    private BType[] getParamTypes(ParameterDef[] paramDefs) {
        if (paramDefs.length == 0) {
            return new BType[0];
        }

        BType[] types = new BType[paramDefs.length];
        for (int i = 0; i < paramDefs.length; i++) {
            types[i] = getVMTypeFromSig(paramDefs[i].getType().getSig());
        }

        return types;
    }

    private int nextIP() {
        return currentPkgInfo.getInstructionList().size();
    }

    private int getIfOpcode(Expression expr) {
        int opcode;
        if (expr instanceof EqualExpression) {
            opcode = InstructionCodes.IFNE;
        } else if (expr instanceof NotEqualExpression) {
            opcode = InstructionCodes.IFEQ;
        } else if (expr instanceof LessThanExpression) {
            opcode = InstructionCodes.IFGE;
        } else if (expr instanceof LessEqualExpression) {
            opcode = InstructionCodes.IFGT;
        } else if (expr instanceof GreaterThanExpression) {
            opcode = InstructionCodes.IFLE;
        } else {
            opcode = InstructionCodes.IFLT;
        }

        return opcode;
    }

    private void emitBinaryArithmeticExpr(BinaryArithmeticExpression expr, int baseOpcode) {
        expr.getLExpr().accept(this);
        expr.getRExpr().accept(this);

        OpcodeAndIndex opcodeAndIndex = getOpcodeAndIndex(expr.getType().getTag(), baseOpcode, regIndexes);
        int opcode = opcodeAndIndex.opcode;
        int exprIndex = opcodeAndIndex.index;

        expr.setTempOffset(exprIndex);
        emit(opcode, expr.getLExpr().getTempOffset(), expr.getRExpr().getTempOffset(), exprIndex);
    }

    private void emitBinaryCompareAndEqualityExpr(BinaryExpression expr, int baseOpcode) {
        expr.getLExpr().accept(this);
        expr.getRExpr().accept(this);

        int opcode = -1;
        int exprOffset = -1;

        // Consider the type of the LHS expression. RHS expression type should be the same
        int typeTag = expr.getLExpr().getType().getTag();
        switch (typeTag) {
            case TypeTags.INT_TAG:
                opcode = baseOpcode;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            case TypeTags.FLOAT_TAG:
                opcode = baseOpcode + FLOAT_OFFSET;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            case TypeTags.STRING_TAG:
                opcode = baseOpcode + STRING_OFFSET;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            case TypeTags.BOOLEAN_TAG:
                opcode = baseOpcode + BOOL_OFFSET;
                exprOffset = ++regIndexes[BOOL_OFFSET];
                break;
            // TODO Handle NULL type
        }

        expr.setTempOffset(exprOffset);
        emit(opcode, expr.getLExpr().getTempOffset(), expr.getRExpr().getTempOffset(), exprOffset);
    }

    private int emit(int opcode, int... operands) {
        return currentPkgInfo.addInstruction(InstructionFactory.get(opcode, operands));
    }

    private int emit(Instruction instruction) {
        return currentPkgInfo.addInstruction(instruction);
    }

    private BType getVMTypeFromSig(TypeSignature typeSig) {
        if (typeSig.getSigChar().equals(TypeSignature.SIG_VOID)) {
            // TODO Handle this condition if(typeSig.equals("V"))
            return null;
        }

        // TODO Need to cache these new connectors

        switch (typeSig.getSigChar()) {
            case TypeSignature.SIG_INT:
                return BTypes.typeInt;
            case TypeSignature.SIG_FLOAT:
                return BTypes.typeFloat;
            case TypeSignature.SIG_STRING:
                return BTypes.typeString;
            case TypeSignature.SIG_BOOLEAN:
                return BTypes.typeBoolean;
            case TypeSignature.SIG_REFTYPE:
                return BTypes.getTypeFromName(typeSig.getName());
            case TypeSignature.SIG_ANY:
                return BTypes.typeAny;
            case TypeSignature.SIG_STRUCT:
                return new BStructType(typeSig.getName(), typeSig.getPkgPath(), null, null);
            case TypeSignature.SIG_CONNECTOR:
                return new BConnectorType(typeSig.getName(), typeSig.getPkgPath());
            case TypeSignature.SIG_ARRAY:
                TypeSignature elementTypeSig = typeSig.getElementTypeSig();
                BType elementType = getVMTypeFromSig(elementTypeSig);
                return new BArrayType(elementType);
            default:
                throw new IllegalStateException("Unknown type signature");
        }
    }

    private int addPackageCPEntry(String pkgPath) {
        pkgPath = (pkgPath != null) ? pkgPath : ".";
        UTF8CPEntry pkgNameCPEntry = new UTF8CPEntry(pkgPath);
        int pkgNameIndex = currentPkgInfo.addCPEntry(pkgNameCPEntry);

        PackageRefCPEntry pkgCPEntry = new PackageRefCPEntry(pkgNameIndex);
        return currentPkgInfo.addCPEntry(pkgCPEntry);
    }

    private int getCallableUnitCallCPIndex(CallableUnitInvocationExpr invocationExpr) {
        Expression[] argExprs = invocationExpr.getArgExprs();
        int[] argRegs = new int[argExprs.length];
        for (int i = 0; i < argExprs.length; i++) {
            Expression argExpr = argExprs[i];
            argExpr.accept(this);
            argRegs[i] = argExpr.getTempOffset();
        }

        // Calculate registers to store return values
        BType[] retTypes = invocationExpr.getTypes();
        int[] retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            retRegs[i] = getNextIndex(retType.getTag(), regIndexes);
        }

        invocationExpr.setOffsets(retRegs);
        if (retRegs.length > 0) {
            ((Expression) invocationExpr).setTempOffset(retRegs[0]);
        }

        FunctionCallCPEntry funcCallCPEntry = new FunctionCallCPEntry(argRegs, retRegs);
        return currentPkgInfo.addCPEntry(funcCallCPEntry);
    }

    private int getConnectorRefCPIndex(BallerinaConnectorDef connectorDef) {
        UTF8CPEntry connectorNameCPEntry = new UTF8CPEntry(connectorDef.getName());
        int connectorNameCPIndex = currentPkgInfo.addCPEntry(connectorNameCPEntry);

        // Add FunctionCPEntry to constant pool
        StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(currentPkgCPIndex, connectorNameCPIndex);
        return currentPkgInfo.addCPEntry(structureRefCPEntry);
    }

    private AnnotationAttributeInfo getAnnotationAttributeInfo(AnnotationAttachment[] annotationAttachments) {
        AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo();
        for (AnnotationAttachment attachment : annotationAttachments) {
            AnnotationAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(attachment);
            attributeInfo.addAnnotationAttachmentInfo(attachmentInfo);
        }

        return attributeInfo;
    }

    private AnnotationAttachmentInfo getAnnotationAttachmentInfo(AnnotationAttachment attachment) {
        int pkgPathCPIndex = addPackageCPEntry(attachment.getPkgPath());
        UTF8CPEntry annotationNameCPEntry = new UTF8CPEntry(attachment.getName());
        int annotationNameCPIndex = currentPkgInfo.addCPEntry(annotationNameCPEntry);

        AnnotationAttachmentInfo attachmentInfo = new AnnotationAttachmentInfo(attachment.getPkgPath(),
                pkgPathCPIndex, attachment.getName(), annotationNameCPIndex);

        attachment.getAttributeNameValuePairs()
                .forEach((attributeName, attributeValue) -> {
                    AnnotationAttributeValue annotationAttribValue = getAnnotationAttributeValue(attributeValue);
                    attachmentInfo.addAnnotationAttribute(attributeName, annotationAttribValue);
                });

        return attachmentInfo;
    }

    private AnnotationAttributeValue getAnnotationAttributeValue(
            org.ballerinalang.model.AnnotationAttributeValue attributeValue) {
        AnnotationAttributeValue annotationAttribValue = new AnnotationAttributeValue();

        // TODO Annotation attribute value should store the type of the value;
        // With the above improvement, following code can be improved a lot

        if (attributeValue.getLiteralValue() != null) {
            // Annotation attribute value is a literal value
            BValue literalValue = attributeValue.getLiteralValue();
            int typeTag = literalValue.getType().getTag();
            annotationAttribValue.setTypeTag(typeTag);
            switch (typeTag) {
                case TypeTags.INT_TAG:
                    annotationAttribValue.setIntValue(((BInteger) literalValue).intValue());
                    break;
                case TypeTags.FLOAT_TAG:
                    annotationAttribValue.setFloatValue(((BFloat) literalValue).floatValue());
                    break;
                case TypeTags.STRING_TAG:
                    annotationAttribValue.setStringValue(literalValue.stringValue());
                    break;
                case TypeTags.BOOLEAN_TAG:
                    annotationAttribValue.setBooleanValue(((BBoolean) literalValue).booleanValue());
                    break;
            }

        } else if (attributeValue.getAnnotationValue() != null) {
            // Annotation attribute value is another annotation attachment
            annotationAttribValue.setTypeTag(TypeTags.ANNOTATION_TAG);
            AnnotationAttachment attachment = attributeValue.getAnnotationValue();
            AnnotationAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(attachment);
            annotationAttribValue.setAnnotationAttachmentValue(attachmentInfo);

        } else {
            annotationAttribValue.setTypeTag(TypeTags.ARRAY_TAG);
            org.ballerinalang.model.AnnotationAttributeValue[] attributeValues = attributeValue.getValueArray();
            AnnotationAttributeValue[] annotationAttribValues = new AnnotationAttributeValue[attributeValues.length];
            for (int i = 0; i < attributeValues.length; i++) {
                annotationAttribValues[i] = getAnnotationAttributeValue(attributeValues[i]);
            }

            annotationAttribValue.setAttributeValueArray(annotationAttribValues);
        }

        return annotationAttribValue;
    }

    private void visitCallableUnitParameterDefs(ParameterDef[] parameterDefs, CallableUnitInfo callableUnitInfo) {
        boolean paramAnnotationFound = false;
        ParamAnnotationAttributeInfo paramAttributeInfo = new ParamAnnotationAttributeInfo(
                parameterDefs.length);
        for (int i = 0; i < parameterDefs.length; i++) {
            ParameterDef parameterDef = parameterDefs[i];
            int lvIndex = getNextIndex(parameterDef.getType().getTag(), lvIndexes);
            parameterDef.setMemoryLocation(new StackVarLocation(lvIndex));
            parameterDef.accept(this);

            AnnotationAttachment[] paramAnnotationAttachments = parameterDef.getAnnotations();
            if (paramAnnotationAttachments.length == 0) {
                continue;
            }

            paramAnnotationFound = true;
            ParamAnnotationAttachmentInfo paramAttachmentInfo = new ParamAnnotationAttachmentInfo(i);
            for (AnnotationAttachment annotationAttachment : paramAnnotationAttachments) {
                AnnotationAttachmentInfo attachmentInfo = getAnnotationAttachmentInfo(annotationAttachment);
                paramAttachmentInfo.addAnnotationAttachmentInfo(attachmentInfo);
            }

            paramAttributeInfo.addParamAnnotationAttachmentInfo(i, paramAttachmentInfo);
        }

        if (paramAnnotationFound) {
            callableUnitInfo.addAttributeInfo(AttributeInfo.PARAMETER_ANNOTATIONS_ATTRIBUTE, paramAttributeInfo);
        }
    }

    /**
     * @since 0.87
     */
    public static class OpcodeAndIndex {
        int opcode;
        int index;

        public OpcodeAndIndex(int opcode, int index) {
            this.opcode = opcode;
            this.index = index;
        }
    }
}