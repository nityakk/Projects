import java.io.FileInputStream;
import java.sql.*;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * Runs queries against a back-end database.
 * This class is responsible for searching for flights.
 */
public class QuerySearchOnly
{
  // `dbconn.properties` config file
  private String configFilename;
  protected List<Itinerary> itinList = new ArrayList<Itinerary>();

  // DB Connection
  protected Connection conn;
  
  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  protected PreparedStatement checkFlightCapacityStatement;
  
  private static final String CHECK_DIRECT_FLIGHTS = "SELECT TOP (?) F.fid as ID, F.day_of_month as Day, F.carrier_id as Carrier, "
			+ "F.flight_num as Number, F.origin_city as Origin, F.dest_city as Destination, F.actual_time as duration, "
			+ "F.capacity as Capacity, F.price as Price FROM Flights as F WHERE F.origin_city = ? AND F.dest_city = ? " 
			+ "AND F.day_of_month = ? AND F.canceled != 1 ORDER BY F.actual_time, F.fid";
  protected PreparedStatement checkDirectFlightsStatement;
  
  private static final String CHECK_INDIRECT_FLIGHTS = "SELECT TOP (?) F1.fid as ID1, F1.day_of_month as Day1, F1.carrier_id as Carrier1, "
		  + "F1.flight_num as Number1, F1.origin_city as Origin1, F1.dest_city as Destination1, F1.actual_time as Duration1, F1.capacity as Capacity1, "
		  + "F1.price as Price1, F2.fid as ID2, F2.day_of_month as Day2, F2.carrier_id as Carrier2, F2.flight_num as Number2, "
		  + "F2.origin_city as Origin2, F2.dest_city as Destination2, F2.actual_time as Duration2, F2.capacity as Capacity2, F2.price as Price2, "
		  + "(F1.actual_time + F2.actual_time) as Total_time "
		  + "FROM Flights F1, Flights F2 "
		  + "WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? and F1.day_of_month = ? "
		  + "AND F2.day_of_month = F1.day_of_month AND F1.canceled != 1 AND F2.canceled != 1 "
		  + "ORDER BY Total_time, F1.fid, F2.fid";
  protected PreparedStatement checkIndirectFlightsStatement;

  class Flight
  {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    @Override
    public String toString()
    {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId +
              " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
              " Capacity: " + capacity + " Price: " + price;
    }
  }

  public QuerySearchOnly(String configFilename)
  {
    this.configFilename = configFilename;
  }

  /** Open a connection to SQL Server in Microsoft Azure.  */
  public void openConnection() throws Exception
  {
    Properties configProps = new Properties();
    configProps.load(new FileInputStream(configFilename));

    String jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
    String jSQLUrl = configProps.getProperty("flightservice.url");
    String jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
    String jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

    /* load jdbc drivers */
    Class.forName(jSQLDriver).newInstance();

    /* open connections to the flights database */
    conn = DriverManager.getConnection(jSQLUrl, // database
            jSQLUser, // user
            jSQLPassword); // password

    conn.setAutoCommit(true); //by default automatically commit after each statement
    /* In the full Query class, you will also want to appropriately set the transaction's isolation level:
          conn.setTransactionIsolation(...)
       See Connection class's JavaDoc for details.
    */
  }

  public void closeConnection() throws Exception
  {
    conn.close();
  }

