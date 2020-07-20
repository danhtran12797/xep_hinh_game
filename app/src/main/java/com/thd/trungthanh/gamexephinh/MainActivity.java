package com.thd.trungthanh.gamexephinh;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_PHOTO = 123;
    private final String[] animals = {"3 X 3", "4 X 4", "5 X 5"};

    private LinearLayout layout_chim, layout_func;
    private Button btnChooseImg, btnChooseNum, btnPlay, btnSeen;
    private Toolbar toolbar;

    ArrayList<String> lstNameChim; // danh sách các tên file ảnh

    ImageView arrItemImg[][]; // mảng chưa các ảnh khi đã phân mảnh từ ảnh gốc

    Bitmap bitmap; // bitmap của ảnh sẽ xếp hình
    ArrayList<Bitmap> lstBitmap1; // ds bitmap lấy từ bitmap(biến) - đây là ds bitmap gốc - nhằm để so sánh với ds bitmap tạm khi mỗi lần bạn hoán đổi các ảnh
    ArrayList<Bitmap> lstBitmap; // ds bitmap đã xáo trộn từ ds bitmap gốc - đây là ds bimap tạm
    int nextImg = 7; // vị trí ảnh mặc định
    int idMain; // id ảnh mặc định
    int number_cell = 2; // 2x2
    // ô(2,1) thì iC=1, jC=0, là ô mà người dùng vừa click
    // iM, jM là vị trí hàng và cột ô màu xàm. vd: ô(2,1) thì iM=1, jM=0
    int iC, jC, iM, jM;

    private Chronometer chronometer; // view đếm thời gian
    long pauseOffset;
    int time = 180000; // thời gian để hoàn thành trò chơi - 180000 = 3 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_game);
        setSupportActionBar(toolbar);

        //lấy tên image từ sources đưa vào List<String> lstNameChim
        String arrImg[] = getResources().getStringArray(R.array.list);
        lstNameChim = new ArrayList<>(Arrays.asList(arrImg));

        //lấy id Hình ảnh default từ vị trí nextImg
        idMain = getResources().getIdentifier(lstNameChim.get(nextImg - 1), "drawable", getPackageName());

        layout_chim = findViewById(R.id.layout_chim);
        layout_func = findViewById(R.id.layout_func);

        Set_Width_Height(); // ctrl + b : để đi đến hàm - tương tự phía dưới

        bitmap = BitmapFactory.decodeResource(getResources(), idMain); // convert id ảnh sang bitmap
        Create_List_Bitmap1();
        Draw_Layout_Img();
        Add_Img();
        Close_Img();

        btnChooseImg = findViewById(R.id.btnChooseImg);
        btnChooseNum = findViewById(R.id.btnChooseNum);
        btnPlay = findViewById(R.id.btnPlay);
        btnSeen = findViewById(R.id.btnSeen);

        chronometer = findViewById(R.id.kaka);

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if ((SystemClock.elapsedRealtime() - chronometer.getBase()) >= time) {
                    Toast.makeText(MainActivity.this, "Bạn không hoàn thành thử thách!\nTry hard it...", Toast.LENGTH_SHORT).show();
                    clearChronometer();
                }
            }
        });
        Translate_Time();

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Choose_Img();
                Close_Img();
            }
        });
        btnChooseNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Choose_Num();
                Close_Img();
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number_cell == 3)
                    Toast.makeText(MainActivity.this, "Bạn có 3' để hoàn thành", Toast.LENGTH_SHORT).show();
                else if (number_cell == 4)
                    Toast.makeText(MainActivity.this, "Bạn có 4' để hoàn thành", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Bạn có 5' để hoàn thành", Toast.LENGTH_SHORT).show();
                Create_List_Bitmap1();
                lstBitmap1 = (ArrayList<Bitmap>) lstBitmap.clone(); // gán ds bitmap tạm(lstBitmap) cho ds bitmap gốc(lstBitmap1), thay đổi lstBitmap k làm thay đổi lstBitmap1

                lstBitmap.set(lstBitmap.size() - 1, BitmapFactory.decodeResource(getResources(), R.drawable.cell_gray_thdgames)); // thay đổi item cuối cùng là bitmap của ảnh màu xám

                int id = lstBitmap.get(lstBitmap.size() - 1).getGenerationId(); // lấy id của bitmap ảnh màu xám

                Collections.shuffle(lstBitmap); // xáo trộn ds bitmap tạm
                for (int i = 0; i < lstBitmap.size(); i++) {
                    if (id == lstBitmap.get(i).getGenerationId()) {
                        iM = i / number_cell;
                        jM = i % number_cell;
                        break;
                    }
                }
                Add_Img();
                Check_Click();
                clearChronometer();
                startChronometer();
            }
        });
        btnSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Seen();
            }
        });
    }

    // animation của view đếm thời gian(Chronometer) khi mở app
    private void Translate_Time() {
        final ViewTreeObserver observer = layout_func.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        chronometer.animate().translationY(-layout_func.getHeight() / 2).setDuration(1000);
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap = BitmapFactory.decodeStream(imageStream);
                    Create_List_Bitmap1();
                    Add_Img();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_game10_media:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                break;
            case R.id.menu_game10_reload:
                clearChronometer();
                Create_List_Bitmap1();
                Add_Img();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearChronometer();
    }

    // dialog chọn ảnh - gồm 18 ảnh từ resource
    private void Dialog_Choose_Img() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        ImageView btnLeft = dialog.findViewById(R.id.btnLeft);
        ImageView btnRight = dialog.findViewById(R.id.btnRight);
        ImageView btnOK = dialog.findViewById(R.id.btnOK);
        final TextView txtNameChim = dialog.findViewById(R.id.txtNameChim);

        final ImageView imgChim = dialog.findViewById(R.id.imgChim);
        imgChim.setImageResource(idMain);

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) imgChim.getLayoutParams();
        param.width = layout_chim.getWidth();
        param.height = layout_chim.getWidth();
        imgChim.setLayoutParams(param);

        txtNameChim.setText(lstNameChim.get(nextImg - 1));

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextImg--;
                if (nextImg == 0)
                    nextImg = 18;
                idMain = getResources().getIdentifier(lstNameChim.get(nextImg - 1), "drawable", getPackageName());
                imgChim.setImageResource(idMain);
                txtNameChim.setText(lstNameChim.get(nextImg - 1));
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextImg++;
                if (nextImg == 19)
                    nextImg = 1;
                idMain = getResources().getIdentifier(lstNameChim.get(nextImg - 1), "drawable", getPackageName());
                imgChim.setImageResource(idMain);
                txtNameChim.setText(lstNameChim.get(nextImg - 1));
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                bitmap = BitmapFactory.decodeResource(getResources(), idMain);
                Create_List_Bitmap1();
                Add_Img();
            }
        });
        dialog.show();
    }

    // dialog chọn số ô muốn chơi: 3x3, 4x4, 5x5
    public void Dialog_Choose_Num() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a number cell");

        // add a radio button list
        final int[] item_select = new int[1];
        item_select[0] = 3;
        builder.setSingleChoiceItems(animals, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                switch (which) {
                    case 0:
                        item_select[0] = 3;
                        break;
                    case 1:
                        item_select[0] = 4;
                        break;
                    case 2:
                        item_select[0] = 5;
                        break;
                }
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                number_cell = item_select[0];
                if (number_cell == 3)
                    time = 180000;
                else if (number_cell == 4)
                    time = 240000;
                else
                    time = 300000;
                Create_List_Bitmap1();
                Draw_Layout_Img();
                Add_Img();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // add tất cả các imageView từ mảng arrItemImg vào layout xếp hình(layout_chim)
    // đồng thời set tag cho các imageView, VD: imageView ở hàng 2 cột 1 -> sẽ có tag: '1,0'
    public void Draw_Layout_Img() {
        layout_chim.removeAllViews(); // xóa các tất cả imageView trước đó

        arrItemImg = new ImageView[number_cell][number_cell];

        for (int i = 0; i < number_cell; i++) {
            LinearLayout linearLayout = new LinearLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
            linearLayout.setLayoutParams(layoutParams);
            for (int j = 0; j < number_cell; j++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                ImageView imageView = new ImageView(this);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setBackgroundResource(R.drawable.custom_item_img);
                imageView.setLayoutParams(params);
                linearLayout.addView(imageView);
                arrItemImg[i][j] = imageView;
                arrItemImg[i][j].setTag(i + "," + j);
                arrItemImg[i][j].setOnClickListener(this);
            }
            layout_chim.addView(linearLayout);
        }
    }

    // set ảnh cho mảng imageView từ list bitmap
    public void Add_Img() {
        for (int i = 1; i < number_cell + 1; i++) {
            for (int j = 1; j < number_cell + 1; j++) {
                int k = number_cell * (i - 1) + j - 1;
                arrItemImg[i - 1][j - 1].setImageBitmap(lstBitmap.get(k));
            }
        }
    }

    // Hàm set chiều dài và rộng cho layout xếp hình dựa vào chiều rộng của màn hình điện thoại
    public void Set_Width_Height() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) layout_chim.getLayoutParams();
        param.width = width;
        param.height = width;
        layout_chim.setLayoutParams(param);
    }

    // tạo danh sách bitmap từ: bitmap(biến) và số ô(number_cell)
    public void Create_List_Bitmap1() {
        lstBitmap = new ArrayList<>();
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getWidth(), true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int width_item = width / number_cell;
        int height_item = height / number_cell;
        int y = 0;

        for (int i = 0; i < number_cell; i++) {
            Bitmap bmRow = Bitmap.createBitmap(bitmap, 0, y, width, height_item);
            int x = 0;
            for (int j = 0; j < number_cell; j++) {
                Bitmap result = Bitmap.createBitmap(bmRow, x, 0, width_item, height_item);
                x += width_item;
                lstBitmap.add(result);
            }
            y += height_item;
        }
    }

    // enable tất cả các ảnh - k cho người dùng click
    public void Close_Img() {
        for (int i = 0; i < number_cell * number_cell; i++) {
            arrItemImg[i / number_cell][i % number_cell].setEnabled(false);
        }
    }

    // dialog xem trước ảnh - preview image
    public void Dialog_Seen() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(lstNameChim.get(nextImg - 1));
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        builder.setView(imageView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    // Hàm cho phép người dùng click
    // vd: layout 3x3: ô xám ở vị trí hàng 1 cột 1 thì chỉ cho phép người dùng click ở ô (1,2) và ô (2,1)
    public void Check_Click() {
        Close_Img();
        for (int i = 0; i < number_cell; i++) {
            for (int j = 0; j < number_cell; j++) {
                if ((i == iM - 1) || (i == iM + 1)) {
                    if (j == jM) {
                        arrItemImg[i][j].setEnabled(true);
                    }
                }
                if ((j == jM - 1) || (j == jM + 1)) {
                    if (i == iM) {
                        arrItemImg[i][j].setEnabled(true);
                    }
                }
            }
        }
    }

    // hàm get tag, sao đó gán cho iC và jC
    public void Get_ij_Cell(ImageView imageView) {
        String s = (String) imageView.getTag();
        String arr[] = s.split(",");
        iC = Integer.parseInt(arr[0]);
        jC = Integer.parseInt(arr[1]);
    }

    // hàm kiểm tra hoàn thành game -  WIN GAME
    // để so sánh 2 bitmap = nhau thì so sánh id của bitmap đó
    public boolean Check_Compled() {
        for (int i = 0; i < lstBitmap.size() - 1; i++) {
            if (lstBitmap.get(i).getGenerationId() != lstBitmap1.get(i).getGenerationId())
                return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        ImageView imageView = (ImageView) v;
        Get_ij_Cell(imageView);
        int n = number_cell * (iC + 1 - 1) + jC + 1 - 1; // từ iC, jC -> n: vị trí trong ds
        int m = number_cell * (iM + 1 - 1) + jM + 1 - 1; // từ iM, jM -> m: vị trí bitmap của trong list-lstBitmap

        imageView.setImageBitmap(lstBitmap.get(m));
        arrItemImg[iM][jM].setImageBitmap(lstBitmap.get(n));

        // swap 2 bitmap trong ds - ô xám và ô người dùng vừa chọn
        Bitmap temp = lstBitmap.get(n);
        lstBitmap.set(n, lstBitmap.get(m));
        lstBitmap.set(m, temp);

        iM = iC;
        jM = jC;
        Check_Click();
        if (Check_Compled()) {
            Toast.makeText(this, "Chúc mừng bạn hoàn thành trong " + chronometer.getText().toString(), Toast.LENGTH_SHORT).show();
            clearChronometer();
            arrItemImg[number_cell - 1][number_cell - 1].setImageBitmap(lstBitmap1.get(lstBitmap1.size() - 1));
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
            arrItemImg[number_cell - 1][number_cell - 1].startAnimation(animation);
            //đóng các item Image
            Close_Img();
        }
    }

    // bắt đầu bộ đếm thời gian
    public void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
    }

    // reset bộ đếm thời gian
    public void clearChronometer() {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }
}
