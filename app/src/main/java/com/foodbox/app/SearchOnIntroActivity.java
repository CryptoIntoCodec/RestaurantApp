package com.foodbox.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.foodbox.app.activities.WelcomeScreenActivity;
import com.foodbox.app.utils.TextUtils;

public class SearchOnIntroActivity extends AppCompatActivity {
    EditText res,dish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_on_intro);
        TextView skipTextView= findViewById(R.id.skip);
        res=findViewById(R.id.ed_search);
        dish=findViewById(R.id.ed_dish);



        skipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SearchOnIntroActivity.this,HomeActivity.class);
                intent.putExtra("type","home");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                finish();

              //  startActivity(new Intent(SearchOnIntroActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


            }
        });


    }


    public void callSearchPage(View view) {
        String search="";
        String type="home";
        if (!TextUtils.isEmpty(res.getText().toString())){
            search=res.getText().toString();
        }else {
            search=dish.getText().toString();
            type="search";
        }
        Intent intent=new Intent(this,HomeActivity.class);
        intent.putExtra("value",search);
        intent.putExtra("type",type);

        startActivity(intent);
    }
}
