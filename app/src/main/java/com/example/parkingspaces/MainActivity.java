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
import com.examples.parkingspaces.ParkingSpaceReservation;
import com.examples.parkingspaces.ParkingSpaceStatus;
import com.examples.parkingspaces.ParkingSpacesGrpc;
import com.examples.parkingspaces.ParkingSpacesRq;
import com.examples.parkingspaces.ReservationCancelRequest;
import com.examples.parkingspaces.ReservationCancelResponse;
import com.examples.parkingspaces.ReservationResponse;
import com.examples.parkingspaces.ReserveCancelState;
import com.examples.parkingspaces.ReserveState;
import com.examples.parkingspaces.ReserveStatus;
import com.examples.parkingspaces.SpaceStates;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;


public class MainActivity extends AppCompatActivity {

    ImageView info;
    Button slot1, slot2, slot3, slot4, cancelReservation, exit;
    TextView show;
    //IP Of Local Network
    final String SERVER_ADDRESS = "192.168.1.197"; //"192.168.1.158";
    final int PORT = 50051;

    private ParkingNotificationsGrpc.ParkingNotificationsStub notification;

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

        InputStream inputStream = getResources().openRawResource(R.raw.ca);

        ManagedChannel channel;

        try {

            Certificate sslCert =
                    CertificateFactory.getInstance("X.509").generateCertificate(inputStream);

            KeyStore keystore = KeyStore.getInstance("PKCS12");

            keystore.load(null, null);

            keystore.setCertificateEntry("server", sslCert);

            SSLContext tls = SSLContext.getInstance("TLS");

            TrustManager[] tm = buildTrustingManager();

            tls.init(null, tm, null);

            channel = OkHttpChannelBuilder.forAddress(SERVER_ADDRESS, PORT)
                    .sslSocketFactory(tls.getSocketFactory())
                    .hostnameVerifier((hostname, session) -> true)
                    .useTransportSecurity()
                    .build();

        } catch (CertificateException e) {
            e.printStackTrace();
            return;
        } catch (KeyStoreException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        this.notification = ParkingNotificationsGrpc.newStub(channel);

        initialUpdatePark(channel);
    }

