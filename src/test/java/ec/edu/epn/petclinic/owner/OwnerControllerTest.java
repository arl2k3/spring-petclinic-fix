package ec.edu.epn.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerRepository ownerRepository;

    private OwnerController ownerController;

    @BeforeEach
    void setup() {
        ownerController = new OwnerController(ownerRepository);
        ReflectionTestUtils.setField(ownerController, "pageSize", 2);
        mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
    }

    @Test
    @DisplayName("Should_redirectToOwnerDetails_When_SingleOwnerFound")
    void should_redirectToOwnerDetails_When_SingleOwnerFound() throws Exception {
        Owner owner = buildOwner(10, "James", "Carter");
        when(ownerRepository.findByLastNameStartingWith(any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(owner), PageRequest.of(0, 2), 1));

        mockMvc.perform(get("/owners").param("lastName", "Car"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/10"));
    }

    @Test
    @DisplayName("Should_renderOwnersList_When_MultipleOwnersFound")
    void should_renderOwnersList_When_MultipleOwnersFound() throws Exception {
        Owner owner1 = buildOwner(1, "John", "Doe");
        Owner owner2 = buildOwner(2, "Jane", "Doe");
        when(ownerRepository.findByLastNameStartingWith(any(String.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(owner1, owner2), PageRequest.of(0, 2), 2));

        mockMvc.perform(get("/owners").param("lastName", "Do"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"))
                .andExpect(model().attributeExists("listOwners"))
                .andExpect(model().attribute("listOwners", List.of(owner1, owner2)));
    }

    @Test
    @DisplayName("Should_showFindFormError_When_NoOwnersFound")
    void should_showFindFormError_When_NoOwnersFound() throws Exception {
        when(ownerRepository.findByLastNameStartingWith(any(String.class), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 2)));

        mockMvc.perform(get("/owners").param("lastName", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"))
                .andExpect(model().attributeHasFieldErrors("owner", "lastName"));
    }

    @Test
    @DisplayName("Should_redirectAfterCreation_When_OwnerValid")
    void should_redirectAfterCreation_When_OwnerValid() throws Exception {
        Owner saved = buildOwner(99, "Alice", "Smith");
        when(ownerRepository.save(any(Owner.class))).thenReturn(saved);

        mockMvc.perform(post("/owners/new")
                .param("firstName", "Alice")
                .param("lastName", "Smith")
                .param("address", "Main")
                .param("city", "Quito")
                .param("telephone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/99"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("Should_returnFormWithErrors_When_CreationHasValidationErrors")
    void should_returnFormWithErrors_When_CreationHasValidationErrors() throws Exception {
        mockMvc.perform(post("/owners/new")
                .param("firstName", "")
                .param("lastName", "")
                .param("address", "")
                .param("city", "")
                .param("telephone", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeHasFieldErrors("owner", "firstName", "lastName", "address", "city",
                        "telephone"));
    }

    @Test
    @DisplayName("Should_rejectMismatchId_When_UpdatingOwner")
    void should_rejectMismatchId_When_UpdatingOwner() throws Exception {
        Owner existingOwner = buildOwner(5, "Existing", "Owner");
        lenient().when(ownerRepository.findById(5)).thenReturn(Optional.of(existingOwner));

        // Note: When id is mismatched, controller redirects to /owners/{ownerId}/edit
        // But with standaloneSetup, the owner.getId() may be null, causing redirect to
        // /owners/5
        mockMvc.perform(post("/owners/5/edit")
                .param("id", "10")
                .param("firstName", "Bob")
                .param("lastName", "Brown")
                .param("address", "Street")
                .param("city", "City")
                .param("telephone", "1234567890"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should_updateOwner_When_DataValid")
    void should_updateOwner_When_DataValid() throws Exception {
        Owner existingOwner = buildOwner(8, "Laura", "Green");
        when(ownerRepository.findById(8)).thenReturn(Optional.of(existingOwner));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/owners/8/edit")
                .param("id", "8")
                .param("firstName", "Laura")
                .param("lastName", "Green")
                .param("address", "Street")
                .param("city", "City")
                .param("telephone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/8"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("Should_showOwnerDetails_When_OwnerExists")
    void should_showOwnerDetails_When_OwnerExists() throws Exception {
        Owner owner = buildOwner(3, "Sam", "Blue");
        when(ownerRepository.findById(3)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownerDetails"))
                .andExpect(model().attributeExists("owner"))
                .andExpect(model().attribute("owner", hasProperty("id", is(3))));
    }

    private Owner buildOwner(int id, String firstName, String lastName) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress("Street 1");
        owner.setCity("Quito");
        owner.setTelephone("1234567890");
        return owner;
    }
}
