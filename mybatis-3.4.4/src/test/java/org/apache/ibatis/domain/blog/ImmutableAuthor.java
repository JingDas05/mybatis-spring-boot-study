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
package org.apache.ibatis.domain.blog;

import java.io.Serializable;

public class ImmutableAuthor implements Serializable {
  protected final int id;
  protected final String username;
  protected final String password;
  protected final String email;
  protected final String bio;
  protected final Section favouriteSection;

  public ImmutableAuthor(int id, String username, String password, String email, String bio, Section section) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.bio = bio;
    this.favouriteSection = section;
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public String getBio() {
    return bio;
  }

  public Section getFavouriteSection() {
    return favouriteSection;
  }

  // 总体思想就是判断各种false，如果都不为false就返回true
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Author)) return false;

    Author author = (Author) o;

    if (id != author.id) return false;
    if (bio != null ? !bio.equals(author.bio) : author.bio != null) return false;
    if (email != null ? !email.equals(author.email) : author.email != null) return false;
    // 首先判断this.password是否为空，如果不为空的话，判断this.password和传入进来的password比较
    // 如果this.password 为空的话，那么判断传递进来的password是否为空
    if (password != null ? !password.equals(author.password) : author.password != null) return false;
    if (username != null ? !username.equals(author.username) : author.username != null) return false;
    if (favouriteSection != null ? !favouriteSection.equals(author.favouriteSection) : author.favouriteSection != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    result = id;
    result = 31 * result + (username != null ? username.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (bio != null ? bio.hashCode() : 0);
    result = 31 * result + (favouriteSection != null ? favouriteSection.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return id + " " + username + " " + password + " " + email;
  }
}
