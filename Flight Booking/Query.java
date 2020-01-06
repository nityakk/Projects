import java.sql.*;
import java.util.List;

//import QuerySearchOnly.Itinerary;

import java.util.ArrayList;

public class Query extends QuerySearchOnly {
	
	protected List<Itinerary> itineraries = super.getItinList();// = new ArrayList<Itinerary>();
	
	
	// Logged In User
	private String username = null; // customer username is unique

	// transactions
	private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
	protected PreparedStatement beginTransactionStatement;

	private static final String COMMIT_SQL = "COMMIT TRANSACTION";
	protected PreparedStatement commitTransactionStatement;

	private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
	protected PreparedStatement rollbackTransactionStatement;
	
	private static final String SEARCH_USER_SQL = "SELECT U.password as password FROM Users as U WHERE U.username = ? AND U.password = ?;";
	  private PreparedStatement searchUserStatement;
	
	private static final String USER_EXISTS_SQL = "SELECT count(*) as count FROM Users as U WHERE U.username = ?;";// AND U.password = ?;";
	protected PreparedStatement userExistsStatement;	
	
	private static final String ADD_USER_SQL = "INSERT INTO Users Values(?, ?, ?);";
	protected PreparedStatement addUserStatement;
	
	private static final String RESERVATION_EXISTS_SQL = "SELECT R.day FROM Reservations as R WHERE R.username = ? AND R.day = ?;";
	protected PreparedStatement reservationExistsStatement;
	
	private static final String GET_RESERVATION_ID_SQL = "SELECT rid FROM Ids;";
	protected PreparedStatement getReservationIdStatement;
	
	private static final String SPOT_AVAILABILITY_SQL = "SELECT COUNT(*) as spot_availability FROM Reservations as R WHERE R.fid1 = ? OR R.fid2 = ?;";
	protected PreparedStatement spotAvailabilityStatement;
	
	private static final String ADD_RESERVATION_SQL = "INSERT INTO Reservations Values(?, ?, ?, ?, ?, ?, ?);";
	protected PreparedStatement addReservationStatement;
	
	private static final String UPDATE_RESERVATION_ID_SQL = "DELETE FROM Ids; INSERT INTO Ids Values(?);";
	protected PreparedStatement updateReservationIdStatement;
	
	private static final String CLEAR_TABLE_SQL = "DELETE FROM Reservations; DELETE FROM Users; DELETE FROM Ids;"; 
	protected PreparedStatement clearTableStatement;
	
	private static final String FIND_RID_USERNAME_SQL = "SELECT * FROM Reservations WHERE username = ? AND rid = ? AND paid = ?;";
	protected PreparedStatement findRIDUsernameStatement;
	
	private static final String FIND_USER_BALANCE_SQL= "SELECT U.balance FROM Users as U WHERE U.username = ?;";
	protected PreparedStatement findUserBalanceStatement;
	
	private static final String UPDATE_USER_BALANCE_SQL = "UPDATE Users SET balance = ? WHERE username = ?;";
	protected PreparedStatement updateUserBalanceStatement;
	
	private static final String UPDATE_PAID_SQL = "UPDATE Reservations SET paid = ? WHERE username = ? AND rid = ?;";
	protected PreparedStatement updatePaidStatement;
	
	private static final String FIND_RESERVATIONS_SQL = "SELECT * FROM Reservations WHERE username = ?;";
	private PreparedStatement findReservationsStatement;
	
	private static final String FIND_FLIGHT_SQL = "SELECT F.day_of_month as Day, F.carrier_id as Carrier, "
			+ "F.flight_num as Number, F.fid as fid, F.origin_city as Origin, F.dest_city as Destination, "
	  		+ "F.actual_time as Duration, F.capacity as Capacity, F.price as Price FROM FLIGHTS as F WHERE F.fid = ?";
	private PreparedStatement findFlightStatement;
	
	private static final String RESERVATION_PRICE_INFO_SQL = "SELECT R.price, R.paid FROM Reservations as R WHERE R.username = ? AND R.rid = ?;";
	private PreparedStatement reservationPriceInfoStatement;
	  
