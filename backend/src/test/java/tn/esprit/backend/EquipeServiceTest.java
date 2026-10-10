package tn.esprit.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.entity.Equipe;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.repository.EntrepriseRepository;
import tn.esprit.backend.repository.EquipeRepository;
import tn.esprit.backend.repository.ProjetRepository;
import tn.esprit.backend.service.impl.EquipeServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private EntrepriseRepository entrepriseRepository;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private EquipeServiceImpl equipeService;

    @Test
    void testAddEquipe() {
        Equipe equipe = Equipe.builder().nom("DevOps").specialite("CI/CD").build();
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe);

        Equipe saved = equipeService.addEquipe(equipe);

        assertThat(saved).isNotNull();
        assertThat(saved.getNom()).isEqualTo("DevOps");
        verify(equipeRepository, times(1)).save(any(Equipe.class));
    }

    @Test
    void testUpdateEquipe() {
        Equipe equipe = Equipe.builder().id(1L).nom("Updated").build();
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe);

        Equipe updated = equipeService.updateEquipe(equipe);

        assertThat(updated.getNom()).isEqualTo("Updated");
        verify(equipeRepository, times(1)).save(equipe);
    }

    @Test
    void testDeleteEquipe() {
        doNothing().when(equipeRepository).deleteById(1L);

        equipeService.deleteEquipe(1L);

        verify(equipeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetEquipeById() {
        Equipe equipe = Equipe.builder().id(1L).nom("Team").build();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));

        Equipe found = equipeService.getEquipeById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getNom()).isEqualTo("Team");
    }

    @Test
    void testGetAllEquipes() {
        Equipe e1 = Equipe.builder().nom("Team A").build();
        Equipe e2 = Equipe.builder().nom("Team B").build();
        when(equipeRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<Equipe> result = equipeService.getAllEquipes();

        assertThat(result).hasSize(2);
    }

    @Test
    void testGetEquipesByEntreprise() {
        Equipe e1 = Equipe.builder().nom("Team A").build();
        when(equipeRepository.findByEntrepriseId(1L)).thenReturn(List.of(e1));

        List<Equipe> result = equipeService.getEquipesByEntreprise(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testAssignEquipeToEntreprise() {
        Equipe equipe = Equipe.builder().id(1L).nom("Team").build();
        Entreprise entreprise = Entreprise.builder().id(10L).nom("ESPRIT").build();

        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(entrepriseRepository.findById(10L)).thenReturn(Optional.of(entreprise));
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe);

        Equipe result = equipeService.assignEquipeToEntreprise(1L, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getEntreprise()).isEqualTo(entreprise);
    }

    @Test
    void testAssignEquipeToProjet() {
        Equipe equipe = Equipe.builder().id(1L).nom("Team").projets(new ArrayList<>()).build();
        Projet projet = Projet.builder().id(100L).sujet("DevOps Project").build();

        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(projetRepository.findById(100L)).thenReturn(Optional.of(projet));
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipe);

        Equipe result = equipeService.assignEquipeToProjet(1L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getProjets()).contains(projet);
    }
}
