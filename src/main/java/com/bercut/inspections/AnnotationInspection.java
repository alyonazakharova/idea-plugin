package com.bercut.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
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
        if (className == null || !className.endsWith("Test")) {
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
            }

            if (!problems.isEmpty()) {
                return problems.toArray(new ProblemDescriptor[0]);
            }
        }
        return null;
    }
}
