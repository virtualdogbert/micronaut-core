package org.particleframework.ast.groovy.annotation

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode

import java.lang.annotation.Annotation
import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.Target

/**
 * Deals with annotation stereotypes
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class AnnotationStereoTypeFinder {

    boolean hasStereoType(AnnotatedNode classNode, Class<? extends Annotation> stereotype) {
        return hasStereoType(classNode, stereotype.name)
    }

    boolean hasStereoType(AnnotatedNode classNode, String... stereotypes) {
        return hasStereoType(classNode, Arrays.asList(stereotypes))
    }

    boolean hasStereoType(AnnotatedNode classNode, List<String> stereotypes) {
        List<AnnotationNode> annotations = classNode.getAnnotations()
        for(ann in annotations) {
            ClassNode annotationClassNode = ann.classNode
            if(stereotypes.contains(annotationClassNode.name)) {
                return true
            }
            else if(!(annotationClassNode.name in [Retention.name, Documented.name, Target.name]) && hasStereoType(annotationClassNode, stereotypes)) {
                return true
            }
        }
        return false
    }


    AnnotationNode findAnnotationWithStereoType(AnnotatedNode annotatedNode, Class<? extends Annotation> stereotype) {
        List<AnnotationNode> annotations = annotatedNode.getAnnotations()
        for(AnnotationNode ann in annotations) {
            ClassNode annotationClassNode = ann.classNode
            if(annotationClassNode.name == stereotype.name) {
                return ann
            }
            else if(!(annotationClassNode.name in [Retention.name, Documented.name, Target.name])) {
                if(findAnnotationWithStereoType(ann.classNode, stereotype) != null) {
                    return ann
                }
            }
        }
        return null
    }
}