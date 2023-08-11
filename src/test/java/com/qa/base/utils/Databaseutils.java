package com.qa.base.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.KeyedHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.json.JSONArray;

/**
 * DBUtils encapsulates methods required to get data from an Oracle database
 *
 * @author gholla01
 */
public class Databaseutils {

	private Connection facetsConnection = null;
	private Connection wprConnection = null;
	private Connection connection = null;
	private Connection msSqlConnection = null;
	private Jutils jutils = new Jutils();

	/**
	 * Setup the database connection using the oracle JDBC driver.
	 *
	 */
	public void setUp() {
		String facetsDB = System.getenv("FACETS_DB");
		String wprDB = System.getenv("WPR_DB");
		String oracleDB = System.getenv("ORACLE_DB");

		if (facetsConnection == null && facetsDB != null
				&& !"".equals(facetsDB)) {
			String facetsUser = System.getenv("FACETS_USER");
			String facetsPassword = System.getenv("FACETS_PASSWORD");
			String facetsServer = System.getenv("FACETS_SERVER");
			String facetsPort = System.getenv("FACETS_PORT");
			String facetsConnectionString = "jdbc:oracle:thin:"
					+ facetsUser.trim() + "/" + facetsPassword.trim() + "@"
					+ facetsServer.trim() + ":" + facetsPort.trim() + "/"
					+ facetsDB.trim();
			try {
				facetsConnection = DriverManager
						.getConnection(facetsConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ facetsDB);
				ex.printStackTrace();
			}
		}

		if (wprConnection == null && wprDB != null && !"".equals(wprDB)) {
			String wprUser = System.getenv("WPR_USER");
			String wprPassword = System.getenv("WPR_PASSWORD");
			String wprServer = System.getenv("WPR_SERVER");
			String wprPort = System.getenv("WPR_PORT");
			String wprConnectionString = "jdbc:sqlserver://" + wprServer 
					+ ":" + wprPort 
					+ ";DatabaseName=" + wprDB
					+ ";user=" + wprUser 
					+ ";password=" + wprPassword;

			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try {
				wprConnection = DriverManager
						.getConnection(wprConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the WPR MSSQL database: "
								+ wprDB);
				ex.printStackTrace();
			}

		}

		if (connection == null && oracleDB != null && !"".equals(oracleDB)) {
			String oracleUser = System.getenv("ORACLE_USER");
			String oraclePassword = System.getenv("ORACLE_PASSWORD");
			String oracleServer = System.getenv("ORACLE_SERVER");
			String oraclePort = System.getenv("ORACLE_PORT");
			String oracleConnectionString = "jdbc:oracle:thin:"
					+ oracleUser.trim() + "/" + oraclePassword.trim() + "@"
					+ oracleServer.trim() + ":" + oraclePort.trim() + ":"
					+ oracleDB.trim();
			try {
				connection = DriverManager
						.getConnection(oracleConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ facetsDB);
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Setup method for passing custom db name to setup EDIFECS_MSSQL connection.
	 * @param msSqlDB MS SQL Server DB name as string
	 */
	public void setUpMsSql(String msSqlDB) {

		if (msSqlConnection == null && msSqlDB != null
				&& !"".equals(msSqlDB)) {
			String user = System.getenv("EDIFECS_MSSQL_USER");
			String password = System.getenv("EDIFECS_MSSQL_PASSWORD");
			String server = System.getenv("EDIFECS_MSSQL_SERVER");
			String port = System.getenv("EDIFECS_MSSQL_PORT");
			String connectionString = "jdbc:sqlserver://" + server 
						+ ":" + port 
						+ ";DatabaseName=" + msSqlDB
						+ ";user=" + user 
						+ ";password=" + password;
			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			try {
				msSqlConnection = DriverManager
						.getConnection(connectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ msSqlDB);
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Close the database connection
	 *
	 */
	public void tearDown() {
		if (facetsConnection != null) {
			try {
				// System.out.println("Closing Facets Database Connection...");
				facetsConnection.close();
				facetsConnection = null;
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		if (wprConnection != null) {
			try {
				// System.out.println("Closing WPR Database Connection...");
				wprConnection.close();
				wprConnection = null;
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				// System.out.println("Closing WPR Database Connection...");
				connection.close();
				connection = null;
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Get Facets table data as an array object
	 *
	 * @param query
	 *            SQL query
	 * @return Facets table array
	 */
	public Object[][] getFacetsTableArray(String query) {
		return getTableArray("facets", query);
	}

	/**
	 * Get WPR table data as an array object
	 *
	 * @param query
	 *            SQL Query
	 * @return WPR table array
	 */
	public Object[][] getWprTableArray(String query) {
		return getTableArray("wpr", query);
	}

	/**
	 * Get Table data as an array object
	 *
	 * @param dbSource
	 *            Database source
	 * @param query
	 *            SQL query
	 * @return table data as array
	 */
	public Object[][] getTableArray(String dbSource, String query) {
		String[][] tableArray = null;
		int totalCols = 0;
		int totalRows = 0;
		int ci = 0;
		int cj = 0;
		Statement statement = null;
		ResultSet resultSet;
		setUp();
		try {
			if ("facets".equals(dbSource)) {
				statement = facetsConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("wpr".equals(dbSource)) {
				statement = wprConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("mssql".equals(dbSource)) {
				statement = msSqlConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("other".equals(dbSource)){
				statement = connection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			}

			resultSet = statement.executeQuery(query);
			resultSet.last();
			totalRows = resultSet.getRow();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			totalCols = rsmd.getColumnCount();
			tableArray = new String[totalRows][totalCols];
			resultSet.beforeFirst();

			while (resultSet.next()) {
				int i = 1;
				cj = 0;
				while (i <= totalCols) {
					// System.out.println(resultSet.getString(i) + " i=" + i);
					tableArray[ci][cj] = resultSet.getString(i++);
					cj++;
				}
				ci++;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		tearDown();
		return tableArray;
	}

	/**
	 * Get resultset as a sorted map based on an array of primary keys
	 * 
	 * @param dbSource Data source
	 * @param query SQL Query
	 * @param primaryKey Primary key
	 * @return SortedMap of SortedMaps
	 */
	public SortedMap<String, SortedMap<String, String>> getResultSetAsSortedMap(
			String dbSource, String query, String... primaryKey) {
		SortedMap<String, String> row = null;
		ResultSetMetaData resultSetMetaData = null;
		ResultSet resultSet = getResultSet(dbSource, query);
		int rowCounter = 0;
		@SuppressWarnings("unused")
		int cellCounter = 0;

		SortedMap<String, SortedMap<String, String>> table = new TreeMap<String, SortedMap<String, String>>();
		try {
			resultSetMetaData = resultSet.getMetaData();
			String primaryKeyValue = null;
			String value = null;
			String columnName = null;
			while (resultSet.next()) {
				primaryKeyValue = "";
				for (String key : primaryKey) {
					primaryKeyValue += resultSet.getString(key);
				}
				row = new TreeMap<String, String>();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					value = resultSet.getString(i);
					columnName = resultSetMetaData.getColumnLabel(i);
					if (value == null) {
						value = "";
					}
					row.put(columnName.toLowerCase(), value.toLowerCase());
					cellCounter++;
				}
				table.put(primaryKeyValue.toLowerCase(), row);

				rowCounter++;
				cellCounter = 0;
			}
			System.out.println("Keyset size: " + table.keySet().size());
			System.out.println("Row count: " + rowCounter);
			// System.out.println("Column count: " +
			// resultSetMetaData.getColumnCount());
			// System.out.println("Cell count: " + cellCounter);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tearDown();
		return table;
	}

	/**
	 * Get resultset as a SortedMap
	 * 
	 * @param dbSource Data source
	 * @param query SQL Query
	 * @return SortedMap of SortedMaps
	 */
	public SortedMap<String, SortedMap<String, String>> getResultSetAsSortedMap(
			String dbSource, String query) {
		SortedMap<String, String> row = null;
		ResultSetMetaData resultSetMetaData = null;
		ResultSet resultSet = getResultSet(dbSource, query);
		@SuppressWarnings("unused")
		int rowCounter = 0;
		@SuppressWarnings("unused")
		int cellCounter = 0;

		// Map<String, Map<String, String>> table = new HashMap<String,
		// Map<String,String>>();
		SortedMap<String, SortedMap<String, String>> table = new TreeMap<String, SortedMap<String, String>>();
		try {
			resultSetMetaData = resultSet.getMetaData();
			StringBuilder keyBuilder = null;
			while (resultSet.next()) {
				keyBuilder = new StringBuilder();
				row = new TreeMap<String, String>();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					String key = resultSetMetaData.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value = "";
					}
					keyBuilder.append(value);
					row.put(key, value);
					cellCounter++;
				}
				table.put(keyBuilder.toString(), row);
				rowCounter++;
			}
			System.out.println("Keyset size: " + table.keySet().size());
			// System.out.println("Row count: " + rowCounter);
			// System.out.println("Column count: " +
			// resultSetMetaData.getColumnCount());
			// System.out.println("Cell count: " + cellCounter);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tearDown();
		return table;
	}

	/**
	 * Get resultset as a map
	 * 
	 * @param dbSource Data source
	 * @param query SQL Query
	 * @return Map
	 */
	public Map<String, String> getOneRowResultSetAsMap(String dbSource,
			String query) {
		ResultSetMetaData resultSetMetaData = null;
		ResultSet resultSet = getResultSet(dbSource, query);
		Map<String, String> table = null;

		try {
			resultSetMetaData = resultSet.getMetaData();
			if (resultSet.next()) {
				table = new HashMap<String, String>();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					String key = resultSetMetaData.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value = "";
					}
					table.put(key, value);
				}
			} else {
				throw new Exception("No query results returned");
			}
			System.out.println("Keyset size: " + table.keySet().size());
			// System.out.println("Row count: " + rowCounter);
			// System.out.println("Column count: " +
			// resultSetMetaData.getColumnCount());
			// System.out.println("Cell count: " + cellCounter);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tearDown();
		return table;
	}

	/**
	 * Execute a query and get a ResultSet
	 *
	 * @param dbSource
	 *            Database source
	 * @param query
	 *            SQL Query
	 * @return resultSet
	 */
	public ResultSet getResultSet(String dbSource, String query) {
		Statement statement = null;
		ResultSet resultSet = null;
		setUp();
		try {
			if ("facets".equals(dbSource)) {
				statement = facetsConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("wpr".equals(dbSource)) {
				statement = wprConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("mssql".equals(dbSource)) {
				statement = msSqlConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("other".equals(dbSource)){
				statement = connection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			}

			resultSet = statement.executeQuery(query);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return resultSet;
	}

	/**
	 * Execute an SQL update query
	 * 
	 * @param dbSource
	 *            database source facets, wpr, other
	 * @param query
	 *            SQL select statement to execute
	 * @return execution status as integer
	 */
	public int executeUpdate(String dbSource, String query) {
		Statement statement;
		int updateQueryResult = -10;

		setUp();
		try {
			if ("facets".equals(dbSource)) {
				statement = facetsConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("wpr".equals(dbSource)) {
				statement = wprConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else {
				statement = connection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			}

			updateQueryResult = statement.executeUpdate(query);
		} catch (SQLException ex) {
			System.out.println("ERROR: Update query failed for " + dbSource
					+ " " + query);
			ex.printStackTrace();
		}
		tearDown();
		return updateQueryResult;
	}

	/**
	 * Get a resultSet from an SQL query as a JSONArray
	 *
	 * @param dbSource
	 *            Database source
	 * @param sqlQuery
	 *            SQL Query
	 * @param jsonArrayFromResultSet
	 *            Json array
	 * @return Resultset as JSONArray
	 */
	public JSONArray getResultSetAsJson(String dbSource, String sqlQuery,
			JSONArray jsonArrayFromResultSet) {
		ResultSet resultSet = getResultSet(dbSource, sqlQuery);
		try {
			jsonArrayFromResultSet = jutils.convertToJSONArray(resultSet);
		} catch (Exception e) {
			System.out
					.println("Error: converting resultset to JSONArray failed: ");
			e.printStackTrace();
		}
		tearDown();
		return jsonArrayFromResultSet;
	}

	/**
	 * Get resultset as a map of maps where key = primary key
	 * 
	 * @param dbSource Data source
	 * @param query SQL Query
	 * @param key Primay key
	 * @return Map of maps
	 */
	public Map<Object, Map<String, Object>> getKeyedResultSetAsMap(String dbSource, String query, String key) {
		connection = setUpDbType(dbSource);
		Map<Object, Map<String, Object>> row = null;
		QueryRunner queryRunner = new QueryRunner();
		 try {
			row = queryRunner.query(connection, query, new KeyedHandler<>(key));
		} catch (SQLException e) {
			System.out.println("Execution of query: " + query + " in " + dbSource + " failed:");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return row;
	}
	
	/**
	 * Get resultset as a list of maps
	 * 
	 * @param dbSource Data source
	 * @param query SQL Query
	 * @return List of maps
	 */
	public List<Map<String, Object>> getResultSetAsMapList(String dbSource, String query){
		connection = setUpDbType(dbSource);
		List<Map<String, Object>> result = null;
		QueryRunner queryRunner = new QueryRunner();
		try {
			result = queryRunner.query(connection, query, new MapListHandler());
		} catch (SQLException e) {
			System.out.println("Execution of query: " + query + " in " + dbSource + " failed:");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tearDown();

		return result;
	}
	

	/**
	 * Get column data as array object
	 *
	 * @param query
	 *            SQL query
	 * @return Facets table column array
	 */
	public Object[][] getFacetsColumnArray(String query) {
		return getColumnArray("facets", query);
	}

	/**
	 * Get column data as array object
	 *
	 * @param query
	 *            SQL query
	 * @return WPR table column array
	 */
	public Object[][] getWprColumnArray(String query) {
		return getColumnArray("wpr", query);
	}

	/**
	 * Get column headers from resultset
	 *
	 * @param dbSource
	 *            Database source
	 * @param query
	 *            SQL query
	 * @return Table column array
	 */
	public Object[][] getColumnArray(String dbSource, String query) {
		String[][] tableArray = null;
		int totalCols = 0;
		int totalRows = 0;
		Statement statement = null;
		ResultSet resultSet;
		setUp();
		System.out.println("Debug Query: -------");
		System.out.println(query);
		try {
			if ("facets".equals(dbSource)) {
				statement = facetsConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else if ("wpr".equals(dbSource)) {
				statement = wprConnection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} else {
				statement = connection.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			}

			resultSet = statement.executeQuery(query);
			resultSet.last();
			totalRows = resultSet.getRow();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			totalCols = rsmd.getColumnCount();
			tableArray = new String[totalRows][totalCols];
			resultSet.beforeFirst();

			int i = 0;
			while (resultSet.next()) {
				i = 0;
				while (i < totalCols) {
					tableArray[0][i++] = rsmd.getColumnName(i);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		tearDown();
		return tableArray;
	}

	/**
	 *
	 * @param connectionType
	 *            Connection type
	 * @return Connection object
	 */
	public Connection setUp(String connectionType) {
		String facetsDB = System.getenv("FACETS_DB");
		String facetsUser = System.getenv("FACETS_USER");
		String facetsPassword = System.getenv("FACETS_PASSWORD");
		String facetsServer = System.getenv("FACETS_SERVER");
		String facetsPort = System.getenv("FACETS_PORT");
		String facetsConnectionString = "jdbc:oracle:thin:" + facetsUser.trim()
				+ "/" + facetsPassword.trim() + "@" + facetsServer.trim() + ":"
				+ facetsPort.trim() + "/" + facetsDB.trim();

		String wprDB = System.getenv("WPR_DB");
		String wprUser = System.getenv("WPR_USER");
		String wprPassword = System.getenv("WPR_PASSWORD");
		String wprServer = System.getenv("WPR_SERVER");
		String wprPort = System.getenv("WPR_PORT");
		String wprConnectionString = "jdbc:oracle:thin:" + wprUser.trim() + "/"
				+ wprPassword.trim() + "@" + wprServer.trim() + ":"
				+ wprPort.trim() + "/" + wprDB.trim();

		String mssqlDB = System.getenv("EDIFECS_MSSQL_DB");
		String mssqlUser = System.getenv("EDIFECS_MSSQL_USER");
		String mssqlPassword = System.getenv("EDIFECS_MSSQL_PASSWORD");
		String mssqlServer = System.getenv("EDIFECS_MSSQL_SERVER");
		String mssqlPort = System.getenv("EDIFECS_MSSQL_PORT");
		String mssqlConnectionString = "jdbc:sqlserver://" + mssqlServer 
				+ ":" + mssqlPort 
				+ ";DatabaseName=" + mssqlDB
				+ ";user=" + mssqlUser 
				+ ";password=" + mssqlPassword;

		if (facetsDB != null && !"".equals(facetsDB)) {
			try {
				facetsConnection = DriverManager
						.getConnection(facetsConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ facetsDB);
				ex.printStackTrace();
			}
		}

		if (wprDB != null && !"".equals(wprDB)) {
			try {
//				System.out.println("Get Data from: " + wprDB);
				wprConnection = DriverManager
						.getConnection(wprConnectionString);
				if (wprConnection != null) {
					// dbConnections.put("wpr", wprConnection);
					// System.out.println("Connected to the Database: " +
					// wprDB);
				}
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ wprDB);
				ex.printStackTrace();
			}
		}

		if (mssqlDB != null && !"".equals(mssqlDB)) {
			try {
//				System.out.println("Get Data from: " + wprDB);
				msSqlConnection = DriverManager
						.getConnection(mssqlConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ mssqlDB);
				ex.printStackTrace();
			}
		}

		if (connectionType.equals("wpr")) {
			return wprConnection;
		} else if (connectionType.equals("facets")) {
			return facetsConnection;
		} else if (connectionType.equals("mssql")) {
			return msSqlConnection;
		} else {
			return null;
		}
		

	}

	/**
	 * Setup DB Type and create a connection
	 * 
	 * @param connectionType Connection type
	 * @return Connection
	 */
	public Connection setUpDbType(String connectionType) {
		String oracleDB = System.getenv(connectionType.toUpperCase() + "_DB");
		String oracleUser = System.getenv(connectionType.toUpperCase()
				+ "_USER");
		String oraclePassword = System.getenv(connectionType.toUpperCase()
				+ "_PASSWORD");
		String oracleServer = System.getenv(connectionType.toUpperCase()
				+ "_SERVER");
		String oraclePort = System.getenv(connectionType.toUpperCase()
				+ "_PORT");
		String pracleConnectionString = "jdbc:oracle:thin:" + oracleUser.trim()
				+ "/" + oraclePassword.trim() + "@" + oracleServer.trim() + ":"
				+ oraclePort.trim() + "/" + oracleDB.trim();

		if (!"".equals(oracleDB)) {
			try {
				connection = DriverManager
						.getConnection(pracleConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ oracleDB);
				ex.printStackTrace();
			}
		}

		return connection;
	}

	/**
	 * Setup DB type and create a connection
	 * 
	 * @param connectionType Connection Type
	 * @param db Database name
	 * @param server Server name
	 * @param port Port
	 * @return Connection
	 */
	public Connection setUpDbType(String connectionType, String db,
			String server, String port) {
		String oracleUser = System.getenv(connectionType.toUpperCase()
				+ "_USER");
		String oraclePassword = System.getenv(connectionType.toUpperCase()
				+ "_PASSWORD");
		String oracleDB = db;
		String oracleServer = server;
		String oraclePort = port;
		String oracleConnectionString = "jdbc:oracle:thin:" + oracleUser.trim()
				+ "/" + oraclePassword.trim() + "@" + oracleServer.trim() + ":"
				+ oraclePort.trim() + "/" + oracleDB.trim();

		if (!"".equals(oracleDB)) {
			try {
				connection = DriverManager
						.getConnection(oracleConnectionString);
			} catch (SQLException ex) {
				System.out
						.println("ERROR: SQL Exception when connecting to the database: "
								+ oracleDB);
				ex.printStackTrace();
			}
		}

		return connection;
	}

	/**
	 * Get multi row resultset as a sorted map of sorted map
	 * 
	 * @param dbSource Database source
	 * @param sql	Prepared Query
	 * @param values	Values for prepared query
	 * @return SortedMap
	 */
	public SortedMap<String, SortedMap<String, String>> getMultiRowsFromPreparedQuery(String dbSource, String sql, Object... values) {
		connection = setUp(dbSource);
		@SuppressWarnings("unused")
		int rowCounter = 0;
		@SuppressWarnings("unused")
		int cellCounter = 0;
		SortedMap<String, String> row = null;
		StringBuilder keyBuilder = null;

		SortedMap<String, SortedMap<String, String>> table = new TreeMap<String, SortedMap<String, String>>();
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		ResultSetMetaData resultSetMetaData;
		try {
			preparedStatement = preparePreparedStatement(sql, values);
			resultSet = preparedStatement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
			while (resultSet.next()) {
				keyBuilder = new StringBuilder();
				row = new TreeMap<String, String>();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					String key = resultSetMetaData.getColumnName(i);
					String value = resultSet.getString(i);
					if (value == null) {
						value = "";
					}
					keyBuilder.append(value);
					row.put(key, value);
					cellCounter++;
				}
				table.put(keyBuilder.toString(), row);
				rowCounter++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tearDown();
		return table;

	}	
	
	/**
	 * Get multi row resultset as a SortedMap of SortedMap with a specified primaryKey
	 * 
	 * @param dbSource Database source
	 * @param sql Prepared Query
	 * @param primaryKey Primary key
	 * @param values Values for prepared query
	 * @return SortedMap
	 */
	public SortedMap<String, SortedMap<String, String>> getMultiRowsFromPreparedQuery(String dbSource, String sql, String primaryKey, Object... values) {
		connection = setUp(dbSource);
		SortedMap<String, SortedMap<String, String>> data = new TreeMap<>();
		SortedMap<String, String> innerMap = null;
		
		String value = null;
		String columnName = null;
		PreparedStatement preparedStatement;
		ResultSet resultSet;

		try {
			preparedStatement = preparePreparedStatement(sql, values);
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			while(resultSet.next()) {
				innerMap = new TreeMap<>();
				String key = resultSet.getString(primaryKey);
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					columnName = rsmd.getColumnName(i+1);
					value = resultSet.getString(columnName);
					innerMap.put(columnName, value);
				}
				data.put(key, innerMap);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		tearDown();
		return data;
	}	
	
	/**
	 * 
	 * @param dbSource Data source
	 * @param sql SQL query
	 * @param values Values to find and replace
	 * @return Map
	 */
	public Map<String, String> getDataFromPreparedQuery(String dbSource, String sql, Object... values) {
		connection = setUp(dbSource);
		HashMap<String, String> data = new HashMap<>();
		String value = null;
		String columnName = null;
		PreparedStatement preparedStatement;
		ResultSet resultSet;

		try {
			preparedStatement = preparePreparedStatement(sql, values);
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			if (resultSet.next()) {
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					columnName = rsmd.getColumnName(i+1);
					value = resultSet.getString(columnName);
					data.put(columnName, value);
				}
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return data;
	}

	/**
	 * Prepared prepared statement
	 * 
	 * @param sql Prepared query
	 * @param values Values for the prepared query
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	private PreparedStatement preparePreparedStatement(String sql,
			Object... values) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		for (int i=0; i < values.length; i++) {
			preparedStatement.setObject(i+1, values[i]);
		}
		return preparedStatement;
	}
}
