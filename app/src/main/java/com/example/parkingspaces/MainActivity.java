package com.example.parkingspaces;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.examples.parkingspaces.ParkingSpaceStatus;
import com.examples.parkingspaces.ParkingSpacesGrpc;
import com.examples.parkingspaces.ParkingSpacesRq;
import com.examples.parkingspaces.SpaceStates;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class MainActivity extends AppCompatActivity {

    ImageView info;
    Button slot1, slot2, slot3, slot4, cancelReservation, exit;
    TextView show;

    final String SERVER_ADDRESS = "0.0.0.0";
    final int PORT = 50051;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = findViewById(R.id.info);
        slot1 = findViewById(R.id.slot1);
        slot2 = findViewById(R.id.slot2);
        slot3 = findViewById(R.id.slot3);
        slot4 = findViewById(R.id.slot4);

        show = findViewById(R.id.show_reservations);
        cancelReservation = findViewById(R.id.cancelReservation);
        exit = findViewById(R.id.exit);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_ADDRESS, PORT)
                .usePlaintext()
                .build();

        fetchAllSpaces(channel);
    }

    private void fetchAllSpaces(ManagedChannel channel) {
        ParkingSpacesGrpc.ParkingSpacesStub parkingSpacesStub = ParkingSpacesGrpc.newStub(channel);
        parkingSpacesStub.fetchAllParkingStates(ParkingSpacesRq.newBuilder().build(), new StreamObserver<ParkingSpaceStatus>(){

            @Override
            public void onNext(ParkingSpaceStatus initialState) {

                switch (initialState.getSpaceID()) {

                    case 1:
                        changeButtonColor(slot1, initialState.getSpaceState());
                        break;

                    case 2:
                        changeButtonColor(slot2, initialState.getSpaceState());
                        break;

                    case 3:
                        changeButtonColor(slot3, initialState.getSpaceState());
                        break;

                    case 4:
                        changeButtonColor(slot4, initialState.getSpaceState());
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                startParking(channel, parkingSpacesStub);
            }
        });
    }

    private void changeButtonColor(Button button, SpaceStates spaceState) {

        if(spaceState == SpaceStates.OCCUPIED)
            button.setBackgroundColor(Color.RED);

        else if(spaceState == SpaceStates.RESERVED)
            button.setBackgroundColor(Color.YELLOW);

        else
            button.setBackgroundColor(Color.RED);
    }

    public void startParking(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async) {

        // Both rpc Notifications running

    }
}