  /**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
    checkDirectFlightsStatement = conn.prepareStatement(CHECK_DIRECT_FLIGHTS);
    checkIndirectFlightsStatement = conn.prepareStatement(CHECK_INDIRECT_FLIGHTS);
  }



  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise it searches for direct flights
   * and flights with two "hops." Only searches for up to the number of
   * itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n".
   * If an error occurs, then return "Failed to search\n".
   *
   * Otherwise, the sorted itineraries printed in the following format:
   *
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
   * [first flight in itinerary]\n
   * ...
   * [last flight in itinerary]\n
   *
   * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
   * in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries)
  {
    // Please implement your own (safe) version that uses prepared statements rather than string concatenation.
    // You may use the `Flight` class (defined above).
    // return transaction_search_unsafe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
	  return transaction_search_safe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
  }
  
  /**
   * Same as {@code transaction_search} 
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_safe(String originCity, String destinationCity, boolean directFlight,
          									int dayOfMonth, int numberOfItineraries) {
	  StringBuffer buff = new StringBuffer();
	  //List<Itinerary> itinList = new ArrayList<Itinerary>();
	  
	  if (numberOfItineraries < 1) {
		  return "No flights match your selection\n";
	  }
	  
	  try {
		  //Direct Flights
		  checkDirectFlightsStatement.clearParameters();
		  checkDirectFlightsStatement.setInt(1, numberOfItineraries);
		  checkDirectFlightsStatement.setString(2, originCity);
		  checkDirectFlightsStatement.setString(3, destinationCity);
		  checkDirectFlightsStatement.setInt(4, dayOfMonth);
		  
		  int currItin = 0;
		  ResultSet dResults = checkDirectFlightsStatement.executeQuery();
		  
		  while(dResults.next()) {
			  Flight f = new Flight();
			  f.fid = dResults.getInt("ID");
			  f.dayOfMonth = dResults.getInt("Day");
			  f.carrierId = dResults.getString("Carrier");
			  f.flightNum = dResults.getString("Number");
			  f.originCity = dResults.getString("Origin");
			  f.destCity = dResults.getString("Destination");
			  f.time = dResults.getInt("Duration");
			  f.capacity = dResults.getInt("Capacity");
			  f.price = dResults.getInt("Price");

			  
			  Itinerary dItin = new Itinerary(f);
			  itinList.add(dItin);
			  
			  currItin++;
		  }
		  
		  dResults.close();
		  
		  //Indirect Flights
		  if(!directFlight) {
			  int remainingItin = numberOfItineraries - currItin;
			  
			  checkIndirectFlightsStatement.clearParameters();
			  checkIndirectFlightsStatement.setInt(1, remainingItin);
			  checkIndirectFlightsStatement.setString(2, originCity);
			  checkIndirectFlightsStatement.setString(3, destinationCity);
			  checkIndirectFlightsStatement.setInt(4, dayOfMonth);
			  ResultSet idResults = checkIndirectFlightsStatement.executeQuery();
			  
			  while (idResults.next() && remainingItin > 0) {
				  Flight f1 = new Flight();
				  f1.fid = idResults.getInt("ID1");
				  f1.dayOfMonth = idResults.getInt("Day1");
				  f1.carrierId = idResults.getString("Carrier1");
				  f1.flightNum = idResults.getString("Number1");
				  f1.originCity = idResults.getString("Origin1");
				  f1.destCity = idResults.getString("Destination1");
				  f1.time = idResults.getInt("Duration1");
				  f1.capacity = idResults.getInt("Capacity1");
				  f1.price = idResults.getInt("Price1");
				  
				  Flight f2 = new Flight();
				  f2.fid = idResults.getInt("ID2");
				  f2.dayOfMonth = idResults.getInt("Day2");
				  f2.carrierId = idResults.getString("Carrier2");
				  f2.flightNum = idResults.getString("Number2");
				  f2.originCity = idResults.getString("Origin2");
				  f2.destCity = idResults.getString("Destination2");
				  f2.time = idResults.getInt("Duration2");
				  f2.capacity = idResults.getInt("Capacity2");
				  f2.price = idResults.getInt("Price2");
				  
				  Itinerary idItin = new Itinerary(f1, f2);
				  itinList.add(idItin);
			  }
			  
			  idResults.close();
		  }
	  } catch (SQLException e) {
		  return "Failed to search\n";
	  }
	  
	  if(itinList.size() == 0) {
		  return "No flights match your selection\n";
	  }
	  
	  Collections.sort(itinList);
	  
	  int currItinId = 0;
	  for(Itinerary itin: itinList) {
		  itin.id = currItinId;
		  buff.append(itin.toString());
		  currItinId++;
	  }

	  return buff.toString();
  }
  
  public List<Itinerary> getItinList () {
	  return itinList;
  }

  /**
   * Same as {@code transaction_search} except that it only performs single hop search and
   * do it in an unsafe manner.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight,
                                          int dayOfMonth, int numberOfItineraries)
  {
    StringBuffer sb = new StringBuffer();

    try
    {
      // one hop itineraries
      String unsafeSearchSQL =
              "SELECT TOP (" + numberOfItineraries + ") day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
                      + "FROM Flights "
                      + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
                      + "ORDER BY actual_time ASC";

      Statement searchStatement = conn.createStatement();
      ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);

      while (oneHopResults.next())
      {
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        sb.append("Day: ").append(result_dayOfMonth)
                .append(" Carrier: ").append(result_carrierId)
                .append(" Number: ").append(result_flightNum)
                .append(" Origin: ").append(result_originCity)
                .append(" Destination: ").append(result_destCity)
                .append(" Duration: ").append(result_time)
                .append(" Capacity: ").append(result_capacity)
                .append(" Price: ").append(result_price)
                .append('\n');
      }
      oneHopResults.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments.
   * You don't need to use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }
  
  class Itinerary implements Comparable<Itinerary>{
	  public Flight f1;
	  public Flight f2;
	  public int id;
	  public int duration;
	  
	  public Itinerary(Flight f1) {
		  this(f1, null);
	  }
	  
	  public Itinerary(Flight f1, Flight f2) {
		  this.f1 = f1;
		  this.f2 = f2;
		  if(f2 != null) {
			  this.duration = f1.time + f2.time;
		  } else {
			  this.duration = f1.time;
		  }
	  }
	  
	  @Override
	  public int compareTo(Itinerary other) {
		  if(this.duration != other.duration) {
			  return this.duration - other.duration;
		  } else if (this.f1.fid != other.f1.fid) {
			  return this.f1.fid - other.f1.fid;
		  } else {
//			  try {
//				  return this.f2.fid - other.f2.fid;
//			  } catch (Exception e) {
//				  return 0;
//			  }
			  return this.f2.fid - other.f2.fid;
		  }
	  }
	  
	  @Override
	  public String toString() {
		  int f2IsNull = 1;
		  if(f2 != null) {
			  f2IsNull = 2;
		  }
		  
		  String itinString = "Itinerary " + this.id + ": " + f2IsNull + " flight(s), " + this.duration + " minutes\n";
		  itinString += f1.toString() + "\n";
		  if (f2IsNull == 2) { //not null
			  itinString += f2.toString() + "\n";
		  }
		  return itinString;
	  }
  }
}
