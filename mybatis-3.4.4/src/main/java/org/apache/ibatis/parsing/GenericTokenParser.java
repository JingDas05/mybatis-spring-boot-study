/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.parsing;

/**
 * 公共token解析器，构造函数是openToken 和 closeToken token处理器
 * 这个公共解析器抽象的很好
 *
 * 顺序查找openToken 和 closeToken 解析得到占位符的字面量，并将其交给TokenHandler处理，然后
 * 将解析结果重新拼装成字符串
 *
 * @author Clinton Begin
 */
public class GenericTokenParser {

  private final String openToken;
  private final String closeToken;
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  // 解析，参数在调用方法的时候传入,精华是这个类不与参数耦合，参数通过调用方法时传递进来
  public String parse(String text) {
    // 检测 text 是否为空
    if (text == null || text.isEmpty()) {
      return "";
    }
    char[] src = text.toCharArray();
    int offset = 0;
    // search open token 查找开始标记
    int start = text.indexOf(openToken, offset);
    if (start == -1) {
      return text;
    }
    // 用来记录解析后的字符串
    final StringBuilder builder = new StringBuilder();
    // 用来记录一个占位符的字面量
    StringBuilder expression = null;
    while (start > -1) {
      //如果是转义符$
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        // 遇到转义的开始标记，直接将前面的字符串以及开始标记追加到builder中
        builder.append(src, offset, start - offset - 1).append(openToken);
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.
        // 查找到开始标记，且未转义
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        // 将前面的字符串追加到builder中
        builder.append(src, offset, start - offset);
        // 修改offset的位置
        offset = start + openToken.length();
        // 从offset开始向后继续查看结束标记
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          if (end > offset && src[end - 1] == '\\') {
            // this close token is escaped. remove the backslash and continue.
            // 处理转义的结束标记，直接添加，并且更改 offset位置
            expression.append(src, offset, end - offset - 1).append(closeToken);
            offset = end + closeToken.length();
            // 继续寻找结束标记
            end = text.indexOf(closeToken, offset);
          } else {
            // 将开始标记和结束标记之间的字符串追加到expression中保存
            expression.append(src, offset, end - offset);
            offset = end + closeToken.length();
            break;
          }
        }
        if (end == -1) {
          // close token was not found.
          // 未找到结束标记
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          // 将占位符的字面量交给TokenHandler处理，并将处理结果追加到builder中保存，保存
          // 最终拼凑出解析后的完整内容
          builder.append(handler.handleToken(expression.toString()));
          offset = end + closeToken.length();
        }
      }
      start = text.indexOf(openToken, offset);
    }
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
