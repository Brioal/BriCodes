package com.brioal.bricodes.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.brioal.bricodes.R;
import com.brioal.bricodes.base.User;
import com.brioal.bricodes.util.ImageReader;
import com.brioal.bricodes.view.CircleImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class RegisterFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = "RegisterInfo";
    @Bind(R.id.register_phone)
    EditText registerPhone;
    @Bind(R.id.register_pass)
    EditText registerPass;
    @Bind(R.id.register_name)
    EditText registerUserName;
    @Bind(R.id.register_btn_register)
    Button registerBtnRegister;
    @Bind(R.id.register_btn_head)
    Button btn_setHead;
    @Bind(R.id.register_head)
    CircleImageView circleImageView;

    RegisterInterface RegisterInterface;
    private View rootView;
    private Button btn_camera;
    private Button btn_ablum;
    private Button btn_cancel;
    private File imageFile;
    private AlertDialog dialog;

    private int GET_FROM_ABLUM = 0;
    private int GET_FROM_CAMERA = 1;

    public interface RegisterInterface {
        void Login(String email, String pass);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_register, container, false);
        ButterKnife.bind(this, rootView);
        setView();
        return rootView;
    }

    public void setView() {
        registerBtnRegister.setOnClickListener(this);
        btn_setHead.setOnClickListener(this);
        File extral = Environment.getExternalStorageDirectory();
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/BriCodes";
            File path1 = new File(path);
            if (!path1.exists()) {
                path1.mkdirs();
            }
            imageFile = new File(extral.getCanonicalPath() + "/BriCOdes/", "head.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_btn_register:
                registerBtnRegister.setText("正在上传头像请稍后...");
                final User bu = new User();
                bu.setUsername(registerUserName.getText().toString());
                bu.setEmail(registerPhone.getText().toString());
                bu.setPassword(registerPass.getText().toString());
                if (imageFile == null) { // 不设置头像则上传默认头像
                    saveBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_head));
                }
                final BmobFile bmobFile = new BmobFile(imageFile);
                bmobFile.uploadblock(getActivity(), new UploadFileListener() {

                    @Override
                    public void onSuccess() {
                        registerBtnRegister.setText("头像上传成功正在注册...");

                        Log.i(TAG, "onSuccess: 头像上传成功");
                        bu.setUserHead(new BmobFile(bmobFile.getFilename(), bmobFile.getGroup(), bmobFile.getFileUrl(getActivity())));
                        bu.save(getActivity());
                        bu.signUp(getActivity(), new SaveListener() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "onSuccess: ");
                                registerBtnRegister.setText("注册成功，正在跳转登陆界面...");
                                bu.save(getActivity());
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                LoginFragment fragment = new LoginFragment();
                                Bundle args = new Bundle();
                                args.putString("userEmail", registerPhone.getText().toString());
                                args.putString("userPass", registerPass.getText().toString());
                                fragment.setArguments(args);
                                transaction.add(R.id.user_container, fragment);
                                transaction.hide(RegisterFragment.this);
                                transaction.commit();
                            }

                            @Override
                            public void onFailure(int code, String msg) {
                                Log.i(TAG, "onFailure: " + msg);
                                if (msg.contains("username")) {
                                    registerBtnRegister.setText("用户名已被占用请更换后重试");
                                } else if ((msg.contains("email"))) {
                                    registerBtnRegister.setText("邮箱已被注册，请前往登陆界面登陆");
                                }
                            }
                        });
                    }

                    @Override
                    public void onProgress(Integer value) {
                        // 返回的上传进度（百分比）
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.i(TAG, "onFailure: 上传文件失败：" + msg);
                    }
                });


                break;


            case R.id.register_btn_head:

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.bottomsheet, null);
                btn_camera = (Button) view.findViewById(R.id.bottom_sheet_btn_camera);
                btn_ablum = (Button) view.findViewById(R.id.bottom_sheet_btn_album);
                btn_cancel = (Button) view.findViewById(R.id.bottom_sheet_btn_cancel);
                btn_camera.setOnClickListener(this);
                btn_ablum.setOnClickListener(this);
                btn_cancel.setOnClickListener(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(view);
                builder.create();
                dialog = builder.show();

                break;
            case R.id.bottom_sheet_btn_camera: // 拍照
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                Uri mOutPutFileUri = Uri.fromFile(imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
                startActivityForResult(intent, GET_FROM_CAMERA);
                dialog.dismiss();
                break;
            case R.id.bottom_sheet_btn_album: // 相册
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                intent1.setType("image/*");
                startActivityForResult(Intent.createChooser(intent1, "选择图片"), GET_FROM_ABLUM);
                dialog.dismiss();

                break;
            case R.id.bottom_sheet_btn_cancel:
                dialog.dismiss();
                break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_FROM_ABLUM) {  //相册
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), uri);

                circleImageView.setImage(bitmap);
                saveBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == GET_FROM_CAMERA) { // 相机
            circleImageView.setImage(ImageReader.readBitmap(getActivity(), imageFile));
        }
    }


    public void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "保存图片");
        if (imageFile.exists()) {
            imageFile.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.i(TAG, "头像保存成功");
    }

}
