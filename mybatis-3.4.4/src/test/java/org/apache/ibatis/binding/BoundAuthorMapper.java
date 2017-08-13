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
package org.apache.ibatis.binding;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@CacheNamespace(readWrite = false)
public interface BoundAuthorMapper {

  //======================================================

  List<Post> findPostsInArray(Integer[] ids);

  //======================================================

  List<Post> findPostsInList(List<Integer> ids);

  //======================================================

  int insertAuthor(Author author);

  int insertAuthorDynamic(Author author);

  //======================================================

//  这个地方没有 property = id , 默认就是id, 注解进行映射
  @ConstructorArgs({
      @Arg(column = "AUTHOR_ID", javaType = int.class)
  })
  @Results({
      @Result(property = "username", column = "AUTHOR_USERNAME"),
      @Result(property = "password", column = "AUTHOR_PASSWORD"),
      @Result(property = "email", column = "AUTHOR_EMAIL"),
      @Result(property = "bio", column = "AUTHOR_BIO")
  })
  @Select({
      "SELECT ",
      "  ID as AUTHOR_ID,",
      "  USERNAME as AUTHOR_USERNAME,",
      "  PASSWORD as AUTHOR_PASSWORD,",
      "  EMAIL as AUTHOR_EMAIL,",
      "  BIO as AUTHOR_BIO",
      "FROM AUTHOR WHERE ID = #{id}"})
  Author selectAuthor(int id);

  //======================================================

  // 这个@ConstructorArgs注解是用 构造函数进行 result mapping的
  @ConstructorArgs({
      @Arg(column = "AUTHOR_ID", javaType = Integer.class),
      @Arg(column = "AUTHOR_USERNAME", javaType = String.class),
      @Arg(column = "AUTHOR_PASSWORD", javaType = String.class),
      @Arg(column = "AUTHOR_EMAIL", javaType = String.class),
      @Arg(column = "AUTHOR_BIO", javaType = String.class),
      @Arg(column = "AUTHOR_SECTION", javaType = Section.class)
  })
  @Select({
      "SELECT ",
      "  ID as AUTHOR_ID,",
      "  USERNAME as AUTHOR_USERNAME,",
      "  PASSWORD as AUTHOR_PASSWORD,",
      "  EMAIL as AUTHOR_EMAIL,",
      "  BIO as AUTHOR_BIO," +
          "  FAVOURITE_SECTION as AUTHOR_SECTION",
      "FROM AUTHOR WHERE ID = #{id}"})
  Author selectAuthorConstructor(int id);

  //======================================================

  // 注意xml配置文件中的 #{2}，这个是取 int three 的参数，从#{0} 开始的
  List<Post> findThreeSpecificPosts(@Param("one") int one,
                                    RowBounds rowBounds,
                                    @Param("two") int two,
                                    int three);

  @Flush
  List<BatchResult> flush();

}
