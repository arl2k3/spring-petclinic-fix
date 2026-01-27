
package ec.edu.epn.petclinic.integration;

import ec.edu.epn.petclinic.owner.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración para OwnerController usando MockMvc.
 * Valida endpoints, validaciones y respuestas HTTP.
 */
@WebMvcTest(OwnerController.class)
@DisplayName("OwnerController Integration Tests")
class OwnerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    @DisplayName("GET /owners/new should display owner creation form")
    void initCreationForm_shouldDisplayForm() throws Exception {
        mockMvc.perform(get("/owners/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/createOrUpdateOwnerForm"))
            .andExpect(model().attributeExists("owner"));
    }

    @Test
    @DisplayName("POST /owners/new with valid data should create owner and redirect")
    void processCreationForm_validData_shouldCreateOwnerAndRedirect() throws Exception {
        // Arrange
        Owner savedOwner = createTestOwner(1, "John", "Doe", "123 Main St", "Springfield", "1234567890");
        given(ownerRepository.save(any(Owner.class))).willReturn(savedOwner);

        // Act & Assert
        mockMvc.perform(post("/owners/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("address", "123 Main St")
                .param("city", "Springfield")
                .param("telephone", "1234567890"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/1"))
            .andExpect(flash().attributeExists("message"));

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("POST /owners/new with invalid telephone should show validation errors")
    void processCreationForm_invalidTelephone_shouldShowErrors() throws Exception {
        mockMvc.perform(post("/owners/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("address", "123 Main St")
                .param("city", "Springfield")
                .param("telephone", "123"))  // Invalid: not 10 digits
            .andExpect(status().isOk())
            .andExpect(view().name("owners/createOrUpdateOwnerForm"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("POST /owners/new with missing required fields should show validation errors")
    void processCreationForm_missingFields_shouldShowErrors() throws Exception {
        mockMvc.perform(post("/owners/new")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", "John"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    @DisplayName("GET /owners/find should display find owners form")
    void initFindForm_shouldDisplayForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    @DisplayName("GET /owners should return all owners when no lastName provided")
    void processFindForm_noLastName_shouldReturnAll() throws Exception {
        // Arrange
        Owner owner1 = createTestOwner(1, "John", "Doe", "123 St", "City", "1234567890");
        Owner owner2 = createTestOwner(2, "Jane", "Smith", "456 Ave", "Town", "2345678901");
        Page<Owner> ownersPage = new PageImpl<>(Arrays.asList(owner1, owner2));
        
        given(ownerRepository.findByLastNameStartingWith(eq(""), any(PageRequest.class)))
            .willReturn(ownersPage);

        // Act & Assert
        mockMvc.perform(get("/owners")
                .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownersList"))
            .andExpect(model().attributeExists("listOwners"))
            .andExpect(model().attribute("listOwners", hasSize(2)));
    }

    @Test
    @DisplayName("GET /owners with lastName should return matching owners")
    void processFindForm_withLastName_shouldReturnMatches() throws Exception {
        // Arrange
        Owner owner = createTestOwner(1, "John", "Smith", "123 St", "City", "1234567890");
        Page<Owner> ownersPage = new PageImpl<>(Collections.singletonList(owner));
        
        given(ownerRepository.findByLastNameStartingWith(eq("Smith"), any(PageRequest.class)))
            .willReturn(ownersPage);

        // Act & Assert
        mockMvc.perform(get("/owners")
                .param("lastName", "Smith")
                .param("page", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/1"));
    }

    @Test
    @DisplayName("GET /owners with no matches should show error")
    void processFindForm_noMatches_shouldShowError() throws Exception {
        // Arrange
        Page<Owner> emptyPage = new PageImpl<>(Collections.emptyList());
        given(ownerRepository.findByLastNameStartingWith(anyString(), any(PageRequest.class)))
            .willReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/owners")
                .param("lastName", "NonExistent")
                .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    @DisplayName("GET /owners/{ownerId} should display owner details")
    void showOwner_existingId_shouldDisplayDetails() throws Exception {
        // Arrange
        Owner owner = createTestOwner(1, "John", "Doe", "123 St", "City", "1234567890");
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        // Act & Assert
        mockMvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/ownerDetails"))
            .andExpect(model().attributeExists("owner"))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("John"))));
    }

    @Test
    @DisplayName("GET /owners/{ownerId} with invalid ID should throw exception")
    void showOwner_invalidId_shouldThrowException() throws Exception {
        // Arrange
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/owners/999"))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /owners/{ownerId}/edit should display edit form")
    void initUpdateForm_shouldDisplayForm() throws Exception {
        // Arrange
        Owner owner = createTestOwner(1, "John", "Doe", "123 St", "City", "1234567890");
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        // Act & Assert
        mockMvc.perform(get("/owners/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/createOrUpdateOwnerForm"))
            .andExpect(model().attributeExists("owner"));
    }

    @Test
    @DisplayName("POST /owners/{ownerId}/edit with valid data should update owner")
    void processUpdateForm_validData_shouldUpdateOwner() throws Exception {
        // Arrange
        Owner existingOwner = createTestOwner(1, "John", "Doe", "123 St", "City", "1234567890");
        given(ownerRepository.findById(1)).willReturn(Optional.of(existingOwner));
        given(ownerRepository.save(any(Owner.class))).willReturn(existingOwner);

        // Act & Assert
        mockMvc.perform(post("/owners/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("address", "456 New Address")
                .param("city", "New City")
                .param("telephone", "9876543210"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/owners/1"))
            .andExpect(flash().attributeExists("message"));

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("POST /owners/{ownerId}/edit with ID mismatch should show error")
    void processUpdateForm_idMismatch_shouldShowError() throws Exception {
        // Arrange
        Owner owner = createTestOwner(2, "John", "Doe", "123 St", "City", "1234567890");
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        // Act & Assert
        mockMvc.perform(post("/owners/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "2")  // Mismatch with URL
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("address", "123 St")
                .param("city", "City")
                .param("telephone", "1234567890"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("POST /owners/{ownerId}/edit with invalid data should show errors")
    void processUpdateForm_invalidData_shouldShowErrors() throws Exception {
        // Arrange
        Owner owner = createTestOwner(1, "John", "Doe", "123 St", "City", "1234567890");
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        // Act & Assert
        mockMvc.perform(post("/owners/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("firstName", "John")
                .param("lastName", "")  // Invalid: blank
                .param("address", "123 St")
                .param("city", "City")
                .param("telephone", "1234567890"))
            .andExpect(status().isOk())
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    // Helper method
    private Owner createTestOwner(Integer id, String firstName, String lastName, 
                                  String address, String city, String telephone) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress(address);
        owner.setCity(city);
        owner.setTelephone(telephone);
        return owner;
    }
}
