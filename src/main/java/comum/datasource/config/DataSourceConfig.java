package comum.datasource.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import comum.generic.jdbc.template.GenericJdbcTemplate;
import comum.jdbc.sql.AuditSQLServerDataSource;

/**
 * Configuration to access the database
 * 
 * @author rafael.goncalves
 */
@Configuration
@ComponentScan
public class DataSourceConfig {

	@Value("${spring.application.name}")
	private String serviceName;

	@Bean
	@ConditionalOnMissingBean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * Configuring dataSource to connect to our database
	 * 
	 * @return AuditSQLServerDataSource.getDataSource()
	 */
	@Bean(name = "dataSource")
	@ConditionalOnMissingBean
	public DataSource dataSource(@Value("${database.host}") String host, @Value("${database.port}") Integer port,
			@Value("${database.username}") String username, @Value("${database.password}") String password,
			@Value("${database.name}") String database, @Value("${database.instance.name}") String instanceName,
			@Value("${database.pool}") Integer pool) {

		AuditSQLServerDataSource ds = new AuditSQLServerDataSource();
		ds.setServerName("10.50.3.96");
		ds.setPort(port);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setDatabaseName(database);
		ds.setMaximumPoolSize(pool);
		ds.setInstanceName(instanceName);
		ds.setApplicationName(serviceName + ".elsql");
		return ds;
	}

	/**
	 * Set de dataSource to be used by the genericJdbcTemplate that's common used by
	 * all the SRM - Trust application
	 * 
	 * @return GenericJdbcTemplate
	 */
	@Bean
	public GenericJdbcTemplate genericJdbcTemplate(DataSource ds) {
		return new GenericJdbcTemplate(ds, new ClassPathResource(serviceName + ".elsql"));
	}

}