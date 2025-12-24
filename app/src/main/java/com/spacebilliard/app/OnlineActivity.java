package com.spacebilliard.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.spacebilliard.app.network.OnlineGameManager;
import com.spacebilliard.app.ui.NeonButton;

public class OnlineActivity extends Activity {

    private OnlineGameManager gameManager;
    private EditText usernameInput;
    private EditText roomNameInput;
    private LinearLayout roomListContainer;
    private NeonButton createRoomBtn;
    private NeonButton refreshBtn;
    private NeonButton backBtn;

    private String currentUsername;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen settings
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Root layout
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0a0018"));

        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(40, 60, 40, 40);
        mainContainer.setGravity(Gravity.CENTER_HORIZONTAL);

        // Title
        TextView title = new TextView(this);
        title.setText("🌐 ONLINE MODE");
        title.setTextColor(Color.CYAN);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        mainContainer.addView(title);

        // Username input
        usernameInput = new EditText(this);
        usernameInput.setHint("Enter your username");
        usernameInput.setTextColor(Color.WHITE);
        usernameInput.setHintTextColor(Color.GRAY);
        usernameInput.setBackgroundColor(Color.parseColor("#1a1a2e"));
        usernameInput.setPadding(30, 20, 30, 20);
        usernameInput.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        usernameParams.setMargins(0, 0, 0, 20);
        usernameInput.setLayoutParams(usernameParams);
        mainContainer.addView(usernameInput);

        // Room name input
        roomNameInput = new EditText(this);
        roomNameInput.setHint("Room name (for creating)");
        roomNameInput.setTextColor(Color.WHITE);
        roomNameInput.setHintTextColor(Color.GRAY);
        roomNameInput.setBackgroundColor(Color.parseColor("#1a1a2e"));
        roomNameInput.setPadding(30, 20, 30, 20);
        roomNameInput.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams roomParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        roomParams.setMargins(0, 0, 0, 20);
        roomNameInput.setLayoutParams(roomParams);
        mainContainer.addView(roomNameInput);

