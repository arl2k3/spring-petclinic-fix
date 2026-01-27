
package ec.edu.epn.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Owner.
 * Valida reglas de negocio, validaciones Bean Validation y comportamiento de métodos.
 */
@DisplayName("Owner Entity Tests")
class OwnerTest {

    private Validator validator;
    private Owner owner;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Arrange: Owner válido por defecto
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid owner should pass validation")
        void validOwner_shouldPassValidation() {
            // Act
            Set<ConstraintViolation<Owner>> violations = validator.validate(owner);

            // Assert
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("Address cannot be blank")
        void addressBlank_shouldFailValidation(String address) {
            // Arrange
            owner.setAddress(address);

            // Act
            Set<ConstraintViolation<Owner>> violations = validator.validate(owner);

            // Assert
            assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .containsAnyOf("must not be blank", "no debe estar vacío");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t"})
        @DisplayName("City cannot be blank")
        void cityBlank_shouldFailValidation(String city) {
            // Arrange
            owner.setCity(city);

            // Act
            Set<ConstraintViolation<Owner>> violations = validator.validate(owner);

            // Assert
            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "12345678901", "abcdefghij", "123-456-7890", "(555) 555-5555"})
        @DisplayName("Telephone must be exactly 10 digits")
        void invalidTelephone_shouldFailValidation(String telephone) {
            // Arrange
            owner.setTelephone(telephone);

            // Act
            Set<ConstraintViolation<Owner>> violations = validator.validate(owner);

            // Assert
            assertThat(violations)
                .isNotEmpty()
                .extracting(v -> v.getPropertyPath().toString())
                .contains("telephone");
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567890", "0000000000", "9999999999"})
        @DisplayName("Valid 10-digit telephone should pass")
        void validTelephone_shouldPassValidation(String telephone) {
            // Arrange
            owner.setTelephone(telephone);

            // Act
            Set<ConstraintViolation<Owner>> violations = validator.validate(owner);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Pet Management Tests")
    class PetManagementTests {

        @Test
        @DisplayName("New owner should have empty pet list")
        void newOwner_shouldHaveEmptyPetList() {
            // Arrange
            Owner newOwner = new Owner();

            // Assert
            assertThat(newOwner.getPets()).isEmpty();
        }

        @Test
        @DisplayName("addPet should add new pet to owner")
        void addPet_shouldAddPetToOwner() {
            // Arrange
            Pet pet = new Pet();
            pet.setName("Fluffy");

            // Act
            owner.addPet(pet);

            // Assert
            assertThat(owner.getPets())
                .hasSize(1)
                .contains(pet);
        }

        @Test
        @DisplayName("addPet should not add pet with ID (not new)")
        void addPet_withId_shouldNotAdd() {
            // Arrange
            Pet existingPet = new Pet();
            existingPet.setName("Existing");
            existingPet.setId(1);

            // Act
            owner.addPet(existingPet);

            // Assert
            assertThat(owner.getPets()).isEmpty();
        }

        @Test
        @DisplayName("getPet by name should return correct pet")
        void getPetByName_shouldReturnCorrectPet() {
            // Arrange
            Pet fluffy = new Pet();
            fluffy.setName("Fluffy");
            fluffy.setId(1);
            
            Pet max = new Pet();
            max.setName("Max");
            max.setId(2);

            owner.addPet(fluffy);
            owner.getPets().add(max); // Bypass addPet for existing pet

            // Act
            Pet foundPet = owner.getPet("Max");

            // Assert
            assertThat(foundPet)
                .isNotNull()
                .isEqualTo(max);
        }

        @Test
        @DisplayName("getPet by name should be case-insensitive")
        void getPetByName_shouldBeCaseInsensitive() {
            // Arrange
            Pet pet = new Pet();
            pet.setName("Fluffy");
            pet.setId(1);
            owner.getPets().add(pet);

            // Act
            Pet found = owner.getPet("FLUFFY");

            // Assert
            assertThat(found).isEqualTo(pet);
        }

        @Test
        @DisplayName("getPet by name should return null if not found")
        void getPetByName_notFound_shouldReturnNull() {
            // Act
            Pet found = owner.getPet("NonExistent");

            // Assert
            assertThat(found).isNull();
        }

        @Test
        @DisplayName("getPet by ID should return correct pet")
        void getPetById_shouldReturnCorrectPet() {
            // Arrange
            Pet pet1 = new Pet();
            pet1.setName("Pet1");
            pet1.setId(10);

            Pet pet2 = new Pet();
            pet2.setName("Pet2");
            pet2.setId(20);

            owner.getPets().add(pet1);
            owner.getPets().add(pet2);

            // Act
            Pet found = owner.getPet(20);

            // Assert
            assertThat(found).isEqualTo(pet2);
        }

        @Test
        @DisplayName("getPet by ID should return null if not found")
        void getPetById_notFound_shouldReturnNull() {
            // Act
            Pet found = owner.getPet(999);

            // Assert
            assertThat(found).isNull();
        }

        @Test
        @DisplayName("getPet with ignoreNew flag should exclude new pets")
        void getPetByName_ignoreNew_shouldExcludeNewPets() {
            // Arrange
            Pet newPet = new Pet();
            newPet.setName("NewPet");
            // No ID set, so it's new

            Pet existingPet = new Pet();
            existingPet.setName("ExistingPet");
            existingPet.setId(1);

            owner.getPets().add(newPet);
            owner.getPets().add(existingPet);

            // Act
            Pet foundNew = owner.getPet("NewPet", true);
            Pet foundExisting = owner.getPet("ExistingPet", true);

            // Assert
            assertThat(foundNew).isNull();
            assertThat(foundExisting).isNotNull();
        }
    }

    @Nested
    @DisplayName("Visit Management Tests")
    class VisitManagementTests {

        @Test
        @DisplayName("addVisit should add visit to correct pet")
        void addVisit_shouldAddVisitToPet() {
            // Arrange
            Pet pet = new Pet();
            pet.setName("Fluffy");
            pet.setId(1);
            owner.getPets().add(pet);

            Visit visit = new Visit();
            visit.setDescription("Annual checkup");

            // Act
            owner.addVisit(1, visit);

            // Assert
            assertThat(pet.getVisits())
                .hasSize(1)
                .contains(visit);
        }

        @Test
        @DisplayName("addVisit with null petId should throw exception")
        void addVisit_nullPetId_shouldThrowException() {
            // Arrange
            Visit visit = new Visit();

            // Act & Assert
            assertThatThrownBy(() -> owner.addVisit(null, visit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pet identifier must not be null");
        }

        @Test
        @DisplayName("addVisit with null visit should throw exception")
        void addVisit_nullVisit_shouldThrowException() {
            // Act & Assert
            assertThatThrownBy(() -> owner.addVisit(1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Visit must not be null");
        }

        @Test
        @DisplayName("addVisit with invalid petId should throw exception")
        void addVisit_invalidPetId_shouldThrowException() {
            // Arrange
            Visit visit = new Visit();

            // Act & Assert
            assertThatThrownBy(() -> owner.addVisit(999, visit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Pet identifier");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain all owner properties")
        void toString_shouldContainAllProperties() {
            // Arrange
            owner.setId(123);

            // Act
            String result = owner.toString();

            // Assert
            assertThat(result)
                .contains("id = 123")
                .contains("firstName = 'John'")
                .contains("lastName = 'Doe'")
                .contains("address = '123 Main St'")
                .contains("city = 'Springfield'")
                .contains("telephone = '1234567890'");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Multiple pets with same name should return first match")
        void multiplePets_sameName_shouldReturnFirstMatch() {
            // Arrange
            Pet pet1 = new Pet();
            pet1.setName("Fluffy");
            pet1.setId(1);

            Pet pet2 = new Pet();
            pet2.setName("Fluffy");
            pet2.setId(2);

            owner.getPets().add(pet1);
            owner.getPets().add(pet2);

            // Act
            Pet found = owner.getPet("Fluffy");

            // Assert
            assertThat(found).isEqualTo(pet1);
        }

        @Test
        @DisplayName("Empty pet name should match null search")
        void emptyPetName_shouldNotMatchNullSearch() {
            // Arrange
            Pet pet = new Pet();
            pet.setName("");
            pet.setId(1);
            owner.getPets().add(pet);

            // Act
            Pet found = owner.getPet(null, false);

            // Assert
            assertThat(found).isNull();
        }
    }
}
