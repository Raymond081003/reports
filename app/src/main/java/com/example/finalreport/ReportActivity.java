package com.example.finalreport;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;

public class ReportActivity extends AppCompatActivity {

    private Spinner reportTypeSpinner;
    private Button exportButton;
    private DatabaseReference databaseReference;
    private String selectedReportType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("sales_inventory");

        // Initialize UI elements
        reportTypeSpinner = findViewById(R.id.reportTypeSpinner);
        exportButton = findViewById(R.id.exportButton);

        // Set up the spinner with options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.report_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);

        reportTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReportType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedReportType = "Daily"; // Default to daily if nothing is selected
            }
        });

        exportButton.setOnClickListener(v -> fetchAndExportData());
    }

    private void fetchAndExportData() {
        databaseReference.child(selectedReportType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder data = new StringBuilder();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    String value = childSnapshot.getValue(String.class);
                    data.append(key).append(": ").append(value).append("\n");
                }

                generateAndSaveImage(data.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAndSaveImage(String data) {
        // Create a bitmap to draw the data
        Bitmap bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        canvas.drawColor(android.graphics.Color.WHITE);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(android.graphics.Color.BLACK);
        paint.setTextSize(24);

        // Draw the data on the bitmap
        String[] lines = data.split("\n");
        int y = 50; // Starting Y-coordinate
        for (String line : lines) {
            canvas.drawText(line, 50, y, paint);
            y += 30;
        }

        // Save the bitmap as an image
        try {
            File directory = new File(Environment.getExternalStorageDirectory(), "SalesReports");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, selectedReportType + "_Report.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Report saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("ExportError", "Error saving report image", e);
            Toast.makeText(this, "Error saving report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
