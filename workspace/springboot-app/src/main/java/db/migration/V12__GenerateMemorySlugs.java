package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.utils.SlugUtil;

public class V12__GenerateMemorySlugs extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();

		try (PreparedStatement select = connection
				.prepareStatement("SELECT memory_id, title, memory_date FROM memories WHERE slug IS NULL")) {

			ResultSet rs = select.executeQuery();

			try (PreparedStatement update = connection
					.prepareStatement("UPDATE memories SET slug = ? WHERE memory_id = ?")) {

				int count = 0;

				while (rs.next()) {

					Memory memory = new Memory();
					memory.setMemoryId(rs.getLong("memory_id"));
					memory.setTitle(rs.getString("title"));
					memory.setMemoryDate(rs.getDate("memory_date").toLocalDate());

					String slug = SlugUtil.toSlug(memory);

					update.setString(1, slug);
					update.setLong(2, memory.getMemoryId());
					update.executeUpdate();

					count++;
				}

				System.out.println("Flyway migration : " + count + " Memory slugs generated");
			}
		}
	}

}
