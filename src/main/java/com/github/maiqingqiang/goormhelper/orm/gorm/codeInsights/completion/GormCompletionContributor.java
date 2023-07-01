package com.github.maiqingqiang.goormhelper.orm.gorm.codeInsights.completion;

import com.github.maiqingqiang.goormhelper.orm.gorm.GormTypes;
import com.goide.GoParserDefinition;
import com.goide.psi.GoCallExpr;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

public class GormCompletionContributor extends CompletionContributor {
    public GormCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement()
                        .withElementType(GoParserDefinition.Lazy.STRING_LITERALS)
                        .withSuperParent(3, new PsiElementPattern.Capture<>(GoCallExpr.class) {
                            @Override
                            public boolean accepts(@Nullable Object o, ProcessingContext context) {
                                return o instanceof GoCallExpr && GormTypes.GORM_CALLABLES_SET.find((GoCallExpr) o, false) != null;
                            }
                        }).andOr(PlatformPatterns.psiElement()),
                new GormColumnCompletionProvider());

        extend(CompletionType.BASIC, PlatformPatterns.psiElement()
                        .withElementType(GoParserDefinition.Lazy.STRING_LITERALS)
                        .withSuperParent(7, new PsiElementPattern.Capture<>(GoCallExpr.class) {
                            @Override
                            public boolean accepts(@Nullable Object o, ProcessingContext context) {
                                return o instanceof GoCallExpr && GormTypes.GORM_CALLABLES_SET.find((GoCallExpr) o, false) != null;
                            }
                        }),
                new GormColumnCompletionProvider());
    }
}
