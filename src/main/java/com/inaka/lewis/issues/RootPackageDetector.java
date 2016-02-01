package com.inaka.lewis.issues;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import lombok.ast.AstVisitor;
import lombok.ast.ClassDeclaration;
import lombok.ast.EnumDeclaration;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.InterfaceDeclaration;
import lombok.ast.Node;


public class RootPackageDetector extends Detector implements Detector.JavaScanner {

    public static final Issue ISSUE_CLASS_IN_ROOT_PACKAGE = Issue.create(
            "RootPackageDetector",
            "Java file should not be inside root package",
            "Every .java file should be inside a custom package inside the root package.",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(RootPackageDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public AstVisitor createJavaVisitor(@NonNull final JavaContext context) {
        return new ForwardingAstVisitor() {
            @Override
            public boolean visitClassDeclaration(ClassDeclaration node) {
                String fileName = node.astName().astValue();
                shouldNotBeInRootPackage(context, node, fileName);
                return super.visitClassDeclaration(node);
            }

            @Override
            public boolean visitInterfaceDeclaration(InterfaceDeclaration node) {
                String fileName = node.astName().astValue();
                shouldNotBeInRootPackage(context, node, fileName);
                return super.visitInterfaceDeclaration(node);
            }

            @Override
            public boolean visitEnumDeclaration(EnumDeclaration node) {
                String fileName = node.astName().astValue();
                shouldNotBeInRootPackage(context, node, fileName);
                return super.visitEnumDeclaration(node);
            }
        };
    }

    private void shouldNotBeInRootPackage(JavaContext context, Node node, String fileName) {
        String packageName = context.getMainProject().getPackage();
        Location nodeLocation = context.getLocation(node);

        String classLocationString = nodeLocation.getFile().toString().replaceAll("/", ".");

        int findPackage = classLocationString.lastIndexOf(packageName);
        String filePackageString = classLocationString.substring(findPackage);
        String previousPath = classLocationString.substring(0, findPackage);

        if (filePackageString.equals(packageName + "." + fileName + ".java")
                && !previousPath.contains("generated")) {
            context.report(ISSUE_CLASS_IN_ROOT_PACKAGE, nodeLocation,
                    " Expecting " + fileName + " not to be in root package " + packageName);
        }

    }

}