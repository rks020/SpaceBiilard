package com.spacebilliard.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.util.Log;

import com.spacebilliard.app.network.OnlineGameManager;
import com.spacebilliard.app.ui.OnlineScorePanel;
import com.spacebilliard.app.ui.NeonButton;
// import com.spacebilliard.app.SimpleOnlineGameView;

public class OnlineGameActivity extends Activity implements OnlineGameManager.OnGameListener {

    private static final String TAG = "OnlineGameActivity";

    private OnlineGameManager gameManager;
    private OnlineGameView gameView;
    private OnlineScorePanel scorePanel;
    private NeonButton backBtn;

    private String roomId;
    private String hostUsername;
    private String guestUsername;
    private boolean isHost;

    private int hostScore = 0;
    private int guestScore = 0;
    private int currentSet = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            android.util.Log.d("OnlineGameActivity", "onCreate started");

            // Full screen
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Get intent data
            roomId = getIntent().getStringExtra("roomId");
            hostUsername = getIntent().getStringExtra("hostUsername");
            guestUsername = getIntent().getStringExtra("guestUsername");
            isHost = getIntent().getBooleanExtra("isHost", false);

            android.util.Log.d("OnlineGameActivity", "Room: " + roomId + ", Host: " + isHost);

            // Root layout
            FrameLayout root = new FrameLayout(this);

            // Use OnlineGameView instead of GameView
            android.util.Log.d("OnlineGameActivity", "Creating OnlineGameView");
            gameView = new OnlineGameView(this);
            root.addView(gameView);

