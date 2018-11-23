/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.reflection.ExceptionUtil;

/**
 *
 * 这个类本身就是一个处理器代理处理器,构造器注入 代理的目标target， 拦截器Interceptor， 签名map signatureMap
 *
 * @author Clinton Begin
 */
public class Plugin implements InvocationHandler {

  private Object target;
  private Interceptor interceptor;
  private Map<Class<?>, Set<Method>> signatureMap;

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  // 进行封装，根据拦截器，返回代理对象
  public static Object wrap(Object target, Interceptor interceptor) {
    // 这个方法获取 拦截器注解上的所有拦截配置，返回封装参数Map
    // key为拦截的接口Executor, ParameterHandler, ResultSetHandler, StatementHandler
    // value 为拦截的方法s
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    // 获取父接口
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      // 创建代理对象
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          // 这个地方很重要，拦截处理器，构造器注入参数，参数从实际拦截器的注解而来
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }

  // 拦截处理器实际执行的方法
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      // 先执行拦截方法
      if (methods != null && methods.contains(method)) {
        // 先去执行拦截器的 intercept()方法，参数是封装好的 目标以及相应的参数
        return interceptor.intercept(new Invocation(target, method, args));
      }
      // 再执行自己的方法
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  // 这个方法获取 拦截器注解上的所有拦截配置，返回封装参数Map
  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    // 获取拦截器注解@Intercepts
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    // issue #251
    if (interceptsAnnotation == null) {
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());      
    }
    // 获取@Intercepts 的值Signature
    Signature[] sigs = interceptsAnnotation.value();
    // 这个数组 key为拦截的接口Executor, ParameterHandler, ResultSetHandler, StatementHandler
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<Class<?>, Set<Method>>();
    for (Signature sig : sigs) {
      Set<Method> methods = signatureMap.get(sig.type());
      if (methods == null) {
        methods = new HashSet<Method>();
        // 这个地方 methods 是空的
        signatureMap.put(sig.type(), methods);
      }
      try {
        // 这个地方才真正在methods添加参数
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }
    return signatureMap;
  }

  //
  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();
    // 寻找type的父接口（Executor, ParameterHandler, ResultSetHandler, StatementHandler），并将结果放到interfaces中
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }

}
