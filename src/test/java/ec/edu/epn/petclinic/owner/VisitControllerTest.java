package ec.edu.epn.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class VisitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerRepository ownerRepository;

    private VisitController visitController;

    private Owner owner;

    @BeforeEach
    void setup() {
        visitController = new VisitController(ownerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(visitController).build();

        owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Owner");
        owner.setLastName("Test");
        owner.setAddress("Street");
        owner.setCity("City");
        owner.setTelephone("1234567890");

        Pet pet = new Pet();
        pet.setId(10);
        pet.setName("Rex");
        pet.setBirthDate(LocalDate.now().minusYears(1));
        owner.getPetsInternal().add(pet);

        when(ownerRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should_renderVisitForm_When_InitNewVisit")
    void should_renderVisitForm_When_InitNewVisit() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", owner.getId(), 10))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
    }

    @Test
    @DisplayName("Should_redirectAfterVisitCreation_When_DataValid")
    void should_redirectAfterVisitCreation_When_DataValid() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", owner.getId(), 10)
                .param("description", "Checkup"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("Should_returnFormWithErrors_When_VisitHasValidationErrors")
    void should_returnFormWithErrors_When_VisitHasValidationErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", owner.getId(), 10)
                .param("description", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
    }
}
