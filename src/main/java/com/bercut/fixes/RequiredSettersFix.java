package com.bercut.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class RequiredSettersFix implements LocalQuickFix {
    private final StringBuilder setterText = new StringBuilder();
    private final int offset;

    public RequiredSettersFix(@NotNull List<PsiField> notSetFields, int offset) {
        for (PsiField field : notSetFields) {
            // boolean isSomething -> setSomething
            // Boolean isSomething -> setIsSomething
            if (field.getType() instanceof PsiPrimitiveType && field.getName().startsWith("is")) {
                setterText.append("\n.set").append(StringUtils.capitalize(field.getName().substring(2))).append("()");
            } else {
                setterText.append("\n.set").append(StringUtils.capitalize(field.getName())).append("()");
            }
        }
        this.offset = offset;
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return "Add missing setters";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement element = descriptor.getPsiElement();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        PsiFile containingFile = element.getContainingFile();
        Document document = psiDocumentManager.getDocument(containingFile);
        if (document != null) {
            document.insertString(offset, setterText);
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            psiDocumentManager.commitDocument(document);
            FileDocumentManager.getInstance().saveDocument(document);
            codeStyleManager.reformatText(containingFile, Collections.singletonList(containingFile.getTextRange()));

        }
    }
}
