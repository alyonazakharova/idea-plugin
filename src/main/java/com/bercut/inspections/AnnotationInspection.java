package com.bercut.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AnnotationInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    public ProblemDescriptor @Nullable [] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
        String className = aClass.getName();
        if (className == null || !className.matches("Chapter\\d+Section\\d+Case\\d+Test")) {
            return null;
        }

        PsiClass superClass = aClass.getSuperClass();
        if (superClass == null) {
            return null;
        }

        List<ProblemDescriptor> problems = new ArrayList<>();

        if (superClass.getName() != null && !superClass.getName().equals("BaseTest")) {
            PsiAnnotation[] psiAnnotations = aClass.getAnnotations();
            PsiAnnotation annotation = Arrays.stream(psiAnnotations)
                    .filter(a -> Objects.requireNonNull(a.getQualifiedName()).contains("TC_ID"))
                    .findFirst()
                    .orElse(null);

            if (annotation == null) {
                problems.add(manager.createProblemDescriptor(
                        Objects.requireNonNull(aClass.getNameIdentifier()),
                        "Class with new implementation must be annotated with TC_ID",
                        true,
                        ProblemHighlightType.GENERIC_ERROR,
                        true));
            } else {
                PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
                if (attributes.length > 0) {
                    PsiNameValuePair nameValuePair = attributes[0];
                    if (nameValuePair.getLiteralValue() == null || nameValuePair.getLiteralValue().isEmpty())
                        problems.add(manager.createProblemDescriptor(
                                nameValuePair,
                                "Annotation value cannot be empty",
                                true,
                                ProblemHighlightType.GENERIC_ERROR,
                                true));
                }
            }

            if (!problems.isEmpty()) {
                return problems.toArray(new ProblemDescriptor[0]);
            }
        }
        return null;
    }
}
