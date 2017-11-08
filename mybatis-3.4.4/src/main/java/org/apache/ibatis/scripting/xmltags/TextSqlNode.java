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
package org.apache.ibatis.scripting.xmltags;

import java.util.regex.Pattern;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

/**
 *
 * 文字sqlNode 解析${}中的值，并调用apply方法，添加到DynamicContext中的sqlBuilder中
 *
 * @author Clinton Begin
 */
public class TextSqlNode implements SqlNode {
  private String text;
  private Pattern injectionFilter;

  public TextSqlNode(String text) {
    this(text, null);
  }

  // 可以传入过滤器，根据正则
  public TextSqlNode(String text, Pattern injectionFilter) {
    this.text = text;
    this.injectionFilter = injectionFilter;
  }

  // 判断是否是动态sql
  public boolean isDynamic() {
    // 构建动态sql检查解析器
    DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
    // 这个地方传入的 DynamicCheckerTokenParser 如果被调用了 handle()方法，会将isDynamic置位，说明是动态的
    GenericTokenParser parser = createParser(checker);
    parser.parse(text);
    return checker.isDynamic();
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 这个地方传入的 BindingTokenParser，如果被调用了 handle()方法，会绑定 ${ }里面的值
    GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
    context.appendSql(parser.parse(text));
    return true;
  }
  
  private GenericTokenParser createParser(TokenHandler handler) {
    // 传入自定义的tokenHandler，生成GenericTokenParser，并且传入  openToken 和 closeToken
    return new GenericTokenParser("${", "}", handler);
  }

  // 内部类，定义绑定token解析器
  // 根据 DynamicContext.bindings集合中信息解析SQL语句中的${}
  private static class BindingTokenParser implements TokenHandler {

    private DynamicContext context;
    private Pattern injectionFilter;

    public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
      this.context = context;
      this.injectionFilter = injectionFilter;
    }

    // 解析传入的值text中的${},ognl去context 的 bindings中寻找值
    @Override
    public String handleToken(String content) {
      Object parameter = context.getBindings().get("_parameter");
      // 为了OGNL解析初始化value
      //检查参数类型，赋值
      if (parameter == null) {
        context.getBindings().put("value", null);
        // 检查是否是基本简单类型
      } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
        context.getBindings().put("value", parameter);
      }
      // ognl解析取值
      Object value = OgnlCache.getValue(content, context.getBindings());
      // issue #274 return "" instead of "null"
      String srtValue = (value == null ? "" : String.valueOf(value));
      // 检查值 是否满足正则injectionFilter，不满足抛出异常ScriptingException
      checkInjection(srtValue);
      return srtValue;
    }

    private void checkInjection(String value) {
      if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
        throw new ScriptingException("Invalid input. Please conform to regex" + injectionFilter.pattern());
      }
    }
  }

  // 内部类，定义绑定动态sql检查器
  private static class DynamicCheckerTokenParser implements TokenHandler {

    private boolean isDynamic;

    public DynamicCheckerTokenParser() {
      // Prevent Synthetic Access
    }

    public boolean isDynamic() {
      return isDynamic;
    }

    @Override
    public String handleToken(String content) {
      this.isDynamic = true;
      return null;
    }
  }
  
}