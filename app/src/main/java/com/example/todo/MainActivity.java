package com.example.todo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    private static int EDIT_TEXT_CODE = 20;
    List<String> items;

    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAdd = findViewById(R.id.btnAdd);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        ItemsAdapter.OnLongClickListener onLongClickListener =  new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                deleteItemAt(position);
            }
        };
        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                editItemAt(position);
            }
        };
        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = etItem.getText().toString();
                if (todoItem.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Todo content cannot be only whitespace", Toast.LENGTH_LONG).show();
                }
                else {
                    addItem(todoItem);
                }
            }
        });
    }
    // handle results of edit activity
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            items.set(data.getExtras().getInt(KEY_ITEM_POSITION), data.getStringExtra(KEY_ITEM_TEXT));
            itemsAdapter.notifyItemChanged(data.getExtras().getInt(KEY_ITEM_POSITION));
            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");

        }
    }
    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }

    // this function will load items by reading every line of the data file
    private void loadItems() {
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }

    // this function saves items by writing them into the data file
    private void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }
    private void deleteItemAt(int itemPos) {
        // delete item
        items.remove(itemPos);
        // notify the adapter to remove item
        itemsAdapter.notifyItemRemoved(itemPos);
        Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
        saveItems();
    }
    private void editItemAt(int itemPos) {
        // Create a new activity (edit activity)
        Intent i = new Intent(MainActivity.this, EditActivity.class);
        i.putExtra(KEY_ITEM_TEXT, items.get(itemPos));
        i.putExtra(KEY_ITEM_POSITION, itemPos);
        // pass the data being edited
        // display updated data
        startActivityForResult(i, EDIT_TEXT_CODE);
    }
    private void addItem(String todoItem) {
        // add items to the model
        items.add(todoItem);
        // notify adapter that an item is inserted
        itemsAdapter.notifyItemInserted(items.size() - 1);
        etItem.setText("");
        Toast.makeText(getApplicationContext(), "Item added", Toast.LENGTH_SHORT).show();
        saveItems();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(rvItems.getContext());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(rvItems.getContext(),
                mLayoutManager.getOrientation());
        rvItems.addItemDecoration(mDividerItemDecoration);

    }
}