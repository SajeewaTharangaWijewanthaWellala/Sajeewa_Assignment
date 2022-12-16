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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.vertx.mysqlclient.MySQLPool.pool;

public class DataBaseVerticle extends AbstractVerticle{

    MySQLPool conn;

    private static Function<Row, Member> MAPPER = (row) ->
            Member.of(
                    //row.getInteger("CustomerID"),
                    row.getString("Name"),
                    row.getString("Age"),
                    row.getString("Address")
            );



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
        batch.add(Tuple.of(data.getString("Name"),data.getString("Age"),data.getString("Address")));
        return conn.preparedQuery("INSERT INTO Sajeewa.Client(Name, Age, Address) VALUES (?, ?, ?);")
                .executeBatch(batch)
                .map(SqlResult::rowCount);
    }


    //get details from dataBase
    public Future<List<Member>> getAllDetails(){

        return conn.query("SELECT * FROM Sajeewa.Client")
                .execute()
                .map(rs -> StreamSupport.stream(rs.spliterator(),false)
                        .map(MAPPER)
                        .collect(Collectors.toList())
                );
    }



    private <T> void allDetails(Message<T> msg){
        this.getAllDetails()
                .onSuccess(result ->{
                    System.out.println("Done!");
                    EventBus eventBus = vertx.eventBus();
                    msg.reply(Json.encodePrettily(result));
                })
                .onFailure(
                        throwable -> msg.reply(throwable.getMessage())
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