package DAO;

import java.io.IOException;
import java.sql.SQLException;

public interface IMyDAO {
    public int getTripsByCountry(String country);
    public void addNewPlaneFunc(String name, int capacity, String company) throws Exception;
    public void deletePlaneByName(String plane_name) throws SQLException;
    public void updateCompanyPlaneByNames(String companyName, String planeName) throws SQLException;
    public void addNewTrip(String countryIn, String countryOut, int minDuration, int minPrice, String companyName) throws SQLException, IOException;
}
