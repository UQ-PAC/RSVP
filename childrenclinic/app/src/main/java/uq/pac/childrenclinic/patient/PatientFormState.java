package uq.pac.childrenclinic.patient;

import java.io.Serializable;
import java.util.List;

public class PatientFormState implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer patientId;

	private String firstName;

	private String lastName;

	private String address;

	private String city;

	private String birthDate;

	private String gender;

	private List<String> clinics;

	private List<Integer> adultIds;

	private Integer authorityId;

	private List<Integer> doctorIds;

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public List<String> getClinics() {
		return clinics;
	}

	public void setClinics(List<String> clinics) {
		this.clinics = clinics;
	}

	public List<Integer> getAdultIds() {
		return adultIds;
	}

	public void setAdultIds(List<Integer> adultIds) {
		this.adultIds = adultIds;
	}

	public Integer getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(Integer authorityId) {
		this.authorityId = authorityId;
	}

	public List<Integer> getDoctorIds() {
		return doctorIds;
	}

	public void setDoctorIds(List<Integer> doctorIds) {
		this.doctorIds = doctorIds;
	}

}
