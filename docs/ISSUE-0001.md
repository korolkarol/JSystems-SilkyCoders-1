## Error submitting intake form

### Expected

- User should be able to submit intake form once provided with required data. User should never be provided with pure
  request error, but rather user-friendly information of what went wrong, or that the service is currently unavailable.

### Actual

Submitting intake form causes error. User see response on the UI:

```json
{
  "timestamp": "2026-03-28T16:42:04.767+00:00",
  "path": "/submit",
  "status": 500,
  "error": "Internal Server Error",
  "requestId": "02b40652-8"
}
```

Underlying exception in application logs:

```
org.springframework.web.reactive.function.client.WebClientResponseException$Unauthorized: 401 Unauthorized from POST https://openrouter.ai/api/v1/chat/completions
at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:322) ~[spring-webflux-6.2.7.jar:6.2.7]
Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException:
Error has been observed at the following site(s):
*__checkpoint ⇢ 401 UNAUTHORIZED from POST https://openrouter.ai/api/v1/chat/completions [DefaultWebClient]
*__checkpoint ⇢ Handler com.lppsa.presentation.web.IntakeController#submit(String, String, String, String, Part, String, Continuation) [DispatcherHandler]
*__checkpoint ⇢ HTTP POST "/submit" [ExceptionHandlingWebHandler]
Original Stack Trace:
at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:322) ~[spring-webflux-6.2.7.jar:6.2.7]
at org.springframework.web.reactive.function.client.DefaultClientResponse.lambda$createException$1(DefaultClientResponse.java:214) ~[spring-webflux-6.2.7.jar:6.2.7]
at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:106) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxOnErrorReturn$ReturnSubscriber.onNext(FluxOnErrorReturn.java:162) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:122) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:129) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onNext(FluxContextWrite.java:107) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxMapFuseable$MapFuseableConditionalSubscriber.onNext(FluxMapFuseable.java:299) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxFilterFuseable$FilterFuseableConditionalSubscriber.onNext(FluxFilterFuseable.java:337) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.Operators$BaseFluxToMonoOperator.completePossiblyEmpty(Operators.java:2096) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.MonoCollect$CollectSubscriber.onComplete(MonoCollect.java:145) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxPeek$PeekSubscriber.onComplete(FluxPeek.java:260) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144) ~[reactor-core-3.7.6.jar:3.7.6]
at reactor.netty.channel.FluxReceive.onInboundComplete(FluxReceive.java:413) ~[reactor-netty-core-1.2.6.jar:1.2.6]
at reactor.netty.channel.ChannelOperations.onInboundComplete(ChannelOperations.java:455) ~[reactor-netty-core-1.2.6.jar:1.2.6]
at reactor.netty.channel.ChannelOperations.terminate(ChannelOperations.java:509) ~[reactor-netty-core-1.2.6.jar:1.2.6]
at reactor.netty.http.client.HttpClientOperations.onInboundNext(HttpClientOperations.java:821) ~[reactor-netty-http-1.2.6.jar:1.2.6]
at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:115) ~[reactor-netty-core-1.2.6.jar:1.2.6]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:107) ~[netty-codec-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.CombinedChannelDuplexHandler$DelegatingChannelHandlerContext.fireChannelRead(CombinedChannelDuplexHandler.java:436) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346) ~[netty-codec-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318) ~[netty-codec-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.CombinedChannelDuplexHandler.channelRead(CombinedChannelDuplexHandler.java:251) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1519) ~[netty-handler-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1377) ~[netty-handler-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1428) ~[netty-handler-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:530) ~[netty-codec-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:469) ~[netty-codec-4.1.121.Final.jar:4.1.121.Final]
at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:290) ~[netty-codec-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1357) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:868) ~[netty-transport-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:799) ~[netty-transport-classes-epoll-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:501) ~[netty-transport-classes-epoll-4.1.121.Final.jar:4.1.121.Final]
at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:399) ~[netty-transport-classes-epoll-4.1.121.Final.jar:4.1.121.Final]
at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:998) ~[netty-common-4.1.121.Final.jar:4.1.121.Final]
at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.121.Final.jar:4.1.121.Final]
at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.121.Final.jar:4.1.121.Final]
at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
```
