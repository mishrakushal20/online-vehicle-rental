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
import com.entites.Category;
import com.util.AuthUtil;
import com.util.FileUploadUtil;

@WebServlet("/updateCategory")
@MultipartConfig
public class UpdateCategory extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!AuthUtil.isAdmin(req)) {
			resp.sendRedirect("login.jsp");
			return;
		}

		String title = req.getParameter("title");
		Part p = req.getPart("img");
		int id = Integer.parseInt(req.getParameter("id"));
		HttpSession session = req.getSession();

		CategoryDao dao = new CategoryDao(DBConnect.getConnection());

		String fileName;
		try {
			String newFileName = FileUploadUtil.buildSafeFileName(p);
			fileName = (newFileName != null) ? newFileName : dao.getCategoryById(id).getImage();
		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", e.getMessage());
			resp.sendRedirect("admin/category.jsp");
			return;
		}

		Category cat = new Category(id, title, fileName);

		if (dao.updateCategory(title, fileName, id)) {

			if (p.getSubmittedFileName() != null && !p.getSubmittedFileName().isEmpty()) {
				String basePath = req.getServletContext().getRealPath("");
				FileUploadUtil.writeToUploadDir(p, basePath, "img" + java.io.File.separator + "category_img",
						fileName);
			}

			session.setAttribute("succMsg", "Update sucesfully");
			resp.sendRedirect("admin/category.jsp");

		} else {
			session.setAttribute("errorMsg", "something wrong on server");
			resp.sendRedirect("admin/category.jsp");
		}

	}

}
