package com.bercut.fixes;

import com.bercut.PsiDocumentUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RequiredFieldSetterFix implements LocalQuickFix {
    private final StringBuilder setterText = new StringBuilder();
    @NotNull
    private final PsiElement root; //fixme!!!

//    public RequiredFieldSetterFix(@NotNull PsiField field, @NotNull PsiElement root) {
//        setterText = ".set" + StringUtils.capitalize(field.getName()) + "()";
//        this.root = root;
//    }

    public RequiredFieldSetterFix(@NotNull List<PsiField> notSetFields, @NotNull PsiElement root) {
        for (PsiField field : notSetFields) {
            setterText.append("\n.set").append(StringUtils.capitalize(field.getName())).append("()");
        }
        this.root = root;
    }


    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return "Add missing required field setter";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement element = descriptor.getPsiElement();
//        if (!(element instanceof PsiModifierListOwner)) {
//            return;
//        }
//        final PsiModifierList modifiers = ((PsiModifierListOwner) element).getModifierList();
//        if (modifiers == null) {
//            return;
//        }
//        PsiStatement statement = JavaPsiFacade.getElementFactory(project).createStatementFromText(setterText, element);

//        PsiIdentifier statement = JavaPsiFacade.getElementFactory(project).createIdentifier(setterText);
//        root.addBefore(statement, root.getLastChild());

        //todo
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile containingFile = element.getContainingFile();
        Document document = psiDocumentManager.getDocument(containingFile);
        document.insertString(root.getTextOffset() + root.getText().length(), setterText);
        PsiDocumentUtils.commitAndSaveDocument(psiDocumentManager, document);
    }
}
