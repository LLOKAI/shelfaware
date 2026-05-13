package com.shelfaware.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shelfaware.api.book.ExternalBookResponse;
import com.shelfaware.exception.ExternalServiceException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

@Component
public class OpenLibraryClient {

    private static final String SOURCE = "OPEN_LIBRARY";

    private final RestClient restClient;

    public OpenLibraryClient(RestClient.Builder restClientBuilder, OpenLibraryProperties properties) {
        this.restClient = restClientBuilder
            .baseUrl(properties.baseUrl())
            .build();
    }

    public SearchResult search(String query, int limit) {
        OpenLibrarySearchResponse response;
        try {
            response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/search.json")
                    .queryParam("q", query)
                    .queryParam("limit", limit)
                    .queryParam("fields", "key,title,author_name,first_publish_year,isbn,cover_i,publisher,subject,number_of_pages_median")
                    .build())
                .retrieve()
                .body(OpenLibrarySearchResponse.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Open Library search is temporarily unavailable", ex);
        }

        if (response == null) {
            return new SearchResult(0, List.of());
        }

        List<ExternalBookResponse> books = response.docs()
            .stream()
            .map(this::toExternalBook)
            .filter(Objects::nonNull)
            .toList();

        return new SearchResult(response.numFound(), books);
    }

    private ExternalBookResponse toExternalBook(OpenLibraryDocument document) {
        if (document.title() == null || document.key() == null || CollectionUtils.isEmpty(document.authorName())) {
            return null;
        }

        String isbn = first(document.isbn());
        return new ExternalBookResponse(
            SOURCE,
            document.key(),
            document.title(),
            String.join(", ", document.authorName()),
            isbn,
            null,
            coverUrl(document.coverId(), isbn),
            first(document.publisher()),
            firstFew(document.subject(), 3),
            publishedDate(document.firstPublishYear()),
            document.numberOfPagesMedian()
        );
    }

    private String coverUrl(Integer coverId, String isbn) {
        if (coverId != null) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
        }

        if (isbn != null) {
            return "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
        }

        return null;
    }

    private LocalDate publishedDate(Integer year) {
        return year == null ? null : LocalDate.of(year, 1, 1);
    }

    private String first(List<String> values) {
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

    private String firstFew(List<String> values, int count) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }

        return String.join(", ", values.stream().limit(count).toList());
    }

    public record SearchResult(long totalResults, List<ExternalBookResponse> books) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenLibrarySearchResponse(
        @JsonProperty("num_found") long numFound,
        List<OpenLibraryDocument> docs
    ) {
        private OpenLibrarySearchResponse {
            docs = docs == null ? List.of() : docs;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenLibraryDocument(
        String key,
        String title,
        @JsonProperty("author_name") List<String> authorName,
        @JsonProperty("first_publish_year") Integer firstPublishYear,
        List<String> isbn,
        @JsonProperty("cover_i") Integer coverId,
        List<String> publisher,
        List<String> subject,
        @JsonProperty("number_of_pages_median") Integer numberOfPagesMedian
    ) {
    }
}
