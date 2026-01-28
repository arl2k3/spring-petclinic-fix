package ec.edu.epn.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VetTest {

    @Test
    @DisplayName("Should_returnEmptyList_when_NoSpecialtiesAdded")
    void should_returnEmptyList_when_NoSpecialtiesAdded() {
        Vet vet = new Vet();

        List<Specialty> specialties = vet.getSpecialties();

        assertThat(specialties).isEmpty();
        assertThat(vet.getNrOfSpecialties()).isZero();
    }

    @Test
    @DisplayName("Should_sortSpecialtiesByName_When_Retrieving")
    void should_sortSpecialtiesByName_When_Retrieving() {
        Vet vet = new Vet();
        Specialty dentistry = new Specialty();
        dentistry.setName("Dentistry");
        Specialty surgery = new Specialty();
        surgery.setName("Surgery");
        Specialty anesthesia = new Specialty();
        anesthesia.setName("Anesthesia");

        vet.addSpecialty(surgery);
        vet.addSpecialty(dentistry);
        vet.addSpecialty(anesthesia);

        List<Specialty> result = vet.getSpecialties();

        assertThat(result)
            .extracting(Specialty::getName)
            .containsExactly("Anesthesia", "Dentistry", "Surgery");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should_incrementNrOfSpecialties_When_AddingNewSpecialty")
    void should_incrementNrOfSpecialties_When_AddingNewSpecialty() {
        Vet vet = new Vet();
        Specialty radiology = new Specialty();
        radiology.setName("Radiology");

        vet.addSpecialty(radiology);

        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
        assertThat(vet.getSpecialties()).containsExactly(radiology);
    }
}
