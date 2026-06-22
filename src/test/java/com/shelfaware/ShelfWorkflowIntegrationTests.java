package com.shelfaware;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.time.LocalDate;
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
    void visitorCanEnterAnIsolatedPopulatedDemoProfile() throws Exception {
        MvcResult demoResult = mockMvc.perform(post("/api/auth/demo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.user.displayName").value("Alex Morgan"))
            .andExpect(jsonPath("$.user.username", org.hamcrest.Matchers.startsWith("demo_")))
            .andReturn();

        String token = objectMapper.readTree(demoResult.getResponse().getContentAsString())
            .get("accessToken").asText();

        mockMvc.perform(get("/api/me/journey").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal.targetBooks").value(12))
            .andExpect(jsonPath("$.goal.targetPages").value(5000))
            .andExpect(jsonPath("$.activeBooks.length()").value(1))
            .andExpect(jsonPath("$.activeBooks[0].currentPage").value(284))
            .andExpect(jsonPath("$.recentSessions.length()", org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(get("/api/me/shelf").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[?(@.favorite == true)]", org.hamcrest.Matchers.hasSize(2)));
    }

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
            .andExpect(jsonPath("$.user.username").value(username))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "usernameOrEmail", username,
                    "password", password
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.username").value(username))
            .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("accessToken").asText();

        MvcResult bookResult = mockMvc.perform(post("/api/books/import")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "externalSource", "OPEN_LIBRARY",
                    "externalId", "/works/OL27687548W",
                    "title", "Spring Boot in Practice",
                    "authors", "Somnath Musib",
                    "isbn", "9781617298813",
                    "categories", "Java, Spring Boot",
                    "pageCount", 600
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Spring Boot in Practice"))
            .andExpect(jsonPath("$.externalSource").value("OPEN_LIBRARY"))
            .andReturn();

        JsonNode bookJson = objectMapper.readTree(bookResult.getResponse().getContentAsString());
        long bookId = bookJson.get("id").asLong();

        mockMvc.perform(put("/api/me/shelf/{bookId}", bookId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", "READING",
                    "privateNotes", "Reference this while improving my Java portfolio."
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("READING"));

        int year = LocalDate.now().getYear();
        String today = LocalDate.now().toString();

        mockMvc.perform(put("/api/me/reading-goals/{year}", year)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("targetBooks", 12, "targetPages", 6000))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetBooks").value(12))
            .andExpect(jsonPath("$.targetPages").value(6000));

        mockMvc.perform(post("/api/me/shelf/{bookId}/progress", bookId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("currentPage", 100, "readOn", today))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.session.pagesRead").value(100))
            .andExpect(jsonPath("$.shelfItem.currentPage").value(100));

        MvcResult completionResult = mockMvc.perform(post("/api/me/shelf/{bookId}/progress", bookId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("currentPage", 600, "readOn", today))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.session.pagesRead").value(500))
            .andExpect(jsonPath("$.session.completedBook").value(true))
            .andExpect(jsonPath("$.shelfItem.status").value("FINISHED"))
            .andExpect(jsonPath("$.milestone").value("Book finished"))
            .andReturn();

        long completionSessionId = objectMapper.readTree(completionResult.getResponse().getContentAsString())
            .get("session").get("id").asLong();

        mockMvc.perform(post("/api/me/shelf/{bookId}/progress", bookId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("currentPage", 601, "readOn", today))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/me/journey")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagesRead").value(600))
            .andExpect(jsonPath("$.completedBooks").value(1))
            .andExpect(jsonPath("$.goal.booksProgress").value(org.hamcrest.Matchers.closeTo(8.33, 0.1)))
            .andExpect(jsonPath("$.streak.currentDays").value(1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                "/api/me/reading-sessions/{sessionId}", completionSessionId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me/shelf").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].currentPage").value(100))
            .andExpect(jsonPath("$[0].status").value("READING"));

        mockMvc.perform(put("/api/me/shelf/{bookId}", bookId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", "READING",
                    "favorite", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(put("/api/books/{bookId}/reviews/me", bookId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "rating", 5,
                    "body", "Practical, focused, and immediately useful for backend design.",
                    "publicReview", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(get("/api/me/insights").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readingCount").value(1))
            .andExpect(jsonPath("$.favoriteCount").value(1))
            .andExpect(jsonPath("$.pagesRead").value(100))
            .andExpect(jsonPath("$.reviewCount").value(1))
            .andExpect(jsonPath("$.averageRating").value(5.0));
    }
}
