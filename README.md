# MyBatis 源码学习

![mybatis-spring](http://mybatis.github.io/images/mybatis-logo.png)

** 模块介绍 **   

binding模块 ---- 绑定了 mapper接口和mapper里的方法对应着java类的关系  

builder模块 ---- 读取xml配置时建造者  

cache模块 ---- 提供了一级缓存的接口和实现类  

cursor模块 ---- 封装了结果集的游标操作  

datasource模块 ---- 提供了自己的datasource实现  

executor模块 ---- 数据库操作的执行器  

io模块 ---- 类加载，资源加载封装  

logging模块 ---- 日志模块封装  

mapping模块 ---- 提供了 xml配置，接口等和类之间的抽象映射  

parsing模块 ---- xml解析模块  

plugin模块 ---- 拦截器模块  

reflection模块 ---- 发射模块，提供了类和对象操作的工具包  

session模块 ---- 提供了数据库访问的sesson  

transaction模块 ---- 事务模块，可以依赖于spring的事务  

type模块 ---- 入参以及结果映射的类型处理器
