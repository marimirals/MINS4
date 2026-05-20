package ru.iu3.lab4.coreservice.grpc;

import io.grpc.*;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@GrpcGlobalClientInterceptor
public class TraceIdClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> TRACE_ID_KEY =
            Metadata.Key.of("trace-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(ClientCall.Listener<RespT> responseListener, Metadata headers) {
                String traceId = MDC.get("traceId");
                if (traceId == null) {
                    traceId = UUID.randomUUID().toString();
                    MDC.put("traceId", traceId);
                }
                headers.put(TRACE_ID_KEY, traceId);
                super.start(responseListener, headers);
            }
        };
    }
}