package ru.iu3.lab4.coreservice.grpc;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.iu3.lab4.coreservice.exception.ReferenceServiceUnavailableException;
import ru.iu3.lab4.grpc.GetAllRequest;
import ru.iu3.lab4.grpc.VehicleReferenceServiceGrpc;
import ru.iu3.lab4.grpc.VehicleRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferenceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(ReferenceGrpcClient.class);

    @GrpcClient("reference-service")
    private VehicleReferenceServiceGrpc.VehicleReferenceServiceBlockingStub stub;

    /**
     * Проверяет существование транспорта.
     * @throws ReferenceServiceUnavailableException если сервис недоступен
     * @throws RuntimeException при других ошибках
     */
    public VehicleDto validateVehicleOrThrow(String vehicleId) {
        try {
            var request = VehicleRequest.newBuilder().setVehicleId(vehicleId).build();
            var response = stub.validateVehicle(request);

            if (!response.getExists()) {
                // ✅ Бросаем корректное исключение, которое поймает ConsoleRunner
                throw new ru.iu3.lab4.coreservice.exception.ReferenceServiceUnavailableException(
                        "Транспорт с ID '" + vehicleId + "' не найден в справочнике"
                );
            }

            // ✅ Возвращаем только ID, тип не нужен для валидации
            return new VehicleDto(vehicleId, null);

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.UNAVAILABLE) {
                log.warn("Reference Service недоступен: {}", e.getStatus().getDescription());
                throw new ReferenceServiceUnavailableException(
                        "Справочник транспорта временно недоступен. Попробуйте позже."
                );
            }
            log.error("gRPC error for vehicle {}: {} - {}",
                    vehicleId,
                    e.getStatus().getCode(),
                    e.getStatus().getDescription());
            throw new RuntimeException("Ошибка связи со справочником: " + e.getStatus().getDescription());
        }
    }

    /**
     * Получает список всех ТС для отображения в консоли (упрощённо, без стрима)
     */
    public List<VehicleDto> getAllVehiclesSafe() {
        try {
            var request = GetAllRequest.newBuilder().build();
            var responses = stub.getAllVehicles(request);

            List<VehicleDto> result = new ArrayList<>();

            while (responses.hasNext()) {
                var r = responses.next();
                result.add(new VehicleDto(
                        r.getVehicleId(),
                        r.getType()
                ));
            }

            return result;

        } catch (StatusRuntimeException e) {
            log.warn("Не удалось получить список транспорта: {} - {}",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription());

            throw new ReferenceServiceUnavailableException(
                    "Справочник транспорта временно недоступен. Попробуйте позже."
            );
        }
    }
}