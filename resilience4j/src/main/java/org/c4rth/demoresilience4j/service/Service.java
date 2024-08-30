package org.c4rth.demoresilience4j.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface Service {
    String failure();

    String failureWithFallback();

    String success();

    String successException();

    String ignoreException();

    CompletionStage<Flux<String>> fluxSuccess();

    Flux<String> fluxFailure();

    CompletionStage<Flux<String>> fluxTimeout();

    CompletionStage<Mono<String>> monoSuccess();

    Mono<String> monoFailure();

    CompletionStage<Mono<String>> monoTimeout();

    CompletableFuture<String> futureSuccess();

    CompletableFuture<String> futureFailure();

    CompletableFuture<String> futureTimeout();

}