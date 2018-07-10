package com.imooc.myo2o.dao.split;

import java.util.Locale;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DynamicDataSourceInterceptor implements Interceptor {
    //正则表达式 判断操作是否是 增加 删除 修改
	private static final String REGEX=".*insert\\u0020.*|.*delete\\u0020.*|.*update\\u0020.*";
	private static Logger logger=LoggerFactory.getLogger(DynamicDataSourceInterceptor.class);
	
	
	public Object intercept(Invocation invocation) throws Throwable {
		boolean synchronizationActive = TransactionSynchronizationManager.isActualTransactionActive(); 
		Object[] objects = invocation.getArgs();
		MappedStatement ms=(MappedStatement) objects[0];
		String lookupKey=DynamicDataSourceHolder.DB_MASTER;
		//如果不为true  就说明没有用事物管理
		if(synchronizationActive!=true){
			//读方法
			if(ms.getSqlCommandType().equals(SqlCommandType.SELECT)){
				//selectkey 为自增id查询主键
				if(ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)){
					lookupKey=DynamicDataSourceHolder.DB_MASTER;
				}else{
					BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
					String sql=boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\t\\r\\n]"," ");
					if(sql.matches(REGEX)){
						lookupKey=DynamicDataSourceHolder.DB_MASTER;
					}else{
						lookupKey=DynamicDataSourceHolder.DB_SLAVE;
					}
				}
			}
		}else{
			lookupKey=DynamicDataSourceHolder.DB_MASTER;
		}
		logger.debug("设置方法[{}] use [{}] Strategy,SqlCommandType[{}]..",ms.getId(),lookupKey
				,ms.getSqlCommandType().name());
			DynamicDataSourceHolder.setDbType(lookupKey);
		return invocation.proceed();
	}

	/**
	 * 返回对象
	 */
	public Object plugin(Object target) {
	    if(target instanceof Executor){
	    	return Plugin.wrap(target, this);
	    }else{
	    	return target;
	    }
	}

	/**
	 * 类初始化的时候 加载一些东西
	 */
	public void setProperties(Properties arg0) {
		// TODO Auto-generated method stub
		
	}
  
}
