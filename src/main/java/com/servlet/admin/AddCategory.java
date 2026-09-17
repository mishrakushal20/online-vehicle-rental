package com.servlet.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.dao.CategoryDao;
import com.db.DBConnect;
import com.util.AuthUtil;
import com.util.FileUploadUtil;

@MultipartConfig
@WebServlet("/addCategory")
public class AddCategory extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!AuthUtil.isAdmin(req)) {
			resp.sendRedirect("login.jsp");
			return;
		}

		String title = req.getParameter("title");
		Part p = req.getPart("img");
		HttpSession session = req.getSession();

		String fileName;
		try {
			fileName = FileUploadUtil.buildSafeFileName(p);
		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", e.getMessage());
			resp.sendRedirect("admin/category.jsp");
			return;
		}

		if (fileName == null) {
			session.setAttribute("errorMsg", "Please choose an image for the category");
			resp.sendRedirect("admin/category.jsp");
			return;
		}

		CategoryDao dao = new CategoryDao(DBConnect.getConnection());

		if (dao.addCategory(title, fileName)) {
			String basePath = req.getServletContext().getRealPath("");
			FileUploadUtil.writeToUploadDir(p, basePath, "img" + java.io.File.separator + "category_img", fileName);

			session.setAttribute("succMsg", "Added successfully");
			resp.sendRedirect("admin/category.jsp");

		} else {
			session.setAttribute("errorMsg", "something wrong on server");
			resp.sendRedirect("admin/category.jsp");
		}

	}

}
