package DAO;

import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Filters.size;

public class MongoDAO implements IMyDAO {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> planeCollection;
    private MongoCollection<Document> tripCollection;
    private MongoCollection<Document> companyCollection;
    private MongoCollection<Document> countryCollection;

    public MongoDAO(){
        mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        mongoDatabase = mongoClient.getDatabase("airtrans");
        planeCollection = mongoDatabase.getCollection("plane");
        tripCollection = mongoDatabase.getCollection("trip");
        companyCollection = mongoDatabase.getCollection("company");
        countryCollection = mongoDatabase.getCollection("country");
    }

    public void testRead(){
        //List<Document> results = new ArrayList<>();
        Document doc = planeCollection.find(eq("plane_modification", "PH-XLK")).first();
        System.out.println(doc.getObjectId("_id"));
    }

    @Override
    public int getTripsByCountry(String country) {
        Bson queryFilter = Filters.all("CountryOut.CountryName", country);
        List<Document> documents = new ArrayList<>();
        tripCollection.find(queryFilter).iterator().forEachRemaining(documents::add);
        System.out.println("List size " + documents.size());
        return documents.size();
    }

    @Override
    public void addNewPlaneFunc(String name, int capacity, String company) throws Exception {

        //TODO> Add plane and determine the company of the plane, if company doesn't exist. If company exist add the plane into the plane field of company.

        if(!hasPlane(name)){
            //planeCollection.insertOne(new Document().append("planeName", name).append("planeCapacity", capacity));
            if(hasCompany(company)){
                addNewPlane(name,capacity)  ;
                companyCollection.updateOne(eq("companyName", company), new Document("$addToSet", new Document("companyPlane",new Document("planeName", name))));
            }
            else {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter the company`s country please!");
                String countryName = reader.readLine();
                if(countryName == "") countryName = "TestCountry";
                addNewCompany(company, countryName, name);
                addNewPlane(name, capacity);
            }
        }
        else{
            System.out.println("This plane already exists!");
        }
    }

    public void addNewPlane(String name, int capacity) {
        Document document = new Document("planeName", name);
        document.append("planeCapacity", capacity);
        planeCollection.insertOne(document);
    }

    private void addNewCompany(String companyName, String countryName, String planeName) {
        if(!hasCountry(countryName)){
            addNewCountry(countryName);
        }
        companyCollection.insertOne(Document.parse("{companyName:'"+ companyName + "', companyCountry:{countryName:'" + countryName + "'}, companyPlane:[{planeName:'" + planeName + "'}]}"));
    }

    public void addNewCountry(String countryName) {
        Document document = new Document();
        document.append("countryName", countryName);
        countryCollection.insertOne(document);
    }

    private boolean hasCountry(String countryName){
        long count = countryCollection.count(eq("countryName", countryName));
        if(count == 0) return false;
        else return true;
    }

    private boolean hasPlane(String planeName){
        Document document = planeCollection.find(eq("planeName", planeName)).first();
        if(document != null) return true;
        else return false;
    }

    private boolean hasCompany(String companyName){
        long count = companyCollection.count(eq("companyName", companyName));
        if(count == 0) return false;
        else return true;
    }

    @Override
    public void deletePlaneByName(String plane_name) throws SQLException {

        try {
            long existingCount = planeCollection.count(eq("planeName", plane_name));
            if(existingCount != 1){
                throw new Exception("Not appropriate count of founded rows, actual= " + existingCount + " but should be 1");
            }
            else{
                companyCollection.findOneAndUpdate(Filters.elemMatch("companyPlane", eq("planeName", plane_name)), new Document("$pull", new Document("companyPlane", new Document("planeName", plane_name))));
            }

            System.out.println("Successfully deleted the plane: " + plane_name);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Something get wrog while deleting the plane: " + plane_name);
        }
    }

    public int getPlaneCollectionSize(){
        return (int) planeCollection.countDocuments();
    }

    public int getCompanyCollectionSize(){
        return (int) companyCollection.countDocuments();
    }

    public int getCompanyPlaneCount(String companyName){
        int res = -1;
        Document projectFields = new Document("_id", 0);
        projectFields.put("count", new BasicDBObject( "$size", "$companyPlane" ));
        AggregateIterable<Document> document = companyCollection.aggregate(Arrays.asList(Aggregates.match(new Document("companyName", companyName)), Aggregates.project(projectFields)));
        for(Document document1 : document){
            res = document1.getInteger("count");
        }
        return res;
    }

    public MongoCollection<Document> getPlaneCollection() {
        return planeCollection;
    }

    public MongoCollection<Document> getTripCollection() {
        return tripCollection;
    }

    public MongoCollection<Document> getCompanyCollection() {
        return companyCollection;
    }

    public MongoCollection<Document> getCountryCollection() {
        return countryCollection;
    }


    @Override
    public void updateCompanyPlaneByNames(String companyName, String planeName) throws SQLException {

    }

    @Override
    public void addNewTrip(String countryIn, String countryOut, int minDuration, int minPrice, String companyName) throws SQLException, IOException {

    }
}
