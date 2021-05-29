package com.example.parkingspaces;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.examples.parkingspaces.ParkingNotificationsGrpc;
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

        ParkingNotificationsGrpc.ParkingNotificationsStub parkingNotificationsStub = ParkingNotificationsGrpc.newStub(channel);
        ParkingSpacesRq request = ParkingSpacesRq.newBuilder().build();

        parkingNotificationsStub.subscribeToParkingStates(request, new StreamObserver<ParkingSpaceStatus>() {
            @Override
            public void onNext(ParkingSpaceStatus actualState) {
                clientActions(channel, async, actualState);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public void clientActions(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async, ParkingSpaceStatus status) {

        updateSlots(status);
        String showInfo = "○ Click on one of the slots to make a reservation\n ○ Slot Green - Free Space\n ○ Slot Yellow - Reserved Slot\n ○ Slot Red - Occupied Slot\n";
        info.setOnClickListener(v -> Toast.makeText(this, showInfo, Toast.LENGTH_LONG).show());

        slot1.setOnClickListener(v -> attemptToReserve(channel, async, 1));

        slot2.setOnClickListener(v -> attemptToReserve(channel, async, 2));

        slot3.setOnClickListener(v -> attemptToReserve(channel, async, 3));

        slot4.setOnClickListener(v -> attemptToReserve(channel, async, 4));

        cancelReservation.setOnClickListener( v -> clientCancelReservation(channel, async));

        exit.setOnClickListener(v-> {
            channel.shutdown();
            finish();
            System.exit(0);
        });
    }

    public void clientCancelReservation(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async) {

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false)
                .setMessage("Make Reservation")
                .setView(input)
                .setPositiveButton("Enter", (dialog, id) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListener(channel, async, alertDialog, input));

    }


    private void attemptToReserve(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async, int slot) {

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false)
                .setMessage("Make Reservation")
                .setView(input)
                .setPositiveButton("Enter", (dialog, id) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListener(channel, async, alertDialog, input, slot));
    }

    @SuppressWarnings("deprecation")
    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        private  final EditText input;
        private int slot;
        private final ManagedChannel channel;

        public CustomListener(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async, Dialog dialog, EditText input, int slot) {
            this.dialog = dialog;
            this.input = input;
            this.slot = slot;
            this.channel = channel;
        }

        public CustomListener(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async, Dialog dialog, EditText input) {
            this.channel = channel;
            this.dialog = dialog;
            this.input = input;
        }

        @Override
        public void onClick(View v) {
            if(isNumeric(input.getText().toString())) {
                String slot = input.getText().toString();
                if((slot.equals("1")) || (slot.equals("2")) || (slot.equals("3")) || (slot.equals("4"))) {

                    //Cancel The Reservation
                }

                else {
                    Toast toast = Toast.makeText(MainActivity.this, "Please Introduce An Valid Slot Between 1 and 4.", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setPadding(20, 20, 20, 20);
                    view.setBackgroundResource(R.color.colorPrimaryDark);
                    toast.show();
                }
            }

            else {
                String p = input.getText().toString();
                if(isPlate(p)){
                    //Need check if plate it´s on the system
                    //show.append("The Slot " + slot + " Was Reserved With The Plate " + p + "\n");
                    //dialog.dismiss();
                }

                else {
                    Toast toast = Toast.makeText(MainActivity.this, "Incorrect Plate, Please Try Again.", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setPadding(20, 20, 20, 20);
                    view.setBackgroundResource(R.color.colorPrimaryDark);
                    toast.show();
                }
            }
        }
    }

    public static boolean isNumeric(final String str) {

        // null or empty
        if (str == null || str.length() == 0)
            return false;

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }

        return true;
    }


    public boolean isPlate(String plate) {

        int numbers = 0, letters = 0;

        if (plate.length() != 8 || plate.charAt(2) != '-' || plate.charAt(5) != '-')
            return false;

        for (int i= 0; i < plate.length(); i++) {

            if (Character.isLetter(plate.charAt(i))) {
                if (plate.charAt(i) == (plate.toUpperCase().charAt(i))) {
                    ++letters;
                    continue;
                }
            }
            if (Character.isDigit(plate.charAt(i)))
                ++numbers;

            if (i == 2) {
                if (numbers != 2 && letters != 2)
                    return false;
            }
            if (i == 4) {
                if ((numbers != 2 || letters != 2) && (numbers != 4))
                    return false;
            }

        }
        return (numbers == 4 && letters == 2);
    }


    public void updateSlots(ParkingSpaceStatus status) {

        switch (status.getSpaceID()) {
            case 1:
                changeButtonColor(slot1, status.getSpaceState());
                break;

            case 2:
                changeButtonColor(slot2, status.getSpaceState());
                break;

            case 3:
                changeButtonColor(slot3, status.getSpaceState());
                break;

            case 4:
                changeButtonColor(slot4, status.getSpaceState());
                break;
        }
    }

    /*
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
    }*/
}