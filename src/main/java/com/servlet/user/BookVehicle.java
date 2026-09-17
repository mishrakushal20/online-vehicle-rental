package com.servlet.user;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.BookingDao;
import com.dao.VehicleDao;
import com.db.DBConnect;
import com.entites.Booking;
import com.entites.User;
import com.util.AuthUtil;

@WebServlet("/bookVehicle")
public class BookVehicle extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// The userId used to come from a hidden form field, i.e. straight from the
		// client, which meant anyone could book a vehicle under a different user's
		// account just by editing the form before submitting it. It must come from
		// the server-side session instead.
		User loggedInUser = AuthUtil.getLoggedInUser(req);
		if (loggedInUser == null) {
			resp.sendRedirect("login.jsp");
			return;
		}
		int userId = loggedInUser.getId();

		int vehicleId = Integer.parseInt(req.getParameter("vehicleId"));

		String fromDate = req.getParameter("fromDate");
		String toDate = req.getParameter("toDate");
		String day = req.getParameter("day");

		Double totalPrice = Double.parseDouble(req.getParameter("totalPrice"));

		String idCard = req.getParameter("idCard");

		String status = "Booked";

		String orderId = "ORD" + new Random().nextInt(1000) + "";
		String bookingDate = LocalDate.now() + "";

		Booking b = new Booking(userId, vehicleId, fromDate, toDate, day, totalPrice, idCard, orderId, status,
				bookingDate);

		BookingDao dao = new BookingDao(DBConnect.getConnection());
		VehicleDao dao2 = new VehicleDao(DBConnect.getConnection());

		HttpSession session = req.getSession();

		if (dao.createBook(b)) {
			dao2.updateVehicleStatus(vehicleId, "Booked");
			session.setAttribute("succMsg", "Booking Sucessfully");
			resp.sendRedirect("success.jsp");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
			resp.sendRedirect("book.jsp?id=" + vehicleId);
		}

	}

}
