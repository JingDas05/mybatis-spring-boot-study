package tk.mybatis.simple;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts(@Signature(type = Executor.class, method = "query",
			args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class SimpleInterceptor implements Interceptor {
	private String name;
	
	public SimpleInterceptor(String name) {
		this.name = name;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		System.out.println("进入拦截器：" + name);
		Object result = invocation.proceed();
		System.out.println("跳出拦截器：" + name);
		return result;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
