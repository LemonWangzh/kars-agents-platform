package com.kars.reactive.base;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class BaseTest {

    @Test
    public void mo() throws InterruptedException {
        Mono<Integer> just = Mono.just(1);
        just.subscribe(System.out::println);
    }

    @Test
    public void Flux(){
        Flux<Integer> range = Flux.range(1, 8);

        range.subscribe(t-> System.out.println(t));
    }

}
