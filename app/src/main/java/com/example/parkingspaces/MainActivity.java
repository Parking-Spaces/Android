package com.example.parkingspaces;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import com.examples.parkingspaces.ParkingSpaceAdministrationGrpc;
import com.examples.parkingspaces.ParkingSpaceReservation;
import com.examples.parkingspaces.ParkingSpaceStatus;
import com.examples.parkingspaces.ParkingSpacesGrpc;
import com.examples.parkingspaces.ParkingSpacesRq;
import com.examples.parkingspaces.PlateData;
import com.examples.parkingspaces.PlateInfo;
import com.examples.parkingspaces.ReservationCancelResponse;
import com.examples.parkingspaces.ReservationResponse;
import com.examples.parkingspaces.ReservationState;
import com.examples.parkingspaces.ReserveStatus;
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

        slot1.setBackgroundColor(Color.GREEN);
        slot2.setBackgroundColor(Color.GREEN);
        slot3.setBackgroundColor(Color.GREEN);
        slot4.setBackgroundColor(Color.GREEN);

        disableButtons();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_ADDRESS, PORT)
                .usePlaintext()
                .build();

        initialUpdatePark(channel);
    }

    private void initialUpdatePark(ManagedChannel channel) {
        ParkingSpacesGrpc.ParkingSpacesStub parkingSpacesStub = ParkingSpacesGrpc.newStub(channel);

        parkingSpacesStub.fetchAllParkingStates(ParkingSpacesRq.newBuilder().build(), new StreamObserver<ParkingSpaceStatus>(){

            @Override
            public void onNext(ParkingSpaceStatus initialState) {
                updateParkStatus(initialState);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                startParking(channel, parkingSpacesStub);
            }
        });
    }

    public void startParking(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async) {

        ParkingNotificationsGrpc.ParkingNotificationsStub parkingNotificationsStub = ParkingNotificationsGrpc.newStub(channel);
        ParkingSpacesRq request = ParkingSpacesRq.newBuilder().build();

        info.setEnabled(true);
        exit.setEnabled(true);

        parkingNotificationsStub.subscribeToParkingStates(request, new StreamObserver<ParkingSpaceStatus>() {
            @Override
            public void onNext(ParkingSpaceStatus state) {
                //Updates The Parking State
                updateParkStatus(state);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {

            }
        });

        clientActions(channel,async,parkingNotificationsStub);
    }

    /*
        Update Slots Status
        Change The Buttons Color And Enable When The Slot It´s Free
        Notify The App When There´s A Fire In Some Parking Slot
     */
    public void updateParkStatus(ParkingSpaceStatus update) {

        if(update.getFireAlarm()) {
            String fire = "There's An Fire On Parking Site Number " + update.getSpaceID() + " \n";
            addNotification(fire);
        }

        switch (update.getSpaceID()) {

            case 1:
                changeButtonColor(slot1, update.getSpaceState());
                break;

            case 2:
                changeButtonColor(slot2, update.getSpaceState());
                break;

            case 3:
                changeButtonColor(slot3, update.getSpaceState());
                break;

            case 4:
                changeButtonColor(slot4, update.getSpaceState());
                break;
        }

    }

    // Change The Buttons Color
    public void changeButtonColor(Button button, SpaceStates spaceState) {

        if(spaceState == SpaceStates.OCCUPIED) {
            button.setBackgroundColor(Color.RED);
        }

        else if(spaceState == SpaceStates.RESERVED) {
            cancelReservation.setEnabled(true);
            button.setBackgroundColor(Color.YELLOW);
        }

        else {
            button.setEnabled(true);
            button.setBackgroundColor(Color.GREEN);
        }
    }

    /*
        Here It´s Where The Client Can Use The App After The Status Update
        The Info Buttons It´s For Inform The User How The App works
        It´s Possible To Make Reservations by Clicking The Slot
        To Cancel A Reservation Just Press The "Cancel Reservation" Button
        To Exit The App Just Press "Exit" Button
     */
    public void clientActions(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async, ParkingNotificationsGrpc.ParkingNotificationsStub notification) {

        ParkingSpaceAdministrationGrpc.ParkingSpaceAdministrationStub admin = ParkingSpaceAdministrationGrpc.newStub(channel);

        String showInfo = "○ Click on one of the slots to make a reservation\n ○ Slot Green - Free Space\n ○ Slot Yellow - Reserved Slot\n ○ Slot Red - Occupied Slot\n";
        info.setOnClickListener(v -> Toast.makeText(this, showInfo, Toast.LENGTH_LONG).show());

        slot1.setOnClickListener(v -> attemptToReserve(admin, async, notification, 1));

        slot2.setOnClickListener(v -> attemptToReserve(admin, async, notification, 2));

        slot3.setOnClickListener(v -> attemptToReserve(admin, async, notification, 3));

        slot4.setOnClickListener(v -> attemptToReserve(admin, async, notification,4));

        cancelReservation.setOnClickListener( v -> clientCancelReservation(async));

        exit.setOnClickListener(v-> {
            channel.shutdown();
            finish();
            System.exit(0);
        });
    }

    private void attemptToReserve(ParkingSpaceAdministrationGrpc.ParkingSpaceAdministrationStub admin, ParkingSpacesGrpc.ParkingSpacesStub async, ParkingNotificationsGrpc.ParkingNotificationsStub notify, int slot) {

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
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new ReservationListener(admin, async, notify, alertDialog, input, slot));
    }

    // To validate The User Input On Reservation
    @SuppressWarnings("deprecation")
    class ReservationListener implements View.OnClickListener {
        private final Dialog dialog;
        private final EditText input;
        private final int slot;
        private final ParkingSpacesGrpc.ParkingSpacesStub async;
        private final ParkingNotificationsGrpc.ParkingNotificationsStub notify;
        private final ParkingSpaceAdministrationGrpc.ParkingSpaceAdministrationStub admin;

        public ReservationListener(ParkingSpaceAdministrationGrpc.ParkingSpaceAdministrationStub admin, ParkingSpacesGrpc.ParkingSpacesStub async, ParkingNotificationsGrpc.ParkingNotificationsStub notify, Dialog dialog, EditText input, int slot) {
            this.async = async;
            this.notify = notify;
            this.dialog = dialog;
            this.input = input;
            this.slot = slot;
            this.admin = admin;

        }

        @Override
        public void onClick(View v) {

            String plate = input.getText().toString();
            final StringBuffer plateDataResponse = new StringBuffer();
            PlateInfo p = PlateInfo.newBuilder().setLicensePlate(plate).build();

            admin.isPlateInParkingLot(p, new StreamObserver<PlateData>() {
                @Override
                public void onNext(PlateData value) {
                    plateDataResponse.append(value);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });

            if(plateDataResponse.toString().equals("False")) {
                String response = reservationSlot(async, plate, slot);
                if(response.equals("SUCCESSFUL")) {
                    show.append("The Slot " + slot + " Was Reserved With The Plate " + plate + "\n");
                    clientWithReservation(notify, plate, slot);
                    dialog.dismiss();
                }
            }

            else {
                Toast toast = Toast.makeText(MainActivity.this, "Could Not Made The Reservation With The Plate, Please Try Again.", Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setPadding(20, 20, 20, 20);
                view.setBackgroundResource(R.color.colorPrimaryDark);
                toast.show();
            }
        }
    }


    public String reservationSlot(ParkingSpacesGrpc.ParkingSpacesStub async, String plate, int slot) {
        final StringBuffer reservationResponse = new StringBuffer();
        ParkingSpaceReservation reservation = ParkingSpaceReservation.newBuilder().setSpaceID(slot).setLicencePlate(plate).build();
        async.attemptToReserveSpace(reservation, new StreamObserver<ReservationResponse>() {
            @Override
            public void onNext(ReservationResponse value) {
                reservationResponse.append(value);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        return reservationResponse.toString();
    }

    public void clientWithReservation(ParkingNotificationsGrpc.ParkingNotificationsStub notification, String plate, int slot) {

        ParkingSpaceReservation reservation = ParkingSpaceReservation.newBuilder().setSpaceID(slot).setLicencePlate(plate).build();

        notification.subscribeToReservationState(reservation, new StreamObserver<ReserveStatus>() {

            @Override
            public void onNext(ReserveStatus state) {
                handleState(state);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public void handleState(ReserveStatus state) {

        String notify;

       if(state.getState() == ReservationState.RESERVE_CANCELLED) {
           notify = "The Reservation Was Canceled.\n";
           addNotification(notify);
       }

       if(state.getState() == ReservationState.RESERVE_CANCELLED_SPACE_OCCUPIED) {
           notify = "The Reservation Was Canceled Because Your Space It´s Occupied For Someone Else.\n";
           addNotification(notify);

       }

       if(state.getState() == ReservationState.RESERVE_CANCELLED_PARKED_SOMEWHERE_ELSE) {
           notify = "The Reservation Was Canceled Because You Park Somewhere Else.\n";
           addNotification(notify);
       }

       if(state.getState() == ReservationState.RESERVE_CONCLUDED) {
           notify = "Reservation Concluded.\n";
           addNotification(notify);
       }
    }


    public void clientCancelReservation(ParkingSpacesGrpc.ParkingSpacesStub async) {

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false)
                .setMessage("Cancel Reservation(Insert Slot or Plate):")
                .setView(input)
                .setPositiveButton("Enter", (dialog, id) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CancelReservationListener(async, alertDialog, input));

    }

    // To validate The User Input On Reservation
    @SuppressWarnings("deprecation")
    class CancelReservationListener implements View.OnClickListener {
        private final ParkingSpacesGrpc.ParkingSpacesStub async;
        private final Dialog dialog;
        private final EditText input;


        public CancelReservationListener(ParkingSpacesGrpc.ParkingSpacesStub async, Dialog dialog, EditText input) {
            this.async = async;
            this.dialog = dialog;
            this.input = input;

        }

        @Override
        public void onClick(View v) {
            if(Util.isNumeric(input.getText().toString())) {
                String slot = input.getText().toString();
                if((slot.equals("1")) || (slot.equals("2")) || (slot.equals("3")) || (slot.equals("4"))) {
                    String responseS = cancelReservationSlot(async, slot, 1);
                    if(responseS.equals("CANCELLED")) {
                        String s = "The Vehicle in The Slot " + slot + " Left.\n";
                        show.append(s);
                        dialog.dismiss();
                    }
                }

                else {
                    Toast toast = Toast.makeText(MainActivity.this, "Please Introduce An Valid Slot, Between 1 and 4, Or a Plate.\n", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setPadding(20, 20, 20, 20);
                    view.setBackgroundResource(R.color.colorPrimaryDark);
                    toast.show();
                }
            }

            else {
                String p = input.getText().toString();
                if(Util.isPlate(p)){
                    String responseP = cancelReservationSlot(async, p, 2);
                    if(responseP.equals("CANCELLED")) {
                        String s = "The Plate " + p + " Left The Park.\n";
                        show.append(s);
                        dialog.dismiss();
                    }
                }

                else {
                    Toast toast = Toast.makeText(MainActivity.this, "Incorrect Plate Format, Please Try Again.", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setPadding(20, 20, 20, 20);
                    view.setBackgroundResource(R.color.colorPrimaryDark);
                    toast.show();
                }
            }
        }
    }

    public String cancelReservationSlot(ParkingSpacesGrpc.ParkingSpacesStub async, String input, int type) {

        final StringBuffer cancelResponse = new StringBuffer();
        ParkingSpaceReservation reservation;

        // Cancel The Reservation Using Slot
        if(type == 1) {
            reservation = ParkingSpaceReservation.newBuilder().setSpaceID(Integer.parseInt(input)).build();
            async.cancelSpaceReservation(reservation, new StreamObserver<ReservationCancelResponse>() {
                @Override
                public void onNext(ReservationCancelResponse value) {
                    cancelResponse.append(value);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });
        }

        // Cancel The Reservation Using Plate
        else {

            reservation = ParkingSpaceReservation.newBuilder().setLicencePlate(input).build();
            async.cancelSpaceReservation(reservation, new StreamObserver<ReservationCancelResponse>() {
                @Override
                public void onNext(ReservationCancelResponse value) {
                    cancelResponse.append(value);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });
        }

        return cancelResponse.toString();

    }


    /*
        Creates And Displays A Notification When The Slot It´s Occupied Incorrectly
        Notify You When A Fire Occurs In The Place You Have Reserved / You Are Occupying
     */
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

    // Initial State Of All Buttons
    public void disableButtons() {
        info.setEnabled(false);
        slot1.setEnabled(false);
        slot2.setEnabled(false);
        slot3.setEnabled(false);
        slot4.setEnabled(false);
        cancelReservation.setEnabled(false);
        exit.setEnabled(false);
    }
}