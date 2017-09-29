/**
 * Copyright 2009-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.io;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 *
 * <p>ResolverUtil is used to locate classes that are available in the/a class path and meet
 * arbitrary conditions. The two most common conditions are that a class implements/extends
 * another class, or that is it annotated with a specific annotation. However, through the use
 * of the {@link Test} class it is possible to search using arbitrary conditions.</p>
 *
 * <p>A ClassLoader is used to locate all locations (directories and jar files) in the class
 * path that contain classes within certain packages, and then to load those classes and
 * check them. By default the ClassLoader returned by
 * {@code Thread.currentThread().getContextClassLoader()} is used, but this can be overridden
 * by calling {@link #setClassLoader(ClassLoader)} prior to invoking any of the {@code find()}
 * methods.</p>
 *
 * <p>General searches are initiated by calling the
 * {@link #find(org.apache.ibatis.io.ResolverUtil.Test, String)} ()} method and supplying
 * a package name and a Test instance. This will cause the named package <b>and all sub-packages</b>
 * to be scanned for classes that meet the test. There are also utility methods for the common
 * use cases of scanning multiple packages for extensions of particular classes, or classes
 * annotated with a specific annotation.</p>
 *
 * <p>The standard usage pattern for the ResolverUtil class is as follows:</p>
 *
 * <pre>
 * ResolverUtil&lt;ActionBean&gt; resolver = new ResolverUtil&lt;ActionBean&gt;();
 * resolver.findImplementation(ActionBean.class, pkg1, pkg2);
 * resolver.find(new CustomTest(), pkg1);
 * resolver.find(new CustomTest(), pkg2);
 * Collection&lt;ActionBean&gt; beans = resolver.getClasses();
 * </pre>
 *
 * @author Tim Fennell
 */
public class ResolverUtil<T> {

    private static final Log log = LogFactory.getLog(ResolverUtil.class);

    /**
     * 函数式接口，判断是否符合
     * type 待检测的类，如果该类符合检测的条件，则返回true,否则返回false
     */
    public static interface Test {
        boolean matches(Class<?> type);
    }

    /**
     * 函数式接口的具体实现类
     */
    public static class IsA implements Test {
        //通过构造函数注入
        private Class<?> parent;

        public IsA(Class<?> parentType) {
            this.parent = parentType;
        }

        @Override
        public boolean matches(Class<?> type) {
            //这个判断parent是否是type的超类或同类
            return type != null && parent.isAssignableFrom(type);
        }

        @Override
        public String toString() {
            return "is assignable to " + parent.getSimpleName();
        }
    }

    /**
     * 函数式接口的具体实现类
     * 判断class是否添加了指定注解，注解通过构造函数注入，如果添加了返回true，否则返回false
     */
    public static class AnnotatedWith implements Test {
        private Class<? extends Annotation> annotation;

        //构造器注入要判断的指定注解
        public AnnotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }
        @Override
        public boolean matches(Class<?> type) {
            return type != null && type.isAnnotationPresent(annotation);
        }
        @Override
        public String toString() {
            return "annotated with @" + annotation.getSimpleName();
        }
    }

    //匹配类型容器
    private Set<Class<? extends T>> matches = new HashSet<Class<? extends T>>();

    //查找class的类加载器，没有就用Thread.currentThread().getContextClassLoader()
    private ClassLoader classloader;

    //符合的类型集合，执行find()方法 没有的话返回空
    public Set<Class<? extends T>> getClasses() {
        return matches;
    }

    public ClassLoader getClassLoader() {
        return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
    }

    public void setClassLoader(ClassLoader classloader) {
        this.classloader = classloader;
    }

   //查找package下面的Parent的同类或者实现类
    public ResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
        if (packageNames == null) {
            return this;
        }
        //实例化对象 IsA并且注入parent类，返回ResolverUtil，查看集合matches可以查看到符合的类型
        Test test = new IsA(parent);
        for (String pkg : packageNames) {
            find(test, pkg);
        }
        return this;
    }

    //查找package下面的annotation注解的类
    public ResolverUtil<T> findAnnotated(Class<? extends Annotation> annotation, String... packageNames) {
        if (packageNames == null) {
            return this;
        }
        Test test = new AnnotatedWith(annotation);
        for (String pkg : packageNames) {
            find(test, pkg);
        }
        return this;
    }

    //传入test函数表达式，传入packageName添加符合的类
    public ResolverUtil<T> find(Test test, String packageName) {
        //获取可以查询的path, ClassLoader#getResources(String)要用
        String path = getPackagePath(packageName);

        try {
            // VFS.list()查找packageName包下的所有资源
            List<String> children = VFS.getInstance().list(path);
            for (String child : children) {
                if (child.endsWith(".class")) {
                    addIfMatching(test, child);
                }
            }
        } catch (IOException ioe) {
            log.error("Could not read package: " + packageName, ioe);
        }
        return this;
    }

    /**
     * Converts a Java package name to a path that can be looked up with a call to
     * {@link ClassLoader#getResources(String)}.
     *
     * @param packageName The Java package name to convert to a path
     */
    protected String getPackagePath(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    /**
     * 这个地方用了策略模式的设计方法，传入的只是Test接口，不知道具体的实现类，类似于lambda表达式
     *
     * @param test the test used to determine if the class matches
     * @param fqn the fully qualified name of a class(查询出来的)
     */
    @SuppressWarnings("unchecked")
    protected void addIfMatching(Test test, String fqn) {
        try {
            // fqn为sample/mybatis/domain/City.class去掉class，并且"/"变成"."，通过这个名字取得到Class类型
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
            ClassLoader loader = getClassLoader();
            if (log.isDebugEnabled()) {
                log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");
            }
            // 获取要检测的类的实际class对象
            Class<?> type = loader.loadClass(externalName);
            //如果符合的话，就将class对象添加到matches集合中，这个地方调用test的match方法，如果符合就添加
            if (test.matches(type)) {
                matches.add((Class<T>) type);
            }
        } catch (Throwable t) {
            log.warn("Could not examine class '" + fqn + "'" + " due to a " +
                    t.getClass().getName() + " with message: " + t.getMessage());
        }
    }
}