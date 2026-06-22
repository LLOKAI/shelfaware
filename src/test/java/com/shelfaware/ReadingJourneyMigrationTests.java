package com.shelfaware;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

class ReadingJourneyMigrationTests {
    @Test
    void favoriteStatusesBecomeFinishedFavoriteShelfItems() throws Exception {
        String url = "jdbc:h2:mem:journey_migration;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target(MigrationVersion.fromVersion("3")).load().migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", ""); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO users (id, display_name, email, username, password_hash, created_at) VALUES (10, 'Reader', 'reader@test.com', 'reader', 'hash', CURRENT_TIMESTAMP)");
            statement.executeUpdate("INSERT INTO books (id, title, authors, created_at, updated_at) VALUES (10, 'Favorite Book', 'Author', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            statement.executeUpdate("INSERT INTO shelf_items (id, book_id, user_id, status, created_at, updated_at) VALUES (10, 10, 10, 'FAVORITE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        }

        Flyway.configure().dataSource(url, "sa", "").target(MigrationVersion.fromVersion("4")).load().migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", ""); Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery("SELECT status, favorite, finished_on, current_page FROM shelf_items WHERE id = 10");
            assertThat(result.next()).isTrue();
            assertThat(result.getString("status")).isEqualTo("FINISHED");
            assertThat(result.getBoolean("favorite")).isTrue();
            assertThat(result.getDate("finished_on")).isNotNull();
            assertThat(result.getInt("current_page")).isZero();
        }
    }
}
