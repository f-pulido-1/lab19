package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 */
@SuppressWarnings("unused")
@Controller
public class ControllerPatientCreate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Request blank patient registration form.
	 * Do not modify this method.
	 */
	@GetMapping("/patient/new")
	public String getNewPatientForm(Model model) {
		model.addAttribute("patient", new Patient());
		return "patient_register";
	}

	// Helper method to get doctor_id based on the last name
	private int getDoctorIdByLastName(Connection con, String doctorLastName) throws SQLException {
		int doctorId = -1;
		String query = "SELECT doctor_id FROM doctor WHERE last_name = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, doctorLastName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctorId = rs.getInt("doctor_id");
			}
		}
		System.out.println("DOCTOR ID: " + doctorId);
		return doctorId;
	}
	
	/*
	 * Process new patient registration
	 */
	@PostMapping("/patient/new")
	public String createPatient(Patient p, Model model) {
		System.out.println("createPatient " + p);  // debug
		// get a connection to the database
		// validate the doctor's last name and obtain the doctor id
		// insert the patient profile into the patient table
		// obtain the generated id for the patient and update patient object
		try (Connection con = getConnection();) {
			if (getDoctorIdByLastName(con, p.getPrimaryName()) > 0) {
				PreparedStatement ps = con.prepareStatement("insert into patient(first_name, last_name, birth_date, street, city, state, zipcode, ssn, primary_name) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, p.getFirst_name());
				ps.setString(2, p.getLast_name());
				ps.setString(3, p.getBirthdate());
				ps.setString(4, p.getStreet());
				ps.setString(5, p.getCity());
				ps.setString(6, p.getState());
				ps.setString(7, p.getZipcode());
				ps.setString(8, p.getSsn());
				ps.setString(9, p.getPrimaryName());
				ps.executeUpdate();
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) p.setId(rs.getInt(1));
				// display message and patient info
				model.addAttribute("message", "Registration successful.");
				model.addAttribute("patient", p);
				return "patient_show";
			}
			model.addAttribute("message", "Registration unsuccessful.");
			model.addAttribute("patient", p);
			return "patient_register";
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error. " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_register";
		}
	}
	
	/*
	 * Request blank form to search for patient by and and id
	 * Do not modify this method.
	 */
	@GetMapping("/patient/edit")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new Patient());
		return "patient_get";
	}
	
	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String showPatient(Patient p, Model model) {
		// get a connection to the database
		// using patient id and patient last name from patient object
		// retrieve patient profile and doctor's last name
		// update patient object with patient profile data
		try (Connection con = getConnection();) {
			String query = "select first_name, last_name, birth_date, street, city, state, zipcode, primary_name" +
							"from patient where patient_id=? and last_name=?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setInt(1, p.getId());
			ps.setString(2, p.getLast_name());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				p.setFirst_name(rs.getString(1));
				p.setLast_name(rs.getString(2));
				p.setBirthdate(rs.getString(3));
				p.setStreet(rs.getString(4));
				p.setCity(rs.getString(5));
				p.setState(rs.getString(6));
				p.setZipcode(rs.getString(7));
				p.setPrimaryName(rs.getString(8));
				model.addAttribute("patient", p);
				System.out.println("end getPatient " + p);  // debug
				return "patient_show";
			} else {
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("patient", p);
				return "patient_get";
			}
		} catch (SQLException e) {
			System.out.println("SQL error in getDoctor " + e.getMessage());
			model.addAttribute("message", "SQL Error. " + e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
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
