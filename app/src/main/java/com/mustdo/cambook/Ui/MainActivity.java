package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.databinding.ActivityMainBinding;

public class MainActivity extends Activity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AccountActivity.class));
            }
        });

    }


    //백키에 대응하는 메소드
    @Override
    public void onBackPressed() {
        //아래코드를 막으면 현재 화면의 종료처리가 중단됨
        //super.onBackPressed();
        if(!isFirstEnd){
            //최초한번 백키를 눌렀다.
            isFirstEnd=true;
            //3초후에 초기화된다.(최초로 한번 백키를 눌렀던 상황이)
            handler.sendEmptyMessageDelayed(1,3000);
            Toast.makeText(this,"뒤로가기를 한번 더 누르시면 종료됩니다.",Toast.LENGTH_LONG).show();
        }else{
            super.onBackPressed();
        }
    }

    boolean isFirstEnd; //백키를 한번 눌렀나?

    //핸들러, 메세지를 던져서 큐에 담고 하나씩 꺼내서 처리하는 메시징 시스템
    Handler handler = new Handler(){
        //이 메소드는 큐에 메세지가 존재하면 뽑아서 호출된다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){ //최초로 백키를 한번 눌렀다.

            }else if(msg.what == 1){ //3초가 지났다. 다시 초기화.
                isFirstEnd=false;
            }
        }
    };

}
