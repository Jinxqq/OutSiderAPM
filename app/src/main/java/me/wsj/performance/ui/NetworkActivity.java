package me.wsj.performance.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import me.wsj.performance.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public
class NetworkActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main2);

      OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

      Request request = new Request.Builder()
              .url("https://gitstar.com.cn/api_notice")
              .get()
              .build();

      okHttpClient.newCall(request).enqueue(new Callback() {
         @Override
         public void onFailure(@NonNull Call call, @NonNull IOException e) {

         }

         @Override
         public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Log.e("NetworkActivity", response.body().string());
         }
      });
   }
}
