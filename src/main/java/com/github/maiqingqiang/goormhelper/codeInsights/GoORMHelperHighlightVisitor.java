package com.github.maiqingqiang.goormhelper.codeInsights;

import com.github.maiqingqiang.goormhelper.Types;
import com.goide.psi.GoFile;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoORMHelperHighlightVisitor implements HighlightVisitor {
    private HighlightInfoHolder holder;

    @Override
    public boolean suitableForFile(@NotNull PsiFile file) {
        return file instanceof GoFile;
    }

    @Override
    public void visit(@NotNull PsiElement element) {
        if (element instanceof PsiComment psiComment) {
            modelAnnotation(psiComment);
            tableAnnotation(psiComment);
        }
    }

    private void modelAnnotation(PsiComment psiComment) {
        int index = psiComment.getText().indexOf(Types.MODEL_ANNOTATION);

        if (index > -1) {
            Matcher matcher;
            if ((matcher = Types.MODEL_ANNOTATION_PATTERN.matcher(psiComment.getText())).find()) {
                holder.add(HighlightInfo.newHighlightInfo(
                        new HighlightInfoType.HighlightInfoTypeImpl(
                                HighlightSeverity.INFORMATION,
                                DefaultLanguageHighlighterColors.METADATA)
                ).range(psiComment, TextRange.from(index, Types.MODEL_ANNOTATION.length())).create());

                holder.add(HighlightInfo.newHighlightInfo(
                        new HighlightInfoType.HighlightInfoTypeImpl(
                                HighlightSeverity.INFORMATION, DefaultLanguageHighlighterColors.IDENTIFIER)
                ).range(psiComment, TextRange.from(index + Types.MODEL_ANNOTATION.length(), matcher.group(0).length() - Types.MODEL_ANNOTATION.length())).create());
            }
        }
    }

    private void tableAnnotation(PsiComment psiComment) {
        int index = psiComment.getText().indexOf(Types.TABLE_ANNOTATION);

        if (index > -1) {
            Matcher matcher;
            if ((matcher = Types.TABLE_ANNOTATION_PATTERN.matcher(psiComment.getText())).find()) {
                holder.add(HighlightInfo.newHighlightInfo(
                        new HighlightInfoType.HighlightInfoTypeImpl(
                                HighlightSeverity.INFORMATION,
                                DefaultLanguageHighlighterColors.METADATA)
                ).range(psiComment, TextRange.from(index, Types.TABLE_ANNOTATION.length())).create());

                holder.add(HighlightInfo.newHighlightInfo(
                        new HighlightInfoType.HighlightInfoTypeImpl(
                                HighlightSeverity.INFORMATION, DefaultLanguageHighlighterColors.IDENTIFIER)
                ).range(psiComment, TextRange.from(index + Types.TABLE_ANNOTATION.length(), matcher.group(0).length() - Types.TABLE_ANNOTATION.length())).create());
            }
        }
    }


    @Override
    public boolean analyze(@NotNull PsiFile file, boolean updateWholeFile, @NotNull HighlightInfoHolder holder, @NotNull Runnable action) {
        this.holder = holder;
        try {
            action.run();
        } finally {
            this.holder = null;
        }

        return true;
    }

    @Override
    public @NotNull HighlightVisitor clone() {
        return new GoORMHelperHighlightVisitor();
    }
}
