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

import com.dao.VehicleDao;
import com.db.DBConnect;
import com.entites.Vehicle;
import com.util.AuthUtil;
import com.util.FileUploadUtil;

@WebServlet("/updateVehicle")
@MultipartConfig
public class UpdateVehicle extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!AuthUtil.isAdmin(req)) {
			resp.sendRedirect("login.jsp");
			return;
		}

		String title = req.getParameter("title");
		String vehicleNumber = req.getParameter("vehicleNumber");
		int categoryId = Integer.parseInt(req.getParameter("categoryId"));
		String availability = req.getParameter("availability");
		Double perDay = Double.parseDouble(req.getParameter("perDay"));
		String insuranceStatus = req.getParameter("insuranceStatus");
		String description = req.getParameter("description");
		String ownerName = req.getParameter("ownerName");
		String contactNo = req.getParameter("contactNo");
		int id = Integer.parseInt(req.getParameter("id"));

		Part p = req.getPart("img");
		HttpSession session = req.getSession();

		VehicleDao dao = new VehicleDao(DBConnect.getConnection());

		String image;
		try {
			String newImage = FileUploadUtil.buildSafeFileName(p);
			image = (newImage != null) ? newImage : dao.getVehicleById(id).getImage();
		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", e.getMessage());
			resp.sendRedirect("admin/view_vehicle.jsp");
			return;
		}

		Vehicle v = new Vehicle(id, title, vehicleNumber, categoryId, availability, perDay, insuranceStatus,
				description, ownerName, contactNo, image);

		if (dao.updateVehicle(v)) {

			if (p.getSubmittedFileName() != null && !p.getSubmittedFileName().isEmpty()) {
				String basePath = req.getServletContext().getRealPath("");
				FileUploadUtil.writeToUploadDir(p, basePath, "img" + java.io.File.separator + "vehicle_img", image);
			}

			session.setAttribute("succMsg", "Update successfully");
			resp.sendRedirect("admin/view_vehicle.jsp");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
			resp.sendRedirect("admin/view_vehicle.jsp");

		}

	}
}
