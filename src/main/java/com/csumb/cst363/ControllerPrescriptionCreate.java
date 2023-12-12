package com.csumb.cst363;

import java.sql.*;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@SuppressWarnings("unused")
@Controller    
public class ControllerPrescriptionCreate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Doctor requests blank form for new prescription.
	 * Do not modify this method.
	 */
	@GetMapping("/prescription/new")
	public String getPrescriptionForm(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_create";
	}

	// Helper method to get doctor info from the doctor_id
	public Doctor getDoctorById(Connection con, int id) throws SQLException {
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
		System.out.println("DOCTOR INFO: " + d);

		return d;
	}

	// Helper method to get patient info from the patient_id
	public Patient getPatientById(Connection con, int id) throws SQLException {
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
		System.out.println("PATIENT INFO: " + p);

		return p;
	}

	// Helper method to obtain drug id from drug name
	public int getDrugIdByName(Connection con, String drug_name) throws SQLException {
		int drugId = -1;

		String query = "SELECT drug_id FROM drug WHERE drug_name = ?";
		try (PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, drug_name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				drugId = rs.getInt("drug_id");
			}
		}
		System.out.println("DRUG ID: " + drugId);

		return drugId;
	}

	/*
	 * Doctor creates a prescription.
	 */
	@PostMapping("/prescription")
	public String createPrescription(Prescription p, Model model) {

		System.out.println("createPrescription " + p);

		/*-
		 * Process the new prescription form. 
		 * 1. Obtain connection to database. 
		 * 2. Validate that doctor id and name exists 
		 * 3. Validate that patient id and name exists 
		 * 4. Validate that Drug name exists and obtain drug id. 
		 * 5. Insert new prescription 
		 * 6. Get generated value for rxid 
		 * 7. Update prescription object and return
		 */

		// Obtain connection to database
		try (Connection con = getConnection();) {
			Doctor d = getDoctorById(con, p.getDoctor_id());
			System.out.println("d: " + d);

			Patient pt = getPatientById(con, p.getPatient_id());
			System.out.println("pt: " + pt);

			int drugId = getDrugIdByName(con, p.getDrugName());

			// Validate that doctor id exists
			if (d.getId() > 0) {
				System.out.println("Validated doctor id exists [X]");
				// Validate that doctor's first name exists
				if (d.getFirst_name().equals(p.getDoctorFirstName())) {
					System.out.println("Validated doctor first name exists [X]");
					// Validate that doctor's last name exists
					if (d.getLast_name().equals(p.getDoctorLastName())) {
						System.out.println("Validated doctor last name exists [X]");
						// Validate that patient id exists
						if (pt.getId() > 0) {
							System.out.println("Validated patient id exists [X]");
							// Validate that patient's first name exists
							if (pt.getFirst_name().equals(p.getPatientFirstName())) {
								System.out.println("Validated patient first name exists [X]");
								// Validate that patient's last name exists
								if (pt.getLast_name().equals(p.getPatientLastName())) {
									System.out.println("Validated patient last name exists [X]");
									// Validate that drug name exists
									if (drugId > 0) {
										System.out.println("Validated drug id & drug name exists [X]");
										PreparedStatement ps = con.prepareStatement("insert into prescription(RXID, doctor_id, patient_id, drug_id, quantity, num_of_refills) values (?, ?, ?, ?, ?, ?)",
												Statement.RETURN_GENERATED_KEYS);
										ps.setString(1, p.getRxid());
										ps.setInt(2, p.getDoctor_id());
										ps.setInt(3, p.getPatient_id());
										ps.setInt(4, getDrugIdByName(con, p.getDrugName()));
										ps.setInt(5, p.getQuantity());
										ps.setInt(6, p.getRefills());

										ps.executeUpdate();
										ResultSet rs = ps.getGeneratedKeys();
										if (rs.next()) p.setRxid(rs.getString(1)); // MIGHT CAUSE ISSUES LATER
										// display message and patient info
										model.addAttribute("message", "Prescription created.");
										model.addAttribute("prescription", p);
										return "prescription_show";
									}
								}
							}
						}
					}
				}
			}
			model.addAttribute("message", "Prescription creation unsuccessful.");
			model.addAttribute("prescription", p);
			return "prescription_create";
		} catch (SQLException e) {
			model.addAttribute("message",  "SQL error. " + e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_create";
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