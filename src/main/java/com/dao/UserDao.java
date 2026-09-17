package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.entites.User;

public class UserDao {

	private Connection conn;

	private PreparedStatement ps;

	private ResultSet rs;

	public UserDao(Connection conn) {
		super();
		this.conn = conn;
	}

	/** Password is hashed with BCrypt before being stored; it is never persisted as plain text. */
	public boolean createUser(User user) {
		boolean f = false;

		try {
			String sql = "insert into user(name,email,password,address,city,state,pincode,role,mobno) values(?,?,?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(sql);

			ps.setString(1, user.getName());
			ps.setString(2, user.getEmail());
			ps.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
			ps.setString(4, user.getAddress());
			ps.setString(5, user.getCity());
			ps.setString(6, user.getState());
			ps.setString(7, user.getPincode());
			ps.setString(8, user.getRole());
			ps.setString(9, user.getMobno());
			int i = ps.executeUpdate();

			if (i == 1) {
				f = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	public boolean checkEmail(String email) {
		boolean f = false;

		try {
			String sql = "select * from user where email=?";
			ps = conn.prepareStatement(sql);

			ps.setString(1, email);

			rs = ps.executeQuery();
			while (rs.next()) {
				f = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	public List<User> getAllUsers() {
		List<User> list = new ArrayList<User>();

		User u = null;
		try {

			String sql = "select * from user";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				u = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10));
				list.add(u); // was missing: getAllUsers() previously always returned an empty list
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Verifies credentials with BCrypt.checkpw instead of comparing passwords
	 * with a plain "=" in SQL, since passwords are now stored hashed.
	 */
	public User login(String email, String password) {
		User u = getUserByEmail(email);
		if (u != null && password != null && BCrypt.checkpw(password, u.getPassword())) {
			return u;
		}
		return null;
	}

	public User getUserByEmail(String email) {
		User u = null;
		try {
			String sql = "select * from user where email=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				u = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return u;
	}

	public User getUserById(int id) {
		User u = null;
		try {
			String sql = "select * from user where id=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				u = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return u;
	}

	public boolean checkEmailAndMobForForgot(String email, String mobNo) {
		boolean f = false;
		try {
			String sql = "select * from user where email=? and mobno=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ps.setString(2, mobNo);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				f = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	/** newpass is hashed with BCrypt before being stored, same as at registration time. */
	public boolean resetPasssword(String newpass, String email) {
		boolean f = false;
		try {
			String sql = "update user set password=? where email=?";
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, BCrypt.hashpw(newpass, BCrypt.gensalt()));
			ps.setString(2, email);

			int a = ps.executeUpdate();

			if (a == 1) {
				f = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

}
