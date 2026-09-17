package com.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.db.DBConnect;

/**
 * Ensures the pooled connection borrowed by DBConnect during a request is
 * always returned to the pool once the request is done, even if a
 * servlet/JSP threw an exception. Without this, connections that DAOs open
 * via DBConnect.getConnection() would never go back to the pool and it
 * would eventually run out of connections under load.
 */
@WebFilter("/*")
public class DBConnectionFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} finally {
			DBConnect.closeThreadConnection();
		}
	}
}
