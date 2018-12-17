package passhue.dev.gurary.passhue;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Tutorial extends AppCompatActivity {

    String userID;
    int count;

    ImageView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        userID = getIntent().getExtras().getString("params0");
        setTitle("Tutorial");

        count =0;
        display = (ImageView) findViewById(R.id.targetdisplay);
        updateDisplay();
    }

    public void updateDisplay(){
        if(count==0){
            display.setImageResource(R.drawable.tutorial1large);
        }else if (count==1){
            display.setImageResource(R.drawable.tutorial2large);
        }else if (count==2){
            display.setImageResource(R.drawable.tutorial3large);
        }else if (count==3){
            display.setImageResource(R.drawable.tutorial4large);
        }else if (count==4){
           display.setImageResource(R.drawable.tutorial5large);
        }

    }

    public void next (View view){
        if(count<4){
            count++;
        }else if(count==4){
            Intent myIntent = new Intent(getApplicationContext(), passwordsetup.class);
            myIntent.putExtra("params0", userID); //Optional parameters
            startActivity(myIntent);
        }
        updateDisplay();
        return;
    }

    public void back (View view){
        if(count>0){
            count--;
        }
        updateDisplay();
        return;
    }

    public void skip (View view){
        Intent myIntent = new Intent(getApplicationContext(), passwordsetup.class);
        myIntent.putExtra("params0", userID); //Optional parameters
        startActivity(myIntent);

    }
}
