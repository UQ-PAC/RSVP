package uq.pac.childrenclinic.patient;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PatientAdultId implements Serializable {

    @Column(name = "patient_id")
    private Integer patientId;

    @Column(name = "adult_id")
    private Integer adultId;

    public PatientAdultId() {
    }

    public PatientAdultId(Integer patientId, Integer adultId) {
        this.patientId = patientId;
        this.adultId = adultId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getAdultId() {
        return adultId;
    }

    public void setAdultId(Integer adultId) {
        this.adultId = adultId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientAdultId that = (PatientAdultId) o;
        return patientId != null && adultId != null
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(adultId, that.adultId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
