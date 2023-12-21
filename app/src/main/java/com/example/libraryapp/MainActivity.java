package com.example.libraryapp;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MotionEvent;
import android.view.View;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BookViewModel bookViewModel;
    public static final int NEW_BOOK_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_BOOK_ACTIVITY_REQUEST_CODE = 2;
    private Book editedBook = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final BookAdapter adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookViewModel = ViewModelProviders.of(this).get(BookViewModel.class);
        bookViewModel.findAll().observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(@Nullable final List<Book> books) {
                adapter.setBooks(books);
            }
        });

        FloatingActionButton addBookButton = findViewById(R.id.add_button);
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
                startActivityForResult(intent, NEW_BOOK_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == NEW_BOOK_ACTIVITY_REQUEST_CODE) {
                Book book = new Book(data.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE),
                        data.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR));
                bookViewModel.insert(book);
                Snackbar.make(findViewById(R.id.coordinator_layout),
                                getString(R.string.book_added),
                                Snackbar.LENGTH_LONG)
                        .show();
            }
            else if (requestCode == EDIT_BOOK_ACTIVITY_REQUEST_CODE) {
                editedBook.setTitle(data.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE));
                editedBook.setAuthor(data.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR));
                bookViewModel.update(editedBook);
                editedBook = null;
                Snackbar.make(findViewById(R.id.coordinator_layout),
                                getString(R.string.book_edited),
                                Snackbar.LENGTH_LONG)
                        .show();
            }
        }
        else
            Snackbar.make(findViewById(R.id.coordinator_layout),
                            getString(R.string.empty_not_saved),
                            Snackbar.LENGTH_LONG)
                    .show();
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final long CLICK_HOLD_THRESHOLD = 100; // Adjust the value as needed

        private TextView bookTitleTextView;
        private TextView bookAuthorTextView;
        private Book book;
        private float startX;
        private long clickStartTime;

        public BookHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.book_list_item, parent, false));
            itemView.setOnTouchListener(this);

            bookTitleTextView = itemView.findViewById(R.id.book_title);
            bookAuthorTextView = itemView.findViewById(R.id.book_author);
        }

        public void bind(Book book) {
            this.book = book;
            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthor());
        }

        @Override
        public void onClick(View v) {
            MainActivity.this.editedBook = this.book;
            Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE, book.getTitle());
            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR, book.getAuthor());
            startActivityForResult(intent, EDIT_BOOK_ACTIVITY_REQUEST_CODE);
        }

        @Override
        public boolean onLongClick(View v) {
            MainActivity.this.bookViewModel.delete(this.book);
            return true;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    // Record the start time when the button is pressed
                    clickStartTime = System.currentTimeMillis();
                    return true;

                case MotionEvent.ACTION_UP:
                    float endX = event.getX();
                    float deltaX = endX - startX;

                    // Calculate the click duration
                    long clickDuration = System.currentTimeMillis() - clickStartTime;

                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        // Swipe detected
                        if (deltaX > 0) {
                            showSnackbar("Zarchiwizowano Książkę (Swipe Right)");
                        } else {
                            showSnackbar("Zarchiwizowano Książkę (Swipe Left)");
                        }
                    } else {
                        // No swipe, handle click and long click based on the duration
                        if (clickDuration < CLICK_HOLD_THRESHOLD) {
                            // Handle regular click
                            onClick(v);
                        } else {
                            // Handle long click
                            onLongClick(v);
                        }
                    }
                    return true;

                default:
                    return false;
            }
        }

        // Helper method to show a Snackbar
        private void showSnackbar(String message) {
            Snackbar snackbar = Snackbar.make(itemView, message, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }


    private class BookAdapter extends RecyclerView.Adapter<BookHolder> {
        private List<Book> books;

        @NonNull
        @Override
        public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BookHolder holder, int position) {
            if (books != null) {
                Book book = books.get(position);
                holder.bind(book);
            }
            else
                Log.d("MainActivity", "No books");
        }

        @Override
        public int getItemCount() {
            if (books != null)
                return books.size();
            return 0;
        }

        void setBooks(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }
    }
}