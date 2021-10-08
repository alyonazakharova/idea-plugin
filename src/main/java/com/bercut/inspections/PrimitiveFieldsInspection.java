package com.bercut.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class PrimitiveFieldsInspection extends AbstractBaseJavaLocalInspectionTool {
//    @Override
//    public ProblemDescriptor @Nullable [] checkField(@NotNull PsiField field, @NotNull InspectionManager manager, boolean isOnTheFly) {
//        return super.checkField(field, manager, isOnTheFly);
//    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            @Override
            public void visitField(PsiField field) {
                if (field.getType() instanceof PsiPrimitiveType) {
                    PsiAnnotation[] psiAnnotations = field.getAnnotations();
                    PsiAnnotation annotation = Arrays.stream(psiAnnotations)
                            .filter(a -> Objects.requireNonNull(a.getQualifiedName()).contains("OptionalInput"))
                            .findFirst()
                            .orElse(null);

                    if (annotation != null) {
                        assert annotation.getContext() != null; //fixme
                        holder.registerProblem(
                                annotation.getContext(),
                                String.format("Primitive type field %s must not be annotated with OptionalInput", field.getName()),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
