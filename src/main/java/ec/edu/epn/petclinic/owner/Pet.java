package ec.edu.epn.petclinic.owner;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import ec.edu.epn.petclinic.model.NamedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Simple business object representing a pet.
 */
@Entity
@Table(name = "pets")
public class Pet extends NamedEntity {

	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;

	@ManyToOne
	@JoinColumn(name = "type_id")
	private PetType type;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id")
	@OrderBy("date ASC")
	private final Set<Visit> visits = new LinkedHashSet<>();

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public PetType getType() {
		return this.type;
	}

	public void setType(PetType type) {
		this.type = type;
	}

	public Collection<Visit> getVisits() {
		return Collections.unmodifiableSet(this.visits);
	}

	public void addVisit(Visit visit) {
		getVisitsInternal().add(visit);
	}

	// internal accessor for mutations
	Set<Visit> getVisitsInternal() {
		return this.visits;
	}

}
