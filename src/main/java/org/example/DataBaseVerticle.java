
package org.example;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import java.util.ArrayList;
import java.util.List;
import static io.vertx.mysqlclient.MySQLPool.pool;

public class DataBaseVerticle extends AbstractVerticle{
    private Router router;
    //private final MySQLPool connection;
    DataBaseVerticle(Router router){
        this.router = router;
    }
    MySQLPool conn;


    // get the connection with the database
    private MySQLPool initDB() {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("Sajeewa")
                .setUser("root")
                .setPassword("St1093203*");

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        MySQLPool pool = pool(vertx, connectOptions, poolOptions);
        return pool;
    }


     //create data  in DataBaseVerticle
    public Future<Integer> postClientData(JsonObject data) {
        List<Tuple> batch = new ArrayList<>();
        batch.add(Tuple.of(data.getString("CustomerName"),data.getString("Age"),data.getString("Address")));
        return conn.preparedQuery("INSERT INTO Sample.Customers(CustomerName, Age, Address) VALUES (?, ?, ?);")
                .executeBatch(batch)
                .map(SqlResult::rowCount);
    }



    //get details from dataBase
    public Future getAllDetails() {

        return conn.query("SELECT * FROM Sample.Customers")
                .execute();


    }



    private <T> void allDetails(Message<T> msg){
        this.getAllDetails()
                .onSuccess(result ->{
                    System.out.println("Done!");
                    EventBus evBus = vertx.eventBus();
                    msg.reply(Json.encodePrettily(result));
                })
                .onFailure(
                        throwable -> msg.reply(throwable.toString())
                );
    }
    private <T> void postDetails(Message<T> msg){
        var body = msg.body();
        JsonObject jsonBody = (JsonObject) body;
        this.postClientData(jsonBody)
                .onSuccess(result->{
                    msg.reply(Json.encodePrettily(result));
                })
                .onFailure(
                        throwable -> msg.reply(throwable.getMessage())
                );
    }


    public void start() throws Exception {
        System.out.println("hello from DB verticle");
        conn = initDB();

        EventBus eveBus = vertx.eventBus();

        //send message through event bus "Get Command"

        eveBus.consumer("GET",this::allDetails);

        //send message through event bus "Post command"

        eveBus.consumer("POST",this::postDetails);



    }
}