package ru.iu3.lab4.referenceservice.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.iu3.lab4.grpc.GetAllRequest;
import ru.iu3.lab4.grpc.VehicleReferenceServiceGrpc;
import ru.iu3.lab4.grpc.VehicleRequest;
import ru.iu3.lab4.grpc.VehicleResponse;
import ru.iu3.lab4.referenceservice.repository.VehicleRepository;

@GrpcService
public class ReferenceGrpcServerImpl extends VehicleReferenceServiceGrpc.VehicleReferenceServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ReferenceGrpcServerImpl.class);
    private final VehicleRepository vehicleRepository;

    public ReferenceGrpcServerImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void validateVehicle(VehicleRequest request, StreamObserver<VehicleResponse> responseObserver) {
        String vehicleId = request.getVehicleId();
        log.debug("ValidateVehicle request: vehicle_id={}", vehicleId);

        try {
            var vehicleOpt = vehicleRepository.findById(vehicleId);
            var response = VehicleResponse.newBuilder()
                    .setVehicleId(vehicleId)
                    .setExists(vehicleOpt.isPresent())
                    .setType(vehicleOpt.map(v -> v.getType()).orElse(""))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error validating vehicle {}", vehicleId, e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Ошибка проверки транспорта").withCause(e).asRuntimeException()
            );
        }
    }

    @Override
    public void getAllVehicles(GetAllRequest request, StreamObserver<VehicleResponse> responseObserver) {
        log.debug("GetAllVehicles request");
        try {
            vehicleRepository.findAll().forEach(v -> {
                var response = VehicleResponse.newBuilder()
                        .setVehicleId(v.getId())
                        .setType(v.getType())
                        .setExists(true)
                        .build();
                responseObserver.onNext(response);
            });
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting all vehicles", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Ошибка получения списка транспорта").withCause(e).asRuntimeException()
            );
        }
    }
}