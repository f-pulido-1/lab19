package com.csumb.cst363;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.xml.transform.Result;

@SuppressWarnings("unused")
@Controller   
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/* 
	 * Patient requests form to search for prescription.
	 * Do not modify this method.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_fill";
	}

	// Helper method to retrieve pharmacy information
	private int getPharmacyIdByNameAndAddress(Connection con, String pharmacy_name, String pharmacy_address) throws SQLException {
		int pharmacyId = -1;

		String query = "SELECT * FROM pharmacy WHERE pharmacy_name = ? AND pharmacy_address = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, pharmacy_name);
			ps.setString(2, pharmacy_address);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				pharmacyId = rs.getInt("pharmacy_id");
			}
		}
		System.out.println("\n* PHARMACY ID: " + pharmacyId);
		return pharmacyId;
	}

	// Helper method to retrieve drug from RXID
	private String getDrugById(Connection con, int drug_id) throws SQLException {
		System.out.println("\n* Accessing `getDrugById` method");
		String drug = "";

		String query = "SELECT * FROM drug WHERE drug_id = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, drug_id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				drug = rs.getString("drug_name");
			}
		}
		System.out.println("\n* RETRIEVING drug");
		return drug;
	}

	// Helper method to retrieve prescription information from RXID and patient last name
	private Prescription getPrescriptionInfoByRxidAndLastName(Connection con, String rxid, String last_name) throws SQLException {
		System.out.println("\n* Accessing `getPrescriptionInfoByRxidAndLastName` method");
		Prescription p = new Prescription();

		String query = "SELECT * FROM prescription WHERE RXID = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, rxid);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				p.setRxid(rs.getString("RXID"));
				// TODO: but this is where we have drug problems
//				p.setDrugName(getDrugById(con, rs.getInt("drug_id");
//				int drugId = getDrugIdByName(con, rs.getString("drug_name"));
//				p.setDrugName(rs.getString(getDrugById(con, rs.getInt("drug_id"))));

				p.setDrugName(getDrugById(con, rs.getInt("drug_id")));

				p.setQuantity(rs.getInt("quantity"));

				p.setPatient_id(rs.getInt("patient_id"));
				Patient pt = getPatientById(con, p.getPatient_id());
				p.setPatientFirstName(pt.getFirst_name());
				p.setPatientLastName(pt.getLast_name());

				p.setDoctor_id(rs.getInt("doctor_id"));
				Doctor d = getDoctorById(con, p.getDoctor_id());
				p.setDoctorFirstName(d.getFirst_name());
				p.setDoctorLastName(d.getLast_name());
//				p.setDateCreated();
				p.setRefills(rs.getInt("num_of_refills"));

//				if (pt.getLast_name().equals(last_name)) { //Alternatively use equalsIgnoreCase
//					p.setPatientFirstName(pt.getFirst_name());
//					p.setPatientLastName(pt.getLast_name());
//					p.setDoctor_id(rs.getInt("doctor_id"));
//					p.setRefills(rs.getInt("num_of_refills"));
//				} else {
//					// Handle the case where the last names do not match
//					System.out.println("Error: Patient last name does not match the provided last name.");
//					return null;
//				}

//				p.setDoctor_id(rs.getInt("doctor_id"));		Commented out bc used in if statement
//				Doctor d = getDoctorById(con, p.getDoctor_id());
//				p.setDoctorFirstName(d.getFirst_name());
//				p.setDoctorLastName(d.getLast_name());
//				p.setDateCreated();
//				p.setRefills(rs.getInt("num_of_refills"));		Commented out bc used in if statement
			}
		}
		System.out.println("\n* RETRIEVING prescription info: " + p);
		return p;
	}
	
	// TODO: duplicate method, finish at the end
	// Helper method to get patient info from the patient_id
	private Patient getPatientById(Connection con, int id) throws SQLException {
		System.out.println("\n* Accessing `getPatientById` method");
		Patient p = new Patient();

		String query = "SELECT * FROM patient WHERE patient_id = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				p.setId(rs.getInt("patient_id"));
				p.setFirst_name(rs.getString("first_name"));
				p.setLast_name(rs.getString("last_name"));
				p.setBirthdate(rs.getString("birth_date"));
				p.setStreet(rs.getString("street"));
				p.setCity(rs.getString("city"));
				p.setState(rs.getString("state"));
				p.setZipcode(rs.getString("zipcode"));
				p.setSsn(rs.getString("ssn"));
				p.setPrimaryName(rs.getString("primary_name"));
			}
		}
		System.out.println("\n* RETRIEVING patient info: " + p);

		return p;
	}

	// TODO: duplicate method, finish at the end
	// Helper method to get doctor info from the doctor_id
	public Doctor getDoctorById(Connection con, int id) throws SQLException {
		System.out.println("\n* Accessing `getDoctorById` method");
		Doctor d = new Doctor();

		String query = "SELECT * FROM doctor WHERE doctor_id = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				d.setId(rs.getInt("doctor_id"));
				d.setFirst_name(rs.getString("first_name"));
				d.setLast_name(rs.getString("last_name"));
				d.setSpecialty(rs.getString("specialty"));
				d.setPractice_since_year(rs.getString("practice_since"));
				d.setSsn(rs.getString("ssn"));
			}
		}
		System.out.println("\n* RETRIEVING doctor info: " + d);
		return d;
	}

	// TODO: duplicate method, finish at the end
	// Helper method to obtain drug id from drug name
	private int getDrugIdByName(Connection con, String drug_name) throws SQLException {
		System.out.println("\n* Accessing `getDrugIdByName` method");
		int drugId = -1;

		String query = "SELECT drug_id FROM drug WHERE drug_name = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, drug_name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				drugId = rs.getInt("drug_id");
			}
		}
		System.out.println("\n* RETRIEVING drug id: " + drugId);
		return drugId;
	}

	//	Helper method to round cost
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	// Helper method to get drug costs from the drug id
	private double getCostByDrugId(Connection con, int drug_id) throws SQLException{
		System.out.println("\n* Accessing `getCostByDrugId` method");
		double cost = 0.00;
		String query = "SELECT cost FROM inventory WHERE drug_id = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, drug_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				cost = rs.getInt("cost");
			}
		}
		cost = round(cost, 2);
		System.out.println("\n* RETRIEVING drug cost: " + cost);
		return cost;
	}

	// Helper method to get pharmacy phone by id
	private String getPharmacyPhoneById(Connection con, int id) throws SQLException {
		System.out.println("\n* Accessing `getPharmacyPhoneById` method");
		String phone = "";

		String query = "SELECT pharmacy_phone FROM pharmacy WHERE pharmacy_id = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				phone = rs.getString("pharmacy_phone");
			}
		}
		return phone;
	}

	public static String getCurrentDate() {
		System.out.println("\n* Accessing `getCurrentDate` method");
		LocalDate currentDate = LocalDate.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedDate = currentDate.format(formatter);
		System.out.println("\n* RETRIEVING current date: " + formattedDate);
		return formattedDate;
	}

	/*
	 * Pharmacy fills prescription.
	 */
	@PostMapping("/prescription/fill")
	public String processFillForm(Prescription p, Model model) {
		System.out.print("\n* Accessing `processFillForm` method");

		System.out.println("\n* processFillForm " + p);

		// Obtain connection to database.
		try (Connection con = getConnection();) {
			System.out.println("\n* OBTAINED CONNECTION TO DATABASE FOR PRESCRIPTION/FILL");

			// Validate pharmacy name and address in the prescription object and obtain the pharmacy id.
			int pharmacyId = getPharmacyIdByNameAndAddress(con, p.getPharmacyName(), p.getPharmacyAddress());

			if (pharmacyId > 0) {
				System.out.println("\n* pharmacyId validated [x]");

				// Get prescription info from rxid and patient last name and copy prescription info.
				Prescription pr = getPrescriptionInfoByRxidAndLastName(con, p.getRxid(), p.getPatientLastName());

				// set the pharmacy id and pharmacy phone on the prescription object
				pr.setPharmacyID(pharmacyId);
				pr.setPharmacyPhone(getPharmacyPhoneById(con, pharmacyId));

				// Get cost of drug and copy into prescription for display.
				double drugCostDouble = getCostByDrugId(con, getDrugIdByName(con, pr.getDrugName()));
				DecimalFormat df = new DecimalFormat("#.##");
				String drugCost = String.valueOf(Double.valueOf(df.format(drugCostDouble)));
				pr.setCost(String.valueOf(drugCost));

				// Insert values into refill table to complete the prescription
				String query = "insert into refill (RXID, pharmacy_id, patient_last_name, quantity, refills_remaining, date_filled) values (?, ?, ?, ?, ? , ?)";
				PreparedStatement ps = con.prepareStatement(query,
						Statement.RETURN_GENERATED_KEYS);

				ps.setString(1, pr.getRxid());
				ps.setInt(2, pharmacyId);
				ps.setString(3, p.getPatientLastName());
				ps.setInt(4, pr.getQuantity());
				ps.setInt(5, pr.getRefills() - 1); // subtract 1 refill at this point
				String currDate = getCurrentDate();
				pr.setDateFilled(currDate);
				ps.setString(6, currDate);

				ps.executeUpdate();
				ResultSet rs = ps.getGeneratedKeys();

				if (rs.next()) {
					p.setDrugName(pr.getDrugName());
					p.setQuantity(pr.getQuantity());
					p.setPatient_id(pr.getPatient_id());
					p.setPatientFirstName(pr.getPatientFirstName());
					p.setPatientLastName(pr.getPatientLastName());
					p.setDoctor_id(pr.getDoctor_id());
					p.setDoctorFirstName(pr.getDoctorFirstName());
					p.setDoctorLastName(pr.getDoctorLastName());
					p.setDateCreated(pr.getDateCreated());
					p.setPharmacyID(pr.getPharmacyID());
					p.setPharmacyPhone(pr.getPharmacyPhone());
					p.setRefills(pr.getRefills());
					p.setDateFilled(pr.getDateFilled());
					double x = Double.parseDouble(pr.getCost()) * pr.getQuantity();
					p.setCost(String.valueOf(x));
				}
				// Successful insertions, display prescription
				System.out.println(p);
				model.addAttribute("message", "Prescription filled successfully.");
				model.addAttribute("prescription", p);
				return "prescription_show";
			}
			// Failure, display failure message
			model.addAttribute("message", "Prescription filling was unsuccessful.");
			model.addAttribute("prescription", p);
			return "prescription_fill";
		} catch (SQLException e) {
			// SQL error, display error message
			model.addAttribute("message", e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
}