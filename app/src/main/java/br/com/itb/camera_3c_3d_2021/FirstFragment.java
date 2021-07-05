package br.com.itb.camera_3c_3d_2021;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import br.com.itb.camera_3c_3d_2021.databinding.FragmentFirstBinding;

import static android.app.Activity.RESULT_OK;

public class FirstFragment extends Fragment {

    // Declarações de objeto da janela
    private FragmentFirstBinding binding;
    private AppCompatImageView ivFoto;
    private AppCompatTextView texto;
    private FloatingActionButton botaoTirarFoto;
    private FloatingActionButton botaoGaleria;
    private FloatingActionButton botaoCamera;
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
        ivFoto = binding.imagem;
        texto = binding.textviewFirst;
        botaoGaleria = binding.btnGaleria;
        botaoTirarFoto = binding.btnFoto;
        previewView = binding.previewView;
        botaoCamera = binding.btnCamera;


        // Request camera permissions
        // Requisição de permissão
        // Questiona permissão
        if (allPermissionsGranted(container.getRootView())) {
        } else {
            // Busca permissão
            permissoesAcesso();
        }

        // Dar início à câmera
        startCamera(container.getRootView(), tipoCamera);



        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // BOTÃO PRÓXIMO - TROCAR DE FRAGMENTO
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

    // MÉTODOS DE UTILIZAÇÃO DA CÂMERA
    // Método de inicialização dos objetos de manipulação da câmera
    private void startCamera(View v, int tipoCamera) {
        ListenableFuture cameraProviderFuture =
                ProcessCameraProvider.getInstance(v.getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                // Provedor da Câmera garante disponibilidade
                cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                // Set up the view finder use case to display camera preview
                // Configurando objeto preview para demonstrar imagem da câmera
                preview = new Preview.Builder().build();
                // Set up the capture use case to allow users to take photos
                // Construindo objeto de captura de imagem pela câmera
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();
                if(tipoCamera == 1) {
                    // Choose the camera by requiring a lens facing
                    // Selecionar o tipo de lente, ou câmera frontal
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                } else if(tipoCamera == 2){
                    // Choose the camera by requiring a lens facing
                    // Selecionar o tipo de lente, ou câmera traseira
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();
                }
                // Attach use cases to the camera with the same lifecycle owner
                // Iniciar a câmera no mesmo ciclo de vida do contexto
                camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture);
                // Connect the preview use case to the previewView
                // Realiza a conexão da câmera com a pré-visualização
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown cameraProviderFuture.get();
                // shouldn't block since the listener is being called so no need to
                // handle InterruptedException.
            }
        }, ContextCompat.getMainExecutor(v.getContext()));
    }

    // Solicita permissões de câmera
    private boolean allPermissionsGranted(View v) {
        if (ContextCompat.checkSelfPermission(
                v.getContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    // Solicitar permissão de acesso à câmera, ao armazenamento
    private void permissoesAcesso() {
        //Condicional para controle de permissões
        // Verifica se há permissão para leitura de arquivos
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if
            (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE))
            {
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new
                                String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSAO_REQUEST);
            }
        }
        //Verifica se há permissões para escrita de arquivos
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if
            (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new
                                String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSAO_REQUEST);
            }
        }
        //Verifica se há permissões para câmera
        if (ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if
            (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new
                                String[]{android.Manifest.permission.CAMERA}, PERMISSAO_REQUEST);
            }
        }
    }

    // Métodos de controle
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new
                SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir =
                this.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES
                );
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        // Caminho absoluto e global da imagem que vai ser gravada
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Método para inclusão de imagem na galeria
    private void galleryAddPic(Uri photoURI) {
        this.getActivity().sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        photoURI));
    }

    // verificar se dispositivo permite escrita ou gravação
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Método para definição das dimensões da imagem
    private void setPic() {
        // Obtendo as dimensões da imagem para a View
        int targetW = ivFoto.getWidth();
        int targetH = ivFoto.getHeight();
        // Obter as dimensões do Bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //bmOptions.inJustDecodeBounds = true;
        //BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Determina como diminuir a escala da imagem
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        // Decodifica o arquivo de imagem para o Bitmap que preencherá a View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        // Cria o bitmap da imagem capturada
        bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        // Declara objeto vetor e rotaciona em 90 graus a imagem
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        if(tipoCamera == 2){
            matrix.invert(matrix);
        }
        //matrix.preRotate(45);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        // Devolve imagem para o ImageView
        ivFoto.setImageBitmap(rotated);
        // Apresenta a imagem na tela
        //ivFoto.setImageBitmap(bitmap);
    }

    // externo
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new
                File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    // local
    public File getAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DCIM), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    // Retorno do disparo da câmera
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode ==
                RESULT_OK) {
            // Chamada do método de adição da imagem na galeria
            galleryAddPic(photoURI);
            // Definição das dimensões da imagem
            setPic();
        } else if (requestCode == PEGA_FOTO && resultCode == RESULT_OK)
        {
            //Captura caminho da imagem selecionada
            Uri imagemSelecionada = data.getData();
            // declara um stream (seguimento de dados) para ler a imagem recuperada do SD Card
            InputStream inputStream = null;
            // recuperando a sequencia de entrada, baseada no caminho (uri) da imagem
            try {
                inputStream = this.getActivity().getContentResolver().openInputStream(imagemSelecionada);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // recuperando um bitmap do stream
            bitmap = BitmapFactory.decodeStream(inputStream);
            // Reduz imagem e configura apresentação
            Bitmap bitmapReduzido = Bitmap
                    .createScaledBitmap(bitmap, 1080, 1080, true);
            ivFoto.setImageBitmap(bitmapReduzido);
            ivFoto.setScaleType(AppCompatImageView.ScaleType.FIT_XY);
        }
    }


}