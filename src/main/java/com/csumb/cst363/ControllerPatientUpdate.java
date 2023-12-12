package com.csumb.cst363;

import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
/*
 * Controller class for patient interactions.
 *   update patient profile.
 */
@SuppressWarnings("unused")
@Controller
public class ControllerPatientUpdate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	// Helper method to display patient info from id
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

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		System.out.println("\n* Accessing `getUpdateForm` method for ID: " + id );
		Patient p = new Patient();
		// Get a connection to the database
		try (Connection con = getConnection();) {
			// Using patient id and patient last name from patient object retrieve patient profile and doctor's last name
			if (id == getPatientById(con, id).getId()) {
				p.setId(id);
				p.setLast_name(getPatientById(con, id).getLast_name());
				p.setFirst_name(getPatientById(con, id).getFirst_name());
				p.setCity(getPatientById(con, id).getCity());
				p.setState(getPatientById(con, id).getState());
				p.setStreet(getPatientById(con, id).getStreet());
				p.setZipcode(getPatientById(con, id).getZipcode());
				p.setBirthdate(getPatientById(con, id).getBirthdate());
				p.setPrimaryName(getPatientById(con, id).getPrimaryName());
				model.addAttribute("patient", p);
				return "patient_edit";
			}
			// Failure, display failure message
			model.addAttribute("message", "Displaying patient profile failure.");
			model.addAttribute("patient", p);
			return "index";
		} catch (SQLException e) {
			// SQL Error, display error message
			model.addAttribute("message", e.getMessage());
			model.addAttribute("patient", p);
			return "index";
		}
	}

	// Helper method to get doctor info from last name
	private Doctor getDoctorByLastName(Connection con, String last_name) throws SQLException {
		System.out.println("\n* Accessing `getDoctorByLastName` method");

		Doctor d = new Doctor();

		String query = "SELECT * FROM doctor WHERE last_name = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, last_name);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				d.setId(rs.getInt("doctor_id"));
				d.setFirst_name(rs.getString("first_name"));
				d.setLast_name(rs.getString("last_name"));
				d.setSpecialty(rs.getString("specialty"));
				d.setSsn(rs.getString("ssn"));
				d.setPractice_since_year(rs.getString("practice_since"));
			}
		}
		return d;
	}
	
	/*
	 * Process changes to patient profile.  
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient p, Model model) {
		System.out.println("\n* Accessing `updatePatient` method: " + p);  // for debug
		// Obtain connection to database
		try (Connection con = getConnection();) {
			System.out.println("\n* Obtained connection");
			// Validate doctor's last name and obtain the doctor id
			int doctorId = getDoctorByLastName(con, p.getPrimaryName()).getId();
			if (doctorId > 0) {
				System.out.println("\n* DoctorId validated [X]");
				// Update the patient's profile for street, city, state, zip and doctor
				String query = "UPDATE patient SET street = ?, city = ?, state = ?, zipcode = ?, primary_name = ? WHERE patient_id = ?";
				PreparedStatement ps = con.prepareStatement(query,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, p.getStreet());
				ps.setString(2, p.getCity());
				ps.setString(3, p.getState());
				ps.setString(4, p.getZipcode());
				ps.setString(5, p.getPrimaryName());
				ps.setInt(6, p.getId());
				ps.executeUpdate();
				ResultSet rs = ps.getGeneratedKeys();
				model.addAttribute("message", "Update successful.");
				model.addAttribute("patient", p);
				return "patient_show";
			}
			// Failure, display failure message
			model.addAttribute("message",  "Patient update failure.");
			model.addAttribute("patient", p);
			return "patient_edit";
		} catch (SQLException e) {
			// SQL Error, display error message
			model.addAttribute("message",  e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
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