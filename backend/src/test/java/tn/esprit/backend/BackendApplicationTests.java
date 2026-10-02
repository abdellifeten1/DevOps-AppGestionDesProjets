package tn.esprit.backend;

import org.junit.jupiter.api.Test;
import tn.esprit.backend.entity.Equipe;

import static org.junit.jupiter.api.Assertions.*;

class BackendApplicationTests {

    @Test
    void testCreationEquipe() {
        Equipe equipe = new Equipe();
        equipe.setNom("DevOps");
        equipe.setSpecialite("CI/CD");
        assertNotNull(equipe);
        assertEquals("DevOps", equipe.getNom());
        assertEquals("CI/CD", equipe.getSpecialite());
    }

    @Test
    void testSettersGettersEquipe() {
        Equipe equipe = new Equipe();
        equipe.setId(1L);
        equipe.setNom("Backend");
        equipe.setSpecialite("Spring");
        assertEquals(1L, equipe.getId());
        assertEquals("Backend", equipe.getNom());
        assertEquals("Spring", equipe.getSpecialite());
    }

    @Test
    void testBuilderEquipe() {
        Equipe equipe = Equipe.builder()
                .nom("Frontend")
                .specialite("Angular")
                .build();
        assertNotNull(equipe);
        assertEquals("Frontend", equipe.getNom());
        assertEquals("Angular", equipe.getSpecialite());
    }
}
