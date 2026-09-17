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

@WebServlet("/addVehicle")
@MultipartConfig
public class AddVehicle extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// This servlet used to have no server-side check at all - only the JSP that
		// renders the "add vehicle" form redirected non-admins away, but nothing
		// stopped anyone from POSTing directly to /addVehicle.
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

		Part p = req.getPart("img");

		HttpSession session = req.getSession();

		String image;
		try {
			image = FileUploadUtil.buildSafeFileName(p);
		} catch (IllegalArgumentException e) {
			session.setAttribute("errorMsg", e.getMessage());
			resp.sendRedirect("admin/add_vehicle.jsp");
			return;
		}

		if (image == null) {
			session.setAttribute("errorMsg", "Please choose an image for the vehicle");
			resp.sendRedirect("admin/add_vehicle.jsp");
			return;
		}

		Vehicle v = new Vehicle(title, vehicleNumber, categoryId, availability, perDay, insuranceStatus, description,
				ownerName, contactNo, image);

		VehicleDao dao = new VehicleDao(DBConnect.getConnection());

		if (dao.checkVehicleNumber(vehicleNumber)) {
			session.setAttribute("errorMsg", "Already Vehicle Number Registered");
			resp.sendRedirect("admin/add_vehicle.jsp");
		}

		else {

			if (dao.createVehicle(v)) {

				String basePath = req.getServletContext().getRealPath("");
				FileUploadUtil.writeToUploadDir(p, basePath, "img" + java.io.File.separator + "vehicle_img", image);

				session.setAttribute("succMsg", "Added successfully");
				resp.sendRedirect("admin/add_vehicle.jsp");
			} else {
				session.setAttribute("errorMsg", "something wrong on server");
				resp.sendRedirect("admin/add_vehicle.jsp");
			}

		}

	}

}
