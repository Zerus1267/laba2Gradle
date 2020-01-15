package DAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class DAO implements IMyDAO {
    private String conString = "jdbc:mysql://localhost:3306/airtrans";
    private String user = "skrekoza";
    private String password = "12345678";
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public DAO(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(conString,user,password);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getTripsByCountry(String country){
        int i = 0;
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("select t.trip_id, c1.country_name as country_out, c2.country_name as country_in, t.trip_min_duration, t.trip_min_price from trip t" +
                    "    join country c1 on t.trip_country_out = c1.country_id" +
                    "    join country c2 on t.trip_country_in = c2.country_id" +
                    "    where c1.country_name = ?");
            preparedStatement.setString(1, country);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                i++;
                //System.out.println("Country out: " + resultSet.getString("country_out") + "; " + "Country in: " + resultSet.getString("country_in") + "; " + "Price: " + resultSet.getInt("trip_min_price") + "; " + "Duration: " + resultSet.getInt("trip_min_duration"));
                //System.out.println(resultSet.getInt("trip_id") + resultSet.getString("country_out") + resultSet.getString("country_in") + resultSet.getInt("trip_min_duration") + resultSet.getInt("trip_min_price"))
            }
            //return resultSet.getFetchSize();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return i;
    }

    public void addNewPlaneFunc(String name, int capacity, String company) throws Exception {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        if(hasCompany(company)){
            addNewPlane(name,capacity,getCompanyId(company));
        }
        else{
            System.out.println("Enter country name of your new company");
            String country = reader.readLine();
            addNewCompanyFunc(company,country);
            addNewPlane(name,capacity,getCompanyId(company));
        }
    }

    private int addNewCompanyFunc(String company, String country) throws SQLException {
        if(hasCountry(country)){
            return addNewCompany(company, country);
        }
        else{
            addNewCountry(country);
            return addNewCompany(company,country);
        }
    }

    private void addNewCountry(String country) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into country (country_name) VALUE (?)");
        preparedStatement.setString(1,country);
        preparedStatement.execute();
    }

    private int addNewCompany(String company, String country) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into company (company_name, company_country_id) VALUE (?,?)",Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1,company);
        PreparedStatement preparedStatement1 = connection.prepareStatement("select country_id from country where country_name = ?");
        preparedStatement1.setString(1, country);

        connection.setAutoCommit(false);
        try {
            ResultSet resultSet = preparedStatement1.executeQuery();
            System.out.println(resultSet.next());
            int countryId = resultSet.getInt(1);
            preparedStatement.setInt(2, countryId);
            preparedStatement.executeUpdate();
            connection.commit();
            ResultSet RS = preparedStatement.getGeneratedKeys();
            if (RS.next()) {
                return RS.getInt(1);
            } else return 0;
        }
        catch (SQLException e){
            connection.rollback();
        }
        finally {
            connection.setAutoCommit(true);
        }
        return 0;
    }

    private boolean hasCountry(String country) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select * from country where country_name = ?");
        preparedStatement.setString(1,country);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    private boolean hasCompany(String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select * from company where company_name = ?");
        preparedStatement.setString(1,name);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    private int getCompanyId(String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select company_id from company where company_name = ?");
        preparedStatement.setString(1,name);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public void addNewPlane(String name, int capacity, int company_id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into plane (plane_modification, plane_capacity, plane_company_id) VALUE (?,?,?)");
        preparedStatement.setString(1,name);
        preparedStatement.setInt(2,capacity);
        preparedStatement.setInt(3,company_id);
        connection.setAutoCommit(false);
        try {
            preparedStatement.execute();
            //System.out.println("Successful adding new plane");
            connection.commit();
        }
        catch (SQLException e){
            //System.out.println("Couldn't insert new plane. Partially");
            connection.rollback();
            e.printStackTrace();
        }
        finally {
            connection.setAutoCommit(true);
        }
    }

    private int getPlaneId(String plane_name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select plane_id from plane where plane_modification = ?");
        preparedStatement.setString(1,plane_name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getInt(1);
        }
        else return 0;
    }

    private int getCountryId(String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select country_id from country where country_name = ?");
        preparedStatement.setString(1,name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getInt(1);
        }
        else return 0;
    }

    public void deletePlaneByName(String plane_name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("delete from plane where plane_modification = ?");
        preparedStatement.setString(1,plane_name);
        connection.setAutoCommit(false);
        try{
            preparedStatement.execute();
            connection.commit();
            System.out.println("Successful delete");
        }
        catch (SQLException e){
            System.out.println("Something went wrong");
            connection.rollback();
            e.printStackTrace();
        }
        finally {
            connection.setAutoCommit(true);
        }
    }

    public void updateCompanyPlaneByNames(String company_name, String plane_name) throws SQLException {
        if(this.hasCompany(company_name)){
            connection.setAutoCommit(false);
            int company_id = this.getCompanyId(company_name);
            int plane_id = this.getPlaneId(plane_name);
            if(plane_id != 0){
                PreparedStatement preparedStatement = connection.prepareStatement("update plane set plane_company_id = ? where plane_id = ?");
                int i = 1;
                preparedStatement.setInt(i++,company_id);
                preparedStatement.setInt(i,plane_id);
                try{
                    preparedStatement.execute();
                    connection.commit();
                    System.out.println("Successfully updated plane's company");
                }
                catch (SQLException e){
                    connection.rollback();
                    e.printStackTrace();
                }
                finally {
                    connection.setAutoCommit(true);
                }
            }
            else {
                System.out.println("Entered plane's name isn't in a database");
                connection.setAutoCommit(true);
            }
        }
        else System.out.println("Entered Company doesn't exist!");
    }

    public void addNewTrip(String countryIn, String countryOut, int minDuration, int minPrice, String company) throws SQLException, IOException {
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        connection.setAutoCommit(false);
        if(this.hasCountry(countryIn)){
            if(this.hasCountry(countryOut)){
                if(this.hasCompany(company)){
                    int compID = this.getCompanyId(company);
                    addNewTripFunc(countryIn,countryOut,minDuration,minPrice,compID);
                }
                else{
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(System.in));
                    System.out.println("Enter the company's country please:");
                    String compCountry = reader.readLine();
                    int compID = addNewCompanyFunc(company,compCountry);
                    System.out.println("Generated id for company:" + compID);
                    addNewTripFunc(countryIn,countryOut,minDuration,minPrice, compID);
                }
            }
            else{
                this.addNewCountry(countryOut);
                this.addNewTrip(countryIn,countryOut,minDuration,minPrice,company);
            }
        }
        else{
            this.addNewCountry(countryIn);
            this.addNewTrip(countryIn,countryOut,minDuration,minPrice,company);
        }
    }
    private void addNewTripFunc(String countryIn, String countryOut, int minDuration, int minPrice, int companyId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into trip(trip_country_out, trip_country_in, trip_min_duration, trip_min_price) value(?,?,?,?);",
                Statement.RETURN_GENERATED_KEYS);
        int i = 1;
        connection.setAutoCommit(false);
        preparedStatement.setInt(i++,this.getCountryId(countryOut));
        preparedStatement.setInt(i++,this.getCountryId(countryIn));
        preparedStatement.setInt(i++,minDuration);
        preparedStatement.setInt(i,minPrice);
        try {
            preparedStatement.executeUpdate();
            ResultSet RS = preparedStatement.getGeneratedKeys();
            int tripID = 0;
            if (RS.next()) {
                tripID = RS.getInt(1);
            }
            PreparedStatement preparedStatement1 = connection.prepareStatement("insert into company_trip value (?,?)");
            preparedStatement1.setInt(1, companyId);
            preparedStatement1.setInt(2, tripID);
            preparedStatement1.executeUpdate();
            connection.commit();
        }
        catch (SQLException e){
            e.printStackTrace();
            connection.rollback();
        }
        finally {
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }
    }
}
