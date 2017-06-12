package org.particleframework.ast.groovy.utils

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression

import java.lang.annotation.Annotation

/**
 * Utility methods for dealing with annotations within the context of AST
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class AstAnnotationUtils {

    /**
     * Finds an annotation for the given annotated node and type
     *
     * @param annotatedNode The annotated node
     * @param annotationName The annotation type
     * @return The annotation or null
     */
    static AnnotationNode findAnnotation(AnnotatedNode annotatedNode, Class<?> type) {
        String annotationName = type.name
        return findAnnotation(annotatedNode, annotationName)
    }

    /**
     * Finds an annotation for the given annotated node and name
     *
     * @param annotatedNode The annotated node
     * @param annotationName The annotation name
     * @return The annotation or null
     */
    static AnnotationNode findAnnotation(AnnotatedNode annotatedNode, String annotationName) {
        List<AnnotationNode> annotations = annotatedNode.getAnnotations()
        for(ann in annotations) {
            if(ann.classNode.name == annotationName) {
                return ann
            }
        }
        return null
    }

    /**
     * Returns true if MethodNode is marked with annotationClass
     * @param methodNode A MethodNode to inspect
     * @param annotationClass an annotation to look for
     * @return true if classNode is marked with annotationClass, otherwise false
     */
    static boolean hasAnnotation(final MethodNode methodNode, final Class<? extends Annotation> annotationClass) {
        def classNode = new ClassNode(annotationClass)
        return hasAnnotation(methodNode, classNode)
    }

    static boolean hasAnnotation(MethodNode methodNode, ClassNode annotationClassNode) {
        return !methodNode.getAnnotations(annotationClassNode).isEmpty()
    }
    /**
     * Copy the annotations from one annotated node to another
     *
     * @param from The source annotated node
     * @param to The target annotated node
     */
    static void copyAnnotations(final AnnotatedNode from, final AnnotatedNode to) {
        copyAnnotations(from, to, null, null)
    }

    /**
     * Copy the annotations from one annotated node to another
     *
     * @param from The source annotated node
     * @param to The target annotated node
     * @param included The includes annotations
     * @param excluded The excluded annotations
     */
    static void copyAnnotations(final AnnotatedNode from, final AnnotatedNode to, final Set<String> included, final Set<String> excluded) {
        final List<AnnotationNode> annotationsToCopy = from.getAnnotations()
        for(final AnnotationNode node : annotationsToCopy) {
            String annotationClassName = node.getClassNode().getName()
            if((excluded==null || !excluded.contains(annotationClassName)) &&
                    (included==null || included.contains(annotationClassName))) {
                final AnnotationNode copyOfAnnotationNode = cloneAnnotation(node)
                to.addAnnotation(copyOfAnnotationNode)
            }
        }
    }

    /**
     * Clones the given annotation node returning a new one
     *
     * @param node The annotation node
     * @return The cloned annotation node
     */
    static AnnotationNode cloneAnnotation(final AnnotationNode node) {
        final AnnotationNode copyOfAnnotationNode = new AnnotationNode(node.getClassNode())
        final Map<String, Expression> members = node.getMembers()
        for(final Map.Entry<String, Expression> entry : members.entrySet()) {
            copyOfAnnotationNode.addMember(entry.getKey(), entry.getValue())
        }
        return copyOfAnnotationNode
    }
}