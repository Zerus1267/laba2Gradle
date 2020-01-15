import DAO.MongoDAO;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MongoWriteTest {

    private MongoDAO mongoDAO;
    List<Document> documents;

    @Before
    public void setMongoDAO(){
        mongoDAO = new MongoDAO();
        documents = new ArrayList<>();
        int i = 0;
        do {
            documents.add(new Document("planeName","PlaneForTest" + i).append("planeCapacity", 200+i));
            i++;
        }
        while (i < 100000);
    }

    @Test
    public void insertTest(){
        documents.forEach(document -> mongoDAO.addNewPlane(document.getString("planeName"), document.getInteger("planeCapacity")));
    }

    @After
    public void deletePlanes(){
        System.out.println("enter the AFTER method");
        mongoDAO.getPlaneCollection().deleteMany(Filters.eq("planeName", Pattern.compile("PlaneForTest")));
    }
}
