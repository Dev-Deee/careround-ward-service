package com.dayo.careround.wardservice.grpc;

import com.dayo.careround.wardservice.data.repositories.WardRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

// ward_id is the ward's publicId, the only ward identifier exposed outside this service.
@GrpcService
@RequiredArgsConstructor
public class WardVerificationGrpcService extends WardVerificationServiceGrpc.WardVerificationServiceImplBase {

    private final WardRepository wardRepository;

    @Override
    public void wardExists(WardExistsRequest request, StreamObserver<WardExistsResponse> responseObserver) {
        UUID wardId;
        try {
            wardId = UUID.fromString(request.getWardId());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("ward_id is not a valid UUID: " + request.getWardId())
                    .asRuntimeException());
            return;
        }

        boolean exists = wardRepository.findByPublicId(wardId).isPresent();

        responseObserver.onNext(WardExistsResponse.newBuilder().setExists(exists).build());
        responseObserver.onCompleted();
    }
}
