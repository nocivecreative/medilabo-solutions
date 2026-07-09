package com.medilabo.notes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test de bout en bout sur un vrai MongoDB ephemere (Testcontainers).
 * @ServiceConnection injecte l'URI du conteneur dans spring.data.mongodb.* — pas de
 * serveur a demarrer a la main, fidele au comportement runtime (persistance reelle).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Notes API (end-to-end, real MongoDB)")
class NotesApplicationE2ETest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("A posted note is persisted and retrievable in the patient history")
    void shouldPersistAndRetrieveNote() throws Exception {
        String payload = "{\"patId\":42,\"note\":\"Le patient declare qu'il fume depuis peu\"}";

        mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.date").isNotEmpty());

        mockMvc.perform(get("/notes/patient/{patId}", 42))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patId").value(42))
                .andExpect(jsonPath("$[0].note").value("Le patient declare qu'il fume depuis peu"));
    }

    @Test
    @DisplayName("History is returned most-recent first")
    void shouldOrderHistoryDesc() throws Exception {
        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patId\":7,\"note\":\"premiere\"}"))
                .andExpect(status().isCreated());
        Thread.sleep(10); // BSON date = precision milliseconde : garantir 2 horodatages distincts
        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patId\":7,\"note\":\"seconde\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/notes/patient/{patId}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].note").value("seconde"))
                .andExpect(jsonPath("$[1].note").value("premiere"));
    }
}
