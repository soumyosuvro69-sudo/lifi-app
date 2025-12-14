package com.example.lifitransmitter;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CameraManager cameraManager;
    private String cameraId;
    private EditText inputText;
    private Button sendButton;
    private final int bitDelay = 300; // must match Arduino receiver delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.inputText);
        sendButton = findViewById(R.id.sendButton);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Camera access error!", Toast.LENGTH_SHORT).show();
        }

        sendButton.setOnClickListener(v -> {
            String text = inputText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Enter text first!", Toast.LENGTH_SHORT).show();
                return;
            }
            sendTextViaLight(text);
        });
    }

    private void sendTextViaLight(String text) {
        new Thread(() -> {
            try {
                for (char c : text.toCharArray()) {
                    String binary = String.format("%8s", Integer.toBinaryString(c))
                            .replace(' ', '0');
                    for (char bit : binary.toCharArray()) {
                        cameraManager.setTorchMode(cameraId, bit == '1');
                        Thread.sleep(bitDelay);
                    }
                    // gap between characters
                    cameraManager.setTorchMode(cameraId, false);
                    Thread.sleep(500);
                }
                cameraManager.setTorchMode(cameraId, false);
                runOnUiThread(() ->
                        Toast.makeText(this, "Transmission complete!", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
