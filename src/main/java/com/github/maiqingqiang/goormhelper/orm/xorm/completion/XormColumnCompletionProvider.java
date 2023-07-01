package com.github.maiqingqiang.goormhelper.orm.xorm.completion;

import com.github.maiqingqiang.goormhelper.Types;
import com.github.maiqingqiang.goormhelper.orm.xorm.XormTypes;
import com.github.maiqingqiang.goormhelper.services.GoORMHelperManager;
import com.github.maiqingqiang.goormhelper.ui.Icons;
import com.github.maiqingqiang.goormhelper.utils.Strings;
import com.goide.documentation.GoDocumentationProvider;
import com.goide.inspections.core.GoCallableDescriptor;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiUtil;
import com.google.common.base.CaseFormat;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class XormColumnCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final Logger LOG = Logger.getInstance(XormColumnCompletionProvider.class);

    @lombok.Data
    static class Tag {
        private String name;
        private List<String> params;

        public Tag(String name, List<String> params) {
            this.name = name;
            this.params = params;
        }
    }

    static boolean hasArgumentAtIndex(@NotNull GoCallExpr call, int argumentIndex, @NotNull PsiElement argument) {
        if (argumentIndex == -1) return true;
        argument = GoPsiUtil.skipParens(argument);
        return argument == ContainerUtil.getOrElse(call.getArgumentList().getExpressionList(), argumentIndex, (Object) null);
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        Project project = parameters.getPosition().getProject();

        GoCallExpr goCallExpr = (GoCallExpr) PsiTreeUtil.findFirstParent(parameters.getPosition(), element -> element instanceof GoCallExpr);

        if (goCallExpr == null) return;

        GoCallableDescriptor descriptor = XormTypes.XORM_CALLABLES_SET.find(goCallExpr, false);

        if (descriptor == null) return;

        Integer argumentIndex = XormTypes.XORM_CALLABLES.get(descriptor);

        if (!hasArgumentAtIndex(goCallExpr, argumentIndex, parameters.getPosition().getParent())) return;

        String schema = scanSchema(parameters.getPosition());

        if (schema.isEmpty()) return;

        List<String> pathList = Objects.requireNonNull(GoORMHelperManager.getInstance(project).getState()).schemaMapping.get(schema);

        if (pathList == null) return;

        for (String path : pathList) {

            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(path);
            if (file == null) continue;

            GoFile goFile = (GoFile) PsiManager.getInstance(project).findFile(file);
            if (goFile == null) continue;

            for (GoTypeDeclaration goTypeDeclaration : goFile.findChildrenByClass(GoTypeDeclaration.class)) {
                GoTypeSpec goTypeSpec = goTypeDeclaration.getTypeSpecList().get(0);

                if (!Objects.equals(goTypeSpec.getName(), schema)) continue;

                scanFields(descriptor, result, goTypeSpec);
            }
        }
    }

    private void scanFields(GoCallableDescriptor descriptor, @NotNull CompletionResultSet result, @NotNull GoTypeSpec goTypeSpec) {
        if (goTypeSpec.getSpecType().getType() instanceof GoStructType goStructType) {
            for (GoFieldDeclaration field : goStructType.getFieldDeclarationList()) {
                String column = "";
                String comment = "";
                String type = "";

                if (field.getType() != null) {
                    type = field.getType().getPresentationText();
                }

                GoTag tag = field.getTag();
                if (tag != null && tag.getValue("xorm") != null) {
                    @NotNull List<Tag> tags = parseTag(tag.getValue("xorm"));

                    for (Tag t : tags) {
                        if (t.getName().startsWith("'") && t.getParams().size() == 0) {
                            column = t.getName().replaceAll("'", "");
                        }

                        if (t.getName().equals("comment") && t.getParams().size() > 0) {
                            comment = t.getParams().get(0).replaceAll("'", "");
                        }
                    }
                }


                if (column.isEmpty()) {
                    String name = field.getFieldDefinitionList().get(0).getName();

                    if (name != null) {
                        column = Strings.replaceCommonInitialisms(name);
                        column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column);
                    }
                }


                if (comment.isEmpty()) {
                    comment = GoDocumentationProvider.getCommentText(GoDocumentationProvider.getCommentsForElement(field), false);
                }

                addElement(result, column, comment, type);

                List<String> whereExpr = XormTypes.XORM_WHERE_EXPR.get(descriptor);
                if (whereExpr != null) {
                    for (String s : whereExpr) {
                        addElement(result, String.format(s, column), comment, type);
                    }
                }
            }
        }
    }

    private String scanSchema(@NotNull PsiElement psiElement) {

        String schema = "";

        GoStatement currentStatement = (GoStatement) PsiTreeUtil.findFirstParent(psiElement, element -> element instanceof GoStatement);

        if (currentStatement == null) return schema;

        schema = findSchema(currentStatement);
        if (!schema.isEmpty()) return schema;


        GoVarDefinition currentGoVarDefinition = PsiTreeUtil.findChildOfType(currentStatement, GoVarDefinition.class);
        if (currentGoVarDefinition != null) {
            schema = searchGoVarDefinitionReferences(currentGoVarDefinition);
            if (!schema.isEmpty()) return schema;
        }

        GoReferenceExpression lastGoReferenceExpression = null;

        for (GoReferenceExpression goReferenceExpression : PsiTreeUtil.findChildrenOfType(currentStatement, GoReferenceExpression.class)) {
            GoType goType = goReferenceExpression.getGoType(ResolveState.initial());
            if (goType != null && (goType.getPresentationText().equals("*Session") || goType.getPresentationText().equals("*DB"))) {
                lastGoReferenceExpression = goReferenceExpression;
            }
        }

        if (lastGoReferenceExpression != null) {
            GoVarDefinition goVarDefinition = (GoVarDefinition) lastGoReferenceExpression.resolve();

            GoStatement resolveStatement = (GoStatement) PsiTreeUtil.findFirstParent(goVarDefinition, element -> element instanceof GoStatement);
            if (resolveStatement == null) return schema;
            schema = findSchema(resolveStatement);
            if (!schema.isEmpty()) return schema;

            schema = searchGoVarDefinitionReferences(goVarDefinition);
            if (!schema.isEmpty()) return schema;

            scanSchema(goVarDefinition);
        }
        return schema;
    }

    private String searchGoVarDefinitionReferences(GoVarDefinition goVarDefinition) {
        String schema = "";
        for (PsiReference psiReference : GoReferencesSearch.search(goVarDefinition)) {
            GoStatement statement = (GoStatement) PsiTreeUtil.findFirstParent(psiReference.getElement(), element -> element instanceof GoStatement);
            if (statement == null) continue;
            schema = findSchema(statement);
            if (!schema.isEmpty()) return schema;

            for (GoVarDefinition varDefinition : PsiTreeUtil.findChildrenOfType(statement, GoVarDefinition.class)) {
                GoType goType = varDefinition.getGoType(ResolveState.initial());

                if (goType != null && (goType.getPresentationText().equals("*Session") || goType.getPresentationText().equals("*DB"))) {
                    return searchGoVarDefinitionReferences(varDefinition);
                }
            }
        }
        return schema;
    }

    private static void addElement(@NotNull CompletionResultSet result, String column, String comment, String type) {
        result.addElement(LookupElementBuilder.create(column)
                .withTypeText(type)
                .withIcon(Icons.Xorm19x12)
                .withTailText(" " + comment, true));
    }

    private static String findSchema(@NotNull GoStatement statement) {
        String schema = "";

        String comment = GoDocumentationProvider.getCommentText(GoDocumentationProvider.getCommentsForElement(statement), false);
        Matcher matcher;
        if ((matcher = Types.MODEL_ANNOTATION_PATTERN.matcher(comment)).find()) schema = matcher.group(1);

        if (!schema.isEmpty()) return schema;

        for (GoCallExpr goCallExpr : PsiTreeUtil.findChildrenOfType(statement, GoCallExpr.class)) {
            GoCallableDescriptor descriptor = XormTypes.XORM_MODEL_CALLABLES_SET.find(goCallExpr, false);
            if (descriptor == null) continue;

            Integer argumentIndex = XormTypes.XORM_MODEL_CALLABLES.get(descriptor);

            System.out.println("argumentIndex " + argumentIndex);
            System.out.println("getExpressionList " + goCallExpr.getArgumentList().getExpressionList().get(argumentIndex));

            GoExpression argument = goCallExpr.getArgumentList().getExpressionList().get(argumentIndex);

            if (argument instanceof GoUnaryExpr goUnaryExpr) {
                if (goUnaryExpr.getExpression() instanceof GoCompositeLit goCompositeLit) {
                    if (goCompositeLit.getTypeReferenceExpression() == null) continue;
                    schema = goCompositeLit.getTypeReferenceExpression().getIdentifier().getText();
                } else if (goUnaryExpr.getExpression() instanceof GoReferenceExpression goReferenceExpression) {
                    GoVarDefinition goVarDefinition = (GoVarDefinition) goReferenceExpression.resolve();

                    if (goVarDefinition == null) continue;

                    GoType goType = goVarDefinition.getGoType(ResolveState.initial());
                    if (goType == null || goType.getTypeReferenceExpression() == null) continue;

                    schema = goType.getTypeReferenceExpression().getIdentifier().getText();
                }
            } else if (argument instanceof GoBuiltinCallExpr goBuiltinCallExpr) {
                GoType goType = PsiTreeUtil.findChildOfType(goBuiltinCallExpr, GoType.class);
                if (goType == null || goType.getTypeReferenceExpression() == null) continue;
                schema = goType.getTypeReferenceExpression().getIdentifier().getText();
            } else if (argument instanceof GoReferenceExpression goReferenceExpression && goReferenceExpression.resolve() instanceof GoVarDefinition goVarDefinition) {
                GoType goType = PsiTreeUtil.findChildOfType(goVarDefinition.getParent(), GoType.class);
                if (goType != null) {
                    if (goType.getTypeReferenceExpression() == null) continue;
                    schema = goType.getTypeReferenceExpression().getIdentifier().getText();
                } else {
                    GoCompositeLit goCompositeLit = PsiTreeUtil.findChildOfType(goVarDefinition.getParent(), GoCompositeLit.class);
                    if (goCompositeLit == null || goCompositeLit.getTypeReferenceExpression() == null) continue;
                    schema = goCompositeLit.getTypeReferenceExpression().getIdentifier().getText();
                }
            }
        }

        return schema;
    }

    private static @NotNull List<Tag> parseTag(String tagStr) {
        tagStr = tagStr.trim();
        boolean inQuote = false;
        boolean inBigQuote = false;
        int lastIdx = 0;
        Tag curTag = null;
        int paramStart = 0;
        List<Tag> tags = new ArrayList<>();

        for (int i = 0; i < tagStr.length(); i++) {
            char t = tagStr.charAt(i);
            switch (t) {
                case '\'' -> inQuote = !inQuote;
                case ' ' -> {
                    if (!inQuote && !inBigQuote) {
                        if (lastIdx < i) {
                            if (curTag == null || curTag.getName().isEmpty()) {
                                curTag = new Tag(tagStr.substring(lastIdx, i), new ArrayList<>());
                            }
                            tags.add(curTag);
                            lastIdx = i + 1;
                            curTag = null;
                        } else if (lastIdx == i) {
                            lastIdx = i + 1;
                        }
                    } else if (inBigQuote && !inQuote) {
                        paramStart = i + 1;
                    }
                }
                case ',' -> {
                    if (!inQuote && !inBigQuote) {
                        throw new IllegalArgumentException("Comma[" + i + "] of " + tagStr + " should be in quote or big quote");
                    }
                    if (!inQuote) {
                        curTag.getParams().add(tagStr.substring(paramStart, i).trim());
                        paramStart = i + 1;
                    }
                }
                case '(' -> {
                    inBigQuote = true;
                    if (!inQuote) {
                        curTag = new Tag(tagStr.substring(lastIdx, i), new ArrayList<>());
                        paramStart = i + 1;
                    }
                }
                case ')' -> {
                    inBigQuote = false;
                    if (!inQuote) {
                        curTag.getParams().add(tagStr.substring(paramStart, i));
                    }
                }
            }
        }

        if (lastIdx < tagStr.length()) {
            if (curTag == null || curTag.getName().isEmpty()) {
                curTag = new Tag(tagStr.substring(lastIdx), new ArrayList<>());
            }
            tags.add(curTag);
        }

        return tags;
    }
}
