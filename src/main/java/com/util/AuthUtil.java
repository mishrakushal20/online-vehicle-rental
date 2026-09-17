package com.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.entites.User;

/**
 * The JSPs already redirect to the login page in the browser when
 * "adminObj"/"userObj" is missing from the session, but that check only
 * protects the page that *renders* a form - it does nothing to protect the
 * servlet that the form posts to. Without a check here too, anyone could
 * call e.g. POST /addVehicle or GET /deleteVehicle directly, without ever
 * logging in. Every servlet that changes data now calls one of these first.
 */
public final class AuthUtil {

	private AuthUtil() {
	}

	public static boolean isAdmin(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return false;
		}
		Object admin = session.getAttribute("adminObj");
		return admin instanceof User && "ADMIN".equals(((User) admin).getRole());
	}

	/** Returns the logged-in user, or null if nobody is logged in this session. */
	public static User getLoggedInUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return null;
		}
		Object user = session.getAttribute("userObj");
		return user instanceof User ? (User) user : null;
	}
}
