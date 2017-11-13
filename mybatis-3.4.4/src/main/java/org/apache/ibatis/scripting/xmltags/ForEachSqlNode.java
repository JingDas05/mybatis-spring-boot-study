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

import java.util.Map;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class ForEachSqlNode implements SqlNode {
  public static final String ITEM_PREFIX = "__frch_";

  // 用于判断循环的终止条件，构造方法中会创建该对象
  private ExpressionEvaluator evaluator;
  // 这个为数组名字， 迭代的集合表达式
  private String collectionExpression;
  // 该节点的子节点
  private SqlNode contents;
  private String open;
  private String close;
  private String separator;
  // 当前迭代的元素，若迭代集合是Map,则index是键，item是值
  private String item;
  // 参数索引值，0， 1， 2，当前迭代的索引
  private String index;
  private Configuration configuration;

  public ForEachSqlNode(Configuration configuration, SqlNode contents, String collectionExpression, String index, String item, String open, String close, String separator) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
    this.configuration = configuration;
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 获取参数信息
    Map<String, Object> bindings = context.getBindings();
    // collectionExpression 是传递进来的参数名字key,把参数转化成迭代器
    // 解析集合表达式对应的实际参数
    final Iterable<?> iterable = evaluator.evaluateIterable(collectionExpression, bindings);
    // 检测集合长度，如果没有元素了就返回 true
    if (!iterable.iterator().hasNext()) {
      return true;
    }
    boolean first = true;
    // 在循环开始之前，调用 DynamicContext。appendSql() 添加open指定的字符串
    applyOpen(context);
    int i = 0;
    // 依次迭代参数数组
    for (Object o : iterable) {
      // context 暂存
      DynamicContext oldContext = context;
      if (first) {
        // 如果是集合的第一项，则将 PrefixedContext。prefix初始化为空字符串
        context = new PrefixedContext(context, "");
      } else if (separator != null) {
        // 如果指定了分隔符，则PrefixedContext.prefix 初始化为指定分隔符
        context = new PrefixedContext(context, separator);
      } else {
        // 未指定分隔符，则 PrefixedContext.prefix 初始化为空字符串
          context = new PrefixedContext(context, "");
      }
      // uniqueNumber 从 0 开始，每次递增1， 用于转换生成新的“#{}”占位符
      int uniqueNumber = context.getUniqueNumber();
      // Issue #709
      // 如果集合是map 类型，将集合中的key和value添加到 DynamicContext bindings集合中保存
      if (o instanceof Map.Entry) {
        @SuppressWarnings("unchecked") 
        Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) o;
        applyIndex(context, mapEntry.getKey(), uniqueNumber);
        applyItem(context, mapEntry.getValue(), uniqueNumber);
      } else {
        // 将集合中的索引和元素添加到 DynamicContext bindings集合中保存
        applyIndex(context, i, uniqueNumber);
        applyItem(context, o, uniqueNumber);
      }
      contents.apply(new FilteredDynamicContext(configuration, context, index, item, uniqueNumber));
      if (first) {
        first = !((PrefixedContext) context).isPrefixApplied();
      }
      context = oldContext;
      i++;
    }
    applyClose(context);
    return true;
  }

  //
  private void applyIndex(DynamicContext context, Object o, int i) {
    if (index != null) {
      // key 为 index
      context.bind(index, o);
      // key 为__frch_index_0
      context.bind(itemizeItem(index, i), o);
    }
  }

  private void applyItem(DynamicContext context, Object o, int i) {
    if (item != null) {
      // key 为 item
      context.bind(item, o);
      // key 为 __frch_item_0
      context.bind(itemizeItem(item, i), o);
    }
  }

  // 向 context 中添加左开符号
  private void applyOpen(DynamicContext context) {
    if (open != null) {
      context.appendSql(open);
    }
  }

  private void applyClose(DynamicContext context) {
    if (close != null) {
      context.appendSql(close);
    }
  }

  private static String itemizeItem(String item, int i) {
    return new StringBuilder(ITEM_PREFIX).append(item).append("_").append(i).toString();
  }

  // 静态内部类，装饰 DynamicContext，或者说是 DynamicContext的代理类
  // 负责处理 #{} 占位符，但它并未完全解析 #{} 占位符
  private static class FilteredDynamicContext extends DynamicContext {
    private DynamicContext delegate;
    // 对应集合项在集合中的索引位置
    private int index;
    // 对应集合项的 index
    private String itemIndex;
    // 对应集合项 item
    private String item;

    public FilteredDynamicContext(Configuration configuration,DynamicContext delegate, String itemIndex, String item, int i) {
      super(configuration, null);
      this.delegate = delegate;
      this.index = i;
      this.itemIndex = itemIndex;
      this.item = item;
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    // #{item} -> #{__frch_item_1}
    // #{itemIndex} -> #{__frch_itemIndex_1}
    @Override
    public void appendSql(String sql) {
      GenericTokenParser parser = new GenericTokenParser("#{", "}", new TokenHandler() {
        @Override
        public String handleToken(String content) {
          String newContent = content.replaceFirst("^\\s*" + item + "(?![^.,:\\s])", itemizeItem(item, index));
          if (itemIndex != null && newContent.equals(content)) {
            newContent = content.replaceFirst("^\\s*" + itemIndex + "(?![^.,:\\s])", itemizeItem(itemIndex, index));
          }
          return new StringBuilder("#{").append(newContent).append("}").toString();
        }
      });

      //将解析后的SQL语句片段追加到delegate中保存
      delegate.appendSql(parser.parse(sql));
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }

  }

  // 内部类，装饰DynamicContext,这里可以看到，每当有新的appendSql()方法逻辑时，就增加一个装饰类
  private class PrefixedContext extends DynamicContext {
    // 底层封装的 DynamicContext 对象
    private DynamicContext delegate;
    // 指定的前缀
    private String prefix;
    // 是否已经处理过前缀
    private boolean prefixApplied;

    public PrefixedContext(DynamicContext delegate, String prefix) {
      super(configuration, null);
      this.delegate = delegate;
      this.prefix = prefix;
      this.prefixApplied = false;
    }

    public boolean isPrefixApplied() {
      return prefixApplied;
    }

    @Override
    public Map<String, Object> getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public void appendSql(String sql) {
      // 如果前缀没有应用过，就添加前缀
      if (!prefixApplied && sql != null && sql.trim().length() > 0) {
        // 追加前缀
        delegate.appendSql(prefix);
        // 表示已经处理过前缀
        prefixApplied = true;
      }
      // 之后添加sql
      delegate.appendSql(sql);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }
  }

}