            // Score panel
            android.util.Log.d("OnlineGameActivity", "Creating score panel");
            scorePanel = new OnlineScorePanel(this);
            FrameLayout.LayoutParams scoreParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (getResources().getDisplayMetrics().density * 160)); // Increased from 100
            scoreParams.gravity = Gravity.TOP;
            scoreParams.setMargins(20, 30, 20, 0);
            scorePanel.setLayoutParams(scoreParams);
            scorePanel.setPlayerNames(hostUsername, guestUsername);
            scorePanel.setIsHost(isHost);
            scorePanel.setCurrentSet(1, 3); // Initialize with Set 1/3
            scorePanel.setTimeLeft(30000); // Initialize with 30 seconds
            root.addView(scorePanel);

            // Back button
            android.util.Log.d("OnlineGameActivity", "Creating back button");
            backBtn = new NeonButton(this, "LEAVE GAME", Color.RED);
            FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                    (int) (getResources().getDisplayMetrics().density * 150),
                    (int) (getResources().getDisplayMetrics().density * 45));
            backParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            backParams.bottomMargin = 30;
            backBtn.setLayoutParams(backParams);
            backBtn.setOnClickListener(v -> leaveGame());
            root.addView(backBtn);

            setContentView(root);

            // Get game manager from application
            android.util.Log.d("OnlineGameActivity", "Getting game manager");
            gameManager = ((OnlineApplication) getApplication()).getGameManager();

            if (gameManager != null) {
                android.util.Log.d("OnlineGameActivity", "Game manager found, setting up");
                // Set this activity as game listener
                gameManager.setGameListener(this);

                // Set game manager and start
                gameView.setOnlineMode(gameManager, isHost);
                gameView.setPlayerNames(hostUsername, guestUsername);
                android.util.Log.d("OnlineGameActivity", "OnlineGameView setup complete");
            } else {
                android.util.Log.e("OnlineGameActivity", "Game manager is NULL!");
                Toast.makeText(this, "Game manager not found!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            android.util.Log.e("OnlineGameActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting game: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onPlayerJoined(String username) {
        runOnUiThread(() -> {
            Toast.makeText(this, username + " joined!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPlayerLeft() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Player left the game", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private android.app.AlertDialog matchEndDialog;

    @Override
    public void onGameStart() {
        Log.d(TAG, "onGameStart called!");
        runOnUiThread(() -> {
            Log.d(TAG, "onGameStart UI thread block executing");
            Toast.makeText(this, "Game started!", Toast.LENGTH_SHORT).show();

            // Dismiss match end dialog if showing (Rematch accepted)
            if (matchEndDialog != null && matchEndDialog.isShowing()) {
                Log.d(TAG, "Dismissing matchEndDialog");
                matchEndDialog.dismiss();
            }
            if (rematchDialog != null && rematchDialog.isShowing()) {
                Log.d(TAG, "Dismissing rematchDialog");
                rematchDialog.dismiss();
            }

            // Reset scores on game start/restart
            this.hostScore = 0;
            this.guestScore = 0;
            this.currentSet = 1;
            if (scorePanel != null) {
                scorePanel.setScores(0, 0);
                scorePanel.setCurrentSet(1, 3);
            }

            // Clear ALL overlays (winner, setFinished, countdown)
            if (gameView != null) {
                Log.d(TAG, "Clearing game view overlays");
                gameView.clearAllOverlays(); // New method to clear everything
            }

            if (gameManager != null) {
                gameManager.sendReady();
            }
            Log.d(TAG, "onGameStart complete");
        });
    }

    @Override
    public void onGameStateReceived(String hostCueBallJson, String guestCueBallJson, String ballsJson) {
        // Forward ball positions to game view
        if (gameView != null) {
            gameView.updateOnlineState(hostCueBallJson, guestCueBallJson, ballsJson, 30000);
        }
    }

    @Override
    public void onGameStateUpdate(String hostCueBallJson, String guestCueBallJson, String ballsJson, long timeLeft,
            int currentSet) {
        // Forward real-time physics update to game view (60 FPS)
        if (gameView != null) {
            gameView.updateOnlineState(hostCueBallJson, guestCueBallJson, ballsJson, timeLeft);
        }

        // Update score panel timer
        if (scorePanel != null) {
            scorePanel.setTimeLeft(timeLeft);
            // We can also update current set here ensuring sync
            scorePanel.setCurrentSet(currentSet, 3);
        }

        this.currentSet = currentSet;
    }

    @Override
    public void onOpponentShot(float angle, float power) {
        // Shot handled by server sync
    }

    @Override
    public void onBallsUpdate(int hostBalls, int guestBalls) {
        runOnUiThread(() -> {
            if (scorePanel != null) {
                scorePanel.setBallsDestroyed(hostBalls, guestBalls);
            }
        });
    }

    @Override
    public void onSetEnded(String winner, int hostScore, int guestScore, int currentSet) {
        this.hostScore = hostScore;
        this.guestScore = guestScore;
        this.currentSet = currentSet;

        runOnUiThread(() -> {
            if (scorePanel != null) {
                scorePanel.setScores(hostScore, guestScore);
                scorePanel.setCurrentSet(currentSet, 3);
            }

            // Only show SET FINISHED if match is NOT over (someone didn't win 2-0)
            // If match continues to next set (scores are 1-1 or match will continue)
            boolean matchWillContinue = (hostScore < 2 && guestScore < 2);

            if (gameView != null && matchWillContinue) {
                // Show SET FINISHED with correct number
                gameView.showSetFinished("SET " + currentSet + " FINISHED");

                // Start countdown AFTER set finished text disappears (2 second delay)
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    new android.os.CountDownTimer(5000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            if (gameView != null) {
                                int count = (int) Math.ceil(millisUntilFinished / 1000.0);
                                gameView.showCountdown(count);
                            }
                        }

                        public void onFinish() {
                            if (gameView != null) {
                                gameView.hideCountdown();
                            }
                        }
                    }.start();
                }, 2000); // 2 second delay
            }
        });
    }

    private android.app.AlertDialog rematchDialog;

    @Override
    public void onRematchRequested(String requestingUser, boolean hostWants, boolean guestWants) {
        runOnUiThread(() -> {
            boolean amIHost = isHost;
            boolean myRequest = amIHost ? hostWants : guestWants;
            boolean oppRequest = amIHost ? guestWants : hostWants;

            if (oppRequest && !myRequest) {
                // Opponent wants rematch, I haven't said yes. Show Invite.
                if (rematchDialog == null || !rematchDialog.isShowing()) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Rematch Request");
                    builder.setMessage(requestingUser + " wants a rematch!");
                    builder.setPositiveButton("ACCEPT", (dialog, which) -> {
                        Toast.makeText(this, "Accepting rematch...", Toast.LENGTH_SHORT).show();
                        if (gameManager != null) {
                            gameManager.sendRematchRequest();
                        }
                    });
                    builder.setNegativeButton("DECLINE", (dialog, which) -> dialog.dismiss());
                    builder.setCancelable(false);
                    rematchDialog = builder.show();
                }
            } else if (myRequest && !oppRequest) {
                // I already asked, waiting for them
                Toast.makeText(this, "Waiting for " + (amIHost ? guestUsername : hostUsername) + "...",
                        Toast.LENGTH_SHORT).show();
            } else if (myRequest && oppRequest) {
                // Both accepted! Game will restart via onGameStart callback
                Toast.makeText(this, "Rematch accepted! Game restarting...", Toast.LENGTH_SHORT).show();

                // Close any rematch dialog
                if (rematchDialog != null && rematchDialog.isShowing()) {
                    rematchDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onMatchEnded(String winner, String finalScore, int hostScore, int guestScore) {
        runOnUiThread(() -> {
            String winnerName = winner.equals("host") ? hostUsername : guestUsername;

            // Show winner name AFTER "SET FINISHED" disappears (3 second delay)
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (gameView != null) {
                    gameView.showWinner(winnerName.toUpperCase() + " WINS!");
                }

                // Show match end dialog after winner text displays (2 more seconds)
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("🏆 MATCH FINISHED!");
                    builder.setMessage(winnerName + " won the match!\n\nFinal Score: " + finalScore);
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        finish();
                    });
                    builder.setNegativeButton("REMATCH", (dialog, which) -> {
                        if (gameManager != null) {
                            gameManager.sendRematchRequest();
                            Toast.makeText(this, "Rematch request sent!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setCancelable(false);

                    matchEndDialog = builder.create();
                    matchEndDialog.show();
                }, 2000); // 2 second delay to show winner text
            }, 3000); // 3 second delay - wait for SET FINISHED to fade
        });
    }

    private void leaveGame() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Leave Game")
                .setMessage("Are you sure you want to leave?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (gameManager != null) {
                        gameManager.leaveRoom();
                    }
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // GameView will cleanup automatically in pause()
    }

    public void updateTimer(long timeLeft) {
        if (scorePanel != null) {
            scorePanel.setTimeLeft(timeLeft);
        }
    }
}
