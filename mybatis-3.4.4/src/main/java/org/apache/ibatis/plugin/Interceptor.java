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

import java.util.Properties;

/**
 * @author Clinton Begin
 */
public interface Interceptor {

  // 运行时要执行的拦截方法，通过参数 invocation可以得到很多有用的信息
  Object intercept(Invocation invocation) throws Throwable;

  // target 就是拦截器要拦截的对象
  Object plugin(Object target);

  //这个设置属性的方法，在扫描配置文件plugin节点XNode 的getChildrenAsProperties()会读取出来，之后传参
  void setProperties(Properties properties);

}
