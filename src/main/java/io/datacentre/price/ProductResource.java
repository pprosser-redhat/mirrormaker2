package io.datacentre.price;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@Path("/stores")
public class ProductResource {

   // @Inject
   // ProductService productService;

    @Channel("product-out")
    Emitter<Product> productEmitter;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @PUT
    @Path("/product")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Product createProduct(Product product) {

       // Product product = new Product(name, description, price);
       // productService.createProduct(product);

        product.persist();
        CompletionStage<Void> acked = productEmitter.send(product);
        acked.toCompletableFuture().join();
        return product;
    }

    @Incoming("product-in")
    public void name(Product product) {

        System.out.println("a product + " + product);
    
    }

}