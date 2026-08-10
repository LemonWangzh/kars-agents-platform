package com.kars.reactive.base;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BaseTest {

    @Test
    public void mo() throws InterruptedException {
        Mono<Integer> just = Mono.just(1);
        just.subscribe(System.out::println);
    }

    @Test
    public void Flux(){
        Flux<Integer> range = Flux.range(1, 8).log();

//        range.filter(t-> t > 5).log()
//                .map(s -> {
//                    System.out.println(s);
//                    return "de:" + s;
//                })
//                .log()
//                .doOnComplete(()-> System.out.println("finish"))
//                .doOnEach(s-> {
//                    System.out.println(s.get());
//                }).log().subscribe();

        range.doOnEach(s->{
            if (s.get() != null && s.get() > 4){
                System.out.println(s.get());
                if (s.get() == 7){
                    System.out.println(s.get()/0);
                }
            }

        }).
                subscribe(t-> System.out.println("正常:" + t), v-> System.out.println("异常结束" + v));

    }

}