        // Button container
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER);

        createRoomBtn = new NeonButton(this, "CREATE ROOM", Color.GREEN);
        LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        createParams.setMargins(0, 0, 10, 20);
        createRoomBtn.setLayoutParams(createParams);

        refreshBtn = new NeonButton(this, "REFRESH", Color.CYAN);
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        refreshParams.setMargins(10, 0, 0, 20);
        refreshBtn.setLayoutParams(refreshParams);

        buttonContainer.addView(createRoomBtn);
        buttonContainer.addView(refreshBtn);
        mainContainer.addView(buttonContainer);

        // Rooms title
        TextView roomsTitle = new TextView(this);
        roomsTitle.setText("AVAILABLE ROOMS");
        roomsTitle.setTextColor(Color.MAGENTA);
        roomsTitle.setTextSize(18);
        roomsTitle.setGravity(Gravity.CENTER);
        roomsTitle.setPadding(0, 20, 0, 20);
        mainContainer.addView(roomsTitle);

        // Room list (scrollable)
        roomListContainer = new LinearLayout(this);
        roomListContainer.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(roomListContainer);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        scrollView.setLayoutParams(scrollParams);
        mainContainer.addView(scrollView);

        // Back button
        backBtn = new NeonButton(this, "BACK TO MENU", Color.RED);
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backParams.setMargins(0, 20, 0, 0);
        backBtn.setLayoutParams(backParams);
        mainContainer.addView(backBtn);

        root.addView(mainContainer);
        setContentView(root);

        // Initialize game manager
        gameManager = new OnlineGameManager(this);

        // Save to application for sharing between activities
        ((OnlineApplication) getApplication()).setGameManager(gameManager);

        setupListeners();
    }

    public void startGame(String roomId, String hostUsername, String guestUsername, boolean isHost) {
        runOnUiThread(() -> {
            android.content.Intent intent = new android.content.Intent(this, OnlineGameActivity.class);
            intent.putExtra("roomId", roomId);
            intent.putExtra("hostUsername", hostUsername);
            intent.putExtra("guestUsername", guestUsername);
            intent.putExtra("isHost", isHost);
            startActivity(intent);
        });
    }

    private void setupListeners() {
        createRoomBtn.setOnClickListener(v -> createRoom());
        refreshBtn.setOnClickListener(v -> refreshRooms());
        backBtn.setOnClickListener(v -> finish());

        // Auto-connect when username is entered
        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !isConnected) {
                connectToServer();
            }
        });
    }

    private void connectToServer() {
        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUsername = username;
        gameManager.connect(username, new OnlineGameManager.OnConnectionListener() {
            @Override
            public void onConnected() {
                isConnected = true;
                runOnUiThread(() -> {
                    Toast.makeText(OnlineActivity.this, "Connected to server!", Toast.LENGTH_SHORT).show();
                    refreshRooms();
                });
            }

            @Override
            public void onDisconnected() {
                isConnected = false;
                runOnUiThread(() -> Toast.makeText(OnlineActivity.this, "Disconnected from server", Toast.LENGTH_SHORT)
                        .show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(OnlineActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void createRoom() {
        if (!isConnected) {
            connectToServer();
            Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
            return;
        }

        String roomName = roomNameInput.getText().toString().trim();
        if (roomName.isEmpty()) {
            Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
            return;
        }

        gameManager.createRoom(roomName);
        Toast.makeText(this, "Room created: " + roomName + "\nWaiting for opponent...", Toast.LENGTH_LONG).show();

        // Clear room name input
        roomNameInput.setText("");

        // Set game listener
        setupGameListener();

        // Auto-refresh room list after 500ms
        new android.os.Handler().postDelayed(() -> {
            refreshRooms();
        }, 500);
    }

    private void setupGameListener() {
        // This will be called when another player joins
        // We'll handle this through OnlineGameManager updates
    }

    private void refreshRooms() {
        if (!isConnected) {
            connectToServer();
            return;
        }

        gameManager.requestRoomList();
    }

    public void updateRoomList(java.util.List<OnlineGameManager.RoomInfo> rooms) {
        runOnUiThread(() -> {
            roomListContainer.removeAllViews();

            if (rooms.isEmpty()) {
                TextView emptyText = new TextView(this);
                emptyText.setText("No rooms available\nCreate one to start!");
                emptyText.setTextColor(Color.GRAY);
                emptyText.setTextSize(16);
                emptyText.setGravity(Gravity.CENTER);
                emptyText.setPadding(0, 40, 0, 40);
                roomListContainer.addView(emptyText);
                return;
            }

            for (OnlineGameManager.RoomInfo room : rooms) {
                LinearLayout roomItem = new LinearLayout(this);
                roomItem.setOrientation(LinearLayout.HORIZONTAL);
                roomItem.setBackgroundColor(Color.parseColor("#1a1a2e"));
                roomItem.setPadding(20, 20, 20, 20);
                LinearLayout.LayoutParams roomItemParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                roomItemParams.setMargins(0, 0, 0, 10);
                roomItem.setLayoutParams(roomItemParams);

                // Room info
                LinearLayout infoContainer = new LinearLayout(this);
                infoContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                infoContainer.setLayoutParams(infoParams);

                TextView roomNameText = new TextView(this);
                roomNameText.setText(room.name);
                roomNameText.setTextColor(Color.CYAN);
                roomNameText.setTextSize(18);
                infoContainer.addView(roomNameText);

                TextView hostText = new TextView(this);
                hostText.setText("Host: " + room.host + " (" + room.players + "/" + room.maxPlayers + ")");
                hostText.setTextColor(Color.GRAY);
                hostText.setTextSize(14);
                infoContainer.addView(hostText);

                roomItem.addView(infoContainer);

                // Join button
                NeonButton joinBtn = new NeonButton(this, "JOIN", Color.GREEN);
                LinearLayout.LayoutParams joinParams = new LinearLayout.LayoutParams(
                        140, ViewGroup.LayoutParams.WRAP_CONTENT);
                joinBtn.setLayoutParams(joinParams);
                joinBtn.setOnClickListener(v -> joinRoom(room.id, room.name));

                roomItem.addView(joinBtn);
                roomListContainer.addView(roomItem);
            }
        });
    }

    private void joinRoom(String roomId, String roomName) {
        gameManager.joinRoom(roomId);
        Toast.makeText(this, "Joining room: " + roomName, Toast.LENGTH_SHORT).show();

        // Auto-refresh room list after 500ms
        new android.os.Handler().postDelayed(() -> {
            refreshRooms();
        }, 500);

        // TODO: Navigate to game screen
    }

    public void updateTimer(long timeLeft) {
        // This method is called from OnlineGameManager to update timer
        // Currently just a placeholder - timer is shown in OnlineGameActivity
        // This prevents the build error
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameManager != null) {
            gameManager.disconnect();
        }
    }
}
