package com.daniele.fantalive.util;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TransactionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
		String requestURI = req.getRequestURI();
		if (!requestURI.equalsIgnoreCase("/fantalive/fantalive-websocket")) {
			Constant.LAST_REFRESH=ZonedDateTime.now();
//			System.out.println("Chiamata per:  " + requestURI + " " + Constant.dateTimeFormatterOut.format(Constant.LAST_REFRESH));
		}
        chain.doFilter(request, response);
//        System.out.println("Committing a transaction for req :  " + requestURI);
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("Init Filter");
	}

	@Override
	public void destroy() {
		System.out.println("Destroy Filter");
	}

}