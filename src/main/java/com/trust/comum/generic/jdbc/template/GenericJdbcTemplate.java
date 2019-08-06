

























































package com.trust.comum.generic.jdbc.template;

import static com.opengamma.elsql.ElSqlConfig.SQL_SERVER_2008;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterBatchUpdateUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;

/**
 * Template class with a basic set of JDBC operations, allowing the use
 * of named parameters rather than traditional '?' placeholders.
 *
 * <p>
 * This class delegates to a wrapped {@link #getJdbcOperations() JdbcTemplate}
 * once the substitution from named parameters to JDBC style '?' placeholders is
 * done at execution time. It also allows for expanding a {@link List}
 * of values to the appropriate number of placeholders.
 *
 * <p>The underlying {@link org.springframework.jdbc.core.JdbcTemplate} is
 * exposed to allow for convenient access to the traditional
 * {@link org.springframework.jdbc.core.JdbcTemplate} methods.
 *
 * <p><b>NOTE: An instance of this class is thread-safe once configured.</b>
 *
 * @author isaac.silva
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class GenericJdbcTemplate extends NamedParameterJdbcTemplate {
	private static final ElSqlConfig DEFAULT_DIALECT_CONFIG = SQL_SERVER_2008;

	private final ElSqlBundle elSqlBundle;

	public GenericJdbcTemplate(DataSource dataSource, Resource sqlResource) {
		this(dataSource, sqlResource, DEFAULT_DIALECT_CONFIG);
	}

	public GenericJdbcTemplate(DataSource dataSource, Resource sqlResource, ElSqlConfig dialect) {
		super(dataSource);
		this.elSqlBundle = ElSqlBundle.parse(dialect, sqlResource);
	}

	public GenericJdbcTemplate(JdbcOperations classicJdbcTemplate, Resource sqlResource) {
		this(classicJdbcTemplate, sqlResource, DEFAULT_DIALECT_CONFIG);
	}

	public GenericJdbcTemplate(JdbcOperations classicJdbcTemplate, Resource sqlResource, ElSqlConfig dialect) {
		super(classicJdbcTemplate);
		this.elSqlBundle = ElSqlBundle.parse(dialect, sqlResource);
	}

	@Nullable
	public <T> T executeForSQLName(String sqlName, SqlParameterSource paramSource, PreparedStatementCallback<T> action) {
		String query = getSqlByName(sqlName);
		return getJdbcOperations().execute(getPreparedStatementCreator(query, paramSource), action);
	}

	@Nullable
	public <T> T executeForSQLName(String sqlName, Map<String, ?> paramMap, PreparedStatementCallback<T> action) {
		String query = getSqlByName(sqlName);
		return execute(query, new MapSqlParameterSource(paramMap), action);
	}

	@Nullable
	public <T> T executeForSQLName(String sqlName, PreparedStatementCallback<T> action) {
		String query = getSqlByName(sqlName);
		return execute(query, EmptySqlParameterSource.INSTANCE, action);
	}

	@Nullable
	public <T> T queryForSQLName(String sqlName, SqlParameterSource paramSource, ResultSetExtractor<T> rse) {
		String query = getSqlByName(sqlName);
		return getJdbcOperations().query(getPreparedStatementCreator(query, paramSource), rse);
	}

	@Nullable
	public <T> T queryForSQLName(String sqlName, Map<String, ?> paramMap, ResultSetExtractor<T> rse) {
		String query = getSqlByName(sqlName);
		return query(query, new MapSqlParameterSource(paramMap), rse);
	}

	@Nullable
	public <T> T queryForSQLName(String sqlName, ResultSetExtractor<T> rse) {
		String query = getSqlByName(sqlName);
		return query(query, EmptySqlParameterSource.INSTANCE, rse);
	}

	private String getSqlByName(String sqlName) {
		String query = elSqlBundle.getSql(sqlName);
		return query;
	}

	public void queryForSQLName(String sqlName, SqlParameterSource paramSource, RowCallbackHandler rch) {
		String query = getSqlByName(sqlName);
		getJdbcOperations().query(getPreparedStatementCreator(query, paramSource), rch);
	}

	public void queryForSQLName(String sqlName, Map<String, ?> paramMap, RowCallbackHandler rch) {
		String query = getSqlByName(sqlName);
		query(query, new MapSqlParameterSource(paramMap), rch);
	}

	public void queryForSQLName(String sqlName, RowCallbackHandler rch) {
		String query = getSqlByName(sqlName);
		query(query, EmptySqlParameterSource.INSTANCE, rch);
	}

	public <T> List<T> queryForSQLName(String sqlName, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
		String query = getSqlByName(sqlName);
		return getJdbcOperations().query(getPreparedStatementCreator(query, paramSource), rowMapper);
	}

	public <T> List<T> queryForSQLName(String sqlName, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
		String query = getSqlByName(sqlName);
		return query(query, new MapSqlParameterSource(paramMap), rowMapper);
	}

	public <T> List<T> queryForSQLName(String sqlName, RowMapper<T> rowMapper) {
		String query = getSqlByName(sqlName);
		return query(query, EmptySqlParameterSource.INSTANCE, rowMapper);
	}

	@Nullable
	public <T> T queryForObjectForSQLName(String sqlName, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
		String query = getSqlByName(sqlName);
		List<T> results = getJdbcOperations().query(getPreparedStatementCreator(query, paramSource), rowMapper);
		return DataAccessUtils.nullableSingleResult(results);
	}

	@Nullable
	public <T> T queryForObjectForSQLName(String sqlName, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
		String query = getSqlByName(sqlName);
		return queryForObject(query, new MapSqlParameterSource(paramMap), rowMapper);
	}

	@Nullable
	public <T> T queryForObjectForSQLName(String sqlName, SqlParameterSource paramSource, Class<T> requiredType) {
		String query = getSqlByName(sqlName);
		return queryForObject(query, paramSource, new SingleColumnRowMapper<>(requiredType));
	}

	@Nullable
	public <T> T queryForObjectForSQLName(String sqlName, Map<String, ?> paramMap, Class<T> requiredType) {
		String query = getSqlByName(sqlName);
		return queryForObject(query, paramMap, new SingleColumnRowMapper<>(requiredType));
	}

	public Map<String, Object> queryForMapForSQLName(String sqlName, SqlParameterSource paramSource) {
		String query = getSqlByName(sqlName);
		Map<String, Object> result = queryForObject(query, paramSource, new ColumnMapRowMapper());
		Assert.state(result != null, "No result map");
		return result;
	}

	public Map<String, Object> queryForMapForSQLName(String sqlName, Map<String, ?> paramMap) {
		String query = getSqlByName(sqlName);
		Map<String, Object> result = queryForObject(query, paramMap, new ColumnMapRowMapper());
		Assert.state(result != null, "No result map");
		return result;
	}

	public <T> List<T> queryForListForSQLName(String sqlName, SqlParameterSource paramSource, Class<T> elementType) {
		String query = getSqlByName(sqlName);
		return query(query, paramSource, new SingleColumnRowMapper<>(elementType));
	}

	public <T> List<T> queryForListForSQLName(String sqlName, Map<String, ?> paramMap, Class<T> elementType) {
		String query = getSqlByName(sqlName);
		return queryForList(query, new MapSqlParameterSource(paramMap), elementType);
	}

	public List<Map<String, Object>> queryForListForSQLName(String sqlName, SqlParameterSource paramSource) {
		String query = getSqlByName(sqlName);
		return query(query, paramSource, new ColumnMapRowMapper());
	}

	public List<Map<String, Object>> queryForListForSQLName(String sqlName, Map<String, ?> paramMap) {
		String query = getSqlByName(sqlName);
		return queryForList(query, new MapSqlParameterSource(paramMap));
	}

	public SqlRowSet queryForRowSetForSQLName(String sqlName, SqlParameterSource paramSource) {
		String query = getSqlByName(sqlName);
		SqlRowSet result = getJdbcOperations().query(getPreparedStatementCreator(query, paramSource), new SqlRowSetResultSetExtractor());
		Assert.state(result != null, "No result");
		return result;
	}

	public SqlRowSet queryForRowSetForSQLName(String sqlName, Map<String, ?> paramMap) {
		String query = getSqlByName(sqlName);
		return queryForRowSet(query, new MapSqlParameterSource(paramMap));
	}

	public int updateForSQLName(String sqlName, SqlParameterSource paramSource) {
		String query = getSqlByName(sqlName);
		return getJdbcOperations().update(getPreparedStatementCreator(query, paramSource));
	}

	public int updateForSQLName(String sqlName, Map<String, ?> paramMap) {
		String query = getSqlByName(sqlName);
		return update(query, new MapSqlParameterSource(paramMap));
	}

	public int updateForSQLName(String sqlName, SqlParameterSource paramSource, KeyHolder generatedKeyHolder) {
		String query = getSqlByName(sqlName);
		return update(query, paramSource, generatedKeyHolder, null);
	}

	public int updateForSQLName(String sqlName, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, @Nullable String[] keyColumnNames) {
		String query = getSqlByName(sqlName);
		PreparedStatementCreator psc = getPreparedStatementCreator(query, paramSource, pscf -> {
			if (keyColumnNames != null) {
				pscf.setGeneratedKeysColumnNames(keyColumnNames);
			} else {
				pscf.setReturnGeneratedKeys(true);
			}
		});
		return getJdbcOperations().update(psc, generatedKeyHolder);
	}

	public int[] batchUpdateForSQLName(String sqlName, Map<String, ?>[] batchValues) {
		String query = getSqlByName(sqlName);
		return batchUpdate(query, SqlParameterSourceUtils.createBatch(batchValues));
	}

	public int[] batchUpdateForSQLName(String sqlName, SqlParameterSource[] batchArgs) {
		String query = getSqlByName(sqlName);
		return NamedParameterBatchUpdateUtils.executeBatchUpdateWithNamedParameters(getParsedSql(query), batchArgs, getJdbcOperations());
	}

	/**
	 * Build a {@link PreparedStatementCreator} based on the given SQL and named parameters.
	 * <p>Note: Directly called from all {@code query} variants. Delegates to the common
	 * {@link #getPreparedStatementCreator(String, SqlParameterSource, Consumer)} method.
	 * @param sqlName the SQL Name statement to execute
	 * @param paramSource container of arguments to bind
	 * @return the corresponding {@link PreparedStatementCreator}
	 * @see #getPreparedStatementCreator(String, SqlParameterSource, Consumer)
	 */
	protected PreparedStatementCreator getPreparedStatementCreatorForSQLName(String sqlName, SqlParameterSource paramSource) {
		String query = getSqlByName(sqlName);
		return getPreparedStatementCreator(query, paramSource, null);
	}

	/**
	 * Build a {@link PreparedStatementCreator} based on the given SQL and named parameters.
	 * <p>Note: Used for the {@code update} variant with generated key handling, and also
	 * delegated from {@link #getPreparedStatementCreator(String, SqlParameterSource)}.
	 * @param sqlName the SQL Name statement to execute
	 * @param paramSource container of arguments to bind
	 * @param customizer callback for setting further properties on the
	 * {@link PreparedStatementCreatorFactory} in use), applied before the
	 * actual {@code newPreparedStatementCreator} call
	 * @return the corresponding {@link PreparedStatementCreator}
	 * @since 5.0.5
	 * @see #getParsedSql(String)
	 * @see PreparedStatementCreatorFactory#PreparedStatementCreatorFactory(String, List)
	 * @see PreparedStatementCreatorFactory#newPreparedStatementCreator(Object[])
	 */
	protected PreparedStatementCreator getPreparedStatementCreatorForSQLName(String sqlName, SqlParameterSource paramSource, @Nullable Consumer<PreparedStatementCreatorFactory> customizer) {
		String query = getSqlByName(sqlName);
		ParsedSql parsedSql = getParsedSql(query);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
		if (customizer != null) {
			customizer.accept(pscf);
		}
		Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
		return pscf.newPreparedStatementCreator(params);
	}
}
