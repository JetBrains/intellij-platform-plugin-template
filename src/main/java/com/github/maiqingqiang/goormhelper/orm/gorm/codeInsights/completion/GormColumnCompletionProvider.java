package com.github.maiqingqiang.goormhelper.orm.gorm.codeInsights.completion;

import com.github.maiqingqiang.goormhelper.Types;
import com.github.maiqingqiang.goormhelper.orm.gorm.GormTypes;
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

import java.util.*;
import java.util.regex.Matcher;

public class GormColumnCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final Logger LOG = Logger.getInstance(GormColumnCompletionProvider.class);

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

        GoCallableDescriptor descriptor = GormTypes.GORM_CALLABLES_SET.find(goCallExpr, false);
        if (descriptor == null) return;

        Integer argumentIndex = GormTypes.GORM_CALLABLES.get(descriptor);

        if (!hasArgumentAtIndex(goCallExpr, argumentIndex, parameters.getPosition().getParent()) && !(parameters.getPosition().getParent().getParent() instanceof GoKey))
            return;

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
                if (tag != null && tag.getValue("gorm") != null) {
                    Map<String, String> tagMap = parseTag(Objects.requireNonNull(tag.getValue("gorm")));

                    if (tagMap.containsKey("COLUMN")) {
                        column = tagMap.get("COLUMN");
                    }

                    if (tagMap.containsKey("COMMENT")) {
                        comment = tagMap.get("COMMENT");
                    }
                }


                if (column.isEmpty()) {

                    if (field.getFieldDefinitionList().size() == 0 && field.getAnonymousFieldDefinition() != null) {
                        GoType goType = field.getAnonymousFieldDefinition().getGoType(ResolveState.initial());
                        if (goType == null) continue;

                        GoTypeSpec spec = (GoTypeSpec) goType.resolve(ResolveState.initial());
                        if (spec == null) continue;

                        scanFields(descriptor, result, spec);
                        continue;
                    }

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

                List<String> whereExpr = GormTypes.GORM_WHERE_EXPR.get(descriptor);
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
            if (goType != null && goType.getPresentationText().equals("*DB")) {
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
            System.out.println("statement " + statement.getText());
            schema = findSchema(statement);
            if (!schema.isEmpty()) return schema;

            for (GoVarDefinition varDefinition : PsiTreeUtil.findChildrenOfType(statement, GoVarDefinition.class)) {
                GoType goType = varDefinition.getGoType(ResolveState.initial());

                if (goType != null && goType.getPresentationText().equals("*DB")) {
                    return searchGoVarDefinitionReferences(varDefinition);
                }
            }
        }
        return schema;
    }

    private static void addElement(@NotNull CompletionResultSet result, String column, String comment, String type) {
        result.addElement(LookupElementBuilder.create(column)
                .withTypeText(type)
                .withIcon(Icons.Gorm35x12)
                .withTailText(" " + comment, true));
    }

    private static String findSchema(@NotNull GoStatement statement) {
        String schema = "";

        String comment = GoDocumentationProvider.getCommentText(GoDocumentationProvider.getCommentsForElement(statement), false);
        Matcher matcher;
        if ((matcher = Types.MODEL_ANNOTATION_PATTERN.matcher(comment)).find()) schema = matcher.group(1);

        if (!schema.isEmpty()) return schema;

        for (GoCallExpr goCallExpr : PsiTreeUtil.findChildrenOfType(statement, GoCallExpr.class)) {
            GoCallableDescriptor descriptor = GormTypes.ORM_MODEL_CALLABLES_SET.find(goCallExpr, false);
            if (descriptor == null) continue;

            Integer argumentIndex = GormTypes.GORM_MODEL_CALLABLES.get(descriptor);

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

    public static @NotNull Map<String, String> parseTag(@NotNull String str) {
        Map<String, String> settings = new HashMap<>();
        String[] names = str.split(";");

        for (int i = 0; i < names.length; i++) {
            int j = i;
            if (names[j].length() > 0) {
                while (names[j].charAt(names[j].length() - 1) == '\\') {
                    i++;
                    names[j] = names[j].substring(0, names[j].length() - 1) + ";" + names[i];
                    names[i] = "";
                }
            }

            String[] values = names[j].split(":");
            String k = values[0].trim().toUpperCase();

            if (values.length >= 2) {
                settings.put(k, String.join(":", Arrays.copyOfRange(values, 1, values.length)));
            } else if (!k.equals("")) {
                settings.put(k, k);
            }
        }

        return settings;
    }
}
