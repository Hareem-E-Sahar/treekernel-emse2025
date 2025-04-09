package de.steppenwolf.helloandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class HelloAndroid extends Activity {

    /** Called when the activity is first created. */
    public static final int INSERT_ID = Menu.FIRST;

    public String rueckgabe;

    public static long fib(int n) {
        if (n <= 1) return n; else return fib(n - 1) + fib(n - 2);
    }

    public static long pascal(int n) {
        if (n <= 1) return 0; else return pascal(n - 1) + 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, R.string.menu_fibu);
        menu.add(0, 2, 0, R.string.menu_pascal);
        menu.add(0, 3, 0, R.string.menu_test);
        menu.add(0, 4, 0, R.string.menu_exit);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 1:
                rueckgabe = Long.toString(fib(10));
                break;
            case 2:
                rueckgabe = Long.toString(pascal(10));
                break;
            case 3:
                rueckgabe = Long.toString(fib(40));
                break;
            case 4:
                finish();
                break;
        }
        TextView tv = new TextView(this);
        tv.setText(rueckgabe);
        setContentView(tv);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        rueckgabe = Long.toString(fib(10));
        tv.setText(rueckgabe);
        setContentView(tv);
    }
}
