package br.com.itb.camera_3c_3d_2021;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.concurrent.ExecutorService;

import br.com.itb.camera_3c_3d_2021.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    // Declarações de objeto da janela
    private FragmentFirstBinding binding;
    private AppCompatImageView imagem;
    private AppCompatTextView texto;
    private FloatingActionButton botaoTirarFoto;
    private FloatingActionButton botaoGaleria;
    private PreviewView previewView;

    // Declarações de Câmera
    private ImageCapture imageCapture;
    File outputDirectory;
    ExecutorService cameraExecutor;
    private static String TAG = "Camera3C3D2021";
    private static String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static int REQUEST_CODE_PERMISSIONS = 10;
    private static String REQUIRED_PERMISSIONS = Manifest.permission.CAMERA;
    //Declaração de constantes
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PERMISSAO_REQUEST = 2;
    private static final int PEGA_FOTO = 3;

    private String currentPhotoPath;
    private Bitmap bitmap;
    private Uri photoURI;
    File photoFile;
    Camera camera;
    CameraSelector cameraSelector;
    Preview preview;
    ProcessCameraProvider cameraProvider;
    private int tipoCamera = 2;



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        // Vínculos dos objetos
        imagem = binding.imagem;
        texto = binding.textviewFirst;
        botaoGaleria = binding.btnGaleria;
        botaoTirarFoto = binding.btnFoto;
        previewView = binding.previewView;

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // BOTÃO PRÓXIMO
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}