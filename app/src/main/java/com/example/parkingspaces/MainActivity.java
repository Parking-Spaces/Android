package com.example.parkingspaces;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    ImageView info;
    Button slot1, slot2, slot3, slot4;
    //EditText slot,plate;
    TextView show;
    EditText cancelReservation;

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
        String showInfo = "○ Click on one of the slots to make a reservation\n ○ Slot Green - Free Space\n ○ Slot Yellow - Reserved Slot\n ○ Slot Red - Occupied Slot\n";

        info.setOnClickListener(v -> Toast.makeText(this, showInfo, Toast.LENGTH_LONG).show());

         /*
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        ParkingSpacesGrpc.ParkingSpacesStub stub = ParkingSpacesGrpc.newStub(channel);
        ParkingNotificationsGrpc.ParkingNotificationsStub stub_notification = ParkingNotificationsGrpc.newStub(channel);
        ParkingSpaceAdministrationGrpc.ParkingSpaceAdministrationStub stub_administration = ParkingSpaceAdministrationGrpc.newStub(channel);

         request = ParkingSpacesGrpc.getFetchAllParkingStatesMethod(req);
        */
    }
}