    private TrustManager[] buildTrustingManager() {
        // Create a trust manager that does not validate certificate chains
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    private void initialUpdatePark(ManagedChannel channel) {
        ParkingSpacesGrpc.ParkingSpacesStub parkingSpacesStub = ParkingSpacesGrpc.newStub(channel);


        parkingSpacesStub.fetchAllParkingStates(ParkingSpacesRq.newBuilder().build(), new StreamObserver<ParkingSpaceStatus>() {

            final List<ParkingSpaceStatus> status = new LinkedList<>();

            @Override
            public void onNext(ParkingSpaceStatus initialState) {
                status.add(initialState);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                runOnUiThread(() -> startParking(channel, parkingSpacesStub, status));
            }
        });
    }

    public void startParking(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async, List<ParkingSpaceStatus> initial) {

        for (ParkingSpaceStatus parkingSpaceStatus : initial) {
            updateParkStatus(parkingSpaceStatus);
        }

        info.setEnabled(true);
        exit.setEnabled(true);

        this.notification.subscribeToParkingStates(ParkingSpacesRq.newBuilder().build(),
                new StreamObserver<ParkingSpaceStatus>() {
                    @Override
                    public void onNext(ParkingSpaceStatus value) {
                        System.out.println("RECEIVED UPDATE");
                        runOnUiThread(() -> updateParkStatus(value));
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {
                    }
                });

        clientActions(channel, async);
    }

    /*
        Update Slots Status
        Change The Buttons Color And Enable When The Slot It´s Free
        Notify The App When There´s A Fire In Some Parking Slot
     */
    public void updateParkStatus(ParkingSpaceStatus update) {

        if (update.getFireAlarm()) {
            String fire = "There's An Fire On Parking Site Number " + update.getSpaceID() + " \n";
            addNotification(fire);

            return;
        }

        System.out.println("Updating... spot " + update.getSpaceID() + " " + update.getSpaceState());

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

        if (spaceState == SpaceStates.OCCUPIED) {
            button.setBackgroundColor(Color.RED);
        } else if (spaceState == SpaceStates.RESERVED) {
            cancelReservation.setEnabled(true);
            button.setBackgroundColor(Color.YELLOW);
        } else {
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
    public void clientActions(ManagedChannel channel, ParkingSpacesGrpc.ParkingSpacesStub async) {

        String showInfo = "○ Click on one of the slots to make a reservation\n ○ Slot Green - Free Space\n ○ Slot Yellow - Reserved Slot\n ○ Slot Red - Occupied Slot\n";

        System.out.println("What are we doing?");

        info.setOnClickListener(v -> Toast.makeText(this, showInfo, Toast.LENGTH_LONG).show());

        slot1.setOnClickListener(v -> attemptToReserve(async, 1));

        slot2.setOnClickListener(v -> attemptToReserve(async, 2));

        slot3.setOnClickListener(v -> attemptToReserve(async, 3));

        slot4.setOnClickListener(v -> attemptToReserve(async, 4));

        cancelReservation.setOnClickListener(v -> clientCancelReservation(async));

        //System.out.println("Test 2");

        exit.setOnClickListener(v -> {
            channel.shutdown();
            finish();
            System.exit(0);
        });
    }

    private void attemptToReserve(ParkingSpacesGrpc.ParkingSpacesStub async, int slot) {

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
        theButton.setOnClickListener(new ReservationListener(async, alertDialog, input, slot));
    }

    // To validate The User Input On Reservation
    @SuppressWarnings("deprecation")
    class ReservationListener implements View.OnClickListener {
        private final Dialog dialog;
        private final EditText input;
        private final int slot;
        private final ParkingSpacesGrpc.ParkingSpacesStub async;

        public ReservationListener(ParkingSpacesGrpc.ParkingSpacesStub async, Dialog dialog, EditText input, int slot) {
            this.dialog = dialog;
            this.input = input;
            this.slot = slot;
            this.async = async;
        }

        @Override
        public void onClick(View v) {

            String plate = input.getText().toString();

            ParkingSpaceReservation request = ParkingSpaceReservation.newBuilder()
                    .setLicencePlate(plate)
                    .setSpaceID(slot).build();

            async.attemptToReserveSpace(request, new StreamObserver<ReservationResponse>() {
                @Override
                public void onNext(ReservationResponse value) {
                    MainActivity.this.runOnUiThread(() -> handleReservationResponse(value.getResponse(), plate));
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    //Request ended
                }
            });

        }

        private void handleReservationResponse(ReserveState state, String plate) {
            if (state == ReserveState.SUCCESSFUL) {
                show.append("The Slot " + slot + " Was Reserved With The Plate " + plate + "\n");
                clientWithReservation(plate, slot);
                dialog.dismiss();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Could Not Made The Reservation With The Plate, Please Try Again.",
                        Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setPadding(20, 20, 20, 20);
                view.setBackgroundResource(R.color.colorPrimaryDark);
                toast.show();
            }
        }
    }

    public void clientWithReservation(String plate, int slot) {

        ParkingSpaceReservation reservation = ParkingSpaceReservation.newBuilder().setSpaceID(slot).setLicencePlate(plate).build();

        notification.subscribeToReservationState(reservation, new StreamObserver<ReserveStatus>() {

            @Override
            public void onNext(ReserveStatus state) {
                System.out.println("Received reservation update");
                runOnUiThread(() -> handleState(state));
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("COMPLETED");
            }
        });
    }

    public void handleState(ReserveStatus state) {

        String notify;

        switch (state.getState()) {
            case RESERVE_CANCELLED:
                notify = "The Reservation Was Canceled.\n";
                addNotification(notify);
                break;
            case RESERVE_CANCELLED_SPACE_OCCUPIED:
                notify = "The Reservation Was Canceled Because Your Space It´s Occupied For Someone Else.\n";
                addNotification(notify);
                break;
            case RESERVE_CANCELLED_PARKED_SOMEWHERE_ELSE:
                notify = "The Reservation Was Canceled Because You Park Somewhere Else.\n";
                addNotification(notify);
                break;
            case RESERVE_CANCELLED_EXPIRED:
                notify = "The reservation has expired!";
                addNotification(notify);
                break;
            case RESERVE_CONCLUDED:
                notify = "Reservation Concluded.\n";
                addNotification(notify);
                break;
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
                .setMessage("Cancel Reservation(Insert Plate):")
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
            String p = input.getText().toString();
            if (Util.isPlate(p)) {
                cancelReservationSlot(async, this, p);
            } else {
                Toast toast = Toast.makeText(MainActivity.this, "Incorrect Plate Format, Please Try Again.", Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setPadding(20, 20, 20, 20);
                view.setBackgroundResource(R.color.colorPrimaryDark);
                toast.show();
            }
        }

        protected void handleCancelReservationResponse(int spaceID, ReserveCancelState state) {

            switch (state) {
                case CANCELLED: {
                    String s = "The Vehicle in The Slot " + spaceID + " Left.\n";
                    show.append(s);
                    dialog.dismiss();

                    updateParkStatus(ParkingSpaceStatus.newBuilder()
                            .setSpaceID(spaceID)
                            .setFireAlarm(false)
                            .setSpaceState(SpaceStates.FREE).build());
                    break;
                }
                case NO_RESERVATION_FOR_PLATE: {

                    String s = "There is no reservation for that plate number\n";
                    show.append(s);
                    dialog.dismiss();
                    break;
                }
            }

        }
    }

    public void cancelReservationSlot(ParkingSpacesGrpc.ParkingSpacesStub async,
                                      CancelReservationListener listener,
                                      String input) {

        ReservationCancelRequest reservation = ReservationCancelRequest.newBuilder()
                .setLicensePlate(input).build();

        async.cancelSpaceReservation(reservation, new StreamObserver<ReservationCancelResponse>() {
            @Override
            public void onNext(ReservationCancelResponse value) {
                runOnUiThread(() ->
                        listener.handleCancelReservationResponse(value.getSpaceID(),
                                value.getCancelState()));
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
            }
        });
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