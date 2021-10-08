package com.bercut.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PrimitiveFieldsInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public ProblemDescriptor @Nullable [] checkField(@NotNull PsiField field, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (field.getType() instanceof PsiPrimitiveType) {
            PsiAnnotation[] psiAnnotations = field.getAnnotations();
            PsiAnnotation annotation = Arrays.stream(psiAnnotations)
                    .filter(a -> Objects.requireNonNull(a.getQualifiedName()).contains("OptionalInput"))
                    .findFirst()
                    .orElse(null);

            List<ProblemDescriptor> problems = new ArrayList<>();
            if (annotation != null) {
                problems.add(manager.createProblemDescriptor(field,
                        String.format("Primitive type field %s must not be annotated with OptionalInput", field.getName()),
                        new RemoveAnnotationQuickFix(annotation, null),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true));
            }
            if (!problems.isEmpty()) {
                return problems.toArray(new ProblemDescriptor[0]);
            }
        }
        return null;
    }
}
