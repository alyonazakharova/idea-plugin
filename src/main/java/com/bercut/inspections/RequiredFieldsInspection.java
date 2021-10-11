package com.bercut.inspections;

import com.bercut.fixes.RequiredSettersFix;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public final class RequiredFieldsInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problems, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitNewExpression(PsiNewExpression expression) {
                super.visitNewExpression(expression);
                if (expression == null) {
                    return;
                }

                PsiJavaCodeReferenceElement classReference = expression.getClassOrAnonymousClassReference();

                if (!isInputClass(classReference)) {
                    return;
                }

                PsiClass psiClass = (PsiClass) classReference.resolve();
                List<PsiField> requiredFields = getRequiredFields(psiClass);

                PsiElement root = getRoot(expression);
                String setters = root.getText().toLowerCase();

                List<PsiField> notSetFields = new ArrayList<>();
                List<PsiField> fieldsWithDefaultValue = new ArrayList<>();

                for (PsiField psiField : requiredFields) {
                    if (!isSetterPresent(setters, psiField)) {
                        if (psiField.getType() instanceof PsiPrimitiveType || psiField.hasInitializer()) {
                            fieldsWithDefaultValue.add(psiField);
                        } else {
                            notSetFields.add(psiField);
                        }
                    }
                }

                if (!notSetFields.isEmpty()) {
                    problems.registerProblem(
                            classReference,
                            String.format("The following fields are required and cannot be null: %s",
                                    notSetFields.stream().map(PsiField::getName).collect(Collectors.toList())),
                            ProblemHighlightType.GENERIC_ERROR,
                            new RequiredSettersFix(notSetFields, root.getTextOffset() + root.getTextLength()));
                }

                if (!fieldsWithDefaultValue.isEmpty()) {
                    problems.registerProblem(
                            classReference,
                            String.format("The following fields have default values but not set: %s",
                                    fieldsWithDefaultValue.stream().map(PsiField::getName).collect(Collectors.toList())),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            new RequiredSettersFix(fieldsWithDefaultValue, root.getTextOffset() + root.getTextLength()));
                }
            }
        };
    }

    private boolean isInputClass(PsiJavaCodeReferenceElement classReference) {
        return classReference != null && classReference.getQualifiedName().matches(".*[CG]S[0-9]+Input[s]?");
    }

    private List<PsiField> getRequiredFields(PsiClass psiClass) {
        List<PsiField> requiredFields = new ArrayList<>();
        if (psiClass != null) {
            for (PsiField psiField : psiClass.getFields()) {
                PsiAnnotation[] psiAnnotations = psiField.getAnnotations();
                PsiAnnotation annotation = Arrays.stream(psiAnnotations)
                        .filter(a -> Objects.requireNonNull(a.getQualifiedName()).contains("OptionalInput"))
                        .findFirst()
                        .orElse(null);

                if (annotation == null) {
                    requiredFields.add(psiField);
                }
            }
        }
        return requiredFields;
    }

    // если объект класса инпутов записывается в переменную (например, GS1Inputs gs1Inputs = new GS1Inputs()),
    // "корневым элементом" в данном случае будет объект класса PsiLocalVariable
    // если создаем объект класса инпутов без записи в переменную, сразу как параметр метода (globalSteps.gs1(new GS1Inputs()))
    // "корневой элемент" - PsiExpressionList
    private PsiElement getRoot(PsiNewExpression expression) {
        PsiElement rootElement = expression;
        PsiElement tmpRootElement = expression.getParent();
        while (!(tmpRootElement instanceof PsiExpressionList || tmpRootElement instanceof PsiLocalVariable)) {
            rootElement = tmpRootElement;
            tmpRootElement = tmpRootElement.getParent();
        }
        return rootElement;
    }

    // boolean isSomething -> setSomething
    // Boolean isSomething -> setIsSomething
    private boolean isSetterPresent(String settersChain, PsiField field) {
        String nameToCheck;
        if (field.getType() instanceof PsiPrimitiveType && field.getName().startsWith("is")) {
            nameToCheck = field.getName().substring(2).toLowerCase();
        } else {
            nameToCheck = field.getName().toLowerCase();
        }
        return settersChain.toLowerCase().contains("set" + nameToCheck);
    }
}
