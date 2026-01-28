package ec.edu.epn.petclinic.vet;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class VetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VetRepository vetRepository;

    private VetController vetController;

    @BeforeEach
    void setup() {
        vetController = new VetController(vetRepository);
        ReflectionTestUtils.setField(vetController, "pageSize", 2);
        mockMvc = MockMvcBuilders.standaloneSetup(vetController).build();
    }

    @Test
    @DisplayName("Should_renderPaginatedVets_When_RequestingHtmlList")
    void should_renderPaginatedVets_When_RequestingHtmlList() throws Exception {
        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("Ana");
        vet1.setLastName("Perez");
        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Luis");
        vet2.setLastName("Gomez");

        PageImpl<Vet> page = new PageImpl<>(List.of(vet1, vet2), PageRequest.of(0, 2), 2);
        when(vetRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/vets.html").param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("vets/vetList"))
            .andExpect(model().attributeExists("currentPage", "totalPages", "totalItems", "listVets"))
            .andExpect(model().attribute("listVets", page.getContent()));
    }

    @Test
    @DisplayName("Should_returnJsonWithVets_When_RequestingRestEndpoint")
    void should_returnJsonWithVets_When_RequestingRestEndpoint() throws Exception {
        Vet vet = new Vet();
        vet.setId(5);
        vet.setFirstName("Carla");
        vet.setLastName("Lopez");

        when(vetRepository.findAll()).thenReturn(List.of(vet));

        mockMvc.perform(get("/vets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vetList", hasSize(1)))
            .andExpect(jsonPath("$.vetList[0].id", is(5)))
            .andExpect(jsonPath("$.vetList[0].firstName", is("Carla")))
            .andExpect(jsonPath("$.vetList[0].lastName", is("Lopez")));
    }
}
