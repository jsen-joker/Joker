package com.jsen.joker.annotation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jsen.joker.annotation.annotation.Entry;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/9/5
 */
//@SupportedAnnotationTypes({"com.jsen.joker.annotation.annotation.Entry"})
//@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationCompileProcess extends AbstractProcessor {

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Test log in MyProcessor.process");
        System.out.println(roundEnv.toString());

        // 遍历annotations获取annotation类型
        for (TypeElement typeElement : annotations) {
            // 使用roundEnv.getElementsAnnotatedWith获取所有被某一类型注解标注的元素，依次遍历
            for (javax.lang.model.element.Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                // 在元素上调用接口获取注解值

                List<Pair> pairs = Lists.newArrayList();
                Entry annotation = element.getAnnotation(Entry.class);
                if (annotation != null) {
                    pairs.add(new Pair(annotation, element.toString()));
                }
                updatePom(pairs);
            }
        }
        return Boolean.TRUE;
    }

    public static class Pair {
        public Entry entry;
        public String className;

        public Pair(Entry entry, String className) {
            this.entry = entry;
            this.className = className;
        }
    }
    private void updatePom(List<Pair> pairs) {
        if (pairs.size() == 0) {
            return;
        }
        try(FileInputStream inputStream = new FileInputStream("pom.xml")) {
            SAXBuilder sb = new SAXBuilder();
            org.jdom2.Document doc = sb.build(inputStream);
            Element project = doc.getRootElement();
            Namespace namespace = project.getNamespace();
            Element properties = project.getChild("properties", namespace);
            if (properties == null) {
                properties = new Element("properties", namespace);
                project.addContent(properties);
            }

//            List<Element> rm = Lists.newArrayList();
//            for (Element child : properties.getChildren("vertx-boot-class", namespace)) {
//                String className = child.getTextTrim();
//                if( pairs.stream().filter(item -> item.className.equals(className)).collect(Collectors.toList()).size() > 0) {
//                    rm.add(child);
//                }
//            }
//            for (Element r : rm) {
//                properties.removeContent(r);
//            }

            properties.removeChildren("vertx-boot-class", namespace);

            List<Element> cds = Lists.newArrayList();
            for (Pair pair : pairs) {
                Element child = new Element("vertx-boot-class", namespace);
                child.setAttribute("priority", pair.entry.priority() + "");
                child.setAttribute("instances", pair.entry.instances() + "");
                child.setText(pair.className);
                cds.add(child);
            }
            properties.addContent(cds);

            try(FileOutputStream outputStream = new FileOutputStream("pom.xml")) {
                XMLOutputter out = new XMLOutputter();
                out.setFormat(out.getFormat().setEncoding("UTF-8"));
                out.output(doc, outputStream);
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = Sets.newLinkedHashSet();
        annotations.add(Entry.class.getCanonicalName());
        return annotations;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     *                      provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }
}
