package ru.iu3.lab4.referenceservice.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.iu3.lab4.grpc.*;
import ru.iu3.lab4.referenceservice.model.Vehicle;
import ru.iu3.lab4.referenceservice.service.VehicleService;

@GrpcService
public class ReferenceGrpcServerImpl extends VehicleReferenceServiceGrpc.VehicleReferenceServiceImplBase {

    private final VehicleService vehicleService;

    public ReferenceGrpcServerImpl(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public void validateVehicle(VehicleRequest request,
                                StreamObserver<VehicleExistsResponse> responseObserver) {

        boolean exists = vehicleService.getAllVehicles().stream()
                .anyMatch(v -> v.getId().equals(request.getVehicleId()));

        VehicleExistsResponse response = VehicleExistsResponse.newBuilder()
                .setExists(exists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllVehicles(GetAllRequest request,
                               StreamObserver<VehicleResponse> responseObserver) {

        for (Vehicle v : vehicleService.getAllVehicles()) {
            responseObserver.onNext(
                    VehicleResponse.newBuilder()
                            .setVehicleId(v.getId())
                            .setType(v.getType())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }
}