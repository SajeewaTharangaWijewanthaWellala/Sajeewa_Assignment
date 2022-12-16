package org.example;

import io.vertx.core.json.Json;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

public class CustomerVerticle extends AbstractVerticle {
    private Router router;

    CustomerVerticle(Router router) {
        this.router = router;
    }

    static HttpServer server;

    public void start() throws Exception {

        EventBus eventBus = vertx.eventBus();

        server = vertx.createHttpServer(new HttpServerOptions().setPort(8888).setHost("localhost"));

        server.requestHandler(router)
                .listen(8888)
                .onSuccess(sv -> {
                    System.out.println("Successfully started");
                })
                .onFailure(event -> {
                    System.out.println("not success");
            ;
        });

        /*
         * Customer details GET request
         */
        router.get("/getResponse").produces("application/json")
                .handler(req -> {
                    eventBus.request("GET", "GET", reply -> {
                        if (reply.succeeded()) {
                            req.json(reply.result().body()).toString();
                        }
                    });
                });
        /*

         * Customer details POST request
         */
        router.post("/postEmployee")
                .handler(BodyHandler.create())
                .handler(req -> {
                    var body = req.getBodyAsJson();
                    eventBus.request("POST", body, reply -> {
                        if (reply.succeeded()) {
                            if (reply.result().body().toString().equals("1")) {
                                req.response()
                                        .end("Successfully updated");
                            } else {
                                req.response()
                                        .end(Json.encodePrettily(reply.result().body()));
                            }
                        }
                    });
                });
    }

        public static void main(String[] args) {
            Vertx vertx = Vertx.vertx();
            Router router = Router.router(vertx);
            vertx.deployVerticle(new CustomerVerticle(router));
            vertx.deployVerticle(new DataBaseVerticle(router));



        }
    }
