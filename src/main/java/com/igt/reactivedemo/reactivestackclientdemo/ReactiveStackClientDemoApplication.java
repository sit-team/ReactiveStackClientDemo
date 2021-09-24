package com.igt.reactivedemo.reactivestackclientdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@SpringBootApplication
public class ReactiveStackClientDemoApplication {

    @Bean
    WebClient client() {
        return WebClient.create("http://localhost:8080/movies");
    }

    @Bean
    CommandLineRunner demo(WebClient client) {
        return args -> client.get()
                .retrieve()
                .bodyToFlux(Movie.class)
                .filter(movie -> movie.getId().equalsIgnoreCase("Back to the future"))
                .flatMap(movie -> client.get()
                        .uri("/{id}/events", movie.getId())
                        .retrieve()
                        .bodyToFlux(MovieEvent.class))
                .subscribe(new Subscriber<MovieEvent>() {
                    Subscription subscription;
                    int onNext;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(2);
                    }

                    @Override
                    public void onNext(MovieEvent movieEvent) {
                        System.out.println(movieEvent);
                        onNext++;
                        if (onNext % 2 == 0) {
                            System.out.println("Requesting 2...");
                            subscription.request(2);
                        } else
                            System.out.println("Taking a break...");
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public static void main(String[] args) {
        SpringApplication.run(ReactiveStackClientDemoApplication.class, args);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Movie {
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class MovieEvent {
        private String id;
        private LocalDateTime when;
    }
}