	private static final String CANCEL_RESERVATION_SQL = "DELETE FROM Reservations WHERE rid = ?;";
	private PreparedStatement cancelReservationStatement;
	
	public Query(String configFilename) {
		super(configFilename);
	}


	/**
	 * Clear the data in any custom tables created. Do not drop any tables and do not
	 * clear the flights table. You should clear any tables you use to store reservations
	 * and reset the next reservation ID to be 1.
	 */
	public void clearTables ()
	{
		try {
			clearTableStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * prepare all the SQL statements in this method.
	 * "preparing" a statement is almost like compiling it.
	 * Note that the parameters (with ?) are still not filled in
	 */
	@Override
	public void prepareStatements() throws Exception
	{
		super.prepareStatements();
		beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
		commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
		rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

		/* add here more prepare statements for all the other queries you need */
		searchUserStatement = conn.prepareStatement(SEARCH_USER_SQL);
		userExistsStatement = conn.prepareStatement(USER_EXISTS_SQL);
		addUserStatement = conn.prepareStatement(ADD_USER_SQL);
		reservationExistsStatement = conn.prepareStatement(RESERVATION_EXISTS_SQL);
		getReservationIdStatement = conn.prepareStatement(GET_RESERVATION_ID_SQL);
		spotAvailabilityStatement = conn.prepareStatement(SPOT_AVAILABILITY_SQL);
		addReservationStatement = conn.prepareStatement(ADD_RESERVATION_SQL);
		updateReservationIdStatement = conn.prepareStatement(UPDATE_RESERVATION_ID_SQL);
		clearTableStatement = conn.prepareStatement(CLEAR_TABLE_SQL);
		findRIDUsernameStatement = conn.prepareStatement(FIND_RID_USERNAME_SQL);
		findUserBalanceStatement = conn.prepareStatement(FIND_USER_BALANCE_SQL);
		updateUserBalanceStatement = conn.prepareStatement(UPDATE_USER_BALANCE_SQL);
		updatePaidStatement = conn.prepareStatement(UPDATE_PAID_SQL);
		findReservationsStatement = conn.prepareStatement(FIND_RESERVATIONS_SQL);
		findFlightStatement = conn.prepareStatement(FIND_FLIGHT_SQL);
		reservationPriceInfoStatement = conn.prepareStatement(RESERVATION_PRICE_INFO_SQL);
		cancelReservationStatement = conn.prepareStatement(CANCEL_RESERVATION_SQL);
	}


	/**
	 * Takes a user's username and password and attempts to log the user in.
	 *
	 * @return If someone has already logged in, then return "User already logged in\n"
	 * For all other errors, return "Login failed\n".
	 *
	 * Otherwise, return "Logged in as [username]\n".
	 */
	public String transaction_login(String username, String password)
	{
		if (this.username != null){
			return "User already logged in\n";
		}
		try {
			
			beginTransaction();
			searchUserStatement.clearParameters();
			searchUserStatement.setString(1, username);
			searchUserStatement.setString(2, password);
		    ResultSet existsResult = searchUserStatement.executeQuery();
		    
		    if (!existsResult.next()) {
		    	rollbackTransaction();
		    	return "Login failed\n";
		    }
		    
		    if(existsResult.getString("password").equals(password)) {
		    	existsResult.close();
			    commitTransaction();
			    this.username = username;
				itineraries = super.getItinList();
		        return "Logged in as " + username + "\n";
		    } else {
		    	return "Login failed\n";
		    }
		} catch (SQLException e) {
			return "Login failed\n";
		}
		
	}

	/**
	 * Implement the create user function.
	 *
	 * @param username new user's username. User names are unique the system.
	 * @param password new user's password.
	 * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
	 *
	 * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
	 */
	public String transaction_createCustomer (String username, String password, int initAmount)
	{
		if (initAmount >= 0){
			try{
				beginTransaction();
				userExistsStatement.clearParameters();
				userExistsStatement.setString(1, username);
		        ResultSet existsResult = userExistsStatement.executeQuery();
		        existsResult.next();

		        if (existsResult.getInt("count") == 0){
		        	addUserStatement.clearParameters();
			        addUserStatement.setString(1, username);
			        addUserStatement.setString(2, password);
			        addUserStatement.setInt(3, initAmount);
			        addUserStatement.executeUpdate();
			        commitTransaction();
			        return "Created user " + username + "\n";
		        }
		        
		        existsResult.close();
	        	rollbackTransaction();
	        	return "Failed to create user\n";
		    } catch (SQLException e) {
		    	//e.printStackTrace();
		    	return "Failed to create user\n";
		    }
		}
		return "Failed to create user\n";
	}

	/**
	 * Implements the book itinerary function.
	 *
	 * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
	 *
	 * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
	 * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
	 * If the user already has a reservation on the same day as the one that they are trying to book now, then return
	 * "You cannot book two flights in the same day\n".
	 * For all other errors, return "Booking failed\n".
	 *
	 * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
	 * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
	 * successful reservation is made by any user in the system.
	 */
	public String transaction_book(int itineraryId)
	{
		itineraries = super.getItinList();
		//return "Booking failed\n";
		if (username == null) {
			return "Cannot book reservations, not logged in\n";
		}
		
		if (itineraryId < 0 || itineraries == null || itineraryId >= itineraries.size()) {
			return "No such itinerary " + itineraryId + "\n";
		}
		
		if (itineraries.get(itineraryId) == null) {
			return "Booking failed\n"; 
		}
		
		int nextId;
		try {
			Itinerary itin = itineraries.get(itineraryId);
			int day = itin.f1.dayOfMonth;
			int fid1 = itin.f1.fid;
			
			beginTransaction();
			//check if reservation on specified name under user's name
			reservationExistsStatement.clearParameters();
			reservationExistsStatement.setString(1, this.username);
			reservationExistsStatement.setInt(2, day);
			
			ResultSet transacResults = reservationExistsStatement.executeQuery();
			while (transacResults.next()) {
				int curr = transacResults.getInt("fid1");
				if(fid1 == curr) {
					transacResults.close();
					rollbackTransaction();
					return "You cannot book two flights in the same day\n";
				}
			}
			transacResults.close();
			
			ResultSet reserveIds = getReservationIdStatement.executeQuery();
			if (reserveIds.next()) {
				nextId = reserveIds.getInt("rid");
			} else {
				nextId = 0; //1?
			}
			reserveIds.close();
			
			
			spotAvailabilityStatement.clearParameters();
			spotAvailabilityStatement.setInt(1, fid1);
			spotAvailabilityStatement.setInt(2, fid1);
			
			ResultSet spotResults = spotAvailabilityStatement.executeQuery();
			spotResults.next();
			int takenSpot = spotResults.getInt("spot_availability");
			int takenSpot2 = 0;
			spotResults.close();
			
			if (itin.f2 != null) {
				
				spotAvailabilityStatement.clearParameters();
				
				spotAvailabilityStatement.clearParameters();
				spotAvailabilityStatement.setInt(1, itin.f2.fid);
				spotAvailabilityStatement.setInt(2, itin.f2.fid);
				
				ResultSet spotResults2 = spotAvailabilityStatement.executeQuery();
				spotResults2.next();
				takenSpot2 = spotResults2.getInt("spot_availability");
				spotResults2.close();
			}
			
			
			if (takenSpot >= itin.f1.capacity) {
				rollbackTransaction();
				return "Booking failed\n";
			}
			
			if(itin.f2 != null && takenSpot2 >= itin.f2.capacity) {
				rollbackTransaction();
				return "Booking failed\n";
			}
			nextId = nextId + 1;
			
			updateReservationIdStatement.clearParameters();
			updateReservationIdStatement.setInt(1, nextId);
			updateReservationIdStatement.executeUpdate();
			
			//add new reservation
			addReservationStatement.clearParameters();
			addReservationStatement.setInt(1, nextId);
			addReservationStatement.setString(2, this.username);
			
			addReservationStatement.setInt(4, day);
			
			addReservationStatement.setInt(5, 0);
			addReservationStatement.setInt(6, fid1);
			
			int price = itin.f1.price;
			if(itin.f2 != null) {
				int fid2 = itin.f2.fid;
				addReservationStatement.setInt(7, fid2);
				price += itin.f2.price;
			} else {
				addReservationStatement.setNull(7, 0);
			}
			
			addReservationStatement.setInt(3, price);
			addReservationStatement.executeUpdate();
			
			commitTransaction();
			
			return "Booked flight(s), reservation ID: " + nextId + "\n";
		} catch (SQLException e) {
			try {
				rollbackTransaction();			
				return "Booking failed\n";
			} catch (SQLException e2) {
				e2.printStackTrace();
				return transaction_book(itineraryId);
			}
		}
	}
		

	/**
	 * Implements the pay function.
	 *
	 * @param reservationId the reservation to pay for../runTests.sh . tmp cases
	 *
	 * @return If no user has logged in, then return "Cannot pay, not logged in\n"
	 * If the reservation is not found / not under the logged in user's name, then return
	 * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
	 * If the user does not have enough money in their account, then return
	 * "User has only [balance] in account but itinerary costs [cost]\n"
	 * For all other errors, return "Failed to pay for reservation [reservationId]\n"
	 *
	 * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
	 * where [balance] is the remaining balance in the user's account.
	 */
	public String transaction_pay (int reservationId)
	{
		if (this.username == null){
			return "Cannot pay, not logged in\n";
		}

		try{
			beginTransaction();
			findRIDUsernameStatement.clearParameters();
			findRIDUsernameStatement.setString(1, this.username);
			findRIDUsernameStatement.setInt(2, reservationId);
			findRIDUsernameStatement.setInt(3, 0);
			ResultSet reserveResult = findRIDUsernameStatement.executeQuery();

			int price = 0;

		    if (!reserveResult.next()){
		    	rollbackTransaction();
		        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
		    }
		    
	    	int paid = reserveResult.getInt("paid");
	    	
	        if (paid == 1){
	        	reserveResult.close();
	        	rollbackTransaction();
	        	return "Failed to pay for reservation " + reservationId + "\n";
	        }
	        
	        price = reserveResult.getInt("price");
	        reserveResult.close();
	        findUserBalanceStatement.clearParameters();
		    findUserBalanceStatement.setString(1, this.username);
	        ResultSet balanceResults = findUserBalanceStatement.executeQuery();
	        int balance = 0;
	        
	        if(balanceResults.next()) {
	        	balance = balanceResults.getInt("balance");
		        
		        if (price > balance){
		        	balanceResults.close();
		        	rollbackTransaction();
		        	return "User has only " + balance + " in account but itinerary costs " + price + "\n";
		        }
		        
		        balance = balance - price;
		        updateUserBalanceStatement.clearParameters();
		        updateUserBalanceStatement.setInt(1, balance);
		        updateUserBalanceStatement.setString(2, this.username);
		        updateUserBalanceStatement.executeUpdate();
		        
		        updatePaidStatement.clearParameters();
		        updatePaidStatement.setInt(1, 1);
		        updatePaidStatement.setString(2, this.username);
		        updatePaidStatement.setInt(3, reservationId);
		        updatePaidStatement.executeUpdate();
	        }
	        commitTransaction();
	        return "Paid reservation: " + reservationId + " remaining balance: " + balance + "\n";
	    } catch (SQLException e) { 
	    	e.printStackTrace(); 
	    	return "Failed to pay for reservation " + reservationId + "\n";
    	}
	}

	/**
	 * Implements the reservations function.
	 *
	 * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
	 * If the user has no reservations, then return "No reservations found\n"
	 * For all other errors, return "Failed to retrieve reservations\n"
	 *
	 * Otherwise return the reservations in the following format:
	 *
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * ...
	 *
	 * Each flight should be printed using the same format as in the {@code Flight} class.
	 *
	 * @see Flight#toString()
	 */
	public String transaction_reservations()
	{
		if (username == null) {
			return "Cannot view reservations, not logged in\n";
		}
		try { 
			findReservationsStatement.clearParameters();
			findReservationsStatement.setString(1, username);
			ResultSet reserveResult = findReservationsStatement.executeQuery();
			StringBuffer buff = new StringBuffer();
			  		  
			// print out the information of reserved flights
			boolean resultIsEmpty = true;
			while (reserveResult.next()) {
				resultIsEmpty = false;
				int rid = reserveResult.getInt("rid");
				String paid = "";
				if (reserveResult.getInt("paid") == 1){
	            	paid = "true";
	            } else {
	            	paid = "false";
	            }
				buff.append("Reservation " + rid + " paid: " + paid + ":\n");
				
				
				findFlightStatement.clearParameters();
				findFlightStatement.setInt(1, reserveResult.getInt("fid1"));
				ResultSet reserveInfo = findFlightStatement.executeQuery();
				
				if (reserveInfo.next()) {
					Flight flight = flightHelper(reserveInfo);
					buff.append(flight.toString() + "\n");
					if (reserveResult.getInt("fid2") != 0) {
						findFlightStatement.clearParameters();
						findFlightStatement.setInt(1, reserveResult.getInt("fid1"));
						ResultSet reserveInfo2 = findFlightStatement.executeQuery();
						reserveInfo2.next();
						Flight flight2 = flightHelper(reserveInfo);
						buff.append(flight2.toString() + "\n");
					}
				}
			}
			  
			if (resultIsEmpty) {
				return "No reservations found\n";
			}
			
			return buff.toString();  
		} catch (SQLException e) { 
			e.printStackTrace();
			return "Failed to retrieve reservations\n";
		}
	}
	
	private Flight flightHelper (ResultSet results) {
		  Flight flight = new Flight();
		  try {
			  flight.capacity = results.getInt("capacity");
			  flight.carrierId = results.getString("Carrier");
			  flight.dayOfMonth = results.getInt("Day");
			  flight.destCity = results.getString("Destination");
			  flight.fid = results.getInt("fid");
			  flight.flightNum = results.getString("Number");
			  flight.originCity = results.getString("Origin");
			  flight.price = results.getInt("Price");
			  flight.time = results.getInt("Duration");
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
		  return flight;
	  }

	/**
	 * Implements the cancel operation.
	 *
	 * @param reservationId the reservation ID to cancel
	 *
	 * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
	 * For all other errors, return "Failed to cancel reservation [reservationId]"
	 *
	 * If successful, return "Canceled reservation [reservationId]"
	 *
	 * Even though a reservation has been canceled, its ID should not be reused by the system.
	 */
	public String transaction_cancel(int reservationId)
	{
	
		if (this.username == null) {
			return "Cannot cancel reservations, not logged in\n";
		}
		try {
    		beginTransaction();
    		
    		reservationPriceInfoStatement.clearParameters();
    		reservationPriceInfoStatement.setString(1, this.username);
    		reservationPriceInfoStatement.setInt(2, reservationId);
    		ResultSet cancelResults = reservationPriceInfoStatement.executeQuery();
    		if (!cancelResults.next()) {
    			commitTransaction();
    			return "Failed to cancel reservation " + reservationId + "\n";
    		}
    		
    		int paid = cancelResults.getInt("paid");
    		
    		if (paid == 1) {
    			findUserBalanceStatement.clearParameters();
    			findUserBalanceStatement.setString(1, this.username);
				ResultSet balanceResults = findUserBalanceStatement.executeQuery();
				balanceResults.next();
				
				int balance = balanceResults.getInt("balance");
				int price = cancelResults.getInt("price");
				balanceResults.close();
				
				// refund
				updateUserBalanceStatement.clearParameters();
				updateUserBalanceStatement.setInt(1, balance + price);
				updateUserBalanceStatement.setString(2, this.username);
				updateUserBalanceStatement.executeUpdate();
    		}
    		
    		cancelResults.close();	
    		
    		// delete reservation from table
    		cancelReservationStatement.clearParameters();
    		cancelReservationStatement.setInt(1, reservationId);
    		cancelReservationStatement.executeUpdate();
    		commitTransaction();
    		return "Canceled reservation " + reservationId + "\n";	   		
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Failed to cancel reservation " + reservationId + "\n";    
    	}
	}


	/* some utility functions below */

	public void beginTransaction() throws SQLException
	{
		conn.setAutoCommit(false);
		beginTransactionStatement.executeUpdate();
	}

	public void commitTransaction() throws SQLException
	{
		commitTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}

	public void rollbackTransaction() throws SQLException
	{
		rollbackTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}
}
