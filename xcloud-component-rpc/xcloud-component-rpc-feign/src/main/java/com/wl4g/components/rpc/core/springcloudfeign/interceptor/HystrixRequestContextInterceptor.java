package com.wl4g.components.rpc.core.springcloudfeign.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

// TODO

/**
 * {@link HystrixRequestContextInterceptor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see https://blog.csdn.net/luliuliu1234/article/details/96472893
 */
public class HystrixRequestContextInterceptor implements HandlerInterceptor {

	public static final HystrixRequestVariableDefault<HttpServletRequest> REQUEST = new HystrixRequestVariableDefault<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 在当前线程初始化HystrixRequestContext, 并设置请求头
		if (!HystrixRequestContext.isCurrentThreadInitialized()) {
			HystrixRequestContext.initializeContext();
		}
		REQUEST.set(request);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if (HystrixRequestContext.isCurrentThreadInitialized()) {
			// 销毁当前线程
			HystrixRequestContext.getContextForCurrentThread().shutdown();
		}
	}

}
