package ec.edu.epn.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VetsTest {

    @Test
    @DisplayName("Should_initializeVetList_When_FirstAccess")
    void should_initializeVetList_When_FirstAccess() {
        Vets vets = new Vets();

        assertThat(vets.getVetList()).isNotNull();
        assertThat(vets.getVetList()).isEmpty();
    }

    @Test
    @DisplayName("Should_reuseSameListInstance_When_CalledMultipleTimes")
    void should_reuseSameListInstance_When_CalledMultipleTimes() {
        Vets vets = new Vets();

        vets.getVetList().add(new Vet());

        assertThat(vets.getVetList()).hasSize(1);
    }
}
