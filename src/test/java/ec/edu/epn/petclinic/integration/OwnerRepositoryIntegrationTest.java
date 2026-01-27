
package ec.edu.epn.petclinic.integration;

import ec.edu.epn.petclinic.owner.Owner;
import ec.edu.epn.petclinic.owner.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de integración para OwnerRepository.
 * Usa base de datos H2 en memoria.
 */
@DataJpaTest
@DisplayName("OwnerRepository Integration Tests")
class OwnerRepositoryIntegrationTest {

    @Autowired
    private OwnerRepository ownerRepository;

    private Owner owner1;
    private Owner owner2;
    private Owner owner3;

    @BeforeEach
    void setUp() {
        // Clean database
        ownerRepository.deleteAll();

        // Create test owners
        owner1 = createOwner("John", "Smith", "123 Main St", "Springfield", "1234567890");
        owner2 = createOwner("Jane", "Smith", "456 Oak Ave", "Shelbyville", "2345678901");
        owner3 = createOwner("Bob", "Johnson", "789 Pine Rd", "Capital City", "3456789012");

        // Save owners
        owner1 = ownerRepository.save(owner1);
        owner2 = ownerRepository.save(owner2);
        owner3 = ownerRepository.save(owner3);
    }

    @Test
    @DisplayName("Should save and retrieve owner by ID")
    void saveAndFindById_shouldWork() {
        // Act
        Optional<Owner> found = ownerRepository.findById(owner1.getId());

        // Assert
        assertThat(found)
            .isPresent()
            .get()
            .satisfies(owner -> {
                assertThat(owner.getFirstName()).isEqualTo("John");
                assertThat(owner.getLastName()).isEqualTo("Smith");
                assertThat(owner.getAddress()).isEqualTo("123 Main St");
                assertThat(owner.getCity()).isEqualTo("Springfield");
                assertThat(owner.getTelephone()).isEqualTo("1234567890");
            });
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    void findById_nonExistent_shouldReturnEmpty() {
        // Act
        Optional<Owner> found = ownerRepository.findById(99999);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find owners by last name starting with")
    void findByLastNameStartingWith_shouldReturnMatches() {
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Owner> result = ownerRepository.findByLastNameStartingWith("Smith", pageable);

        // Assert
        assertThat(result.getContent())
            .hasSize(2)
            .extracting(Owner::getLastName)
            .containsOnly("Smith");
    }

    @Test
    @DisplayName("Should find owners with case-sensitive prefix")
    void findByLastNameStartingWith_caseSensitive_shouldMatch() {
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Owner> result = ownerRepository.findByLastNameStartingWith("Joh", pageable);

        // Assert
        assertThat(result.getContent())
            .hasSize(1)
            .first()
            .satisfies(owner -> {
                assertThat(owner.getLastName()).isEqualTo("Johnson");
                assertThat(owner.getFirstName()).isEqualTo("Bob");
            });
    }

    @Test
    @DisplayName("Should return empty page when no matches found")
    void findByLastNameStartingWith_noMatches_shouldReturnEmpty() {
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Owner> result = ownerRepository.findByLastNameStartingWith("Nonexistent", pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should return all owners when searching with empty string")
    void findByLastNameStartingWith_emptyString_shouldReturnAll() {
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<Owner> result = ownerRepository.findByLastNameStartingWith("", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should paginate results correctly")
    void findByLastNameStartingWith_pagination_shouldWork() {
        // Arrange - add more Smiths
        ownerRepository.save(createOwner("Alice", "Smith", "111 Elm St", "Springfield", "4567890123"));
        ownerRepository.save(createOwner("Charlie", "Smith", "222 Maple St", "Springfield", "5678901234"));

        // Act - first page
        Pageable page1 = PageRequest.of(0, 2);
        Page<Owner> result1 = ownerRepository.findByLastNameStartingWith("Smith", page1);

        // Act - second page
        Pageable page2 = PageRequest.of(1, 2);
        Page<Owner> result2 = ownerRepository.findByLastNameStartingWith("Smith", page2);

        // Assert
        assertThat(result1.getContent()).hasSize(2);
        assertThat(result1.getTotalPages()).isEqualTo(2);
        assertThat(result1.getTotalElements()).isEqualTo(4);

        assertThat(result2.getContent()).hasSize(2);
        assertThat(result2.isLast()).isTrue();
    }

    @Test
    @DisplayName("Should update owner successfully")
    void updateOwner_shouldPersist() {
        // Arrange
        owner1.setAddress("New Address 999");
        owner1.setTelephone("9999999999");

        // Act
        Owner updated = ownerRepository.save(owner1);
        ownerRepository.flush();
        
        Optional<Owner> found = ownerRepository.findById(owner1.getId());

        // Assert
        assertThat(updated.getAddress()).isEqualTo("New Address 999");
        assertThat(found)
            .isPresent()
            .get()
            .satisfies(owner -> {
                assertThat(owner.getAddress()).isEqualTo("New Address 999");
                assertThat(owner.getTelephone()).isEqualTo("9999999999");
            });
    }

    @Test
    @DisplayName("Should delete owner successfully")
    void deleteOwner_shouldRemoveFromDatabase() {
        // Arrange
        Integer idToDelete = owner1.getId();

        // Act
        ownerRepository.deleteById(idToDelete);
        Optional<Owner> found = ownerRepository.findById(idToDelete);

        // Assert
        assertThat(found).isEmpty();
        assertThat(ownerRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count all owners")
    void count_shouldReturnCorrectNumber() {
        // Act
        long count = ownerRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find all owners")
    void findAll_shouldReturnAllOwners() {
        // Act
        var owners = ownerRepository.findAll();

        // Assert
        assertThat(owners)
            .hasSize(3)
            .extracting(Owner::getLastName)
            .containsExactlyInAnyOrder("Smith", "Smith", "Johnson");
    }

    // Helper method
    private Owner createOwner(String firstName, String lastName, String address, String city, String telephone) {
        Owner owner = new Owner();
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress(address);
        owner.setCity(city);
        owner.setTelephone(telephone);
        return owner;
    }
}
