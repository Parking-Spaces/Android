package com.example.parkingspaces;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.examples.parkingspaces.ParkingSpacesGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


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


        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        ParkingSpacesGrpc.ParkingSpacesStub parkingSpacesStub = ParkingSpacesGrpc.newStub(channel);



    }

    // Creates and displays a notification
    @SuppressWarnings("deprecation")
    private void addNotification(String notification) {
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.parking)
                .setContentTitle("Parking Spaces")
                .setContentText(notification);

        // Creates the intent needed to show the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
