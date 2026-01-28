package ec.edu.epn.petclinic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PetClinicApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should_loadApplicationContext_When_StartingApplication")
    void should_loadApplicationContext_When_StartingApplication() {
        // If the context fails to start, the test will fail.
        assertThat(applicationContext).isNotNull();
    }
}
