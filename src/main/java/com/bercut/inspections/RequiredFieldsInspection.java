package com.bercut.inspections;

import com.bercut.fixes.RequiredFieldSetterFix;
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
                if (classReference == null) {
                    return;
                }

                //todo
//                PsiElement lastChild = expression.getLastChild();
//                lastChild.addAfter()

                if (isInputClass(classReference)) {
                    PsiClass psiClass = (PsiClass) classReference.resolve();
                    List<PsiField> requiredFields = getRequiredFields(psiClass);

//                    String expr = getAllSetters(expression).toLowerCase();
                    PsiElement root = getRoot(expression);
                    String expr = root.getText().toLowerCase();

                    List<PsiField> notSetFields = new ArrayList<>();

                    for (PsiField psiField : requiredFields) {
                        if (!expr.contains("set" + psiField.getName().toLowerCase())) {
                            if (!isSigosPresent(expr, psiField)) {
//                                problems.registerProblem(
//                                        classReference,
//                                        String.format("Field %s is required and cannot be null", psiField.getName()),
//                                        ProblemHighlightType.GENERIC_ERROR);

                                notSetFields.add(psiField);

//                                problems.registerProblem(
//                                        classReference,
//                                        String.format("Field %s is required and cannot be null (%s & %s: %s)",
//                                                psiField.getName(), root.getTextOffset(), root.getTextLength(),
//                                                root.getText()),
//                                        new RequiredFieldSetterFix(psiField, root));
                            }
                        }
                    }

                    if (!notSetFields.isEmpty()) {
                        problems.registerProblem(
                                classReference,
                                String.format("The following fields are required and cannot be null:\n%s", notSetFields),
                                new RequiredFieldSetterFix(notSetFields, root));
                    }
                }
            }
        };
    }

    private boolean isInputClass(PsiJavaCodeReferenceElement classReference) {
        return classReference.getQualifiedName().matches(".*[CG]S[0-9]+Input[s]?");
    }

    // fixme
    // Input классы наследуются от StepInput, у которого есть static поля notNullErrorTemplate и nullableErrorTemplate
    // добавить исключение этих полей из списка обязательных
    // добавить исключение примитивных типов (но проверять isSigos)
    private List<PsiField> getRequiredFields(PsiClass psiClass) {
        List<PsiField> requiredFields = new ArrayList<>();
        if (psiClass != null) {
            List<PsiField> psiFields = Arrays.stream(psiClass.getAllFields())
                    .filter(psiField -> !"notNullErrorTemplate".equals(psiField.getName())
                            && !"nullableErrorTemplate".equals(psiField.getName()))
                    .collect(Collectors.toList());

            for (PsiField psiField : psiFields) {
                PsiAnnotation[] psiAnnotations = psiField.getAnnotations();
                if (psiField.getType() instanceof PsiPrimitiveType) {
                    if (psiField.getName().toLowerCase().contains("sigos")) {
                        requiredFields.add(psiField);
                    }
                } else {
                    // проверка полей, которые имеют значение по умолчанию без аннотации @DefaultValue
                    if (!psiField.hasInitializer()) {
                        long nonRequiredCount = Arrays.stream(psiAnnotations)
                                .map(PsiAnnotation::getQualifiedName)
                                .filter(Objects::nonNull)
                                .filter(name -> name.contains("OptionalInput") || name.contains("DefaultValue")) // можно убрать DefaultValue, потому что было проверено в условии выше
                                .count();

                        if (nonRequiredCount == 0) {
                            requiredFields.add(psiField);
                        }
                    }
                }
            }
        }
        return requiredFields;
    }

//    private String getAllSetters(PsiNewExpression expression) {
//        PsiElement rootElement = expression;
//        PsiElement prevElement = expression;
//        PsiElement tmpRootElement = expression.getParent();
//        while (!rootIsAchieved(tmpRootElement)) {
//            prevElement = rootElement;
//            rootElement = tmpRootElement;
//            tmpRootElement = tmpRootElement.getParent();
//        }
//        rootElement = prevElement;
//        return rootElement.getText();
//    }

    private PsiElement getRoot(PsiNewExpression expression) {
        PsiElement rootElement = expression;
        PsiElement prevElement = expression;
        PsiElement tmpRootElement = expression.getParent();
        while (!rootIsAchieved(tmpRootElement)) {
            prevElement = rootElement;
            rootElement = tmpRootElement;
            tmpRootElement = tmpRootElement.getParent();
        }
//        rootElement = prevElement;
        return rootElement;
    }

    private boolean rootIsAchieved(PsiElement psiElement) {
        return psiElement instanceof PsiExpressionList || psiElement instanceof PsiLocalVariable;
    }

    // если поле isSigosTest примитивное то setSigosTest
    // если Boolean - setIsSigosTest
    private boolean isSigosPresent(String settersChain, PsiField psiField) {
        if (psiField.getName().toLowerCase().contains("sigostest")) {
            return settersChain.toLowerCase().contains("sigostest");
        }
        return false;
    }
}
