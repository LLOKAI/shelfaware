package com.shelfaware;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ShelfWorkflowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanCreateBookTrackShelfReviewAndSeeInsights() throws Exception {
        String username = "reader_" + System.nanoTime();
        String password = "spring-boot-portfolio";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "displayName", "Portfolio Reader",
                    "email", username + "@example.com",
                    "username", username,
                    "password", password
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(username));

        MvcResult bookResult = mockMvc.perform(post("/api/books")
                .with(httpBasic(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "Spring Boot in Practice",
                    "authors", "Somnath Musib",
                    "isbn", "9781617298813",
                    "categories", "Java, Spring Boot",
                    "pageCount", 600
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Spring Boot in Practice"))
            .andReturn();

        JsonNode bookJson = objectMapper.readTree(bookResult.getResponse().getContentAsString());
        long bookId = bookJson.get("id").asLong();

        mockMvc.perform(put("/api/me/shelf/{bookId}", bookId)
                .with(httpBasic(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", "READING",
                    "privateNotes", "Reference this while improving my Java portfolio."
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("READING"));

        mockMvc.perform(put("/api/books/{bookId}/reviews/me", bookId)
                .with(httpBasic(username, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "rating", 5,
                    "body", "Practical, focused, and immediately useful for backend design.",
                    "publicReview", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(get("/api/me/insights").with(httpBasic(username, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readingCount").value(1))
            .andExpect(jsonPath("$.reviewCount").value(1))
            .andExpect(jsonPath("$.averageRating").value(5.0));
    }
}
