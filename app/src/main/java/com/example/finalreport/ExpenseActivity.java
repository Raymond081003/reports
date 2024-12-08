package com.example.finalreport;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExpenseActivity extends AppCompatActivity {

    private EditText etExpenseName, etExpenseAmount;
    private Button btnAddExpense;
    private TextView tvExpensesList;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etExpenseName = findViewById(R.id.etExpenseName);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        tvExpensesList = findViewById(R.id.tvExpensesList);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("expenses");

        btnAddExpense.setOnClickListener(view -> addExpense());
        fetchExpenses();
    }

    private void addExpense() {
        String name = etExpenseName.getText().toString().trim();
        String amount = etExpenseAmount.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amount)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseReference.push().getKey();
        Expense expense = new Expense(id, name, Double.parseDouble(amount));

        if (id != null) {
            databaseReference.child(id).setValue(expense)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
                            etExpenseName.setText("");
                            etExpenseAmount.setText("");
                        } else {
                            Toast.makeText(this, "Error adding expense", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchExpenses() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder expenses = new StringBuilder("Expenses:\n");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense expense = dataSnapshot.getValue(Expense.class);
                    if (expense != null) {
                        expenses.append(expense.getName()).append(": $")
                                .append(expense.getAmount()).append("\n");
                    }
                }
                tvExpensesList.setText(expenses.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpenseActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
